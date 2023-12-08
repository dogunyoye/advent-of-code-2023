package com.github.aoc2023.dogunyoye;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

public class Day08Test {
    private static List<String> data;
    private static List<String> data2;

    @BeforeClass
    public static void setUp() throws IOException {
        data = Files.readAllLines(Path.of("src/test/resources/Day08Part1TestInput.txt"));
        data2 = Files.readAllLines(Path.of("src/test/resources/Day08Part2TestInput.txt"));
    }

    @Test
    public void testPartOne() throws IOException {
        assertEquals(6, new Day08().findNumberOfStepsToEnd(data));
    }

    @Test
    public void testPartTwo() throws IOException {
        assertEquals(6, new Day08().findNumberOfStepsToEndStartingFromANodes(data2));
    }
}
