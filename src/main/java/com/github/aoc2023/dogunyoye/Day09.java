package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Day09 {

    private static int processSequence(List<Integer> sequence, boolean forwards) {
        if (sequence.stream().allMatch(n -> n == 0)) {
            return 0;
        }

        final List<Integer> differences = new ArrayList<>();

        for (int i = 0; i < sequence.size() - 1; i++) {
            differences.add(sequence.get(i+1) - sequence.get(i));
        }

        if (forwards) {
            return sequence.get(sequence.size() - 1) + processSequence(differences, forwards);
        }

        return sequence.get(0) - processSequence(differences, forwards);
    }

    private List<List<Integer>> getSequences(List<String> data) {
        return data.stream()
            .map((line) -> {
                final String[] parts = line.split(" ");
                return Arrays.stream(parts).mapToInt(n -> Integer.parseInt(n)).boxed().toList();
            })
            .toList();
    }

    public int calculateSumOfExrapolatedValues(List<String> data, boolean forwards) {
        final List<List<Integer>> sequences = getSequences(data);
        return sequences.stream().map((seq) -> Day09.processSequence(seq, forwards)).mapToInt(n -> n).sum();
    }
    
    public static void main(String[] args) throws IOException {
        final List<String> data = Files.readAllLines(Path.of("src/main/resources/Day09.txt"));
        System.out.println("Part 1: " + new Day09().calculateSumOfExrapolatedValues(data, true));
        System.out.println("Part 2: " + new Day09().calculateSumOfExrapolatedValues(data, false));
    }
}
