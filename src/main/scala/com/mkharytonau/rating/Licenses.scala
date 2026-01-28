package com.mkharytonau.rating

import com.mkharytonau.rating.domain.License.{FIOInRussian, FIOInEnglish}
import com.github.tototoshi.csv.CSVReader
import scala.io.Source
import com.mkharytonau.rating.domain._

object Licenses {
  def load(path: ResourcePath, gender: Gender): List[License] = {
    val raw = CSVReader.open(Source.fromResource(path.value)).allWithHeaders()

    val licenses = raw.flatMap { fields =>
      if (fields("Номер лицензии").trim().startsWith("AG"))
      {
        if (AG.fromString(fields("Возрастная категория")).isEmpty) println(s"Can't parse AG for ${fields("ФИО на русском")}")
        if (Gender.fromString(fields("Пол")).isEmpty) println(s"Can't parse Gender for ${fields("ФИО на русском")}")

        Some(
          License(
            LicenseId(fields("Номер лицензии").trim()),
            FIOInRussian(fields("ФИО на русском").trim()),
            FIOInEnglish(fields("ФИО на английском").trim()),
            Gender.fromString(fields("Пол")).get,
            AG.fromString(fields("Возрастная категория")).get,
            Club.fromString(fields("Спорт клуб"))
          )
        )
      }
      else None
    }

    licenses.filter(_.gender == gender)
  }
}
