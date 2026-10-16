# advent-of-code-2023

Advent of Code 2023 solutions in Java (21 + Maven).

All questions: https://adventofcode.com/2023

## Prerequisites

- JDK 21
- Maven 3.9+

## Build

```sh
mvn clean verify
```

## Test

```sh
mvn test
```

Runs the JUnit sample-input tests in `src/test` (51 tests, all green).

## Run a day's solution

```sh
mvn exec:java -Dexec.mainClass="com.github.aoc2023.dogunyoye.DayXX"
```

where `XX` is the zero-padded day (`Day01` … `Day25`).

Each solution reads its personal puzzle input from `src/main/resources/DayXX.txt`.
Those files are git-ignored (see `.gitignore`) — add your own inputs there;
they are never committed.

## Layout

- `src/main/java/.../dogunyoye/DayXX.java` — one class per day, `main()` prints Part 1 / Part 2
- `src/test/java/.../dogunyoye/DayXXTest.java` — sample-input regression tests
- `src/test/resources/Day*TestInput.txt` — published AoC examples (committed)
- `src/main/resources/DayXX.txt` — your personal inputs (local only, ignored)
