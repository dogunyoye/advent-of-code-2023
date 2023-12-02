package com.github.aoc2023.dogunyoye;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

public class Day02Test {
    private static List<String> gamesList;

    @BeforeClass
    public static void setUp() throws IOException {
        gamesList = Files.readAllLines(Path.of("src/test/resources/Day02TestInput.txt"));
    }

    @Test
    public void testPartOne() throws IOException {
        assertEquals(8, new Day02().sumCandidateGameIds(gamesList));
    }

    @Test
    public void testPartTwo() throws IOException {
        assertEquals(2286, new Day02().sumMaxCubesPerGame(gamesList));
    }
}
