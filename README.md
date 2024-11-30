# Advent of Code Kotlin Notebook Framework

Solve Advent of Code interactively, without leaving your IDE, with [Kotlin Notebook](https://kotlinlang.org/docs/kotlin-notebook-overview.html)!

https://github.com/user-attachments/assets/46c85513-7156-412c-b26a-bade8593a429

## Prerequisites

To use [Kotlin Notebook](https://kotlinlang.org/docs/kotlin-notebook-overview.html) you need to have [IntelliJ IDEA **Ultimate**](https://www.jetbrains.com/idea/) (paid version).

You can start a trial for 30 days, or use [Early Access](https://www.jetbrains.com/idea/nextversion/) version for free (not always available).

Install [Kotlin Notebook Plugin](https://www.jetbrains.com/help/idea/kotlin-notebook.html#install-plugin).

## Getting Started

Check out the example: [example.ipynb](example.ipynb)

1. Clone the repository and open it in IntelliJ IDEA.

```bash
git clone https://github.com/Toldoven/aoc-kotlin-notebook.git
```

2. Create a Notebook.

![](https://i.imgur.com/i5Kigvb.png)

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

## Troubleshooting

If you can't resolve framework classes, try to change this from `All Project Libraries` to `aoc.main`.

![](https://i.imgur.com/VvYToNC.png)

Don't forget to restart the Kernel after changing the dependencies.

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


