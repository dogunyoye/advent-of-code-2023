package com.github.aoc2023.dogunyoye;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

public class Day07Test {
    private static List<String> data;

    @BeforeClass
    public static void setUp() throws IOException {
        data = Files.readAllLines(Path.of("src/test/resources/Day07TestInput.txt"));
    }

    @Test
    public void testPartOne() throws IOException {
        assertEquals(6440, new Day07().calculateTotalWinnings(data));
    }

    @Test
    public void testPartTwo() throws IOException {
        assertEquals(5905, new Day07().calculateTotalWinningsWithJokerRule(data));
    }
}
