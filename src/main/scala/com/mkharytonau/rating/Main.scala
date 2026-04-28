package com.mkharytonau.rating

import com.github.tototoshi.csv._
import java.io.File
import scala.io.Source
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scala.util.{Try, Failure, Success}
import com.mkharytonau.rating.domain._
import com.mkharytonau.rating.EventResultsReader.ParseResult
import com.mkharytonau.rating.EventResultsReader.ParseGender
import com.mkharytonau.rating.domain.Gender.Women
import com.mkharytonau.rating.domain.Gender.Men
import cats.syntax.show._

object Main extends App {
  val nameMapping = NameMapping.load(ResourcePath("2026/name_mapping.csv"))

  val competitionConfigs = List(
    CompetitionConfig(
      name = CompetitionName("Империал Индор Триатлон"),
      events = List(
        EventConfig(
          EventName("Империал Индор Триатлон", "ImperialIndoorTriathlon"),
          EventCategory.Multi,
          ResourcePath("2026/ImperialIndoorTriathlon"),
          EventResultsReader.Configured("ФИО", ParseResult.IndoorTriathlon, ParseGender.ByField("Пол", "М", "Ж")),
          EventResultsCalculator.Standart,
          700.0
        )
      )
    ),
    CompetitionConfig(
      name = CompetitionName("Зимний триатлон. Логойск"),
      events = List(
        EventConfig(
          EventName("Зимний триатлон. Логойск", "LogoiskWinterTri"),
          EventCategory.Multi,
          ResourcePath("2026/Logoisk_Winter_Triathlon"),
          EventResultsReader.Configured("Фамилия Имя", ParseResult.HoursMinutesSecondsMillisOrTens("Время"), ParseGender.ByField("Пол", "М", "Ж")),
          EventResultsCalculator.Standart,
          700.0
        ),
        EventConfig(
          EventName("Зимний дуатлон. Логойск", "LogoiskWinterDuo"),
          EventCategory.Duathlon,
          ResourcePath("2026/Logoisk_Winter_Triathlon/duo"),
          EventResultsReader.Configured("Фамилия Имя", ParseResult.HoursMinutesSecondsMillisOrTens("Время"), ParseGender.ByField("Пол", "М", "Ж")),
          EventResultsCalculator.Standart,
          700.0,
          locatedInInnerFolder = true
        )
      )
    ),
    CompetitionConfig(
      name = CompetitionName("Минск Индор Триатлон"),
      events = List(
        EventConfig(
          EventName("Минск Индор Триатлон", "MinskIndoorTriathlon"),
          EventCategory.Multi,
          ResourcePath("2026/Minsk_Indoor_Triathlon"),
          EventResultsReader.Configured("ФИО", ParseResult.IndoorTriathlon, ParseGender.ByField("Пол", "М", "Ж")),
          EventResultsCalculator.Standart,
          700.0
        )
      )
    ),
    CompetitionConfig(
      name = CompetitionName("Дуатлон. Дрогичин"),
      events = List(
        EventConfig(
          EventName("Дуатлон. Дрогичин", "DuathlonDrogichin"),
          EventCategory.Duathlon,
          ResourcePath("2026/Duathlon_Drogichin"),
          EventResultsReader.Configured("Фамилия Имя", ParseResult.HoursMinutesSecondsMillisOrTens("Время"), ParseGender.ByField("Пол", "М", "Ж")),
          EventResultsCalculator.Standart,
          700.0
        )
      )
    ),
  )

  List[Gender](Men, Women).foreach { gender => 

    val licenses = Licenses.load(ResourcePath("2026/licenses.csv"), gender)

  val competitionsCalculated = competitionConfigs.map { competitionConfig =>
    val eventsCalculated = competitionConfig.events.map { eventConfig =>
      val eventCalculated = eventConfig.resultCalculator.calculate(
        eventConfig.resultsLoader.read(eventConfig.resultsPath, gender),
        eventConfig,
        licenses,
        nameMapping,
      )
      EventResultsWriter.CSV.write( // TODO implicit side effect
        eventCalculated,
        eventConfig,
        s"${gender.show}_calculated"
      )
      EventResultsWriter.HTML.write( // TODO implicit side effect
        eventCalculated,
        eventConfig,
        s"${gender.show}_calculated"
      )

      EventCalculated(
        eventConfig.name,
        eventConfig.eventCategory,
        eventCalculated
      )
    }

    CompetitionCalculated(eventsCalculated)
  }

  val rating = RatingCalculator.Standart2026.calculate(
    licenses,
    competitionsCalculated
  )

  RatingWriter.CSV.write(rating, ResourcePath(s"2026/rating_${gender.show}.csv"))
  RatingWriter.HTML.write(rating, ResourcePath(s"2026/rating_${gender.show}.html"))

  val statistics = StatisticsCaclulator.calculate(licenses, competitionsCalculated, rating)
  StatisticsWriter.write(statistics, ResourcePath(s"2026/statistics_${gender.show}.json"))
  }
}
