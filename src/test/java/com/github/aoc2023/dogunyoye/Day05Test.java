package com.github.aoc2023.dogunyoye;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

public class Day05Test {
    private static List<String> recipes;

    @BeforeClass
    public static void setUp() throws IOException {
        recipes = Files.readAllLines(Path.of("src/test/resources/Day05TestInput.txt"));
    }

    @Test
    public void testPartOne() throws IOException {
        assertEquals(35, new Day05().findLowestLocationNumber(recipes));
    }

    @Test
    public void testPartTwo() throws IOException {
        assertEquals(46, new Day05().findLowestLocationForSeedNumberRange(recipes));
    }
}
