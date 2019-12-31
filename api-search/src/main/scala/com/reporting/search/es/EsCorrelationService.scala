package com.reporting.search.es

import cats.syntax.either._
import com.reporting.search.SearchServiceError.CorrelationError
import com.reporting.search._
import com.reporting.search.es.EsSearchService.{propertyTermsAggs, datasourceTermsAggs, AnyToken, IndexReference}
import com.reporting.search.es.Utils.{checkErrors, _}
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.search._
import com.sksamuel.elastic4s.searches.SearchRequest
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{ExecutionContext, Future}

class EsCorrelationService(esClient: EsClient)(implicit ec: ExecutionContext)
    extends CorrelationService
    with StrictLogging {

  private def buildCorrelationQuery(corr: Correlation): SearchRequest = {
    val (exclude, include) = {
      corr.datasourceTo match {
        case Some(to) => (List(s"-$AnyToken${corr.datasourceFrom}"), to)
        case None => (List(s"-${corr.datasourceFrom}"), List(AnyToken))
      }
    }

    val indexes = (include ++ exclude).mkString(",")
    val fields = corr.properties.getOrElse(Seq.empty)

    logger.debug(s"correlation query: indexes = [$indexes], fields = [$fields]")

    search(indexes)
      .query(queryStringQuery(corr.query.term).asfields(fields: _*))
  }

  override def correlation(corr: Correlation): Future[Either[SearchServiceError, RowSet]] = {
    val q = buildCorrelationQuery(corr)
    val res = esClient.it.execute(q)

    def toError(e: String) = CorrelationError(e, Some(corr))

    res.map(
      checkErrors(_, toError)
        .flatMap(toRowSet(_, toError))
    )
  }

  //TODO: add consumer test for correlationStats
  def correlationStats(corr: Correlation): Future[Either[SearchServiceError, CorrStats]] = {
    def toError(e: String) = CorrelationError(e, Some(corr))

    corr.properties match {
      case Some(p :: _) =>
        val q = buildCorrelationQuery(corr)
        val qWithAggs = q.aggs(
          termsAgg(propertyTermsAggs, p)
            .subaggs(termsAgg(datasourceTermsAggs, IndexReference))
        )
        val res = esClient.it.execute(qWithAggs)
        res.map(
          checkErrors(_, toError)
            .flatMap(toCorrStats(_, toError))
        )

      case _ =>
        Future.successful(Left(toError("Correlation stats request must have non-empty properties")))
    }
  }

  private[this] def toCorrStats[T <: SearchServiceError](
    sr: SearchResponse,
    toError: String => T
  ): Either[T, CorrStats] = {
    Either.catchNonFatal {
      val buckets = sr.aggregations.terms(propertyTermsAggs).buckets.map { fb =>
        val subBuckets = fb.terms(datasourceTermsAggs).buckets.map(ib => Bucket(ib.key, ib.docCount, Seq.empty))
        Bucket(fb.key, fb.docCount, subBuckets)
      }

      CorrStats(buckets)
    }.leftMap { t =>
      val err = "Failed to transform result to CorrStats"
      logger.error(err, t)
      toError(s"$err: ${t.toString}")
    }
  }
}
