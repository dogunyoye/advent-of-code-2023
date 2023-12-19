package com.github.aoc2023.dogunyoye;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

public class Day15Test {
    private static List<String> data;

    @BeforeClass
    public static void setUp() throws IOException {
        data = Files.readAllLines(Path.of("src/test/resources/Day15TestInput.txt"));
    }

    @Test
    public void testPartOne() throws IOException {
        assertEquals(1320, new Day15().sumOfHashAlgorithmRuns(data));
    }

    @Test
    public void testPartTwo() throws IOException {
        assertEquals(145, new Day15().calculateFocusingPower(data));
    }
}
