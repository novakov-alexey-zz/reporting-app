package com.reporting.gendata.gens

import com.reporting.gendata.Marketing
import org.scalacheck.Gen

object MarketingGen extends AbstractContactGen[Marketing] {

  def generate(id: Int): Gen[Marketing] =
    for {
      degree <- academicDegreeGn
      lName <- GenUtils.lastNameGen
      salut <- salutationGen
      fName <- if (isMen(salut)) GenUtils.menFirstNameGen else GenUtils.womenFirstNameGen
      addr <- addressGen
      post <- postCode
      city <- cityGen
      born <- birthDayGen
      email <- GenUtils.email(fName, lName)
      phone <- phoneNumber
      mob <- phoneNumber
      updateDate <- GenUtils.updateDate
    } yield Marketing(id, salut, degree, lName, fName, addr, post, city, born, email, phone, mob)
}
