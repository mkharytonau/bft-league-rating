package com.mkharytonau.rating

import com.github.tototoshi.csv._
import java.io.File
import scala.io.Source
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._
import scala.util.{Try, Failure, Success}
import com.mkharytonau.rating.domain._

object MainWomen extends App {
  val licenses = Licenses.loadWomen(ResourcePath("2025/licenses.csv"))
  val nameMapping = NameMapping.load(ResourcePath("2025/name_mapping.csv"))

  val competitionConfigs = List(
    CompetitionConfig(
      name = CompetitionName("Зимний триатлон. Логойск"),
      events = List(
        EventConfig[Any, Unit](
          EventName("Зимний триатлон", "LogoiskWinterTri"),
          EventCategory.Kross,
          ResourcePath("2025/Logoisk_winter_tri/women_tri_raw.csv"),
          EventResultsReader.OBelarus,
          ResourcePath("2025/Logoisk_winter_tri/women_tri_calculated.csv"),
          ResourcePath("2025/Logoisk_winter_tri/women_tri_calculated.html"),
          EventResultsCalculator.Standart,
          700.0
        ),
        EventConfig[Any, Unit](
          EventName("Зимний дуатлон", "LogoiskWinterDuo"),
          EventCategory.Kross,
          ResourcePath("2025/Logoisk_winter_tri/women_duo_raw.csv"),
          EventResultsReader.OBelarus,
          ResourcePath("2025/Logoisk_winter_tri/women_duo_calculated.csv"),
          ResourcePath("2025/Logoisk_winter_tri/women_duo_calculated.html"),
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
          ResourcePath("2025/Minsk_Indoor_Triathlon/women.csv"),
          EventResultsReader.MinskIndoorTriathlon,
          ResourcePath("2025/Minsk_Indoor_Triathlon/women_calculated.csv"),
          ResourcePath("2025/Minsk_Indoor_Triathlon/women_calculated.html"),
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
          ResourcePath("2025/Drogichin/women.csv"),
          new EventResultsReader.Athlinks("ИМЯ ФАМИЛИЯ", "РЕЗУЛЬТАТ"),
          ResourcePath("2025/Drogichin/women_calculated.csv"),
          ResourcePath("2025/Drogichin/women_calculated.html"),
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
          ResourcePath("2025/Mogilev/women.csv"),
          new EventResultsReader.Athlinks("ИМЯ ФАМИЛИЯ", "РЕЗУЛЬТАТ"),
          ResourcePath("2025/Mogilev/women_calculated.csv"),
          ResourcePath("2025/Mogilev/women_calculated.html"),
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
          ResourcePath("2025/BraslavKross/women.csv"),
          EventResultsReader.OBelarus2,
          ResourcePath("2025/BraslavKross/women_calculated.csv"),
          ResourcePath("2025/BraslavKross/women_calculated.html"),
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
          ResourcePath("2025/BrestAquathlon/women.csv"),
          EventResultsReader.OBelarus,
          ResourcePath("2025/BrestAquathlon/women_calculated.csv"),
          ResourcePath("2025/BrestAquathlon/women_calculated.html"),
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
          ResourcePath("2025/Zaslavl_Multitriathlon/women.csv"),
          EventResultsReader.ZaslavlMultitriathlon,
          ResourcePath("2025/Zaslavl_Multitriathlon/women_calculated.csv"),
          ResourcePath("2025/Zaslavl_Multitriathlon/women_calculated.html"),
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
          ResourcePath("2025/KubokPolesiaGomel/women.csv"),
          EventResultsReader.OBelarus,
          ResourcePath("2025/KubokPolesiaGomel/women_calculated.csv"),
          ResourcePath("2025/KubokPolesiaGomel/women_calculated.html"),
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
          ResourcePath("2025/Svyatsk/women_sprint.csv"),
          new EventResultsReader.Athlinks("Участник", "Результат"),
          ResourcePath("2025/Svyatsk/women_sprint_calculated.csv"),
          ResourcePath("2025/Svyatsk/women_sprint_calculated.html"),
          EventResultsCalculator.Standart,
          700.0
        ),
        EventConfig[Any, Unit](
          EventName("Святск олимпийка", "SvyatskOlympic"),
          EventCategory.Olympic,
          ResourcePath("2025/Svyatsk/women_olympic.csv"),
          new EventResultsReader.Athlinks("Участник", "Результат"),
          ResourcePath("2025/Svyatsk/women_olympic_calculated.csv"),
          ResourcePath("2025/Svyatsk/women_olympic_calculated.html"),
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
          ResourcePath("2025/BrestOlympic/women.csv"),
          new EventResultsReader.Athlinks("Участник", "Результат"),
          ResourcePath("2025/BrestOlympic/women_calculated.csv"),
          ResourcePath("2025/BrestOlympic/women_calculated.html"),
          EventResultsCalculator.Standart,
          800.0
        ),
      )
    ),
    CompetitionConfig(
      name = CompetitionName("Минский Триатлон"),
      events = List(
        EventConfig[Any, Unit](
          EventName("МТ половинка", "MinskTriathlonHalfIronmen"),
          EventCategory.HalfIronmen,
          ResourcePath("2025/MinskTriathlon/women_113.csv"),
          EventResultsReader.MT,
          ResourcePath("2025/MinskTriathlon/women_113_calculated.csv"),
          ResourcePath("2025/MinskTriathlon/women_113_calculated.html"),
          EventResultsCalculator.Standart,
          900.0
        ),
        EventConfig[Any, Unit](
          EventName("МТ олимпийка", "MinskTriathlonOlympic"),
          EventCategory.Olympic,
          ResourcePath("2025/MinskTriathlon/women_olympic.csv"),
          EventResultsReader.MT,
          ResourcePath("2025/MinskTriathlon/women_olympic_calculated.csv"),
          ResourcePath("2025/MinskTriathlon/women_olympic_calculated.html"),
          EventResultsCalculator.Standart,
          800.0
        ),
        EventConfig[Any, Unit](
          EventName("МТ спринт", "MinskTriathlonSprint"),
          EventCategory.Sprint,
          ResourcePath("2025/MinskTriathlon/women_sprint.csv"),
          EventResultsReader.MT,
          ResourcePath("2025/MinskTriathlon/women_sprint_calculated.csv"),
          ResourcePath("2025/MinskTriathlon/women_sprint_calculated.html"),
          EventResultsCalculator.Standart,
          700.0
        )
      )
    )
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

  RatingWriter.CSV.write(rating, ResourcePath("2025/rating_women.csv"))
  RatingWriter.HTML.write(rating, ResourcePath("2025/rating_women.html"))
}
