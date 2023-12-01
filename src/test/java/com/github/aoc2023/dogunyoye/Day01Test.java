package com.github.aoc2023.dogunyoye;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

public class Day01Test {

    private static List<String> documentLines;
    private static List<String> revisedDocumentLines;

    @BeforeClass
    public static void setUp() throws IOException {
        documentLines = Files.readAllLines(Path.of("src/test/resources/Day01Part1TestInput.txt"));
        revisedDocumentLines = Files.readAllLines(Path.of("src/test/resources/Day01Part2TestInput.txt"));
    }

    @Test
    public void testPartOne() throws IOException {
        assertEquals(142, new Day01().sumOfAllCalibrationValues(documentLines));
    }

    @Test
    public void testPartTwo() throws IOException {
        assertEquals(281, new Day01().sumOfAllRevisedCalibrationValues(revisedDocumentLines));
    }
}