package com.reporting.search.es

import cats.instances.string._
import cats.syntax.either._
import cats.syntax.eq._
import com.reporting.search._
import com.reporting.search.es.EsSearchService.SourceAggs
import com.reporting.search.es.HighlightsController.calcHighlights
import com.sksamuel.elastic4s.http.search._
import com.sksamuel.elastic4s.http.{search => _, _}
import com.typesafe.scalalogging.StrictLogging

import scala.collection.mutable

object Utils extends StrictLogging {
  def checkErrors[T <: SearchServiceError, U](res: Response[U], toError: String => T): Either[T, U] =
    res match {
      //TODO: analyze failed shards: "total":6,"successful":4,"skipped":0,"failed":2
      case RequestSuccess(_, _, _, r) => Right(r)
      case RequestFailure(_, _, _, error) => Left(toError(error.toString))
    }

  def toRowSet[T <: SearchServiceError](sr: SearchResponse, toError: String => T): Either[T, RowSet] =
    Either.catchNonFatal {
      val countBySource = sr.aggregations.names
        .find(_ === SourceAggs)
        .map { _ =>
          sr.aggregations
            .terms(SourceAggs)
            .buckets
            .map(b => (b.key, b.docCount))
        }

      def toProperty: PartialFunction[(String, AnyRef), (String, Value[_])] = {
        case (k, v) =>
          v match {
            case h :: Nil => (k, Value(h))
            case _ :: _ => (k, Value(v))
            case x => (k, Value(x))
          }
      }

      val rows = sr.hits.hits.map { h =>
        val source = h.sourceAsMap.map(toProperty)
        val scripted = Option(h.fields).map(_.map(toProperty)).getOrElse(Seq.empty)
        val cols = mutable.Map((MetaProperties.Datasource, Value(h.index))) ++ source ++ scripted
        val hs = calcHighlights(source, sr.aggregations)
        Row(cols.toSeq, hs)
      }

      RowSet(Metadata(sr.hits.total, countBySource), rows)
    }.leftMap { t =>
      logger.error("Failed to transform result to RowSet", t)
      toError(t.toString)
    }

}
