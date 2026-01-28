package com.mkharytonau.rating

import com.mkharytonau.rating.domain.License.{FIOInRussian, FIOInEnglish}
import com.github.tototoshi.csv.CSVReader
import scala.io.Source
import com.mkharytonau.rating.domain._
import scala.concurrent.duration.FiniteDuration
import scala.util.{Try, Failure, Success}
import scala.concurrent.duration._
import com.mkharytonau.rating.domain.Gender.Women
import com.mkharytonau.rating.domain.Gender.Men

trait EventResultsReader {
  def read(path: ResourcePath, gender: Gender): EventResults
}

object EventResultsReader {
  trait ParseResult {
    def parseResult(fields: Map[String, String]): FiniteDuration
  }

  object ParseResult {
    class HoursMinutesSecondsMillisOrTens(resultField: String)
        extends ParseResult {
      def parseResult(fields: Map[String, String]): FiniteDuration = {
        Try {
          val (hh, mm, ss, millisOrTens) = fields(resultField) match {
            case s"$hh:$mm:$ss.$millisOrTens" => (hh, mm, ss, millisOrTens)
          }
          val hours = hh.toInt
          val minutes = mm.toInt
          val seconds = ss.toInt
          val millis = millisOrTens.length() match {
            case 1 => millisOrTens.toInt * 100
            case 2 => millisOrTens.toInt * 10
            case 3 => millisOrTens.toInt * 1
          }
          hours.hours + minutes.minutes + seconds.seconds + millis.millis
        } match {
          case Failure(exception) =>
            println(
              s"Unable to parse ${fields(resultField)} as FiniteDuration"
            );
            throw exception
          case Success(value) =>
            value
        }
      }

      def apply(resultField: String): HoursMinutesSecondsMillisOrTens =
        new HoursMinutesSecondsMillisOrTens(resultField)
    }

    object IndoorTriathlon extends ParseResult {
      def parseResult(fields: Map[String, String]): FiniteDuration = {
        Try {
          val (mm, ss) = fields("Итоговое время") match {
            case s"$mm:$ss" => (mm, ss)
          }
          val minutes = mm.toInt
          val seconds = ss.toInt
          minutes.minutes + seconds.seconds
        } match {
          case Failure(exception) =>
            println(
              s"Unable to parse ${fields("Итоговое время")} as FiniteDuration"
            );
            throw exception
          case Success(value) =>
            value
        }
      }
    }
  }

  trait ParseGender {
    def parseGender(fields: Map[String, String]): Gender
  }

  object ParseGender {
    class ByField(genderField: String, men: String, women: String)
        extends ParseGender {
      def parseGender(fields: Map[String, String]): Gender = {
        fields(genderField) match {
          case `men`   => Men
          case `women` => Women
        }
      }
    }

    object ByField {
      def apply(genderField: String, men: String, women: String): ByField =
        new ByField(genderField, men, women)
    }
  }

  class Configured(
      nicknameField: String,
      parseResult: ParseResult,
      parseGender: ParseGender
  ) extends EventResultsReader {
    def read(path: ResourcePath, gender: Gender): EventResults = {
      val reader = CSVReader.open(Source.fromResource(path.value + "/results.csv"))
      val (rawHeader, rawResults) = reader.allWithOrderedHeaders()
      reader.close()

      val header = Header(rawHeader.map(ColumnName(_)))
      val results = rawResults.flatMap(fields =>
        Option.when(parseGender.parseGender(fields) == gender)(
          EventResult(
            Nickname(fields(nicknameField)),
            parseResult.parseResult(fields),
            fields
          )
        )
      )

      EventResults(header, results)
    }
  }

  object Configured {
    def apply(
        nicknameField: String,
        parseResult: ParseResult,
        parseGender: ParseGender
    ): Configured =
      new Configured(nicknameField, parseResult, parseGender)
  }
}
