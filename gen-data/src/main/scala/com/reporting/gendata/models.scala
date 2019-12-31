package com.reporting.gendata

import java.time.LocalDateTime

final case class Banking(clientId: Int, iban: String, bic: String, updateDate: LocalDateTime)
final case class Contact(
  clientId: Int,
  salutation: String,
  academicDegree: String,
  lastName: String,
  firstName: String,
  address: String,
  postCode: Int,
  city: String,
  birthDay: LocalDateTime,
  privateEmail: String,
  privatePhone: String,
  privateMobile: String,
  updateDate: LocalDateTime
)
final case class Marketing(
  clientId: Int,
  salutation: String,
  academicDegree: String,
  lastName: String,
  firstName: String,
  address: String,
  postCode: Int,
  city: String,
  birthDay: LocalDateTime,
  privateEmail: String,
  privatePhone: String,
  privateMobile: String
)
final case class Bill(clientId: Int, id: String, amount: Double, updateDate: LocalDateTime)
final case class ItUsage(
  clientId: Int,
  userId: String,
  email: String,
  registered: LocalDateTime,
  lastLogin: LocalDateTime,
  updateDate: LocalDateTime
)
