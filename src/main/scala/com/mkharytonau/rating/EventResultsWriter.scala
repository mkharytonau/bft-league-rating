package com.mkharytonau.rating

import com.mkharytonau.rating.domain.EventResultWithCalculation
import com.mkharytonau.rating.domain.ResourcePath
import com.github.tototoshi.csv.CSVWriter
import scala.io.Source
import com.mkharytonau.rating.domain.Header
import com.mkharytonau.rating.domain.EventResultsCalculated
import java.io.PrintWriter
import com.mkharytonau.rating.html.Html
import com.mkharytonau.rating.domain.EventConfig
import com.mkharytonau.rating.domain.Gender
import cats.syntax.show._

trait EventResultsWriter {
  def write(
      calculated: EventResultsCalculated,
      eventConfig: EventConfig,
      filename: String
  ): Unit
}

object EventResultsWriter {
  object CSV extends EventResultsWriter {
    def write(
        calculated: EventResultsCalculated,
        eventConfig: EventConfig,
        filename: String
    ): Unit = {
      val writer = CSVWriter.open(
        s"/Users/mkharytonau/Projects/bft-league-rating/src/main/resources/${eventConfig.resultsPath.value}/$filename.csv" // TODO don't hardcode path to resources folder
      )
      val headerWithCalculation = calculated.results.header.value
        .map(_.value) ++ List("Место", "Очки в рейтинг")

      writer.writeRow(headerWithCalculation)
      calculated.calculated.foreach { calc =>
        val row = calculated.results.header.value.map(columnName =>
          calc.result.rawFields(columnName.value)
        ) ++ calc.calculation
          .map(c => List(c.place.value.toString, c.points.value.toString))
          .getOrElse(List.empty)
        writer.writeRow(row)
      }

      writer.close()
    }
  }

  object HTML extends EventResultsWriter {
    def write(
        calculated: EventResultsCalculated,
        eventConfig: EventConfig,
        filename: String
    ): Unit = {

      val filePath =
        s"/Users/mkharytonau/Projects/bft-league-rating/src/main/resources/${eventConfig.resultsPath.value}/$filename.html" // TODO don't hardcode path to resources folder
      val writer = new PrintWriter(filePath)

      val headerWithCalculation = calculated.results.header.value
        .map(_.value) ++ List("Место", "Очки в рейтинг")

      val maxPoints = calculated.calculated
        .flatMap(_.calculation.map(_.points.value))
        .maxOption
      val rows = calculated.calculated.map { calc =>
        val row = calculated.results.header.value.map(columnName =>
          calc.result.rawFields(columnName.value)
        ) ++ calc.calculation
          .map(c => List(c.place.value.toString, c.points.value.toString))
          .getOrElse(List.empty)
        val gradient = calc.calculation.flatMap(c =>
          maxPoints.map(m => (c.points.value / m) * 100)
        )
        (gradient, row)
      }

      val htmlString = Html.resultsPage(
        pageTitle = eventConfig.name.ratingName,
        header = eventConfig.name.ratingName,
        resultsTable = Html.resultsTable(
          headerWithCalculation,
          rows
        )
      )

      writer.write(htmlString)
      writer.close()
    }
  }
}
