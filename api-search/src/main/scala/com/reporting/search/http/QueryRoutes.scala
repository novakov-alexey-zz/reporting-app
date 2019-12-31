package com.reporting.search.http

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.{FromStringUnmarshaller, Unmarshaller}
import com.reporting.search.SearchServiceError
import com.reporting.search.SearchServiceError.QuerySourceError
import com.reporting.search.http.CsvParameters._
import com.reporting.search.http.ResponseHandler.toResponse
import com.typesafe.scalalogging.StrictLogging
import io.circe.parser.decode
import io.circe.{Decoder, Encoder}

import scala.concurrent.{ExecutionContext, Future}

trait CsvParameters {
  implicit def csvSeqParamMarshaller: FromStringUnmarshaller[Seq[String]] =
    Unmarshaller(ex ⇒ s ⇒ Future.successful(s.split(",")))

  implicit def csvListParamMarshaller: FromStringUnmarshaller[List[String]] =
    Unmarshaller(ex ⇒ s ⇒ Future.successful(s.split(",").toList))
}

object CsvParameters extends CsvParameters

class QueryRoutes[T, U](implicit ec: ExecutionContext, in: Decoder[T], out: Encoder[U])
    extends StrictLogging
    with ApiRoute {
  type QueryHandler = (T, Option[Seq[String]]) => Future[Either[SearchServiceError, U]]

  def api(f: QueryHandler): Route =
    apiPrefix {
      path("query" / "all") {
        pathEndOrSingleSlash {
          post {
            entity(as[String])(handleQuery(_, None, f))
          }
        }
      } ~
        path("query" / "some") {
          pathEndOrSingleSlash {
            post {
              parameters('ds.as[Seq[String]]) { ds =>
                entity(as[String])(handleQuery(_, Some(ds), f))
              }
            }
          }
        }
    }

  private[this] def handleQuery(body: String, datasources: Option[Seq[String]], f: QueryHandler) = {
    logger.debug(s"received search query: datasource = '{}', body = {}", datasources.getOrElse("all"), body)

    val parsed = decode[T](body).left.map(e => s"Failed to decode request body, error: ${e.toString}")
    val result = parsed match {
      case Right(q) => f(q, datasources)
      case Left(e) => Future.successful(Left(QuerySourceError(e)))
    }

    complete {
      toResponse[U](result, s"request failed for a query: $body")
    }
  }
}
