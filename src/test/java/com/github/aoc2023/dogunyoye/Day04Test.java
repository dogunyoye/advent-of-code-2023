package com.github.aoc2023.dogunyoye;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

public class Day04Test {
    private static List<String> cards;

    @BeforeClass
    public static void setUp() throws IOException {
        cards = Files.readAllLines(Path.of("src/test/resources/Day04TestInput.txt"));
    }

    @Test
    public void testPartOne() throws IOException {
        assertEquals(13, new Day04().calculatePoints(cards));
    }

    @Test
    public void testPartTwo() throws IOException {
        assertEquals(30, new Day04().totalScratchCards(cards));
    }
}
