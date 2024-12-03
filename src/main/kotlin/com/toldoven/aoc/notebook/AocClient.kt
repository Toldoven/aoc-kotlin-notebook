package com.toldoven.aoc.notebook

import it.skrape.core.htmlDocument
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.Method
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.html5.*
import java.io.File
import java.net.URL
import java.security.MessageDigest
import java.time.Duration


class AocClient(private val sessionToken: String) {

    val tokenHash: String by lazy {
        MessageDigest
            .getInstance("SHA-256")
            .digest(sessionToken.toByteArray())
            .joinToString("") { "%02x".format(it) }
            .take(16)
    }

    private val baseUrl = URL("https://adventofcode.com/")

    private val aocSkrape = skrape(HttpFetcher) {
        request {
            headers = mapOf(
                "Cookie" to "session=$sessionToken",
                "User-Agent" to "https://github.com/Toldoven/aoc-kotlin-notebook by toldoven@proton.me",
            )
        }
    }

    private fun aocSkrapeDay(day: AocDay) = aocSkrape.apply {
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

    suspend fun fetchInput(day: AocDay): String {
        day.requireUnlocked()

        return aocSkrape.apply {
            request {
                url = URL(baseUrl, "/${day.year}/day/${day.day}/input").toString()
            }
        }.response {

            verifyResponseCode(responseStatus.code)

            responseBody.trim()
        }
    }

    suspend fun fetchAocPageDay(day: AocDay): AocPageDay {
        day.requireUnlocked()

        return aocSkrapeDay(day).response {

            verifyResponseCode(responseStatus.code)

            htmlDocument {
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

    suspend fun nextDayEta() = aocSkrape.apply {
        request {
            url = baseUrl.toString()
        }
    }.response {
        verifyResponseCode(responseStatus.code)

        runCatching {
            htmlDocument {
                val (day, serverEta) = "pre.calendar > span" {
                    findFirst {
                        val day = "span.calendar-day" {
                            findFirst {
                                text.toInt()
                            }
                        }

                        val serverEta = script {
                            findFirst {
                                Regex("var server_eta = (\\d+);")
                                    .find(html)
                                    ?.groups
                                    ?.get(1)
                                    ?.value
                                    ?.toLong()
                                    ?.let { Duration.ofSeconds(it) }
                            }
                        } ?: throw Exception("Can't find ETA")

                        day to serverEta
                    }
                }

                val year = h1(".title-event") {
                    a {
                        findFirst {
                            text.toInt()
                        }
                    }
                }

                AocDay(year, day) to serverEta
            }
        }.getOrNull()
    }

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
            verifyResponseCode(responseStatus.code)

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

            val path = System.getenv("AOC_TOKEN_FILE") ?: "./.aocToken"

            val file = File(path)

            require(file.isFile) {
                "Advent of Code token file is missing. Create a file and paste your Advent of Code token inside. File path: ${file.canonicalPath}"
            }

            val token = file.readText().also {
                require(it.isNotBlank()) {
                    "Advent of Code token file is empty. Paste your Advent of Code token inside. File path: ${file.canonicalPath}"
                }
            }

            return AocClient(token)
        }

        fun fromEnv(): AocClient {
            val token = System.getenv("AOC_TOKEN")
                ?: throw Exception("No Advent of Code session token specified! Please specify the 'AOC_TOKEN' environment variable")

            return AocClient(token)
        }
    }
}