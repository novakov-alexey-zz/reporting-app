package com.reporting.search.es

import cats.instances.string._
import cats.syntax.eq._
import com.reporting.search.{Highlight, Value}
import com.sksamuel.elastic4s.http.ElasticDsl.termsAgg
import com.sksamuel.elastic4s.http.search.Aggregations
import com.sksamuel.elastic4s.searches.SearchRequest
import com.sksamuel.elastic4s.searches.aggs.TermsAggregation

import scala.annotation.tailrec

object HighlightsController {
  val similaritiesType = "similarities"
  // once another row has the same value then we consider current row property to be highlighted
  val similaritiesMinDocCount = 2

  @tailrec
  def buildHighlights(in: (SearchRequest, Option[Seq[Highlight]])): SearchRequest = in match {
    case (r, Some(h :: hs)) =>
      h.`type` match {
        case `similaritiesType` =>
          val siblingAggs = h.properties.foldLeft(List[TermsAggregation]()) { (l, p) =>
            l :+ termsAgg(p, p).minDocCount(similaritiesMinDocCount)
          }
          buildHighlights((r.aggregations(r.aggs ++ siblingAggs), Some(hs)))

        case _ => r
      }

    case (r, _) => r
  }

  def calcHighlights(source: Map[String, Value[_]], agg: Aggregations): Option[Set[String]] = {

    val h = agg.names
      .filterNot(_ === EsSearchService.SourceAggs)
      .flatMap { n =>
        val propName = n.stripSuffix(".keyword")
        agg
          .terms(n)
          .buckets
          .find { b =>
            val key = b.dataAsMap.get("key_as_string")
              // TODO: temporary remove timezone "Z" suffix from date field key,
              // proper fix is to configure date formatter on index mapping level
              .map(_.toString.stripSuffix("Z"))
              .getOrElse(b.key)
            source.get(propName).exists(_.v == key)
          }
          .map(_ => propName)
      }
      .toSet

    if (h.nonEmpty) Some(h) else None
  }
}
