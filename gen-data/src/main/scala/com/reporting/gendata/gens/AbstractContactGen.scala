package com.reporting.gendata.gens

import java.time.LocalDateTime

import cats.instances.string._
import cats.syntax.eq._
import org.scalacheck.Gen

trait AbstractContactGen[T] extends Generator[T] {
  protected val clientId: Gen[Int] = Gen.chooseNum(1, 100000)
  protected val salutationGen: Gen[String] = Gen.oneOf(Dictionary.salutation)
  protected val academicDegreeGn: Gen[String] = Gen.oneOf(Dictionary.academicDegree)

  protected val addressGen: Gen[String] = for {
    s <- Gen.oneOf(Dictionary.streets)
    n <- Gen.chooseNum(1, 50)
  } yield s"$s $n"

  protected val postCode: Gen[Int] = Gen.oneOf(Dictionary.statesAndCitiesAndZipCodes).map {
    case (_, (_, _, code)) => code
  }
  protected val cityGen: Gen[String] = Gen.oneOf(Dictionary.statesAndCitiesAndZipCodes).map {
    case (_, (_, city, _)) => city
  }

  protected val phoneNumber: Gen[String] = Gen.chooseNum(1, 10000000).map(n => f"+49-147-$n%07d")
  protected val minBornDate: LocalDateTime = LocalDateTime.now.minusYears(60)
  protected val maxBornDate: LocalDateTime = LocalDateTime.now.minusYears(25)
  protected val birthDayGen: Gen[LocalDateTime] = GenUtils.localDateTimeGen(minBornDate, maxBornDate)

  def isMen(salutation: String): Boolean = "Herr" === salutation

  override def generate(): Gen[T] =
    clientId.flatMap(generate)

  def generate(id: Int): Gen[T]
}
