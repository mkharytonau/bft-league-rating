package com.mkharytonau.rating

import com.mkharytonau.rating.domain.License.{FIOInRussian, FIOInEnglish}
import com.github.tototoshi.csv.CSVReader
import scala.io.Source
import com.mkharytonau.rating.domain._
import scala.concurrent.duration.FiniteDuration
import scala.util.{Try, Failure, Success}
import scala.concurrent.duration._

trait EventResults {
  def load(path: ResourcePath): List[EventResult]
}

object EventResults {
  object OBelarus {
    def load(path: ResourcePath): List[EventResult] = {
      val raw = CSVReader.open(Source.fromResource(path.value)).allWithHeaders()

      raw.map(fields =>
        EventResult(
          Nickname(fields("Фамилия Имя")),
          parseDuration(fields("Время")),
          fields
        )
      )
    }

    def parseDuration(str: String): FiniteDuration = Try {
      val (hh, mm, ss, millisOrTens) = str match {
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
        println(s"Unable to parse $str as FiniteDuration");
        throw exception
      case Success(value) =>
        value
    }
  }
}
