package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Day12 {

    private record ConditionRecord(String record, int[] sequence, String regex) { }

    private record State(int idx, int num) { }

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

    /*
     * Slow solution which generates all possible combinations and tests them
     * against a regular expression
     */
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

    private static boolean canFill(String record, int start, int end) {
        if (end > record.length()) {
            return false;
        }

        for (int i = start; i < end; i++) {
            if (record.charAt(i) == '.') {
                return false;
            }
        }

        if (end < record.length() && record.charAt(end) == '#') {
            return false;
        }

        return true;
    }

    /**
     * Optimised solution which recursively builds combinations, bailing early if the combination
     * breaks the ordering constraint.
     * 
     * `memo` is used to reduce the amount of duplicate work whilst walking the recursion tree
     * 
     * Solution inspired from: https://www.reddit.com/r/adventofcode/comments/18hg99r/2023_day_12_simple_tutorial_with_memoization/
     */
    private static long findValidCombinationsOptimised(String record, int[] damaged, Map<State, Long> memo, int i) {
        if (damaged.length == 0) {
            if (i < record.length() && record.indexOf("#", i) > -1) {
                return 0;
            }

            return 1; 
        }

        for (; i < record.length(); i++) {
            if (record.charAt(i) == '#' || record.charAt(i) == '?') {
                break;
            }
        }

        if (i >= record.length()) {
            return 0;
        }

        final State key = new State(i, damaged.length);
        if (memo.containsKey(key)) {
            return memo.get(key);
        }

        long result = 0;
        if (canFill(record, i, i + damaged[0])) {
            result += findValidCombinationsOptimised(record, Arrays.copyOfRange(damaged, 1, damaged.length), memo, (i + damaged[0] + 1));
        }

        if (record.charAt(i) == '?') {
            result += findValidCombinationsOptimised(record, damaged, memo, i+1);
        }

        memo.put(key, result);
        return result;
    }

    public int sumAllValidArrangements(List<String> data) {
        final List<ConditionRecord> conditionRecords = createConditionRecords(data, 0);
        return conditionRecords.stream().map(Day12::findValidCombinations).mapToInt((m) -> m).sum();
    }

    public long sumAllValidArrangementsUnfolded(List<String> data) {
        long sum = 0;
        final List<ConditionRecord> conditionRecords = createConditionRecords(data, 5);
        final Map<State, Long> memo = new HashMap<>();

        for (final ConditionRecord cr : conditionRecords) {
            sum += findValidCombinationsOptimised(cr.record(), cr.sequence(), memo, 0);
            memo.clear();
        }

        return sum;
    }
    
    public static void main(String[] args) throws IOException {
        final List<String> data = Files.readAllLines(Path.of("src/main/resources/Day12.txt"));
        System.out.println("Part 1: " + new Day12().sumAllValidArrangements(data));
        System.out.println("Part 2: " + new Day12().sumAllValidArrangementsUnfolded(data));
    }
}
