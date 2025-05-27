package com.mkharytonau.rating

import com.mkharytonau.rating.domain.License.{FIOInRussian, FIOInEnglish}
import com.github.tototoshi.csv.CSVReader
import scala.io.Source
import com.mkharytonau.rating.domain._

object Licenses {
  private def load(path: ResourcePath): List[License] = {
    val raw = CSVReader.open(Source.fromResource(path.value)).allWithHeaders()

    raw.flatMap { fields =>
      if (fields("Номер").trim().startsWith("AG"))
        Some(
          License(
            LicenseId(fields("Номер").trim()),
            FIOInRussian(fields("ФИО на русском").trim()),
            FIOInEnglish(fields("ФИО на английском").trim()),
            Gender.fromString(fields("Пол")),
            AG.fromString(fields("Категория")).get,
            Club.fromString(fields("Спорт клуб")),
            Birthday(fields("Дата рождения"))
          )
        )
      else None
    }
  }

  def loadMen(path: ResourcePath): List[License] =
    load(path).filter(_.gender == Gender.Men)
  def loadWomen(path: ResourcePath): List[License] =
    load(path).filter(_.gender == Gender.Women)
}
