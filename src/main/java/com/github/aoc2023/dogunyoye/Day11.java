package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Day11 {

    private char[][] buildMap(List<String> data) {
        final List<Integer> rowInsertionIdxs = new ArrayList<>();
        final List<Integer> columnInsertionIdxs = new ArrayList<>();

        final int oldLength = data.get(0).length();

        for (int i = data.size() - 1; i >= 0; i--) {
            final String row = data.get(i);
            if (row.chars().allMatch((c) -> c == '.')) {
                rowInsertionIdxs.add(i);
            }
        }

        for (final int idx : rowInsertionIdxs) {
            data.add(idx, ".".repeat(oldLength));
        }

        for (int i = oldLength - 1; i >= 0; i--) {
            boolean allDots = true;
            for (int j = 0; j < data.size(); j++) {
                final char c = data.get(j).charAt(i);
                if (c != '.') {
                    allDots = false;
                    break;
                }
            }

            if (allDots) {
                columnInsertionIdxs.add(i);
            }
        }

        System.out.println(columnInsertionIdxs);


        for (int i = 0; i < data.size(); i++) {
            final StringBuilder sb = new StringBuilder(data.get(i));
            for (final int idx : columnInsertionIdxs) {
                sb.insert(idx, '.');
            }
            data.set(i, sb.toString());
        }

        for (String line : data) {
            System.out.println(line);
        }

        return null;
    }
    
    public static void main(String[] args) throws IOException {
        final List<String> data = Files.readAllLines(Path.of("src/main/resources/Day11.txt"));
        new Day11().buildMap(data);
    }
}
