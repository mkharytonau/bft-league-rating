package com.mkharytonau.rating

import com.mkharytonau.rating.domain._
import com.github.tototoshi.csv.CSVWriter
import com.mkharytonau.rating.html.Html
import scalatags.Text.all._
import scalatags.Text.TypedTag

trait RatingWriter {
  def write(rating: Rating, path: ResourcePath): Unit
}

object RatingWriter {
  object CSV extends RatingWriter {
    def write(rating: Rating, path: ResourcePath): Unit = {
      val writer = CSVWriter.open(
        ResourcesDir.path(path.value)
      )

      writer.writeRow(rating.header.value.map(_.value))

      rating.rows.foreach { ratingRow =>
        val license = ratingRow.license
        val row = List(
          ratingRow.place.value.toString,
          ratingRow.trend.show,
          license.fioInRussian.value,
          license.club.map(_.value).getOrElse(""),
          license.ag.show,
          ratingRow.placeAG.map(_.value.toString).getOrElse("")
        ) ++ ratingRow.eventsPoints.map(
          _.pointsMaybe.map(_.value.toString).getOrElse("")
        ) ++ List(f"${ratingRow.totalPoints.value}%.2f")

        writer.writeRow(row)
      }

      writer.close()
    }
  }

  object HTML extends RatingWriter {

    private val categoryLabel: EventCategory => String = {
      case EventCategory.Sprint   => "Спринт"
      case EventCategory.Stayer   => "Стайер"
      case EventCategory.Duathlon => "Дуатлон"
      case EventCategory.Multi    => "Мульти"
    }

    private val categoryClass: EventCategory => String = {
      case EventCategory.Sprint   => "cat-sprint"
      case EventCategory.Stayer   => "cat-stayer"
      case EventCategory.Duathlon => "cat-duathlon"
      case EventCategory.Multi    => "cat-multi"
    }

    // the visible summary columns; per-event points move into each row's
    // expandable detail instead of one column per event
    private val summaryHeader =
      List("№", "▼▲", "ФИО", "Клуб", "AG", "Место в AG", "Сумма", "")

