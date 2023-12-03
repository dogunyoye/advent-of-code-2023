package com.github.aoc2023.dogunyoye;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

public class Day03Test {
    private static List<String> schematic;

    @BeforeClass
    public static void setUp() throws IOException {
        schematic = Files.readAllLines(Path.of("src/test/resources/Day03TestInput.txt"));
    }

    @Test
    public void testPartOne() throws IOException {
        assertEquals(4361, new Day03().sumOfAllPartNumbers(schematic));
    }

    @Test
    public void testPartTwo() throws IOException {
        assertEquals(467835, new Day03().sumOfAllGearRatios(schematic));
    }
}
