package com.reporting.search.es
import com.reporting.search.AppConfig
import com.sksamuel.elastic4s.http._
import com.typesafe.scalalogging.StrictLogging

class EsClient(val it: ElasticClient, host: String, port: Int) extends StrictLogging with AutoCloseable {

  def close(): Unit = {
    it.close()
    logger.info(s"ElasticClient stopped at $host:$port")
  }
}

object EsClient {
  def apply(cfg: AppConfig): EsClient = {
    val props = ElasticProperties(Seq(ElasticNodeEndpoint("http", cfg.es.host, cfg.es.port, None)))
    new EsClient(ElasticClient(props), cfg.es.host, cfg.es.port)
  }
}
