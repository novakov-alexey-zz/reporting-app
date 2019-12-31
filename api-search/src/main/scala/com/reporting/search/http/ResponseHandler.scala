package com.reporting.search.http

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import com.reporting.search.SearchServiceError
import com.typesafe.scalalogging.StrictLogging
import io.circe.Encoder
import io.circe.syntax._

import scala.concurrent.{ExecutionContext, Future}

object ResponseHandler extends StrictLogging {

  def toResponse[T](
    result: Future[Either[SearchServiceError, T]],
    errMsg: String
  )(implicit ec: ExecutionContext, encoder: Encoder[T]): Future[HttpResponse] = {
    result.map { r =>
      val (status, entity) = r match {
        case Right(data) =>
          (StatusCodes.OK, HttpEntity(ContentTypes.`application/json`, data.asJson.noSpaces))
        case Left(e) =>
          logger.error(errMsg, e)
          (StatusCodes.InternalServerError, HttpEntity(e.toString))
      }
      HttpResponse(status, entity = entity)
    }
  }
}
