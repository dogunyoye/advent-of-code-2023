package com.github.aoc2023.dogunyoye;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

public class Day17Test {
    private static List<String> data;

    @BeforeClass
    public static void setUp() throws IOException {
        data = Files.readAllLines(Path.of("src/test/resources/Day17TestInput.txt"));
    }

    @Test
    public void testPartOne() throws IOException {
        assertEquals(102, new Day17().findMinimalHeatLossOfCrucible(data));
    }

    @Test
    public void testPartTwo() throws IOException {
        assertEquals(94, new Day17().findMinimalHeatLossOfUltraCrucible(data));
    }
}
