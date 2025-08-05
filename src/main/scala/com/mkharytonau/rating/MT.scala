package com.mkharytonau.rating

import com.github.tototoshi.csv.CSVReader
import scala.io.Source
import com.github.tototoshi.csv.CSVWriter

object MT extends App {
  val reader = CSVReader.open(Source.fromResource("2025/mt.csv"))
  val (header, data) = reader.allWithOrderedHeaders()

  val writerMale113 = CSVWriter.open(
    s"/Users/mkharytonau/Projects/bft-league-rating/src/main/resources/2025/MinskTriathlon/men_113.csv"
  )
  val writerMaleOlymp = CSVWriter.open(
    s"/Users/mkharytonau/Projects/bft-league-rating/src/main/resources/2025/MinskTriathlon/men_olympic.csv"
  )

  val writerMaleSprint = CSVWriter.open(
    s"/Users/mkharytonau/Projects/bft-league-rating/src/main/resources/2025/MinskTriathlon/men_sprint.csv"
  )

  val writerFemale113 = CSVWriter.open(
    s"/Users/mkharytonau/Projects/bft-league-rating/src/main/resources/2025/MinskTriathlon/women_113.csv"
  )
  val writerFemaleOlymp = CSVWriter.open(
    s"/Users/mkharytonau/Projects/bft-league-rating/src/main/resources/2025/MinskTriathlon/women_olympic.csv"
  )
  val writerFemaleSprint = CSVWriter.open(
    s"/Users/mkharytonau/Projects/bft-league-rating/src/main/resources/2025/MinskTriathlon/women_sprint.csv"
  )

  val mergedHeader = header
    .map {
      case "Name" => "FullName"
      case "Lastname"  => "" // Remove Lastname from header
      case other       => other
    }
    .filter(_ != "")

  writerMale113.writeRow(mergedHeader)
  writerMaleOlymp.writeRow(mergedHeader)
  writerMaleSprint.writeRow(mergedHeader)
  writerFemale113.writeRow(mergedHeader)
  writerFemaleOlymp.writeRow(mergedHeader)
  writerFemaleSprint.writeRow(mergedHeader)

  data.foreach { row =>
    val gender = row("AG")
    val contest = row("Contest")
    // Merge FirstName and LastName into FullName
    val mergedRow = row
      .updated("FullName", row("Lastname") + " " + row("Name"))
      .removed("Name")
      .removed("Lastname")

    if (gender.contains("Male")) {
      if (row("Abc") != "DNF" && row("Abc") != "DNS") {
        contest match {
          case c if c.contains("Половинка") =>
            writerMale113.writeRow(mergedHeader.map(mergedRow))
          case c if c.contains("Олимпик") =>
            writerMaleOlymp.writeRow(mergedHeader.map(mergedRow))
          case c if c.contains("Спринт") =>
            writerMaleSprint.writeRow(mergedHeader.map(mergedRow))
        }
      }
    } else if (gender.contains("Female")) {
      if (row("Abc") != "DNF" && row("Abc") != "DNS") {
        contest match {
          case c if c.contains("Половинка") =>
            writerFemale113.writeRow(mergedHeader.map(mergedRow))
          case c if c.contains("Олимпик") =>
            writerFemaleOlymp.writeRow(mergedHeader.map(mergedRow))
          case c if c.contains("Спринт") =>
            writerFemaleSprint.writeRow(mergedHeader.map(mergedRow))
          case c if c.contains("Minsk Lady run") => () // do nothing
        }
      }
    }
  }

  writerMale113.close()
  writerMaleOlymp.close()
  writerMaleSprint.close()
  writerFemale113.close()
  writerFemaleOlymp.close()
  writerFemaleSprint.close()

}
