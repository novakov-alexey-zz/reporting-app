package com.reporting.gendata.gens

import com.reporting.gendata.Contact
import org.scalacheck.Gen

object ContactGen extends AbstractContactGen[Contact] {

  override def generate(id: Int): Gen[Contact] =
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
    } yield Contact(id, salut, degree, lName, fName, addr, post, city, born, email, phone, mob, updateDate)
}
