package com.github.aoc2023.dogunyoye;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

public class Day11Test {
    private static List<String> data;
    private static List<String> data2;

    @BeforeClass
    public static void setUp() throws IOException {
        data = Files.readAllLines(Path.of("src/test/resources/Day11TestInput.txt"));
        data2 = Files.readAllLines(Path.of("src/test/resources/Day11TestInput.txt"));
    }

    @Test
    public void testPartOne() throws IOException {
        assertEquals(374, new Day11().findSumOfShortestLengths(data));
    }

    @Test
    public void testPartTwo() throws IOException {
        assertEquals(8410, new Day11().findSumOfShortestLengthsPart2(data2, 100));
    }
}
