package com.reporting.gendata.gens

import java.time.LocalDateTime

import com.reporting.gendata.Bill
import org.scalacheck.Gen

object BillsGen extends Generator[List[Bill]] {
  private val clientId = Gen.chooseNum(1, 100000)
  private val idGen = Gen.chooseNum(1, 99999999)
  private val amountGen = Gen.chooseNum(40, 2000d)
  private val billsCount = Gen.chooseNum(1, 4)

  private def billId(i: Int): Gen[String] =
    idGen.map(n => f"$n%08d#$i%03d") // example: "08154711#001", second part is growing sequence

  private def billsGen(n: Int, clientId: Int, d: LocalDateTime): Gen[List[Bill]] = {
    val bills = (1 to n).map(i => billId(i).flatMap(id => amountGen.flatMap(a => Bill(clientId, id, a, d))))
    bills.foldLeft(Gen.const(List[Bill]())) {
      case (acc, g) =>
        acc.flatMap(s => g.flatMap(b => Gen.const(b +: s)))
    }
  }

  override def generate: Gen[List[Bill]] =
    clientId.flatMap(generate)

  def generate(clientId: Int): Gen[List[Bill]] =
    for {
      c <- billsCount
      d <- GenUtils.updateDate
      bills <- billsGen(c, clientId, d)
    } yield bills
}
