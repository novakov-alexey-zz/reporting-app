package com.reporting.gendata

import com.reporting.gendata.Config._
import com.reporting.gendata.ConnectionUtils._
import com.reporting.gendata.demo.TwoContactsCase.CaseData
import com.reporting.gendata.demo.{Case1, Case2}
import com.reporting.gendata.storage.ElasticsearchStorage
import com.reporting.gendata.storage.ElasticsearchStorage._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

object Main extends App {
  waitForHostsAvailable(60, 2, Seq(Host -> Port))
  println(s"========== Ready. Generating data set of $ContactsCount contacts with $BatchSize batch size =========")

  val storage = new ElasticsearchStorage
  val startTime = System.nanoTime()

  println("==================== Adding demo data =================================================")
  addDemoData(ContactsCount)
  println("==================== Demo data is added ===============================================")

  println("==================== Adding random data =================================================")
  val randomContacts = new RandomContactSet(storage)
  val (batches1, batchCount1) = randomContacts.addRandomData(0, ContactsCount / 2)

  val randomMarketings = new RandomMarketingSet(storage)
  val (batches2, batchCount2) = randomMarketings.addRandomData(ContactsCount / 2, ContactsCount)
  val batchCount = batchCount1 + batchCount2

  val waitTime = 10.minutes
  println(s"========== $batchCount batches submitted. Waiting $waitTime for completion ===========")
  Await.result(Future.sequence(batches1 ++ batches2), waitTime)

  showElapsedTime(startTime)

  private def showElapsedTime(startTime: Long): Unit = {
    val duration = (System.nanoTime() - startTime).nanos.toSeconds
    printf(s"\nTook time (hh:mm:ss): %02d:%02d:%02d\n", duration / 3600, (duration % 3600) / 60, duration % 60)
  }

  private def addDemoData(nextClientId: Int): Unit = {
    def store(data: CaseData): Either[String, Unit] =
      data match {
        case ((c, m), ba, it, bi) =>
          storage.storeContacts(Seq(c))
          storage.storeMarketing(Seq(m))
          storage.storeBankings(ba)
          storage.storeItUsages(it)
          storage.storeBills(bi)
      }

    val res = for {
      res <- Case1.generate(nextClientId)
      _ <- store(res._2)
      res2 <- Case2.generate(res._1 + 1)
      _ <- store(res2._2)
    } yield ()

    res.left.foreach(e => sys.error(s"Failed to add demo data, error: $e"))
  }
}
