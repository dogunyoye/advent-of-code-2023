package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Day19 {

    private record PartRating(int x, int m, int a, int s) { }

    private record Workflow(String name, Map<Character, Function<Integer, Boolean>> processes) { }

    private List<Workflow> buildWorkflows(List<String> data, int idx) {
        final List<Workflow> workflows = new ArrayList<>();
        return workflows;
    }

    private List<PartRating> buildPartRatings(int idx, List<String> data) {
        final List<PartRating> partRatings = new ArrayList<>();
        for (int i = idx; i < data.size(); i++) {
            final String line = data.get(i).substring(1, data.get(i).length() - 1);
            final String[] parts = line.split(",");
            final List<Integer> nums = new ArrayList<>();
            for (String n : parts) {
                nums.add(Integer.parseInt(n.split("=")[1]));
            }
            partRatings.add(new PartRating(nums.get(0), nums.get(1), nums.get(2), nums.get(3)));
        }

        return partRatings;
    }

    private static int findEmptyLine(List<String> data) {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).isEmpty()) {
                return i;
            }
        }

        throw new RuntimeException("No blank line found!");
    }

    private boolean testPartRating(PartRating pr) {
        return false;
    }

    public int findSumOfAcceptedParts(List<String> data) {
        return 0;
    }
    
    public static void main(String[] args) throws IOException {
        final List<String> data = Files.readAllLines(Path.of("src/main/resources/Day19.txt"));
        System.out.println(new Day19().buildPartRatings(findEmptyLine(data) + 1, data));
    }
}
