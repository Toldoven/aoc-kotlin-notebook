package com.toldoven.aoc.notebook

import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlinx.jupyter.api.HTML
import org.jetbrains.kotlinx.jupyter.api.MimeTypedResult
import java.io.File


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