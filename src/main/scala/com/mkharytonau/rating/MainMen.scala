package com.mkharytonau.rating

import com.github.tototoshi.csv._
import java.io.File
import scala.io.Source
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scala.util.{Try, Failure, Success}
import com.mkharytonau.rating.domain._

object MainMen extends App {
  val licenses = Licenses.loadMen(ResourcePath("2025/licenses.csv"))
  val nameMapping = NameMapping.load(ResourcePath("2025/name_mapping.csv"))

  val competitionConfigs = List(
    CompetitionConfig(
      name = CompetitionName("Зимний триатлон. Логойск"),
      events = List(
        EventConfig[Any, Unit](
          EventName("Зимний триатлон", "LogoiskWinterTri"),
          EventCategory.Kross,
          ResourcePath("2025/Logoisk_winter_tri/men_tri_raw.csv"),
          EventResultsReader.OBelarus,
          ResourcePath("2025/Logoisk_winter_tri/men_tri_calculated.csv"),
          ResourcePath("2025/Logoisk_winter_tri/men_tri_calculated.html"),
          EventResultsCalculator.Standart,
          700.0
        ),
        EventConfig[Any, Unit](
          EventName("Зимний дуатлон", "LogoiskWinterDuo"),
          EventCategory.Kross,
          ResourcePath("2025/Logoisk_winter_tri/men_duo_raw.csv"),
          EventResultsReader.OBelarus,
          ResourcePath("2025/Logoisk_winter_tri/men_duo_calculated.csv"),
          ResourcePath("2025/Logoisk_winter_tri/men_duo_calculated.html"),
          EventResultsCalculator.Standart,
          650.0
        )
      )
    ),
    CompetitionConfig(
      name = CompetitionName("Минск Индор Триатлон"),
      events = List(
        EventConfig[Any, Unit](
          EventName("Индор триатлон", "MinskIndoorTriathlon"),
          EventCategory.Kross,
          ResourcePath("2025/Minsk_Indoor_Triathlon/men.csv"),
          EventResultsReader.MinskIndoorTriathlon,
          ResourcePath("2025/Minsk_Indoor_Triathlon/men_calculated.csv"),
          ResourcePath("2025/Minsk_Indoor_Triathlon/men_calculated.html"),
          EventResultsCalculator.Standart,
          700.0
        ),
      )
    ),
    CompetitionConfig(
      name = CompetitionName("Дрогичин Дуатлон"),
      events = List(
        EventConfig[Any, Unit](
          EventName("Дрогичин Дуатлон", "DuathlonDrogichin"),
          EventCategory.Duathlon,
          ResourcePath("2025/Drogichin/men.csv"),
          new EventResultsReader.Athlinks("ИМЯ ФАМИЛИЯ", "РЕЗУЛЬТАТ"),
          ResourcePath("2025/Drogichin/men_calculated.csv"),
          ResourcePath("2025/Drogichin/men_calculated.html"),
          EventResultsCalculator.Standart,
          700.0
        ),
      )
    ),
    CompetitionConfig(
      name = CompetitionName("МогиЛев"),
      events = List(
        EventConfig[Any, Unit](
          EventName("МогиЛев", "TriathlonMogiLev"),
          EventCategory.Sprint,
          ResourcePath("2025/Mogilev/men.csv"),
          new EventResultsReader.Athlinks("ИМЯ ФАМИЛИЯ", "РЕЗУЛЬТАТ"),
          ResourcePath("2025/Mogilev/men_calculated.csv"),
          ResourcePath("2025/Mogilev/men_calculated.html"),
          EventResultsCalculator.Standart,
          700.0
        ),
      )
    ),
    CompetitionConfig(
      name = CompetitionName("Браслав"),
      events = List(
        EventConfig[Any, Unit](
          EventName("Браслав", "KrossTriathlonBraslav"),
          EventCategory.Sprint,
          ResourcePath("2025/BraslavKross/men.csv"),
          EventResultsReader.OBelarus2,
          ResourcePath("2025/BraslavKross/men_calculated.csv"),
          ResourcePath("2025/BraslavKross/men_calculated.html"),
          EventResultsCalculator.Standart,
          700.0
        ),
      )
    ),
    CompetitionConfig(
      name = CompetitionName("Брест акватлон"),
      events = List(
        EventConfig[Any, Unit](
          EventName("Брест акватлон", "BrestAquathlon"),
          EventCategory.Duathlon,
          ResourcePath("2025/BrestAquathlon/men.csv"),
          EventResultsReader.OBelarus,
          ResourcePath("2025/BrestAquathlon/men_calculated.csv"),
          ResourcePath("2025/BrestAquathlon/men_calculated.html"),
          EventResultsCalculator.Standart,
          700.0
        ),
      )
    ),
    CompetitionConfig(
      name = CompetitionName("Заславль мультитриатлон"),
      events = List(
        EventConfig[Any, Unit](
          EventName("Заславль мультитриатлон", "ZaslavlMultitriathlon"),
          EventCategory.Sprint,
          ResourcePath("2025/Zaslavl_Multitriathlon/men.csv"),
          EventResultsReader.ZaslavlMultitriathlon,
          ResourcePath("2025/Zaslavl_Multitriathlon/men_calculated.csv"),
          ResourcePath("2025/Zaslavl_Multitriathlon/men_calculated.html"),
          EventResultsCalculator.Standart,
          700.0
        ),
      )
    ),
    CompetitionConfig(
      name = CompetitionName("Гомель спринт"),
      events = List(
        EventConfig[Any, Unit](
          EventName("Гомель спринт", "KubokPolesiaGomel"),
          EventCategory.Sprint,
          ResourcePath("2025/KubokPolesiaGomel/men.csv"),
          EventResultsReader.OBelarus,
          ResourcePath("2025/KubokPolesiaGomel/men_calculated.csv"),
          ResourcePath("2025/KubokPolesiaGomel/men_calculated.html"),
          EventResultsCalculator.Standart,
          700.0
        ),
      )
    ),
    CompetitionConfig(
      name = CompetitionName("Святск"),
      events = List(
        EventConfig[Any, Unit](
          EventName("Святск спринт", "SvyatskSprint"),
          EventCategory.Sprint,
          ResourcePath("2025/Svyatsk/men_sprint.csv"),
          new EventResultsReader.Athlinks("Участник", "Результат"),
          ResourcePath("2025/Svyatsk/men_sprint_calculated.csv"),
          ResourcePath("2025/Svyatsk/men_sprint_calculated.html"),
          EventResultsCalculator.Standart,
          700.0
        ),
        EventConfig[Any, Unit](
          EventName("Святск олимпийка", "SvyatskOlympic"),
          EventCategory.Olympic,
          ResourcePath("2025/Svyatsk/men_olympic.csv"),
          new EventResultsReader.Athlinks("Участник", "Результат"),
          ResourcePath("2025/Svyatsk/men_olympic_calculated.csv"),
          ResourcePath("2025/Svyatsk/men_olympic_calculated.html"),
          EventResultsCalculator.Standart,
          800.0
        )
      )
    ),
    CompetitionConfig(
      name = CompetitionName("Брест олимпийка"),
      events = List(
        EventConfig[Any, Unit](
          EventName("Брест олимпийка", "BrestOlympic"),
          EventCategory.Olympic,
          ResourcePath("2025/BrestOlympic/men.csv"),
          new EventResultsReader.Athlinks("Участник", "Результат"),
          ResourcePath("2025/BrestOlympic/men_calculated.csv"),
          ResourcePath("2025/BrestOlympic/men_calculated.html"),
          EventResultsCalculator.Standart,
          800.0
        ),
      )
    ),
  )

  val competitionsCalculated = competitionConfigs.map { competitionConfig =>
    val eventsCalculated = competitionConfig.events.map { eventConfig =>
      val eventCalculated = eventConfig.resultCalculator.calculate(
        eventConfig.resultsLoader.read(eventConfig.resultsPath),
        eventConfig,
        licenses,
        nameMapping,
        ()
      )
      EventResultsWriter.CSV.write( // TODO implicit side effect
        eventCalculated,
        eventConfig
      )
      EventResultsWriter.HTML.write( // TODO implicit side effect
        eventCalculated,
        eventConfig
      )

      EventCalculated[Unit](eventConfig.name, eventConfig.eventCategory, eventCalculated)
    }

    CompetitionCalculated(eventsCalculated)
  }

  val rating = RatingCalculator.Standart2025.calculate(
    licenses,
    competitionsCalculated
  )

  RatingWriter.CSV.write(rating, ResourcePath("2025/rating_men.csv"))
  RatingWriter.HTML.write(rating, ResourcePath("2025/rating_men.html"))
}
