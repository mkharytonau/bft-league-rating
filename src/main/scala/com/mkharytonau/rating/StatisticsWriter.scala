package com.mkharytonau.rating

import com.mkharytonau.rating.domain.Statistics
import com.mkharytonau.rating.domain.ResourcePath
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._

object StatisticsWriter {
	def write(statistics: Statistics, path: ResourcePath): Unit = {
		val filePath =
			s"/Users/mkharytonau/Projects/bft-league-rating/src/main/resources/${path.value}" // TODO don't hardcode path to resources folder
		val writer = new java.io.PrintWriter(filePath)

		val json = statistics.asJson.spaces2

		writer.write(json)
		writer.close()
	}
}