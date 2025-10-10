package com.mkharytonau.rating

import com.mkharytonau.rating.domain.CompetitionCalculated
import com.mkharytonau.rating.domain.Statistics
import com.mkharytonau.rating.domain.EventStatistics
import com.mkharytonau.rating.domain.EventStatistics.StatisticsCategories
import com.mkharytonau.rating.domain.EventStatistics.StatisticsCategories.ByLicense

object StatisticsCaclulator {
	def calculate(competitions: List[CompetitionCalculated]): Statistics = {
		val eventsStatistics = competitions.flatMap(_.events).map { eventCalculated => 
			
			val participants = eventCalculated.results.calculated.size
			val licensed = eventCalculated.results.calculated.count(_.calculation.isDefined)
			val unlicensed = participants - licensed
			val categories = EventStatistics.StatisticsCategories(
				license = ByLicense(
					licensed = licensed,
					unlicensed = unlicensed
				),
				ageGroup = eventCalculated.results.calculated.flatMap(_.calculation).groupBy(_.license.ag).view.mapValues(_.size).toMap
			)
			
			EventStatistics(
				eventCalculated.name,
				participants,
				categories
			)
			}

		Statistics(eventsStatistics)
	}
}