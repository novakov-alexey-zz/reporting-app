package com.reporting.search

import scala.concurrent.Future

trait SearchService {
  def query(query: Query, dsName: Option[Seq[String]]): Future[Either[SearchServiceError, RowSet]]
}

trait CorrelationService {
  def correlation(corr: Correlation): Future[Either[SearchServiceError, RowSet]]
  def correlationStats(corr: Correlation): Future[Either[SearchServiceError, CorrStats]]
}

trait MetaService {
  def datasources(): Future[Either[SearchServiceError, Datasources]]
  def schema(datasource: Option[String]): Future[Either[SearchServiceError, Schema]]
}

sealed trait SearchServiceError {
  def message: String
}

object SearchServiceError {
  case class QuerySourceError(cause: String, query: Option[Query] = None, datasources: Option[Seq[String]] = None)
      extends SearchServiceError {
    override def message: String =
      s"Failed to execute query ${query.getOrElse("")} for datasources: [$datasources], cause: $cause"
  }

  case class CorrelationError(cause: String, corr: Option[Correlation] = None) extends SearchServiceError {
    override def message: String = {
      val params = corr.map { c =>
        "query: [%s], datasource: [from: %s, to: %s], with properties: [%s]".format(
          c.query.term,
          c.datasourceFrom,
          c.datasourceTo,
          c.properties
        )
      }.getOrElse("")

      s"Failed to execute correlation query, cause: $cause. $params"
    }
  }

  case class ListSourcesError(cause: String) extends SearchServiceError {
    override def message: String = s"List of datasources query is failed, cause: $cause"
  }

  case class SchemaError(cause: String) extends SearchServiceError {
    override def message: String = s"Schema query is failed, cause: $cause"
  }
}
