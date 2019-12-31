package com.reporting.gendata

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}

import argonaut.Argonaut._
import argonaut.{CodecJson, EncodeJson, _}

class JsonCodecs(formatter: DateTimeFormatter) {
  implicit def localDateTimeAsISO8601Encoder: EncodeJson[LocalDateTime] =
    EncodeJson(s => jString(s.atZone(ZoneId.systemDefault()).format(formatter)))

  implicit def localDateTimeAsISO8601Decoder: DecodeJson[LocalDateTime] =
    DecodeJson(_.as[String].map(LocalDateTime.parse))

  implicit def contactCodecJson: CodecJson[Contact] =
    casecodec13(Contact.apply, Contact.unapply)(
      "client_id",
      "salutation",
      "academic_degree",
      "last_name",
      "first_name",
      "address",
      "post_code",
      "city",
      "birth_day",
      "private_email",
      "private_phone",
      "private_mobile",
      "update_date"
    )

  implicit def marketingCodecJson: CodecJson[Marketing] =
    casecodec12(Marketing.apply, Marketing.unapply)(
      "client_id",
      "salutation",
      "academic_degree",
      "last_name",
      "first_name",
      "address",
      "post_code",
      "city",
      "birth_day",
      "private_email",
      "private_phone",
      "private_mobile"
    )

  implicit def bankingCodeJson: EncodeJson[Banking] =
    jencode4L((Banking.unapply _).andThen(_.get))("client_id", "iban", "bic", "update_date")

  implicit def itUsageCodeJson: EncodeJson[ItUsage] =
    jencode6L((ItUsage.unapply _).andThen(_.get))(
      "client_id",
      "user_id",
      "email",
      "registered",
      "last_login",
      "update_date"
    )

  implicit def billCodeJson: EncodeJson[Bill] =
    jencode4L((Bill.unapply _).andThen(_.get))("client_id", "id", "amount", "update_date")
}
