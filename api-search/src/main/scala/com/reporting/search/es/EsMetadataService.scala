package com.reporting.search.es

import com.reporting.search.SearchServiceError.{ListSourcesError, SchemaError}
import com.reporting.search._
import com.reporting.search.es.EsSearchService.{AnyToken, DocTypeName}
import com.reporting.search.es.Utils._
import com.sksamuel.elastic4s.http.ElasticDsl._
import com.sksamuel.elastic4s.http.cat.CatIndicesResponse
import com.sksamuel.elastic4s.http.index.mappings.IndexMappings

import scala.concurrent.{ExecutionContext, Future}

class EsMetadataService(esClient: EsClient)(implicit ec: ExecutionContext) extends MetaService {

  override def datasources(): Future[Either[ListSourcesError, Datasources]] =
    esClient.it
      .execute(catIndices())
      .map(
        checkErrors(_, ListSourcesError)
          .map(transformIndices)
      )

  private[this] def transformIndices(indices: Seq[CatIndicesResponse]) =
    Datasources(indices.map(_.index))

  override def schema(datasource: Option[String]): Future[Either[SearchServiceError, Schema]] = {
    def transformMappings(result: Seq[IndexMappings]) = {
      Schema(result.map { im =>
        val props =
          im.mappings
            .get(DocTypeName)
            .map(_.flatMap {
              case (k, v) =>
                val map = v.asInstanceOf[Map[String, String]]
                map.get("type").map(Property(k, _))
            })
            .toSeq
            .flatten

        Datasource(im.index, props)
      })
    }

    esClient.it
      .execute(getMapping(datasource.getOrElse(AnyToken)))
      .map(
        checkErrors(_, SchemaError)
          .map(transformMappings)
      )
  }
}
