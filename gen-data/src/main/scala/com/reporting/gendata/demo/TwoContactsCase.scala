package com.reporting.gendata.demo

import argonaut.{DecodeJson, Parse}
import com.reporting.gendata._
import com.reporting.gendata.demo.TwoContactsCase.CaseResult
import com.reporting.gendata.gens.{BankingGen, BillsGen, ItUsageGen}
import com.reporting.gendata.storage.ElasticsearchStorage.formatter

object TwoContactsCase {
  type CaseData = ((Contact, Marketing), Seq[Banking], Seq[ItUsage], Seq[Bill])
  type CaseResult = (Int, CaseData)
}

trait TwoContactsCase {
  private val codecs = new JsonCodecs(formatter)
  import codecs._

  def contact1(clientId: Int): String

  def contact2(clientId: Int): String

  def generate(nextClientId: Int): Either[String, CaseResult] = {

    def parse[T](json: String)(implicit ev: DecodeJson[T]) = Parse.decodeEither[T](json)

    for {
      c1 <- parse[Contact](contact1(nextClientId))
      secondContact = nextClientId + 1
      c2 <- parse[Marketing](contact2(secondContact))

      bankingErr = "Banking gen failed"
      b1 <- BankingGen.generate(nextClientId).sample.toRight(bankingErr)

      itUsageErr = "ItUsage gen failed"
      it1 <- ItUsageGen.generate(nextClientId, c1.firstName, c1.lastName).sample.toRight(itUsageErr)

      billsErr = "Bills gen failed"
      bill1 <- BillsGen.generate(nextClientId).sample.toRight(billsErr)

    } yield (secondContact, ((c1, c2), Seq(b1), Seq(it1), Seq(bill1).flatten))
  }
}
