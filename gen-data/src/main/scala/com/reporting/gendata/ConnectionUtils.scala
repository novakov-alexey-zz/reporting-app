package com.reporting.gendata

import java.net.Socket

import scala.annotation.tailrec
import scala.language.postfixOps
import scala.util.Try

object ConnectionUtils {

  private def isConnectionReady(hostName: String, port: Int): Boolean = {
    Try(new Socket(hostName, port)).map(_.close()).isSuccess
  }

  /** the hosts parameter is a sequence of (host,port) pairs */
  def waitForHostsAvailable(maxRetries: Int, retryTimeout: Int, hosts: Seq[(String, Int)]): Unit = {

    @tailrec
    def iterateRetry(retryCount: Int, missingHosts: Seq[(String, Int)]): Unit = {

      val nonReadyHosts = missingHosts.filterNot(isConnectionReady _ tupled)

      if (nonReadyHosts.nonEmpty) {
        if (retryCount <= 0) {
          println(s"Unable to connect to ${nonReadyHosts.mkString(", ")} for $maxRetries times")
          System.exit(-1)
        } else {
          println("waiting for host(s) to be ready: " + nonReadyHosts.map { case (h, p) => s"$h:$p" }.mkString(","))
          println(s"retrying in $retryTimeout seconds...")

          Thread.sleep(retryTimeout * 1000)
          iterateRetry(retryCount - 1, nonReadyHosts)
        }
      }
    }

    iterateRetry(maxRetries, hosts)
  }
}
