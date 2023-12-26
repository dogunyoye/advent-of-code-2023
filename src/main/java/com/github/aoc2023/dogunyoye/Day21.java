package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class Day21 {

    private record Position(int i, int j) { }

    private record State(Position pos, int steps, List<Position> neighbours) { 
        private State(Position pos, int steps) {
            this(pos, steps, new ArrayList<>(
                List.of(
                    new Position(pos.i - 1, pos.j),
                    new Position(pos.i, pos.j + 1),
                    new Position(pos.i + 1, pos.j),
                    new Position(pos.i, pos.j - 1))
            ));
        }
    }

    // https://stackoverflow.com/a/2172061/2981152
    private int mod(int x, int y) {
        int result = x % y;
        if (result < 0) {
            result += y;
        }

        return result;
    }

    private boolean isNeighbour(Position p, char[][] map, boolean isPart2) {
        if (!isPart2) {
            final boolean inBounds = p.i() >= 0 && p.i() < map.length && p.j() >= 0 && p.j() < map[0].length;
            return inBounds && map[p.i()][p.j()] != '#';
        }

        return map[mod(p.i(), map.length)][mod(p.j(), map[0].length)] != '#';
    }

    private char[][] buildMap(List<String> data) {
        final int mapLength = data.get(0).length();
        final int mapDepth = data.size();

        final char[][] map = new char[mapDepth][mapLength];

        for (int i = 0; i < mapDepth; i++) {
            final String line = data.get(i);
            for (int j = 0; j < mapLength; j++) {
                map[i][j] = line.charAt(j);
            }
        }

        return map;
    }

    private Position findStart(char[][] map) {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                if (map[i][j] == 'S') {
                    return new Position(i, j);
                }
            }
        }

        throw new RuntimeException("Could not find start!");
    }

    // Slow recursive solution, solves part 1
    // memo-ising helps, but for larger max steps this method will OOM
    @SuppressWarnings({"unused"})
    private Set<Position> traverseMap(char[][] map, Map<State, Set<Position>> memo,
        State statePos, int steps, int maxSteps, boolean isPart2) {

        if (steps == maxSteps - 1) {
            return new HashSet<>(
                statePos.neighbours()
                .stream()
                .filter((p) -> isNeighbour(p, map, isPart2))
                .toList()
            );
        }

        if (memo.containsKey(statePos)) {
            return memo.get(statePos);
        }

        final Set<Position> result = new HashSet<>();
        for (final Position n : statePos.neighbours()) {
            if (!isNeighbour(n, map, isPart2)) {
                continue;
            }
            result.addAll(traverseMap(map, memo, new State(n, statePos.steps - 1), steps + 1, maxSteps, isPart2));
        }

        memo.put(statePos, result);
        return result;
    }

    private long traverseMapOptimised(char[][] map, Position start, int maxSteps, boolean isPart2) {
        final Set<Position> visited = new HashSet<>();
        final Queue<State> queue = new ArrayDeque<>();

        queue.add(new State(start, maxSteps));

        long total = 0;

        while (!queue.isEmpty()) {
            final State pos = queue.poll();
            final int s = pos.steps();

            if (s % 2 == 0) {
                ++total;
            }

            if (s == 0) {
                continue;
            }

            for (final Position n : pos.neighbours()) {
                if (!isNeighbour(n, map, isPart2) || visited.contains(n)) {
                    continue;
                }
                visited.add(n);
                queue.add(new State(n, s - 1));
            }
        }

        return total - 1;
    }

    private long findPlots(char[][] map, int maxSteps, boolean isPart2) {
        final Map<State, Set<Position>> memo = new HashMap<>();
        return traverseMapOptimised(map, findStart(map), maxSteps, isPart2);
        //return traverseMap(map, memo, new State(findStart(map), maxSteps), 0, maxSteps, isPart2).size();
    }

    public long findPossibleGardenPlotsAfter64Steps(List<String> data) {
        final char[][] map = buildMap(data);
        return findPlots(map, 64, false);
    }

    public long findPossibleGardenPlotsAfter26501365Steps(List<String> data) {
        final char[][] map = buildMap(data);
        final long[] plots = new long[3];
        final int y0 = 65;
        final int y1 = 65 + 131;
        final int y2 = 65 + (2 * 131);
        final int[] values = new int[]{y0, y1, y2};

        int i = 0;
        for (int v : values) {
            plots[i] = findPlots(map, v, true);
            i++;
        }

        System.out.println(Arrays.toString(plots));

        final long c = plots[0];
        final long a = (plots[2] - (2 * plots[1]) + c)/2;
        final long b = plots[1] - c - a;

        // final long a = (plots[0]/2) - plots[1] + (plots[2]/2);
        // final long b = -3 * (plots[0]/2) + (2 * plots[1]) - (plots[2]/2);

        final long n = ((26501365 - 65) / 131);

        return (a * (long)Math.pow(n, 2)) + (b * n) + c;
    }
    
    public static void main(String[] args) throws IOException {
        final List<String> data = Files.readAllLines(Path.of("src/main/resources/Day21.txt"));
        System.out.println("Part 1: " + new Day21().findPossibleGardenPlotsAfter64Steps(data));
        System.out.println("Part 2: " + new Day21().findPossibleGardenPlotsAfter26501365Steps(data));
    }
}
