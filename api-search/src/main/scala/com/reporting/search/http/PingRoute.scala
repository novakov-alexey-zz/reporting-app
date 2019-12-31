package com.reporting.search.http

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.{complete, get, pathEndOrSingleSlash}
import akka.http.scaladsl.server.Route

object PingRoute {
  val api: Route =
    get {
      pathEndOrSingleSlash {
        complete {
          HttpResponse(StatusCodes.OK, entity = "Welcome to api-search!")
        }
      }
    }
}
