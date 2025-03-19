package com.mkharytonau.rating

import com.mkharytonau.rating.domain.EventResultWithCalculation
import com.mkharytonau.rating.domain.ResourcePath
import com.github.tototoshi.csv.CSVWriter
import scala.io.Source
import com.mkharytonau.rating.domain.Header
import com.mkharytonau.rating.domain.EventResultsCalculated

trait EventResultsWriter[A] {
  def write(
      calculated: EventResultsCalculated[A],
      path: ResourcePath
  ): Unit
}

object EventResultsWriter {
  object Standart extends EventResultsWriter[Unit] {
    def write(
        calculated: EventResultsCalculated[Unit],
        path: ResourcePath
    ): Unit = {
      val writer = CSVWriter.open(
        s"/Users/mkharytonau/Projects/bft-league-rating/src/main/resources/${path.value}" // TODO don't hardcode path to resources folder
      )
      val headerWithCalculation = calculated.results.header.value
        .map(_.value) ++ List("Место", "Очки в рейтинг за выступление")

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
}
