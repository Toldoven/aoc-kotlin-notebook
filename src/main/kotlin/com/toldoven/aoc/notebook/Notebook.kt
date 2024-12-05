package com.toldoven.aoc.notebook

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlinx.jupyter.api.HTML
import org.jetbrains.kotlinx.jupyter.api.MimeTypedResult
import java.io.File
import java.time.Duration
import java.time.Instant
import kotlin.time.toKotlinDuration

private class AocDayCache(cacheDir: File, day: AocDay, private val tokenHash: String) {

    private val cacheDirDay = File(cacheDir, "year${day.year}/day${day.day}")

    init {
        cacheDirDay.mkdirs()
    }

    fun getFile(
        path: String,
        unique: Boolean = true,
        fetchFunc: () -> String,
    ): String {

        val file = if (unique) {
            File(File(cacheDirDay, "user_$tokenHash"), path)
        } else {
            File(cacheDirDay, path)
        }

        return if (file.isFile) {
            file.readText()
        } else {
            fetchFunc().also {
                file.parentFile.mkdirs()
                file.writeText(it)
            }
        }
    }

    companion object {
        fun fromEnvOrDefault(tokenHash: String, day: AocDay): AocDayCache {
            val path = System.getenv("AOC_CACHE_DIR") ?: ".aocCache"

            val dir = File(path)

            return AocDayCache(dir, day, tokenHash)
        }
    }
}

class InteractiveAocDay(
    private val client: AocClient,
    private val day: AocDay,
) {
    private val cache = AocDayCache.fromEnvOrDefault(client.tokenHash, day)

    fun input() = cache.getFile("input.txt") {
        runBlocking {
            client.fetchInput(day)
        }
    }

    fun viewPartOne() = cache.getFile("part_one.html", false) {
        runBlocking {
            client.fetchAocPageDay(day).partOne.html
        }
    }
        .let(::HTML)

    fun viewPartTwo() = cache.getFile("part_two.html", false) {
        runBlocking {
            client.fetchAocPageDay(day).partTwo?.html
                ?: throw Exception("Part two is not unlocked yet!")
        }
    }
        .let(::HTML)


    private fun getSolution(part: Int) = cache.getFile("part${part}_solution.txt") {
        runBlocking { client.fetchAocPageDay(day) }
            .getPart(part)
            ?.solution
            ?.value ?: throw Exception("Solution is not unlocked yet")
    }

    private fun formatSubmissionResult(answer: String, resultBody: String) = HTML("""
        <div>
            <p>Your answer: $answer.</p>
            $resultBody
        </div>
    """.trimIndent())

    private suspend fun submit(part: Int, answer: String): MimeTypedResult {

        val (outcome, outcomeHtml) = client.submit(part, day, answer)

        if (outcome != SubmissionOutcome.WRONG_LEVEL && outcome != SubmissionOutcome.WAIT) {
            return formatSubmissionResult(answer, outcomeHtml)
        }

        val solution = runCatching { getSolution(part) }.getOrNull() ?: run {
            return formatSubmissionResult(answer, outcomeHtml)
        }

        return buildString {
            if (solution == answer) {
                append("<p>The answer is correct!</p>")
                append("<p>Your answer: $answer</p>")
            } else {
                append("<p>Wrong answer!")
                append("<br/>")
                append(" ⇒ Got: $answer")
                append("<br/>")
                append(" ⇒ Expected: $solution")
                append("</p>")
            }

            append("<small>You already completed this part.</small>")
        }
            .let(::HTML)
    }

    fun submitPartOne(answer: Any) = runBlocking {
        submit(1, answer.toString())
    }

    fun submitPartTwo(answer: Any) = runBlocking {
        submit(2, answer.toString())
    }

    fun waitUntilUnlocked() = runBlocking {

        val (dayEta, durationEta) = client.nextDayEta()
            ?.takeIf { (dayEta, _) -> day == dayEta }
            ?: run {
                println("Server ETA is not available, falling back to local ETA")
                day to day.untilStartsEstimate()
            }

        if (day < dayEta || (day == dayEta && durationEta.isNegative)) {
            println("Day is already unlocked!")
            return@runBlocking
        }

        fun Number.pad() = toString().padStart(2, '0')

        val now = Instant.now()

        (durationEta.seconds downTo 0L).asSequence().map { secondsLeft ->
            val timeLeft = Duration.ofSeconds(secondsLeft)
            val instant = now.plusSeconds(durationEta.seconds - secondsLeft)
            timeLeft to instant
        }.forEach { (timeLeft, instant) ->
            val delay = Duration.between(Instant.now(), instant)
                .toMillis()
                .coerceAtLeast(0)

            delay(delay)

            val formated = timeLeft.toKotlinDuration().toComponents { hours, minutes, seconds, _ ->
                "${hours.pad()}:${minutes.pad()}:${seconds.pad()}"
            }

            print("AoC ${day.year} Day ${day.day} starts in ${formated}\r")
        }

        println("Day started!")
    }
}