package com.reporting.gendata.gens
import org.scalacheck.Gen

trait Generator[T] {
  def generate(): Gen[T]
}
