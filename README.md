[![Maven Central](https://img.shields.io/maven-central/v/com.toldoven.aoc/aoc-kotlin-notebook.svg?color=0)](https://central.sonatype.com/artifact/com.toldoven.aoc/aoc-kotlin-notebook)
[![License](https://img.shields.io/github/license/toldoven/aoc-kotlin-notebook.svg)](https://github.com/Toldoven/aoc-kotlin-notebook/blob/master/LICENSE)

# Advent of Code Kotlin Notebook Framework

Solve Advent of Code interactively, without leaving your IDE, with [Kotlin Notebook](https://kotlinlang.org/docs/kotlin-notebook-overview.html)!

https://github.com/user-attachments/assets/46c85513-7156-412c-b26a-bade8593a429

# Prerequisites

To use [Kotlin Notebook](https://kotlinlang.org/docs/kotlin-notebook-overview.html) you need to have [IntelliJ IDEA **Ultimate**](https://www.jetbrains.com/idea/) (paid version).

You can start a trial for 30 days, or use [Early Access](https://www.jetbrains.com/idea/nextversion/) version for free (not always available).

Install [Kotlin Notebook Plugin](https://www.jetbrains.com/help/idea/kotlin-notebook.html#install-plugin).

# Setup

## 1. Create a new Kotlin Notebook

![](https://i.imgur.com/i5Kigvb.png)

## 2. Add a dependency

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

## 3. Get your Advent of Code token

Open the developer console on the [Advent of Code website](https://adventofcode.com/) and grab the `session` cookie.

![](https://i.imgur.com/ucUbr3a.png)

## 4. Specify the token

### Using an environment variable

Open Kotlin Notebook settings and set the environment variable `AOC_TOKEN`.

![](https://i.imgur.com/rzNHhHq.png)

![](https://i.imgur.com/2gVWC6F.png)

When constructing a client use `AocClient.fromEnv()`.

### Using a file

Create a file in the current working directory called `.aocToken` and paste your token inside.

When constructing a client use `AocClient.fromFile()`.

You can specify a different path to the file with the `AOC_TOKEN_FILE` environment variable.

## 5. Update .gitignore

> [!CAUTION]
> If you're uploading your solutions to GitHub — don't forget to add files with your token to .gitignore.
> If you're using Kotlin Notebook environment variables, they are not ignored by default.
> They are saved in the `.idea/kotlinNotebook.xml` file.

All data that doesn't change is cached. This includes puzzle descriptions, input, and solutions.

The default cache folder is `.aocCache` in the current working directory. You can change it with `AOC_CACHE_DIR` environment variable.

Add cache folder to `.gitignore`.

Redistributing Advent of Code puzzle descriptions and inputs is **NOT ALLOWED.**

Since input and solutions are unique for each user, they are stored in a folder named with a secure hashed session token.

# Usage

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

# TODO

A few ideas I will hopefully get to implement.

- A way to run tests.
  - Perhaps a way to extract tests from the description automatically with an LLM. Not sure if this is legal, though.
- Synchronize with server time.
- A way to receive a notification when you can submit again after failing.
- A way to fetch a puzzle as soon as it becomes available.

# Additional Resources

- [Learn more about Kotlin Notebook](https://www.jetbrains.com/help/idea/kotlin-notebook.html#best-practices)
- [Solve Advent of Code 2024 Puzzles in Kotlin and Win Prizes](https://blog.jetbrains.com/kotlin/2024/11/advent-of-code-2024-in-kotlin/)


