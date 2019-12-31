package com.reporting.gendata.storage

import java.time.format.DateTimeFormatter

import argonaut.Argonaut._
import argonaut._
import com.reporting.gendata._
import com.reporting.gendata.storage.ElasticsearchStorage._

class ElasticsearchStorage(client: Http = HttpClient) extends Storage {
  private val url = s"http://$Host:$Port"
  private val bulkUrl = s"$url/_bulk"
  private val codecs = new JsonCodecs(formatter)
  import codecs._

  deleteIndexes()
  createIndexes()

  override def storeMarketing(batch: Seq[Marketing]): Either[String, Unit] = indexData(batch, MarketingIndex)
  override def storeContacts(batch: Seq[Contact]): Either[String, Unit] = indexData(batch, ContactIndex)
  override def storeBankings(batch: Seq[Banking]): Either[String, Unit] = indexData(batch, BankingIndex)
  override def storeBills(batch: Seq[Bill]): Either[String, Unit] = indexData(batch, BillsIndex)
  override def storeItUsages(batch: Seq[ItUsage]): Either[String, Unit] = indexData(batch, ItUsageIndex)

  private def deleteIndexes(): Unit =
    Index2Action.keys.foreach(i => client.delete(s"$url/$i"))

  private def createIndexes(): Unit =
    Index2Action.keys.foreach(i => client.put(s"$url/$i", IndexTemplate, IndexApiHeaders))

  private def indexData[T](batch: Seq[T], index: String)(implicit encoder: EncodeJson[T]): Either[String, Unit] = {
    val body = toBulkRequest(batch, index)
    client.post(bulkUrl, body, BulkApiHeaders).map(checkErrors)
  }

  private def toBulkRequest[T](batch: Seq[T], index: String)(implicit encoder: EncodeJson[T]): String =
    batch
      .map(c => "%s\n%s".format(Index2Action(index), c.asJson.nospaces))
      .mkString("\n") + "\n"

  private def checkErrors(response: String): Either[String, Unit] = {
    val errors =
      Parse.parseWith(response, e => Right(e.field("errors").flatMap(_.bool).getOrElse(true)), msg => Left(msg))

    errors.flatMap { e =>
      if (e) Left(s"There were some errors in response: $response")
      else Right(())
    }
  }
}

object ElasticsearchStorage {
  val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
  val Host: String = sys.env.getOrElse("ES_HOST", "localhost")
  val Port: Int = sys.env.get("ES_PORT").map(_.toInt).getOrElse(9200)

  val ContactIndex = "contact"
  val MarketingIndex = "marketing"
  val BankingIndex = "banking"
  val ItUsageIndex = "itusage"
  val BillsIndex = "bills"

  val Index2Action = Map(
    ContactIndex -> indexAction(ContactIndex),
    BillsIndex -> indexAction(BillsIndex),
    BankingIndex -> indexAction(BankingIndex),
    ItUsageIndex -> indexAction(ItUsageIndex),
    MarketingIndex -> indexAction(MarketingIndex)
  )

  val IndexTemplate: String =
    """
      |{
      |    "settings" : {
      |        "index" : {
      |            "number_of_shards" : 3,
      |            "number_of_replicas" : 0
      |        }
      |    }
      |}
    """.stripMargin

  val BulkApiHeaders = Seq("Content-Type" -> "application/x-ndjson")
  val IndexApiHeaders = Seq("Content-Type" -> "application/json")

  private def indexAction(index: String): String =
    s"""{ "index" : { "_index" : "$index", "_type" : "_doc"} }"""
}
