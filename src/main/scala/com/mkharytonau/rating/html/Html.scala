package com.mkharytonau.rating.html

import scalatags.Text.all._
import scalatags.Text.TypedTag
import scalatags.Text.tags2.{style, title}

object Html {
  def resultsTable(
      header: Seq[String],
      rows: Seq[Seq[String]]
  ): TypedTag[String] =
    table(
      thead(
        tr(header.map(th(_)))
      ),
      tbody(
        rows.map { row =>
          tr(row.map(td(_)))
        }
      )
    )

  def ratingTable(
      header: Seq[String],
      rows: Seq[Seq[String]]
  ): TypedTag[String] =
    table(
      thead(
        tr(header.map(th(_)))
      ),
      tbody(
        rows.map { row =>
          tr(row.map { cell =>
            cell.headOption match {
              case Some('▲') => td(cls := "green")(cell)
              case Some('▼') => td(cls := "red")(cell)
              case Some('−') => td(cls := "yellow")(cell)
              case _         => td(cell)
            }
          })
        }
      )
    )

  def resultsPage(
      pageTitle: String,
      header: String,
      resultsTable: TypedTag[String]
  ) = "<!DOCTYPE html>" +
    html(
      head(
        meta(charset := "UTF-8"),
        meta(
          name := "viewport",
          content := "width=device-width, initial-scale=1.0"
        ),
        title(pageTitle),
        style("""
				  table {
				  width: 100%;
				  border-collapse: collapse;
				  }
				  th, td {
				  border: 1px solid #ddd;
				  padding: 8px;
				  text-align: left;
				  }
				  th {
				  background-color: #f2f2f2;
				  }
				  tr:nth-child(even) {
				  background-color: #f9f9f9;
				  }
				  tr:hover {
				  background-color: #ddd;
				  }
				""")
      ),
      body(
        a(href := "index.html", "← Все категории"),
        h1(header),
        resultsTable
      )
    )

  def ratingPage(
      pageTitle: String,
      header: String,
      ratingTable: TypedTag[String]
  ) = "<!DOCTYPE html>" +
    html(
      head(
        meta(charset := "UTF-8"),
        meta(
          name := "viewport",
          content := "width=device-width, initial-scale=1.0"
        ),
        title(pageTitle),
        style("""
          table {
          width: 100%;
          border-collapse: collapse;
          }
          th, td {
          border: 1px solid #ddd;
          padding: 8px;
          text-align: left;
          }
          th {
          background-color: #f2f2f2;
          }
          tr:nth-child(even) {
          background-color: #f9f9f9;
          }
          tr:hover {
          background-color: #ddd;
          }
          .red {
          color: red;
          }
          .green {
          color: green;
          }
          .yellow {
          color:rgb(237, 208, 41);
          }
        """)
      ),
      body(
        a(href := "rating.html", "← Все категории"),
        h1(header),
        ratingTable
      )
    )
}
