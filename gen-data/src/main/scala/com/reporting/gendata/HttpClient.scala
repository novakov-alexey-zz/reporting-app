package com.reporting.gendata

import requests.Response

trait Http {
  def delete(url: String): Either[String, String]
  def put(url: String, body: String, headers: Seq[(String, String)]): Either[String, String]
  def post(url: String, body: String, headers: Seq[(String, String)]): Either[String, String]
}

object HttpClient extends Http {
  val timeout = 30000

  def delete(url: String): Either[String, String] = {
    val r = requests.delete(url = url)
    checkStatusCode(r)
  }

  def put(url: String, body: String, headers: Seq[(String, String)]): Either[String, String] = {
    val r = requests.put(url = url, data = body, headers = headers)
    checkStatusCode(r)
  }

  def post(url: String, body: String, headers: Seq[(String, String)]): Either[String, String] = {
    val r = requests.post(url = url, data = body, headers = headers, connectTimeout = timeout, readTimeout = timeout)
    checkStatusCode(r)
  }

  private def checkStatusCode(r: Response): Either[String, String] = r.statusCode match {
    case 200 => Right(r.text)
    case c => Left(s"query failed with $c status code. Reason: ${r.text}")
  }
}
