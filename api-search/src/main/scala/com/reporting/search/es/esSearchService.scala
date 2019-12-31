package com.reporting.search.es

import com.reporting.search.SearchServiceError.QuerySourceError
import com.reporting.search.es.EsSearchService._
import com.reporting.search.es.HighlightsController.buildHighlights
import com.reporting.search.es.RequestBuilder._
import com.reporting.search.es.Utils._
import com.reporting.search.{SearchServiceError, _}
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.search._
import com.sksamuel.elastic4s.http.{search => _, _}
import com.sksamuel.elastic4s.searches.SearchRequest
import com.typesafe.scalalogging.StrictLogging

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}

class EsSearchService(esClient: EsClient)(implicit ec: ExecutionContext) extends SearchService with StrictLogging {

  override def query(query: Query, datasources: Option[Seq[String]]): Future[Either[SearchServiceError, RowSet]] = {
    val req = buildRequest(query, datasources)
    val res = esClient.it.execute(req)

    def toError(e: String) = QuerySourceError(e, Some(query), datasources)

    handleResponse(res, toError)
  }

  private def buildRequest(query: Query, datasources: Option[Seq[String]]): SearchRequest = {
    val indexOrAll = datasources.map(_.mkString(",")).getOrElse(AnyToken)
    val q = formatQueryTerm(query.query)
    logger.debug(s"Formatted query term: $q")
    val req = search(indexOrAll).query(q)

    (scriptedProperties _)
      .andThen(r => termsAggr((r, datasources)))
      .andThen(r => pagination((r, query.pagination)))
      .andThen(r => buildHighlights((r, query.highlights)))((req, query.scriptProperty))
  }

  //TODO: add consumer test case for different 'and' tokens
  private[this] def formatQueryTerm(queryTerm: QueryTerm): String =
    queryTerm.andToken
      .map(t => queryTerm.term.stripPrefix(t).stripSuffix(t).replaceAll(t, s" $NativeAndToken "))
      .getOrElse(queryTerm.term)

  private def handleResponse(
    res: Future[Response[SearchResponse]],
    toError: String => SearchServiceError
  ): Future[Either[SearchServiceError, RowSet]] =
    res.map(
      checkErrors(_, toError)
        .flatMap(toRowSet(_, toError))
    )

}

object RequestBuilder {

  @tailrec
  def scriptedProperties(in: (SearchRequest, Option[Seq[ScriptProperty]])): SearchRequest = in match {
    case (r, Some(p :: ps)) =>
      val params = p.params.mapValues {
        case Left(l) => l
        case Right(s) => s
      }
      val withField =
        r.scriptfields(r.fields.scriptFields :+ scriptField(p.property).script(script(p.script).params(params)))
      scriptedProperties((withField.fetchSource(true), Some(ps)))

    case (r, _) => r
  }

  def termsAggr(in: (SearchRequest, Option[_])): SearchRequest = in match {
    case (r, Some(d :: Nil)) => r // when exact datasource is specified, we do not need to do aggregation
    case (r, _) => r.aggs(termsAgg(SourceAggs, IndexReference))
  }

  //TODO: add consumer test case for pagination
  def pagination: PartialFunction[(SearchRequest, Option[Pagination]), SearchRequest] = {
    case (r, Some(p)) =>
      val offset = p.page * p.size
      val from =
        // returning last possible page when offset is greater than max offset
        if (offset + p.size > MaxOffset) MaxOffset - p.size
        // or go with original offset
        else offset
      r.from(from).size(p.size)

    case (r, None) => r
  }
}

object EsSearchService {
  val SourceAggs = "source"
  val MaxOffset = 10000
  val NativeAndToken = "AND"
  val DocTypeName = "_doc"
  val IndexReference = "_index"
  val AnyToken = "*" // refers to any index on search
  val (propertyTermsAggs, datasourceTermsAggs) = ("by_property", "by_datasource")
}