    def write(rating: Rating, path: ResourcePath): Unit = {
      val filePath = ResourcesDir.path(path.value)
      val writer = new java.io.PrintWriter(filePath)

      val headerHtml = {
        val last = summaryHeader(summaryHeader.size - 2) // "Сумма"
        summaryHeader.init.init.map(th(_)) :+ th(
          span(last),
          br(),
          span(
            style := "font-size: 0.5em; color: gray;"
          )("Нажмите,"),
          raw("&nbsp;"),
          span(
            style := "font-size: 0.5em; color: gray;"
          )("чтобы получить"),
          raw("&nbsp;"),
          span(
            style := "font-size: 0.5em; color: gray;"
          )("объяснение")
        ) :+ th("")
      }

      def eventChip(
          eventPoints: EventPoints,
          countedNames: Set[EventName]
      ): TypedTag[String] = {
        val counted = countedNames.contains(eventPoints.eventName)
        val pointsStr =
          eventPoints.pointsMaybe.map(p => f"${p.value}%.2f").getOrElse("—")
        span(cls := (if (!counted) "chip dimmed" else "chip"))(
          span(cls := s"cat-badge ${categoryClass(eventPoints.eventCategory)}"),
          span(cls := "chip-name")(eventPoints.eventName.ratingName),
          span(cls := "chip-points")(pointsStr)
        )
      }

      def emptyCategoryChip(category: EventCategory): TypedTag[String] =
        span(cls := "chip chip-empty")(
          span(cls := s"cat-badge ${categoryClass(category)}"),
          span(cls := "chip-name")(categoryLabel(category)),
          span(cls := "chip-points")("не участвовал")
        )

      def breakdownSection(
          title: String,
          chips: Seq[TypedTag[String]]
      ): TypedTag[String] =
        div(cls := "breakdown-section")(
          div(cls := "breakdown-title")(title),
          div(cls := "chip-row")(chips)
        )

      def detailRow(ratingRow: RatingRow): TypedTag[String] = {
        val breakdown = ratingRow.breakdown
        val countedNames = breakdown.countingEventNames

        // a category can have events on record with nobody's result in them
        // (e.g. the athlete never raced a Stayer distance); those come back
        // as Some(EventPoints(..., pointsMaybe = None)) rather than a true
        // None, so check pointsMaybe explicitly instead of just the Option
        val priorityChips = breakdown.priorityByCategory.map {
          case (_, Some(eventPoints)) if eventPoints.pointsMaybe.isDefined =>
            eventChip(eventPoints, countedNames)
          case (category, _) => emptyCategoryChip(category)
        }
        val otherChips = breakdown.otherCounting
          .filter(_.pointsMaybe.isDefined)
          .map(eventChip(_, countedNames))
        val notCounted = ratingRow.eventsPoints.filter(ep =>
          ep.pointsMaybe.isDefined && !countedNames.contains(ep.eventName)
        )
        val notCountedChips = notCounted.map(eventChip(_, countedNames))

        val sections = List(
          Some(breakdownSection("Лучшие по категориям", priorityChips)),
          Option.when(otherChips.nonEmpty)(
            breakdownSection(
              s"Лучшие остальные (${otherChips.size})",
              otherChips
            )
          ),
          Option.when(notCountedChips.nonEmpty)(
            breakdownSection("Не учтено", notCountedChips)
          )
        ).flatten

        tr(cls := "detail-row")(
          td(attr("colspan") := summaryHeader.size.toString)(sections)
        )
      }

      val rows = rating.rows.flatMap { ratingRow =>
        val license = ratingRow.license
        val clubStr = license.club.map(_.value).getOrElse("")
        val agPlace = ratingRow.placeAG
          .map(p => span(p.value.toString))
          .getOrElse(
            span(style := "font-size: 0.5em;")("Награждается в абсолюте")
          )
        val jsCaluclatorPath = ratingRow.eventsPoints
          .map(eventPoints =>
            s"${eventPoints.eventName.jsCalculatorName}=${eventPoints.pointsMaybe.map(_.value.toString).getOrElse("")}"
          )
          .mkString("&")
        val genderParam = license.gender match {
          case Gender.Men   => "men"
          case Gender.Women => "women"
        }
        val totalPointsStr = f"${ratingRow.totalPoints.value}%.2f"
        val place = ratingRow.place.value match {
          case 1                           => span("🥇")
          case 2                           => span("🥈")
          case 3                           => span("🥉")
          case _ if ratingRow.theBestTrend => span("🚀")
          case _ => span(ratingRow.place.value.toString)
        }
        val rankCls = ratingRow.place.value match {
          case 1 => " rank-1"
          case 2 => " rank-2"
          case 3 => " rank-3"
          case _ => ""
        }
        // the rank/trend cells show emoji/arrows, not plain numbers, so
        // table-tools.js's numeric sort needs the real value via data-sort
        val row = List(
          td(
            attr("data-label") := "№",
            attr("data-sort") := ratingRow.place.value.toString
          )(place), {
            val trendAttrs = Seq(
              attr("data-label") := "▼▲",
              attr("data-sort") := ratingRow.trend.value.toString
            )
            ratingRow.trend.show.headOption match {
              case Some('▲') =>
                td(cls := "green", trendAttrs)(ratingRow.trend.show)
              case Some('▼') =>
                td(cls := "red", trendAttrs)(ratingRow.trend.show)
              case Some('−') =>
                td(cls := "yellow", trendAttrs)(ratingRow.trend.show)
              case _ =>
                td(trendAttrs)(ratingRow.trend.show)
            }
          },
          td(
            style := "white-space: nowrap;",
            attr("data-label") := "ФИО"
          )(
            img(
              src := s"./img/avatars/thumbnails/${license.fioInRussian.value}.jpg",
              alt := "",
              style := "border-radius: 50%; max-height: 2em; max-width: 2em; vertical-align: middle;",
              onerror := "this.src='./img/avatars/thumbnails/placeholder.jpg'"
            ),
            raw("&nbsp;"),
            span(license.fioInRussian.value)
          ),
          td(attr("data-label") := "Клуб")(clubStr),
          td(
            style := "white-space: nowrap;",
            attr("data-label") := "AG"
          )(license.ag.show),
          td(
            attr("data-label") := "Место в AG",
            attr("data-sort") := ratingRow.placeAG.map(_.value.toString).getOrElse("")
          )(agPlace),
          td(attr("data-label") := "Сумма")(
            a(
              href := s"./rating_points_calculator.html?$jsCaluclatorPath&gender=$genderParam&scalaTotalValue=$totalPointsStr"
            )(totalPointsStr)
          ),
          td(cls := "expand-toggle")("▸")
        )

        val gradientPct = rating.winnerPoints.map(winnerPoints =>
          ratingRow.totalPoints.value / winnerPoints.value * 100.0
        )
        val summaryRow = tr(
          cls := s"rating-row-summary$rankCls",
          attr("style") := s"--bar:${gradientPct.getOrElse(0)}%"
        )(row)

        List(summaryRow, detailRow(ratingRow))
      }

      val htmlTable = table(cls := "enhanced-table rating-table")(
        thead(
          tr(headerHtml)
        ),
        tbody(rows)
      )

      val htmlString = Html.ratingPage(
        "Рейтинг Любительской Лиги триатлона",
        "Рейтинг Любительской Лиги триатлона",
        htmlTable
      )

      writer.write(htmlString)
      writer.close()
    }
  }
}
