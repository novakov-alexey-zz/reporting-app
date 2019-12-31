package com.reporting.gendata

import com.reporting.gendata.gens._
import com.reporting.gendata.storage.ElasticsearchStorage
import com.reporting.gendata.storage.ElasticsearchStorage._
import org.scalatest.{Assertion, FlatSpec, Matchers}

import scala.Predef._
import scala.collection.mutable

class ElasticsearchStorageTest extends FlatSpec with Matchers {
  private val requests = mutable.ArrayBuffer[String]()
  private val clientMock = createMock(requests)
  private val storage = new ElasticsearchStorage(clientMock)
  private val count = 10

  it should "send Contact bulk requests" in {
    //given
    val contacts = (1 to count).flatMap(_ => ContactGen.generate().sample)
    //when
    val res = storage.storeContacts(contacts)
    //then
    checkResult(res, ContactIndex)
  }

  it should "send Banking bulk requests" in {
    //given
    val bankings = (1 to count).flatMap(_ => BankingGen.generate().sample)
    //when
    val res = storage.storeBankings(bankings)
    //then
    checkResult(res, BankingIndex)
  }

  it should "send Bills bulk requests" in {
    //given
    val bills = (1 to count).flatMap(_ => BillsGen.generate.sample).flatten
    //when
    val res = storage.storeBills(bills)
    //then

    // every doc has an action line so multiply by 2 plus number of bills is in range of billsCount Gen per clientId
    checkResult(res, BillsIndex, Some((l: Int) => l should be > (count * 2)))
  }

  it should "send ItUsage bulk requests" in {
    //given
    val itUsages = (1 to count).flatMap(_ => ItUsageGen.generate.sample)
    //when
    val res = storage.storeItUsages(itUsages)
    //then
    checkResult(res, ItUsageIndex)
  }

  it should "send Marketing bulk requests" in {
    //given
    val marketing = (1 to count).flatMap(_ => MarketingGen.generate().sample)
    //when
    val res = storage.storeMarketing(marketing)
    //then
    checkResult(res, MarketingIndex)
  }

  private def checkResult(res: Either[String, Unit], index: String, assertF: Option[Int => Assertion] = None) = {
    res.left.foreach(println)
    res.isRight should be(true)

    val bulk = requests.filter(_.contains(s""""_index" : "$index""""))

    bulk.length should be(1)

    // every doc has an action line so multiply by 2
    val checkNumberOfActions = assertF.getOrElse((i: Int) => i should be(count * 2))

    bulk.headOption
      .map(h => checkNumberOfActions(h.split("\n").length))
      .getOrElse(fail("should have size 1"))
  }

  private def createMock(requests: mutable.ArrayBuffer[String]): Http = {
    new Http {
      private val success = Right("")

      override def delete(url: String): Either[String, String] = success
      override def put(url: String, body: String, headers: Seq[(String, String)]): Either[String, String] = success
      override def post(url: String, body: String, headers: Seq[(String, String)]): Either[String, String] = {
        requests += body
        success
      }
    }
  }

}
