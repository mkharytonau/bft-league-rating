package com.mkharytonau.rating

import com.github.tototoshi.csv._
import java.io.File
import scala.io.Source
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scala.util.{Try, Failure, Success}
import BFTLeagueRating._
import com.mkharytonau.rating.domain.ResourcePath

object Main extends App {

  val licenses =
    CSVReader.open(Source.fromResource("2025/licenses.csv")).allWithHeaders()
  val menRaw = CSVReader
    .open(Source.fromResource("2025/Logoisk_winter_tri/women_duo_raw.csv"))
    .allWithHeaders()

	val menWithResult = menRaw.map(row => (row, parseDuration(row("Время")))).sortBy { case (_, result) => result }
	val (champion, championResult) = menWithResult.head
	
	val menWithPlaceWithPoints = menWithResult.map { case (athlete, athleteResult) => 
		val place = menWithResult.indexWhere { case (a, _) => a("Фамилия Имя") == athlete("Фамилия Имя") } + 1
		(athlete, place, calculatePoints(athleteResult, championResult, 700D)) 
	}
	
	menWithPlaceWithPoints.foreach { case (athlete, place, points) => println(s"${athlete("Фамилия Имя")}, $place, $points")}
}

object BFTLeagueRating {
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
	
	def calculatePoints(athleteResult: FiniteDuration, championResult: FiniteDuration, competitionPoints: Double): Double = 
		competitionPoints * scala.math.max((1 - (athleteResult - championResult) / 0.8 / championResult), 0)
}
