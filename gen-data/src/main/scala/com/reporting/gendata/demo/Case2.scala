package com.reporting.gendata.demo

/**
 * search for: Name, Surname, Birthdate and e-mail address
 *
 * search case: same names in different cities and similar E-mails -> duplicates / same person
 */
object Case2 extends TwoContactsCase {

  def contact1(clientId: Int): String =
    s"""
         |{
         |  "client_id": $clientId,
         |  "salutation": "Herr",
         |  "academic_degree": "Dipl.-Logist.",
         |  "last_name": "Doe",
         |  "first_name": "John",
         |  "address": "Hauptstrasse 140",
         |  "post_code": 10160,
         |  "city": "Berlin",
         |  "birth_day": "1988-01-21T01:25:33.512",
         |  "private_email": "john.smith@gmail.com",
         |  "private_phone": "+49-156-1230001",
         |  "private_mobile": "+49-151-4315951",
         |  "update_date": "2018-01-21T01:25:33.512"
         |}
       """.stripMargin

  def contact2(clientId: Int): String =
    s"""
         |{
         |  "client_id": $clientId,
         |  "salutation": "Herr",
         |  "academic_degree": "Dipl.-Logist.",
         |  "last_name": "Doe",
         |  "first_name": "John",
         |  "address": "Breite Strasse 19",
         |  "post_code": 60328,
         |  "city": "Frankfurt",
         |  "birth_day": "1988-01-21T01:25:33.512",
         |  "private_email": "johnsmithffm@gmail.com",
         |  "private_phone": "+49-177-3210321",
         |  "private_mobile": "+49-151-4315951"
         |}
       """.stripMargin
}
