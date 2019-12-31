package com.reporting.search.http

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Directives.pathPrefix
import akka.http.scaladsl.server.Route

trait ApiRoute {
  def apiPrefix(r: Route): Route = pathPrefix("api" / "v1")(r)
}
