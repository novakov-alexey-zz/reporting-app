package com.reporting.search

import com.reporting.search.EsSchema._
import com.reporting.search.es._
import com.sksamuel.elastic4s.RefreshPolicy
import com.sksamuel.elastic4s.embedded.LocalNode
import com.sksamuel.elastic4s.http.ElasticClient
import com.sksamuel.elastic4s.http.ElasticDsl._
import org.scalatest.Matchers

import scala.concurrent.ExecutionContext
import scala.sys.process._

class EsTestNode extends Matchers {
  val ecHolder = new ExecutionContextHolder
  implicit val ec: ExecutionContext = ecHolder.ec

  private val (underlyingClient, localNode) = prepareTestNode
  val client = new EsClient(underlyingClient, "test", 9200)
  val searchService = new EsSearchService(client)
  val metaService = new EsMetadataService(client)
  val correlationService = new EsCorrelationService(client)

  private def prepareTestNode = {
    //given
    val dataPath = "api-search/target/esdata"
    purgeDataFolder(dataPath)

    val localNode = LocalNode("testcluster", dataPath)
    val client = localNode.client(shutdownNodeOnClose = true)

    createIndexes(client)

    //when
    val contactDoc = Seq(firstNameField -> firstName, ageField -> 50, birthdayField -> "1981-04-28T05:28:44.375")
    val itUsageDoc = Seq(emailField -> (firstName + "@gmail.com"), userIdField -> 11110)
    val bankingDoc = Seq(ibanField -> "DE99 99999999 0000000000", bicField -> "DEUTDEBB181", userIdField -> 11110)

    val newDocs = client.execute {
      bulk(
        indexInto(contactDs / typeName)
          .fields(contactDoc: _*)
          .refresh(RefreshPolicy.Immediate),
        indexInto(itUsageDs / typeName)
          .fields(itUsageDoc: _*)
          .refresh(RefreshPolicy.Immediate),
        indexInto(bankingDs / typeName)
          .fields(bankingDoc: _*)
          .refresh(RefreshPolicy.Immediate)
      )
    }.await

    //then
    newDocs.isSuccess should be(true)
    (client, localNode)
  }

  private def createIndexes(client: ElasticClient) = {
    //when
    val contactIndex = client.execute {
      createIndex(contactDs).mappings(
        mapping(typeName)
          .fields(textField(firstNameField), intField(ageField), dateField(birthdayField), intField(userIdField))
      )
    }.await

    //then
    contactIndex.isSuccess should be(true)

    //when
    val itUsageIndex = client.execute {
      createIndex(itUsageDs).mappings(mapping(typeName).fields(textField(emailField), intField(userIdField)))
    }.await

    //then
    itUsageIndex.isSuccess should be(true)

    //when
    val bankingIndex = client.execute {
      createIndex(bankingDs).mappings(
        mapping(typeName).fields(textField(ibanField), textField(bicField), intField(userIdField))
      )
    }.await

    //then
    bankingIndex.isSuccess should be(true)
  }

  def close(): Unit = {
    ecHolder.close()
    client.close()
    localNode.close()
  }

  private def purgeDataFolder(dataPath: String) = s"rm -R $dataPath".!
}
