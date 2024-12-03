package com.toldoven.aoc.notebook

import java.time.*

enum class SubmissionOutcome {
    CORRECT,
    INCORRECT,
    WAIT,
    WRONG_LEVEL,
}

data class AocPageDay(
    val partOne: AocPagePart,
    val partTwo: AocPagePart?,
) {
    fun getPart(part: Int) = when (part) {
        1 -> partOne
        2 -> partTwo
        else -> throw Exception("There is not part $part")
    }
}

data class AocPagePart(
    val html: String,
    val solution: AocPageSolution?,
)

data class AocPageSolution(
    val html: String,
    val value: String,
)

data class AocDay(val year: Int, val day: Int) {
    init {
        require(day in 1..25) {
            "Day $day is not the day of Advent of Code. Please enter a number between 1 and 25"
        }
        require(year >= 2015) {
            "There is no Advent Of Code for year $year"
        }
    }

    fun requireUnlocked() {
        val time = LocalDateTime.of(year, Month.DECEMBER, day, 0, 0).atZone(zone)
        val now = ZonedDateTime.now(zone)

        if (now.isAfter(time)) {
            return
        }

        val duration = Duration.between(now, time)

        val humanReadableTime = duration.humanReadable()

        throw Exception("This day is not unlocked yet. Unlocks in $humanReadableTime")
    }

    companion object {
        private val zone = ZoneId.of("America/New_York")
    }
}