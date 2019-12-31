package com.reporting.gendata

import com.reporting.gendata.gens._
import org.scalatest._

class GenTest extends FlatSpec with Matchers {

  it should "generate random contacts" in {
    val obj1 = ContactGen.generate().sample
    val obj2 = ContactGen.generate().sample
    (obj1 === obj2) should be(false)
  }

  it should "generate random banking details" in {
    val obj1 = BankingGen.generate().sample
    val obj2 = BankingGen.generate().sample
    (obj1 === obj2) should be(false)
  }

  it should "generate random itUsage" in {
    val obj1 = ItUsageGen.generate.sample
    val obj2 = ItUsageGen.generate.sample
    (obj1 === obj2) should be(false)
  }

  it should "generate random Bills" in {
    val obj1 = BillsGen.generate.sample
    val obj2 = BillsGen.generate.sample
    (obj1 === obj2) should be(false)
  }

  it should "generate random Marketing" in {
    val obj1 = MarketingGen.generate().sample
    val obj2 = MarketingGen.generate().sample
    (obj1 === obj2) should be(false)
  }
}
