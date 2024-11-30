package com.toldoven.aoc.notebook

import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.Method
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.Doc
import it.skrape.selects.html5.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlinx.jupyter.api.HTML
import org.jetbrains.kotlinx.jupyter.api.MimeTypedResult
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.time.Duration
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId
import java.time.ZonedDateTime

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

    companion object {
        fun fromHtmlDoc(doc: Doc) = doc.run {
            val partDescriptionList = findAll("article.day-desc")
            val partSolutionList = "main > p" {
                findAll {
                    filter {
                        it.text.startsWith("Your puzzle answer was ")
                    }
                }
            }

            assert(partDescriptionList.size in 1..2)
            assert(partSolutionList.size in 0..2)

            val aocPageDayList = partDescriptionList.mapIndexed { index, doc ->
                val solution = partSolutionList.getOrNull(index)?.let {
                    AocPageSolution(
                        it.html,
                        it.code {
                            findFirst { text }
                        },
                    )
                }
                AocPagePart(doc.html, solution)
            }

            AocPageDay(
                aocPageDayList[0],
                aocPageDayList.getOrNull(1),
            )
        }
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
        val time = LocalDateTime.of(year, Month.DECEMBER, day, 0, 0, 0).atZone(zone)
        val now = ZonedDateTime.now(zone)

        if (now.isAfter(time)) return

        val duration = Duration.between(now, time)

        val humanReadableTime: String = sequenceOf(
            Duration::toDays to "day",
            Duration::toHours to "hour",
            Duration::toMinutes to "minute",
            Duration::toSeconds to "second",
        )
            .map { (a, b) -> a(duration) to b }
            .firstOrNull { (a, _) -> a > 0 }
            .let { it ?: (1L to "second") }
            .let { (count, string) ->
                buildString {
                    append(count)
                    append(' ')
                    append(string)
                    if (count > 1L) append("s")
                }
            }

        throw Exception("This day is not unlocked yet. Unlocks in $humanReadableTime")
    }

    companion object {
        private val zone = ZoneId.of("America/New_York")
    }
}

class AocClient(private val sessionToken: String) {

    private val baseUrl = URL("https://adventofcode.com/")

    private val aocSkrape = skrape(HttpFetcher) {
        request {
            headers = mapOf("Cookie" to "session=$sessionToken")
        }
    }

    private suspend fun aocSkrapeDay(day: AocDay) = aocSkrape.apply {
        request {
            url = URL(baseUrl, "/${day.year}/day/${day.day}").toString()
        }
    }

    private fun verifyResponseCode(code: Int) {
        if (code in 200..299) return

        if (code == 404) {
            throw Exception("Server error HTTP ${code}. Puzzle might not be unlocked yet, try again!")
        }

        if (code == 500) {
            throw Exception("Server error HTTP ${code}. It's likely that the session AOC token is invalid.")
        }

        throw Exception("Unknown server error HTTP $code")
    }

    fun fetchInput(day: AocDay): String {
        day.requireUnlocked()

        val url = URL(baseUrl, "/${day.year}/day/${day.day}/input")

        val input = url.openConnection()
            .let { it as HttpURLConnection }
            .apply {
                requestMethod = "GET"
                setRequestProperty("Cookie", "session=$sessionToken")
                verifyResponseCode(responseCode)
            }
            .inputStream
            .bufferedReader()
            .readText()

        return input
    }

    suspend fun fetchAocPageDay(day: AocDay): AocPageDay {
        day.requireUnlocked()

        return aocSkrapeDay(day).response {

            verifyResponseCode(responseStatus.code)

            htmlDocument {
                AocPageDay.fromHtmlDoc(this)
            }
        }
    }

    suspend fun partOneHTML(day: AocDay): String = fetchAocPageDay(day).partOne.html

    suspend fun partTwoHTML(day: AocDay): String = fetchAocPageDay(day).partTwo?.html ?:
    throw Exception("Part two is not unlocked yet!")


    suspend fun submit(part: Int, day: AocDay, answer: String): Pair<SubmissionOutcome, String> {
        day.requireUnlocked()

        val result = aocSkrape.apply {
            request {
                url = URL(baseUrl, "/${day.year}/day/${day.day}/answer").toString()
                method = Method.POST
                body {
                    form {
                        "level" to part
                        "answer" to answer
                    }
                }
            }
        }.response {
            htmlDocument {
                article {
                    findFirst {
                        this
                    }
                }
            }
        }

        val outcomeText = result.p {
            findFirst { text }
        }

        val outcome = when {
            outcomeText.startsWith("That's the right answer!") -> SubmissionOutcome.CORRECT
            outcomeText.startsWith("That's not the right answer") -> SubmissionOutcome.INCORRECT
            outcomeText.startsWith("You gave an answer too recently") -> SubmissionOutcome.WAIT
            outcomeText.startsWith("You don't seem to be solving the right level") -> SubmissionOutcome.WRONG_LEVEL
            else -> throw Exception("Unknown submission outcome. Text: $outcomeText")
        }

        return outcome to result.html
    }

    fun interactiveDay(year: Int, day: Int) = InteractiveAocDay(this, AocDay(year, day))

    companion object {
        fun fromFile(): AocClient {
            val exception = "Advent of Code token is missing. Create a '.session' in the same folder as your notebook file and paste your Advent of Code token inside"

            val token = File(".session")
                .also { require(it.isFile) { exception } }
                .readText()
                .also { require(it.isNotBlank()) { exception } }

            return AocClient(token)
        }

        fun fromEnv(): AocClient {
            val token = System.getenv("AOC_TOKEN") ?: throw Exception("No Advent of Code session token specified! Please specify the 'AOC_TOKEN' environment variable")

            return AocClient(token)
        }
    }
}

private fun cacheFile(file: File, fetchFunc: () -> String) = if (file.isFile) {
    file.readText()
} else {
    fetchFunc().also {
        file.parentFile.mkdirs()
        file.writeText(it)
    }
}

class InteractiveAocDay(
    private val client: AocClient,
    private val day: AocDay,
) {
    private val cachePath = File(".aocCache/day/${day.year}/day${day.day}/")

    fun input() = cacheFile(
        File(cachePath, "input.txt")
    ) {
        runBlocking {
            client.fetchInput(day).trim()
        }
    }

    fun viewPartOne() = cacheFile(
        File(cachePath, "part_one.html")
    ) {
        runBlocking {
            client.partOneHTML(day)
        }
    }
        .let(::HTML)

    fun viewPartTwo() = cacheFile(
        File(cachePath, "part_two.html")
    ) {
        runBlocking {
            client.partTwoHTML(day)
        }
    }
        .let(::HTML)


    private fun getSolution(part: Int) = cacheFile(
        File(cachePath, "part${part}_solution.txt")
    ) {
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
}