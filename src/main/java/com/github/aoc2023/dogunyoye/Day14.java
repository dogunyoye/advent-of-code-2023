package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class Day14 {

    private static final Consumer<char[][]> MOVE_NORTH =
        (map) -> {
            for (int i = 0; i < map.length; i++) {
                for (int j = 0; j < map[0].length; j++) {
                    if (map[i][j] == 'O') {
                        int ii = i;
                        while(canMoveNorth(ii, j, map)) {
                            map[ii][j] = '.';
                            map[--ii][j] = 'O';
                        } 
                    }
                }
            }
        };
    
    private static final Consumer<char[][]> MOVE_WEST =
        (map) -> {
            for (int i = 0; i < map.length; i++) {
                for (int j = 0; j < map[0].length; j++) {
                    if (map[i][j] == 'O') {
                        int jj = j;
                        while(canMoveWest(i, jj, map)) {
                            map[i][jj] = '.';
                            map[i][--jj] = 'O';
                        } 
                    }
                }
            }
        };

    private static final Consumer<char[][]> MOVE_SOUTH =
        (map) -> {
            for (int i = map.length - 1; i >= 0; i--) {
                for (int j = 0; j < map[0].length; j++) {
                    if (map[i][j] == 'O') {
                        int ii = i;
                        while(canMoveSouth(ii, j, map)) {
                            map[ii][j] = '.';
                            map[++ii][j] = 'O';
                        } 
                    }
                }
            }
        };

    private static final Consumer<char[][]> MOVE_EAST =
        (map) -> {
            for (int i = 0; i < map.length; i++) {
                for (int j = map[0].length - 1; j >= 0; j--) {
                    if (map[i][j] == 'O') {
                        int jj = j;
                        while(canMoveEast(i, jj, map)) {
                            map[i][jj] = '.';
                            map[i][++jj] = 'O';
                        } 
                    }
                }
            }
        };

    private char[][] buildMap(List<String> data) {
        final int mapDepth = data.size();
        final int mapLength = data.get(0).length();
        final char[][] map = new char[mapDepth][mapLength];

        for (int i = 0; i < mapDepth; i++) {
            for (int j = 0; j < mapLength; j++) {
                map[i][j] = data.get(i).charAt(j);
            }
        }
        return map;
    }

    private static boolean canMoveNorth(int i, int j, char[][] map) {
        if (i == 0) {
            return false;
        }
        return map[i-1][j] == '.';
    }

    private static boolean canMoveWest(int i, int j, char[][] map) {
        if (j == 0) {
            return false;
        }
        return map[i][j - 1] == '.';
    }

    private static boolean canMoveSouth(int i, int j, char[][] map) {
        if (i == map.length - 1) {
            return false;
        }
        return map[i + 1][j] == '.';
    }

    private static boolean canMoveEast(int i, int j, char[][] map) {
        if (j == map[0].length - 1) {
            return false;
        }
        return map[i][j + 1] == '.';
    }

    private void tiltRocks(char[][] map, Consumer<char[][]> move) {
        move.accept(map);
    }

    private String mapToString(char[][] map) {
        String result = "";
        for (int i = 0; i < map.length; i++) {
            result += Arrays.toString(map[i]);
        }
        return result;
    }

    private int calculateSumOfRocks(char[][] map) {
        int sum = 0;
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                if (map[i][j] == 'O') {
                    sum += (map.length - i);
                }
            }
        }

        return sum;
    }

    public int calculateTotalLoad(List<String> data) {
        final char[][] map = buildMap(data);
        tiltRocks(map, MOVE_NORTH);
 
        return calculateSumOfRocks(map);
    }

    public int calculateTotalLoadAfter1000000000Cycles(List<String> data) {
        final char[][] map = buildMap(data);
        final Set<String> stateSet = new HashSet<>();
        final Set<Integer> sumsSet = new LinkedHashSet<>();

        int cycles = 0;
        int interval = 0;
        boolean found = false;

        while(cycles != 1000000000) {
            ++cycles;

            tiltRocks(map, MOVE_NORTH);
            tiltRocks(map, MOVE_WEST);
            tiltRocks(map, MOVE_SOUTH);
            tiltRocks(map, MOVE_EAST);

            if (!stateSet.add(mapToString(map)) && !found) {
                interval = cycles;
                found = true;
            }

            if (found && (cycles % interval == 0)) {
                if (!sumsSet.add(calculateSumOfRocks(map))) {
                    break;
                }
            }
        }

        return sumsSet.stream().mapToInt((n) -> n).toArray()[1];
    } 
    
    public static void main(String[] args) throws IOException {
        final List<String> data = Files.readAllLines(Path.of("src/main/resources/Day14.txt"));
        System.out.println("Part 1: " + new Day14().calculateTotalLoad(data));
        System.out.println("Part 2: " + new Day14().calculateTotalLoadAfter1000000000Cycles(data));
    }
}
