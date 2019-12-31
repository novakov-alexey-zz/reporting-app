package com.reporting.search.http

import akka.http.scaladsl.server.Directives.{path, pathEndOrSingleSlash, pathPrefix, _}
import akka.http.scaladsl.server.Route
import com.reporting.search.SearchServiceError
import com.reporting.search.http.ResponseHandler.toResponse
import com.typesafe.scalalogging.StrictLogging
import io.circe.Encoder

import scala.concurrent.{ExecutionContext, Future}

class MetaRoutes(implicit ec: ExecutionContext) extends StrictLogging with ApiRoute {
  type QueryHandler[T, U] = T => Future[Either[SearchServiceError, U]]

  def datasources[U](f: QueryHandler[Unit, U])(implicit out: Encoder[U]): Route =
    apiPrefix {
      path("meta" / "datasources") {
        pathEndOrSingleSlash {
          get {
            handleQuery(f, ())
          }
        }
      }
    }

  def schema[U](f: QueryHandler[Option[String], U])(implicit out: Encoder[U]): Route =
    apiPrefix {
      path("meta" / "schema") {
        pathEndOrSingleSlash {
          get {
            handleQuery(f, None)
          }
        }
      } ~
        path("meta" / "schema" / Segment) { ds =>
          pathEndOrSingleSlash {
            get {
              handleQuery(f, Some(ds))
            }
          }
        }
    }

  private[this] def handleQuery[T, U](f: QueryHandler[T, U], in: T)(implicit out: Encoder[U]) = {
    complete {
      val result = f(in)
      toResponse[U](result, "meta request failed")
    }
  }
}
