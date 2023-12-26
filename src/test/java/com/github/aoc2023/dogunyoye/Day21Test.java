package com.github.aoc2023.dogunyoye;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

public class Day21Test {
    private static List<String> data;
    private static char[][] map;

    @BeforeClass
    public static void setUp() throws IOException {
        data = Files.readAllLines(Path.of("src/test/resources/Day21TestInput.txt"));
        map = new Day21().buildMap(data);
    }

    @Test
    public void testPartOne() throws IOException {
        assertEquals(16, new Day21().findPlots(map, 6, false));
    }
}
