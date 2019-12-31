package com.reporting.search.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.{Http, HttpsConnectionContext}
import akka.stream.ActorMaterializer
import com.reporting.search.AppConfig
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

class HttpServer(cfg: AppConfig, httpsContext: HttpsConnectionContext, routes: Route*)(implicit system: ActorSystem)
    extends LazyLogging
    with AutoCloseable {

  implicit val ec: ExecutionContext = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  private[this] val r = routes.reduceLeft[Route](_ ~ _)

  Http().setDefaultClientHttpsContext(httpsContext)

  private[this] lazy val httpsBind =
    Http().bindAndHandle(r, cfg.server.host, cfg.server.httpsPort, connectionContext = httpsContext)

  private[this] lazy val httpBind =
    Http().bindAndHandle(r, cfg.server.host, cfg.server.httpPort)

  def start(): Unit = {
    def go(protocol: String, port: Int, binding: Future[ServerBinding]): Unit = {
      binding.onComplete {
        case Success(b) => logger.info(s"$protocol server started at ${cfg.server.host}:$port")
        case Failure(ex) =>
          logger.error(s"Failed to bind the 'api-search' to ${cfg.server.host}:$port", ex)
      }
    }

    go("HTTP", cfg.server.httpPort, httpBind)
    go("HTTPS", cfg.server.httpsPort, httpsBind)
  }

  def close(): Unit = {
    def go(protocol: String, port: Int, binding: Future[ServerBinding]): Unit = {
      binding.flatMap(_.unbind()).onComplete { _ =>
        Await.ready(system.terminate(), 30.seconds)
        logger.info(s"$protocol server stopped at ${cfg.server.host}:$port")
      }
    }

    go("HTTP", cfg.server.httpPort, httpBind)
    go("HTTPS", cfg.server.httpsPort, httpsBind)
  }

  def awaitTermination(): Unit = {
    Await.result(system.whenTerminated, Duration.Inf)
  }

}
