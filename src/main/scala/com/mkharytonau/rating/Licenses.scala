package com.mkharytonau.rating

import com.mkharytonau.rating.domain.License.{FIOInRussian, FIOInEnglish}
import com.github.tototoshi.csv.CSVReader
import scala.io.Source
import com.mkharytonau.rating.domain._

object Licenses {
  def load(path: ResourcePath): List[License] = {
    val raw = CSVReader.open(Source.fromResource(path.value)).allWithHeaders()

    raw.map { fields =>
      val fioInRussian = FIOInRussian(fields("ФИО на русском").trim())

      License(
        LicenseId(fields("Номер").trim()),
        fioInRussian,
        FIOInEnglish(fields("ФИО на английском").trim()),
        Club.fromString(fields("Спорт клуб"))
      )
    }
  }
}
