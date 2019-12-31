package com.reporting.gendata

import com.reporting.gendata.Config.BatchSize
import com.reporting.gendata.gens._
import com.reporting.gendata.storage.Storage

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait RandomSetScenario[T] {
  type PersonalInfo = (
    mutable.ArrayBuffer[T],
    mutable.ArrayBuffer[ItUsage],
    mutable.ArrayBuffer[Banking],
    mutable.ArrayBuffer[List[Bill]]
  )

  val storage: Storage

  def addRandomData(start: Int, end: Int): (mutable.ArrayBuffer[Future[Unit]], Int) = {
    def container =
      new PersonalInfo(mutable.ArrayBuffer(), mutable.ArrayBuffer(), mutable.ArrayBuffer(), mutable.ArrayBuffer())

    val (batches, batchCount) = (start until end).toStream
      .grouped(BatchSize)
      .map(_.foldLeft(container) {
        case ((aa, bb, cc, dd), i) =>
          val (a, b, c, d) = generate(i)
          (aa :+ a, bb :+ b, cc :+ c, dd :+ d)
      })
      .foldLeft((mutable.ArrayBuffer[Future[Unit]](), 0)) {
        case ((acc, i), cnt @ (a, b, c, d)) =>
          val size = a.length + b.length + c.length + d.foldLeft(0)(_ + _.length)
          val fs = store(i, cnt)
          val total = i + fs.length
          println(s"submitted batch of $size size, progress: ${BatchSize * total}/${(end - start) * fs.length}")
          (acc ++ fs, total)
      }

    (batches, batchCount)
  }

  def store(i: Int, info: PersonalInfo): mutable.ArrayBuffer[Future[Unit]] =
    info match {
      case (a, b, c, d) =>
        val f1 = toFuture(i, storeContact(a))
        val f2 = toFuture(i + 1, storage.storeItUsages(b))
        val f3 = toFuture(i + 2, storage.storeBankings(c))
        val f4 = toFuture(i + 3, storage.storeBills(d.flatten))
        mutable.ArrayBuffer(f1, f2, f3, f4)
    }

  def generate(i: Int): (T, ItUsage, Banking, List[Bill])

  protected def toFuture(batchId: Int, thunk: => Either[String, Unit]): Future[Unit] = {
    val f = Future(thunk).flatMap { r =>
      if (r.isRight) Future.successful(())
      else Future.failed(new RuntimeException(r.left.get))
    }

    f.onComplete(t => {
      t.foreach(_ => println(s"batch completed $batchId"))
      t.failed.foreach(e => println(s"batch failed $batchId: $e"))
    })

    f
  }

  def storeContact(c: mutable.ArrayBuffer[T]): Either[String, Unit]
}

class RandomContactSet(override val storage: Storage) extends RandomSetScenario[Contact] {

  def generate(i: Int): (Contact, ItUsage, Banking, List[Bill]) = {
    val contact = ContactGen.generate(i).sample.get
    val usage = ItUsageGen.generate(contact.clientId, contact.firstName, contact.lastName).sample.get
    val banking = BankingGen.generate(contact.clientId).sample.get
    val bills = BillsGen.generate(contact.clientId).sample.get
    (contact, usage, banking, bills)
  }

  def storeContact(c: mutable.ArrayBuffer[Contact]): Either[String, Unit] = storage.storeContacts(c)
}

class RandomMarketingSet(override val storage: Storage) extends RandomSetScenario[Marketing] {

  def generate(i: Int): (Marketing, ItUsage, Banking, List[Bill]) = {
    val marketing = MarketingGen.generate(i).sample.get
    val usage = ItUsageGen.generate(marketing.clientId, marketing.firstName, marketing.lastName).sample.get
    val banking = BankingGen.generate(marketing.clientId).sample.get
    val bills = BillsGen.generate(marketing.clientId).sample.get
    (marketing, usage, banking, bills)
  }

  def storeContact(c: mutable.ArrayBuffer[Marketing]): Either[String, Unit] = storage.storeMarketing(c)
}
