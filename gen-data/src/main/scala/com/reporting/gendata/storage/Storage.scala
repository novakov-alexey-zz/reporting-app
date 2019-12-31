package com.reporting.gendata.storage

import com.reporting.gendata._

trait Storage {
  def storeMarketing(marketing: Seq[Marketing]): Either[String, Unit]
  def storeContacts(contacts: Seq[Contact]): Either[String, Unit]
  def storeBankings(banking: Seq[Banking]): Either[String, Unit]
  def storeBills(contract: Seq[Bill]): Either[String, Unit]
  def storeItUsages(itUsages: Seq[ItUsage]): Either[String, Unit]
}
