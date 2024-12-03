package com.toldoven.aoc.notebook

import java.time.Duration

fun Duration.humanReadable() = sequenceOf(
    Duration::toDays to "day",
    Duration::toHours to "hour",
    Duration::toMinutes to "minute",
    Duration::toSeconds to "second",
)
    .map { (a, b) -> a(this) to b }
    .firstOrNull { (a, _) -> a > 1 }
    .let { it ?: (1L to "second") }
    .let { (count, string) ->
        buildString {
            append(count)
            append(' ')
            append(string)
            if (count > 1L) append("s")
        }
    }