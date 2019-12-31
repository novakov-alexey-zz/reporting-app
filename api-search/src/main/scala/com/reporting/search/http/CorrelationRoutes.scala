package com.reporting.search.http

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.reporting.search.SearchServiceError
import com.reporting.search.SearchServiceError.CorrelationError
import com.reporting.search.http.ResponseHandler.toResponse
import com.typesafe.scalalogging.StrictLogging
import io.circe.parser.decode
import io.circe.{Decoder, Encoder}

import scala.concurrent.{ExecutionContext, Future}

object CorrelationRoutes extends StrictLogging with ApiRoute {

  type QueryHandler[T, U] = T => Future[Either[SearchServiceError, U]]

  def search[T, U](f: QueryHandler[T, U])(implicit ec: ExecutionContext, in: Decoder[T], out: Encoder[U]): Route = {
    apiPrefix {
      path("correlation" / "search") {
        pathEndOrSingleSlash {
          post {
            entity(as[String])(handleQuery(_, f))
          }
        }
      }
    }
  }

  def stats[T, U](f: QueryHandler[T, U])(implicit ec: ExecutionContext, in: Decoder[T], out: Encoder[U]): Route =
    apiPrefix {
      path("correlation" / "stats") {
        pathEndOrSingleSlash {
          post {
            entity(as[String])(handleQuery(_, f))
          }
        }
      }
    }

  private def handleQuery[T, U](
    body: String,
    f: QueryHandler[T, U]
  )(implicit ec: ExecutionContext, in: Decoder[T], out: Encoder[U]): Route = {
    logger.debug(s"received correlation request: body = {}", body)

    val parsed = decode[T](body).left.map(e => s"Failed to decode request body, error: ${e.toString}")
    val result = parsed match {
      case Right(q) => f(q)
      case Left(e) => Future.successful(Left(CorrelationError(e)))
    }

    complete {
      toResponse[U](result, s"request failed for a query: $body")
    }
  }
}
