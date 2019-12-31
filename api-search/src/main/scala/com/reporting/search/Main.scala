package com.reporting.search

import akka.actor.ActorSystem
import com.reporting.search.es._
import com.reporting.search.http._
import com.reporting.search.jsonCodes.all._
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.ExecutionContextExecutor

object Main extends App with CORSHandler with StrictLogging {
  val cfg: AppConfig = AppConfig.load.fold(e => sys.error(s"Failed to load api-search configuration: $e"), identity)

  val esClient = EsClient(cfg)
  val ecHolder = new ExecutionContextHolder()

  implicit val system: ActorSystem = ActorSystem("api-search-actor-system")
  implicit val apiEc: ExecutionContextExecutor = system.dispatcher

  val httpsContext = SslContext.createContext()
  val server = new HttpServer(cfg, httpsContext, routes: _*)
  server.start()
  server.awaitTermination()

  sys.ShutdownHookThread {
    esClient.close()
    ecHolder.close()
  }

  private def routes = {
    val search = new EsSearchService(esClient)(ecHolder.ec)
    val queryRoute = new QueryRoutes[Query, RowSet]().api(search.query)

    val correlation = new EsCorrelationService(esClient)(ecHolder.ec)
    val corrSearchRoute = CorrelationRoutes.search[Correlation, RowSet](correlation.correlation)
    val corrStatsRoute = CorrelationRoutes.stats[Correlation, CorrStats](correlation.correlationStats)

    val meta = new EsMetadataService(esClient)(ecHolder.ec)
    val metaRoutes = new MetaRoutes()
    val datasourcesRoute = metaRoutes.datasources[Datasources](_ => meta.datasources())
    val schemaRoute = metaRoutes.schema[Schema](meta.schema)

    Seq(queryRoute, corrSearchRoute, corrStatsRoute, datasourcesRoute, schemaRoute, PingRoute.api).map(corsHandler)
  }
}
