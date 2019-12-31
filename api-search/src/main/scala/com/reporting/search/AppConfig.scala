package com.reporting.search

import java.io.File

import com.typesafe.config.{ConfigFactory, ConfigParseOptions}
import pureconfig.error.ConfigReaderFailures
import pureconfig.loadConfig

case class Server(host: String, httpsPort: Int, httpPort: Int)
case class EsConfig(host: String, port: Int)
case class AppConfig(es: EsConfig, server: Server)

object AppConfig {
  private[this] val parseOptions = ConfigParseOptions.defaults().setAllowMissing(false)

  def load: Either[ConfigReaderFailures, AppConfig] = {
    val path = sys.env.getOrElse("APP_CONFIG_PATH", "api-search/src/main/resources/api-search.conf")
    val config = ConfigFactory.parseFile(new File(path), parseOptions).resolve()
    loadConfig[AppConfig](config)
  }
}
