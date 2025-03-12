package com.mkharytonau.rating

import com.mkharytonau.rating.domain.License.{FIOInRussian, FIOInEnglish}
import com.github.tototoshi.csv.{CSVReader, DefaultCSVFormat, CSVFormat}
import scala.io.Source
import com.mkharytonau.rating.domain._

object NameMapping {
  implicit val CSVFormat: CSVFormat = new DefaultCSVFormat {
    override val delimiter: Char = ';'
  }

  def load(path: ResourcePath): Map[FIOInRussian, List[Nickname]] = {
    val raw = CSVReader.open(Source.fromResource(path.value)).allWithHeaders()

    raw.map { fields =>
      val nicknames = fields("Альтернативы")
        .split(";")
        .toList
        .map(str => Nickname(str.trim()))
      (FIOInRussian(fields("ФИО на русском").trim()), nicknames)
    }.toMap
  }
}
