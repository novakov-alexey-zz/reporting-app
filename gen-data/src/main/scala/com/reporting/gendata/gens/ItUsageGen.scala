package com.reporting.gendata.gens

import java.time.LocalDateTime

import com.reporting.gendata.ItUsage
import org.scalacheck.Gen

object ItUsageGen extends Generator[ItUsage] {
  //DSGVOABCXYZ
  private val userId = Gen.alphaUpperStr.map(_.take(6))
  private val clientId = Gen.chooseNum(1, 100000)

  private val registeredDate = {
    val fiveYrsBack = LocalDateTime.now().minusYears(5)
    GenUtils.localDateTimeGen(fiveYrsBack)
  }

  private val lastLogin = GenUtils.localDateTimeGen(LocalDateTime.now().minusDays(30))

  override def generate: Gen[ItUsage] = {
    for {
      clientId <- clientId
      lName <- GenUtils.lastNameGen
      fName <- GenUtils.menFirstNameGen
      itUsage <- generate(clientId, fName, lName)
    } yield itUsage
  }

  def generate(clientId: Int, fName: String, lName: String): Gen[ItUsage] =
    for {
      id <- userId
      email <- GenUtils.email(fName, lName)
      registered <- registeredDate
      logged <- lastLogin
      updateDate <- GenUtils.updateDate
    } yield ItUsage(clientId, "DSGVO" + id, email, registered, logged, updateDate)
}
