package com.reporting.gendata

object Config {
  val ContactsCount: Int = sys.env.get("COUNT").map(_.toInt).getOrElse(100000)
  val BatchSize: Int = sys.env.get("BATCH_SIZE").map(_.toInt).getOrElse(5000)
}
