package com.reporting.gendata.demo

/**
 * search for: Name, Surname, Birthday and e-mail address
 *
 * search case: normal search -> same names in different city and different E-mails -> different persons
 */
object Case1 extends TwoContactsCase {

  def contact1(clientId: Int): String =
    s"""
        |{
        |  "client_id": $clientId,
        |  "salutation": "Herr",
        |  "academic_degree": "Dipl.-Logist.",
        |  "last_name": "Smith",
        |  "first_name": "Jonas",
        |  "address": "Kurze Strasse 24",
        |  "post_code": 19055,
        |  "city": "Schwerin",
        |  "birth_day": "1988-01-21T01:25:33.512",
        |  "private_email": "jonas.smith@gmail.com",
        |  "private_phone": "+49-153-1700001",
        |  "private_mobile": "+49-151-4315951",
        |  "update_date": "2018-01-21T01:25:33.512"
        |}
      """.stripMargin

  def contact2(clientId: Int): String =
    s"""
        |{
        |  "client_id": $clientId,
        |  "salutation": "Herr",
        |  "academic_degree": "Dipl.-Inf.",
        |  "last_name": "Smith",
        |  "first_name": "Jonas",
        |  "address": "Lange Strasse 98",
        |  "post_code": 60328,
        |  "city": "Frankfurt",
        |  "birth_day": "1988-01-21T01:25:33.512",
        |  "private_email": "jonas-smith@gmail.com",
        |  "private_phone": "+49-147-0000001",
        |  "private_mobile": "+49-147-6417551"
        |}
      """.stripMargin
}
