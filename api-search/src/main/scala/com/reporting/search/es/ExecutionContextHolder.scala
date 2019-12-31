package com.reporting.search.es

import java.util.concurrent.{ForkJoinPool, TimeUnit}

import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext

// Execution context wrapper for Fork-Join based pool
class ExecutionContextHolder extends LazyLogging with AutoCloseable {

  private[this] val pool = new ForkJoinPool()

  val ec: ExecutionContext = ExecutionContext.fromExecutor(pool)

  def close(): Unit = {
    pool.shutdown()
    pool.awaitTermination(30, TimeUnit.SECONDS)
    logger.info(s"Pool {} stopped", this)
  }
}
