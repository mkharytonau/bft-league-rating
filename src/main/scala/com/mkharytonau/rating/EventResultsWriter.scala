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

trait EventResultsWriter[C, A] {
  def write(
      calculated: EventResultsCalculated[A],
      eventConfig: EventConfig[C, A]
  ): Unit
}

object EventResultsWriter {
  object CSV extends EventResultsWriter[Any, Unit] {
    def write(
        calculated: EventResultsCalculated[Unit],
        eventConfig: EventConfig[Any, Unit]
    ): Unit = {
      val writer = CSVWriter.open(
        s"/Users/mkharytonau/Projects/bft-league-rating/src/main/resources/${eventConfig.calculatedPathCsv.value}" // TODO don't hardcode path to resources folder
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

  object HTML extends EventResultsWriter[Any, Unit] {
    def write(
        calculated: EventResultsCalculated[Unit],
        eventConfig: EventConfig[Any, Unit]
    ): Unit = {
      val filePath = s"/Users/mkharytonau/Projects/bft-league-rating/src/main/resources/${eventConfig.calculatedPathHtml.value}" // TODO don't hardcode path to resources folder
      val writer = new PrintWriter(filePath)

      val headerWithCalculation = calculated.results.header.value
        .map(_.value) ++ List("Место", "Очки в рейтинг")

      val rows = calculated.calculated.map { calc =>
        calculated.results.header.value.map(columnName =>
          calc.result.rawFields(columnName.value)
        ) ++ calc.calculation
          .map(c => List(c.place.value.toString, c.points.value.toString))
          .getOrElse(List.empty)
      }

      val htmlString = Html.resultsPage(
        pageTitle = eventConfig.name.value,
        header = eventConfig.name.value,
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
