package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class Day08 {

    private record Pair<K, V>(K left, V right) { }

    private record Network(char[] sequence, Map<String, Pair<String, String>> directions) { }

    private Network createNetwork(List<String> data) {
        final char[] sequence = data.get(0).toCharArray();
        final Map<String, Pair<String, String>> directions = new HashMap<>();

        for (int i = 2; i < data.size(); i++) {
            final String[] parts = data.get(i).split(" = ");
            final String key = parts[0];

            final String[] parts2 = parts[1].replaceAll("[(|)]", "").split(", ");
            final String left = parts2[0];
            final String right = parts2[1];

            directions.put(key, new Pair<String,String>(left, right));
        }

        return new Network(sequence, directions);
    }

    private long gcd(long x, long y) {
        if (y == 0) {
            return Math.abs(x);
        }

        return gcd(y, x % y);
    }

    private long lcm(List<Long> numbers) {
        long lcm = 1;
        for (final long n : numbers) {
            lcm = (lcm * n)/gcd(lcm, n);
        }

        return lcm;
    }

    public int findNumberOfStepsToEnd(List<String> data) {
        final Network network = createNetwork(data);
        String current = "AAA";
        final int length = network.sequence().length;
        int steps = 0;

        while (!"ZZZ".equals(current)) {
            final Pair<String, String> directions = network.directions().get(current);
            final char direction = network.sequence()[steps % length];
            current = 'L' == direction ? directions.left() : directions.right();
            steps++;
        }

        return steps;
    }

    public long findNumberOfStepsToEndStartingFromANodes(List<String> data) {
        final Network network = createNetwork(data);
        final long length = network.sequence().length;
        long steps = 0;

        final List<String> nodes =
            network.directions().entrySet()
                .stream()
                .filter((entry) -> entry.getKey().endsWith("A"))
                .map((entry) -> entry.getKey())
                .toList();
        
        final Map<String, String> locationMap = new HashMap<>();
        final List<Long> lcms = new ArrayList<>();

        for (final String s : nodes) {
            locationMap.put(s, new String(s));
        }

        while(!locationMap.isEmpty()) {
            final char direction = network.sequence()[(int)(steps % length)];
            final Set<Entry<String, String>> entrySet = locationMap.entrySet();

            final List<String> toRemove = new ArrayList<>();

            for (final Entry<String, String> e : entrySet) {
                final Pair<String, String> directions = network.directions().get(e.getValue());
                final String nextStep = 'L' == direction ? directions.left() : directions.right();
                if (nextStep.endsWith("Z")) {
                    lcms.add((steps + 1));
                    toRemove.add(e.getKey());
                } else {
                    locationMap.put(e.getKey(), nextStep);
                }
            }

            for (final String r : toRemove) {
                locationMap.remove(r);
            }

            steps++;
        }

        return lcm(lcms);
    }
    
    public static void main(String[] args) throws IOException {
        final List<String> data = Files.readAllLines(Path.of("src/main/resources/Day08.txt"));
        System.out.println("Part 1: " + new Day08().findNumberOfStepsToEnd(data));
        System.out.println("Part 2: " + new Day08().findNumberOfStepsToEndStartingFromANodes(data));
    }
}
