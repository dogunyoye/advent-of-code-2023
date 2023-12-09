package com.github.aoc2023.dogunyoye;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

public class Day09Test {
    private static List<String> data;

    @BeforeClass
    public static void setUp() throws IOException {
        data = Files.readAllLines(Path.of("src/test/resources/Day09TestInput.txt"));
    }

    @Test
    public void testPartOne() throws IOException {
        assertEquals(114, new Day09().calculateSumOfExrapolatedValues(data, true));
    }

    @Test
    public void testPartTwo() throws IOException {
        assertEquals(2, new Day09().calculateSumOfExrapolatedValues(data, false));
    }
}
