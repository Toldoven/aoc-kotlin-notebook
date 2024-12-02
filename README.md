[![maven-central](https://img.shields.io/maven-central/v/com.toldoven.aoc/aoc-kotlin-notebook.svg?color=0)](https://central.sonatype.com/artifact/com.toldoven.aoc/aoc-kotlin-notebook)
[![License](https://img.shields.io/github/license/toldoven/aoc-kotlin-notebook.svg)](https://github.com/Toldoven/aoc-kotlin-notebook/blob/master/LICENSE)

# Advent of Code Kotlin Notebook Framework

Solve Advent of Code interactively, without leaving your IDE, with [Kotlin Notebook](https://kotlinlang.org/docs/kotlin-notebook-overview.html)!

https://github.com/user-attachments/assets/46c85513-7156-412c-b26a-bade8593a429

## Prerequisites

To use [Kotlin Notebook](https://kotlinlang.org/docs/kotlin-notebook-overview.html) you need to have [IntelliJ IDEA **Ultimate**](https://www.jetbrains.com/idea/) (paid version).

You can start a trial for 30 days, or use [Early Access](https://www.jetbrains.com/idea/nextversion/) version for free (not always available).

Install [Kotlin Notebook Plugin](https://www.jetbrains.com/help/idea/kotlin-notebook.html#install-plugin).

## Getting Started

1. Create a new Kotlin Notebook.

![](https://i.imgur.com/i5Kigvb.png)

2. Add a dependency to this library.

Just type this inside of the Kotlin Notebook:

```kotlin
@file:DependsOn("com.toldoven.aoc:aoc-kotlin-notebook:1.0.0")
```

Or with Gradle inside of `build.gradle.kts` if you don't want to add it manually every time you create a new notebook:

```kotlin
dependencies {
    implementation("com.toldoven.aoc:aoc-kotlin-notebook:1.0.0")
}
```

Don't forget to restart the Notebook kernel after adding a new dependency with Gradle.

3. Get your Advent of Code token and set an environment variable.

Open the developer console on the Advent of Code website and grab the `session` cookie.

![](https://i.imgur.com/ucUbr3a.png)

Then open Kotlin Notebook settings and set the environment variable `AOC_TOKEN`.

![](https://i.imgur.com/rzNHhHq.png)

![](https://i.imgur.com/2gVWC6F.png)

4. Use it!

Check out the example: [example.ipynb](example.ipynb)

```kotlin
val aoc = AocClient.fromEnv().interactiveDay(2019, 1)

aoc.viewPartOne()

aoc.input()

aoc.submitPartOne("...")

aoc.viewPartTwo()

aoc.submitPartOne("...")
```
> [!NOTE]
> To display the value in the Notebook — it needs to be on the last line of the block, or you can use `DISPLAY(...)` function.

![](https://i.imgur.com/xBAVuPQ.png)

## TODO

This project is not finished. I still have a few ideas I will hopefully get to implement.

- A way to run tests.
  - Perhaps a way to extract tests from the description automatically with LLM. Not sure if this is legal, though.
- Synchronize with server time.
- A way to receive a notification when you can submit again after failing.
- A way to fetch a puzzle as soon as it becomes available.
- Add proper utils module.

## Additional Resources

- [Learn more about Kotlin Notebook](https://www.jetbrains.com/help/idea/kotlin-notebook.html#best-practices)
- [Solve Advent of Code 2024 Puzzles in Kotlin and Win Prizes](https://blog.jetbrains.com/kotlin/2024/11/advent-of-code-2024-in-kotlin/)


