package com.reporting.gendata.gens

import java.time.{Instant, LocalDateTime, ZoneId}

import org.scalacheck.Gen

object GenUtils {
  private val domain = Gen.oneOf(Dictionary.domains)
  val menFirstNameGen: Gen[String] = Gen.oneOf(Dictionary.menFirstNames)
  val womenFirstNameGen: Gen[String] = Gen.oneOf(Dictionary.womenFirstNames)
  val lastNameGen: Gen[String] = Gen.oneOf(Dictionary.lastNames)
  val updateDate: Gen[LocalDateTime] = localDateTimeGen(LocalDateTime.of(2000, 1, 1, 0, 0, 0))

  def email(firstName: String, lastName: String): Gen[String] =
    domain.map(d => s"$firstName.$lastName@$d".toLowerCase)

  private def localDateGen(rangeStart: LocalDateTime, rangeEnd: LocalDateTime): Gen[Long] = {
    val start = rangeStart.atZone(ZoneId.systemDefault()).toInstant.toEpochMilli
    val end = rangeEnd.atZone(ZoneId.systemDefault()).toInstant.toEpochMilli
    Gen.choose(start, end)
  }

  def localDateTimeGen(rangeStart: LocalDateTime, rangeEnd: LocalDateTime = LocalDateTime.now()): Gen[LocalDateTime] = {
    localDateGen(rangeStart, rangeEnd)
      .map(l => Instant.ofEpochMilli(l).atZone(ZoneId.systemDefault()).toLocalDateTime)
  }
}
