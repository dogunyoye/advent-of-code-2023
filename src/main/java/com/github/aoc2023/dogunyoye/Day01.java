package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Day01 {

    private static Map<String, Character> map;

    static {
        map = new HashMap<>();
        map.put("one", '1');
        map.put("two", '2');
        map.put("three", '3');
        map.put("four", '4');
        map.put("five", '5');
        map.put("six", '6');
        map.put("seven", '7');
        map.put("eight", '8');
        map.put("nine", '9');
    }

    private String normalisedValue(String line) {
        final Map<Integer, Character> indexToValueMap = new HashMap<>();

        for (int i = 0; i < line.length(); i++) {
            final char c = line.charAt(i);
            if (Character.isDigit(c)) {
                indexToValueMap.put(i, c);
            }
        }

        map.entrySet().forEach((e) -> {
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
        int sum = 0;
        for (final String line : lines) {
            final String onlyDigits = line.replaceAll("[a-z]", "");
            final String digits = String.format("%c%c", onlyDigits.charAt(0), onlyDigits.charAt(onlyDigits.length()-1));
            sum += Integer.parseInt(digits);
        }
        return sum;
    }

    public int sumOfAllRevisedCalibrationValues(List<String> lines) {
        int sum = 0;
        for (final String line : lines) {
            final String digits = normalisedValue(line);
            sum += Integer.parseInt(digits);
        }
        return sum;
    }
    
    public static void main(String[] args) throws IOException {
        final List<String> documentLines = Files.readAllLines(Path.of("src/main/resources/Day01.txt"));
        System.out.println("Part 1: " + new Day01().sumOfAllCalibrationValues(documentLines));
        System.out.println("Part 2: " + new Day01().sumOfAllRevisedCalibrationValues(documentLines));
    }
}
