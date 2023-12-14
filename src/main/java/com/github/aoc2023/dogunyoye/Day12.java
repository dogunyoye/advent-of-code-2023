package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Day12 {

    private record ConditionRecord(String record, int[] sequence, String regex) { }

    private List<ConditionRecord> createConditionRecords(List<String> data, int repeat) {
        final List<ConditionRecord> conditionRecords = new ArrayList<>();
        for (final String line : data) {
            final String[] parts = line.split(" ");
            String record = parts[0];
            if (repeat > 0) {
                record += "?";
                record = record.repeat(repeat);
                // chop off trailing '?'
                record = record.substring(0, record.length() - 1);
            }

            String damaged = parts[1];
            if (repeat > 0) {
                damaged += ",";
                damaged = damaged.repeat(repeat);
                // chop off trailing ','
                damaged = damaged.substring(0, damaged.length() - 1);
            }

            final int[] sequence =
                Arrays.stream(damaged.split(",")).mapToInt((s) -> Integer.parseInt(s)).toArray();

            String regex = "\\.*";

            for (int i = 0; i < sequence.length; i++) {
                if (i == sequence.length - 1) {
                    regex += String.format("#{%d}\\.*", sequence[i]);
                } else {
                    regex += String.format("#{%d}\\.+", sequence[i]);
                }
            }

            conditionRecords.add(new ConditionRecord(record, sequence, regex));
        }

        return conditionRecords;
    }

    private static int recurseCombination(ConditionRecord cr, List<Integer> temp, int[] data, int start, int end, int index, int n) {
        if (index == n) {
            final StringBuilder sb = new StringBuilder(new String(cr.record));
            for (final int idx : temp.reversed()) {
                sb.setCharAt(idx, '#');
            }

            final String completed = sb.toString().replaceAll("\\?", ".");
            return completed.matches(cr.regex()) ? 1 : 0;
        }

        int matches = 0;
        for (int i = start; i <= end && end - i + 1 >= n - index; i++) {
            temp.set(index, data[i]);
            matches += recurseCombination(cr, temp, data, i + 1, end, index + 1, n);
        }

        return matches;
    }

    private static int findValidCombinations(ConditionRecord cr) {
        final long workingCount = cr.record().chars().filter((c) -> c == '#').count();
        final long total = Arrays.stream(cr.sequence()).sum();
        final int remaining = ((int)(total - workingCount));

        // indices for all the '?' in the condition record
        final List<Integer> placeholderIdxs = new ArrayList<>();
        for (int i = 0; i < cr.record().length(); i++) {
            if (cr.record().charAt(i) == '?') {
                placeholderIdxs.add(i);
            }
        }

        final List<Integer> temp = new ArrayList<>();
        for (int i = 0; i < remaining; i++) {
            temp.add(0);
        }

        final int[] data = placeholderIdxs.stream().mapToInt((x) -> x).toArray();
        return recurseCombination(cr, temp, data, 0, data.length - 1, 0, remaining);
    }

    public int sumAllValidArrangements(List<String> data) {
        final List<ConditionRecord> conditionRecords = createConditionRecords(data, 0);
        return conditionRecords.stream().map(Day12::findValidCombinations).mapToInt((m) -> m).sum();
    }
    
    public static void main(String[] args) throws IOException {
        final List<String> data = Files.readAllLines(Path.of("src/main/resources/Day12.txt"));
        //System.out.println("Part 1: " + new Day12().sumAllValidArrangements(data));
        new Day12().createConditionRecords(data, 5);
    }
}
