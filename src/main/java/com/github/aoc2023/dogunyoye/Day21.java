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

    private record State(Position pos, int steps) { }

    // https://stackoverflow.com/a/2172061/2981152
    private int mod(int x, int y) {
        int result = x % y;
        if (result < 0) {
            result += y;
        }

        return result;
    }

    private List<Position> getNeighbours(Position pos, char[][] map, boolean isPart2) {
        final int mapDepth = map.length;
        final int mapLength = map[0].length;

        final int i = pos.i();
        final int j = pos.j();

        final List<Position> neighbours = new ArrayList<>();

        if (!isPart2) {
            // North
            if (i - 1 >= 0 && map[i - 1][j] != '#') {
                neighbours.add(new Position(i - 1, j));
            }

            // East
            if (j + 1 < mapLength && map[i][j + 1] != '#') {
                neighbours.add(new Position(i, j + 1));
            }

            // South
            if (i + 1 < mapDepth && map[i + 1][j] != '#') {
                neighbours.add(new Position(i + 1, j));
            }

            // West
            if (j - 1 >= 0 && map[i][j - 1] != '#') {
                neighbours.add(new Position(i, j - 1));
            }

            return neighbours;
        }

        // We're in part 2
        // we can go out of bounds and wrap around
    
        // North
        if (map[mod(i - 1, mapDepth)][mod(j, mapLength)] != '#') {
            neighbours.add(new Position(i - 1, j));
        }

        // East
        if (map[mod(i, mapDepth)][mod(j + 1, mapLength)] != '#') {
            neighbours.add(new Position(i, j + 1));
        }

        // South
        if (map[mod(i + 1, mapDepth)][mod(j, mapLength)] != '#') {
            neighbours.add(new Position(i + 1, j));
        }

        // West
        if (map[mod(i, mapDepth)][mod(j - 1, mapLength)] != '#') {
            neighbours.add(new Position(i, j - 1));
        }

        return neighbours;
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
    private Set<Position> traverseMap(char[][] map, Map<State, Set<Position>> memo, Position pos, int steps, int maxSteps) {
        if (steps == maxSteps - 1) {
            return new HashSet<>(getNeighbours(pos, map, false));
        }

        final State state = new State(pos, steps);
        if (memo.containsKey(state)) {
            return memo.get(state);
        }

        final Set<Position> result = new HashSet<>();
        final List<Position> neighbours = getNeighbours(pos, map, false);
        for (final Position n : neighbours) {
            result.addAll(traverseMap(map, memo, n, steps + 1, maxSteps));
        }

        memo.put(new State(pos, steps), result);
        return result;
    }

    private long traverseMapOptimised(char[][] map, Position start, int maxSteps, boolean isPart2) {
        final Set<Position> visited = new HashSet<>();
        final Queue<Position> queue = new ArrayDeque<>();
        final Map<Position, Integer> stepsMap = new HashMap<>();

        queue.add(start);
        stepsMap.put(start, maxSteps);

        long total = 0;

        while (!queue.isEmpty()) {
            final Position pos = queue.poll();
            int s = stepsMap.get(pos);

            if (s % 2 == 0) {
                ++total;
            }

            if (s == 0) {
                continue;
            }

            for (final Position n : getNeighbours(pos, map, isPart2)) {
                if (visited.contains(n)) {
                    continue;
                }
                visited.add(n);
                queue.add(n);
                stepsMap.put(n, s - 1);
            }
        }

        return total - 1;
    }

    private long findPlots(char[][] map, int maxSteps, boolean isPart2) {
        return traverseMapOptimised(map, findStart(map), maxSteps, isPart2);
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
