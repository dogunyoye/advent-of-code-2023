package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Advent of Code 2023, Day 1. */
public class Day01 {

    private static final Map<String, Character> WORD_TO_DIGIT = Map.of(
        "one", '1',
        "two", '2',
        "three", '3',
        "four", '4',
        "five", '5',
        "six", '6',
        "seven", '7',
        "eight", '8',
        "nine", '9'
    );

    private static String normalisedValue(String line) {
        final Map<Integer, Character> indexToValueMap = new HashMap<>();

        for (int i = 0; i < line.length(); i++) {
            final char c = line.charAt(i);
            if (Character.isDigit(c)) {
                indexToValueMap.put(i, c);
            }
        }

        WORD_TO_DIGIT.entrySet().forEach((e) -> {
            int index = line.indexOf(e.getKey());
            while (index != -1) {
                indexToValueMap.put(index, e.getValue());
                index = line.indexOf(e.getKey(), index + 1);
            }
        });

        final int first = indexToValueMap.keySet().stream().min(Integer::compare).get();
        final int last = indexToValueMap.keySet().stream().max(Integer::compare).get();

        return String.format("%c%c", indexToValueMap.get(first), indexToValueMap.get(last));
    }

    public int sumOfAllCalibrationValues(List<String> lines) {
        return lines.stream()
            .map(line -> line.replaceAll("[a-z]", ""))
            .map(line -> String.format("%c%c", line.charAt(0), line.charAt(line.length()-1)))
            .mapToInt(digits -> Integer.parseInt(digits))
            .sum();
    }

    public int sumOfAllRevisedCalibrationValues(List<String> lines) {
        return lines.stream()
            .map(Day01::normalisedValue)
            .mapToInt(digits -> Integer.parseInt(digits))
            .sum();
    }

    public static void main(String[] args) throws IOException {
        final List<String> documentLines = Files.readAllLines(Path.of("src/main/resources/Day01.txt"));
        System.out.println("Part 1: " + new Day01().sumOfAllCalibrationValues(documentLines));
        System.out.println("Part 2: " + new Day01().sumOfAllRevisedCalibrationValues(documentLines));
    }
}
