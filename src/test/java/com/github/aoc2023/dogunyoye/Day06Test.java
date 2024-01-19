package com.github.aoc2023.dogunyoye;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

public class Day06Test {
    private static List<String> data;

    @BeforeClass
    public static void setUp() throws IOException {
        data = Files.readAllLines(Path.of("src/test/resources/Day06TestInput.txt"));
    }

    @Test
    public void testPartOne() throws IOException {
        assertEquals(288, new Day06().calculateMarginOfError(data));
    }

    @Test
    public void testPartTwoBruteForce() throws IOException {
        assertEquals(71503, new Day06().findNumberOfWaysToBeatRecordBruteForce(data));
    }

    @Test
    public void testPartTwo() throws IOException {
        assertEquals(71503, new Day06().findNumberOfWaysToBeatRecord(data));
    }
}
