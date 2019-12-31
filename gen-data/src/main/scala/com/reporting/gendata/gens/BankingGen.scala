package com.reporting.gendata.gens
import com.reporting.gendata.Banking
import org.scalacheck.Gen

object BankingGen extends Generator[Banking] {
  private val clientId = Gen.chooseNum(1, 100000)
  // DEkk bbbb bbbb cccc cccc cc
  private val germanIban = {
    val checkDigits = Gen.chooseNum(0, 99)
    val bankAndBranch = Gen.chooseNum(0, 99999999L)
    val accNumber = Gen.chooseNum(0, 9999999999L)

    for {
      cd <- checkDigits
      b <- bankAndBranch
      a <- accNumber
    } yield f"DE$cd%02d $b%08d $a%010d"
  }

  private val germanBics = Gen.oneOf(Dictionary.bics)

  override def generate(): Gen[Banking] =
    clientId.flatMap(generate)

  def generate(clientId: Int): Gen[Banking] =
    for {
      i <- germanIban
      b <- germanBics
      d <- GenUtils.updateDate
    } yield Banking(clientId, i, b, d)
}
