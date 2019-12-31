package com.reporting.search

// IN
final case class Pagination(page: Int, size: Int)
final case class QueryTerm(term: String, andToken: Option[String] = None)

/*
//TODO: Either[Long, String] is a workaround to decode string and integers, however it will fail on floating type
we need to get rid of Circe completely, since it does not work with Scala existential types and Any/AnyRef
another Json candidate library could be 'upickle'
 */
final case class ScriptProperty(property: String, script: String, params: Map[String, Either[Long, String]])
final case class Query(
  query: QueryTerm,
  scriptProperty: Option[Seq[ScriptProperty]] = None,
  highlights: Option[Seq[Highlight]] = None,
  pagination: Option[Pagination] = None
)
final case class Correlation(
  query: QueryTerm,
  datasourceFrom: String,
  datasourceTo: Option[Seq[String]] = None,
  properties: Option[Seq[String]] = None,
  pagination: Option[Pagination] = None
)
final case class Highlight(`type`: String, properties: Seq[String])

// OUT
// Search
final case class Value[T](v: T)
final case class Row(props: Seq[(String, Value[_])], highlights: Option[Set[String]] = None)
final case class Metadata(count: Long, countBySource: Option[Seq[(String, Long)]])
final case class RowSet(metadata: Metadata, rows: Seq[Row])

// Stats
final case class Bucket(key: String, count: Long, subBuckets: Seq[Bucket])
final case class CorrStats(buckets: Seq[Bucket])

// Meta-data
final case class Datasources(list: Seq[String])
final case class Property(name: String, `type`: String)
final case class Datasource(name: String, properties: Seq[Property])
final case class Schema(datasources: Seq[Datasource])

object MetaProperties {
  val Datasource = "datasource"
}
