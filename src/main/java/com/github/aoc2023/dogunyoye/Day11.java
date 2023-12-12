package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Day11 {

    private class Position {

        private long i;
        private long j;
        
        Position(long i, long j) {
            this.i = i;
            this.j = j;
        }

        long i() {
            return this.i;
        }

        long j() {
            return this.j;
        }

        private void move(long iMove, long jMove) {
            this.i += iMove;
            this.j += jMove;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            result = prime * result + (int) (i ^ (i >>> 32));
            result = prime * result + (int) (j ^ (j >>> 32));
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Position other = (Position) obj;
            if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
                return false;
            if (i != other.i)
                return false;
            if (j != other.j)
                return false;
            return true;
        }

        private Day11 getEnclosingInstance() {
            return Day11.this;
        }

        @Override
        public String toString() {
            return "Position [i=" + i + ", j=" + j + "]";
        }
    }

    private List<Integer> getRowInsertionIdxs(List<String> data) {
        final List<Integer> rowInsertionIdxs = new ArrayList<>();
        for (int i = data.size() - 1; i >= 0; i--) {
            final String row = data.get(i);
            if (row.chars().allMatch((c) -> c == '.')) {
                rowInsertionIdxs.add(i);
            }
        }

        return rowInsertionIdxs;
    }

    private List<Integer> getColumnInsertionIdxs(List<String> data) {
        final List<Integer> columnInsertionIdxs = new ArrayList<>();
        final int length = data.get(0).length();

        for (int i = length - 1; i >= 0; i--) {
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

        return columnInsertionIdxs;
    }

    private char[][] buildMap(List<String> data) {
        final List<Integer> rowInsertionIdxs = getRowInsertionIdxs(data);
        final List<Integer> columnInsertionIdxs = getColumnInsertionIdxs(data);
        final int oldLength = data.get(0).length();

        for (final int idx : rowInsertionIdxs) {
            data.add(idx, ".".repeat(oldLength));
        }

        for (int i = 0; i < data.size(); i++) {
            final StringBuilder sb = new StringBuilder(data.get(i));
            for (final int idx : columnInsertionIdxs) {
                sb.insert(idx, '.');
            }
            data.set(i, sb.toString());
        }

        final int mapLength = data.get(0).length();
        final int mapDepth = data.size();

        final char[][] map = new char[mapDepth][mapLength];

        for (int i = 0; i < mapDepth; i++) {
            for (int j = 0; j < mapLength; j++) {
                map[i][j] = data.get(i).charAt(j);
            }
        }

        return map;
    }

    private char[][] buildMapNoExpansion(List<String> data) {
        final int mapLength = data.get(0).length();
        final int mapDepth = data.size();

        final char[][] map = new char[mapDepth][mapLength];

        for (int i = 0; i < mapDepth; i++) {
            for (int j = 0; j < mapLength; j++) {
                map[i][j] = data.get(i).charAt(j);
            }
        }

        return map;
    }

    private List<Position> getGalaxyPositions(char[][] map, int mapLength, int mapDepth) {
        final List<Position> galaxyPositions = new ArrayList<>();
        for (int i = 0; i < mapDepth; i++) {
            for (int j = 0; j < mapLength; j++) {
                if (map[i][j] == '#') {
                    galaxyPositions.add(new Position(i, j));
                }
            }
        }

        return galaxyPositions;
    }

    private static long manhattanDistance(Position current, Position end) {
        return Math.abs(current.i() - end.i()) + Math.abs(current.j() - end.j());
    }

    /*
     * Naive solution which expands the map and cross checks every pair
     * This answer could be achieved much faster with `findSumOfShortestLengthsPart2(data, 2)`
     */
    public long findSumOfShortestLengths(List<String> data) {
        final char[][] map = buildMap(data);
        final int mapDepth = data.size();
        final int mapLength = data.get(0).length();
        final List<Position> galaxyPositions = getGalaxyPositions(map, mapLength, mapDepth);

        long sum = 0;
        for (int i = 0; i < galaxyPositions.size(); i++) {
            final Position start = galaxyPositions.get(i);
            for (int j = i + 1; j < galaxyPositions.size(); j++) {
                final Position end = galaxyPositions.get(j);
                sum += manhattanDistance(start, end);
            }
        }

        return sum;
    }

    public long findSumOfShortestLengthsPart2(List<String> data, long offset) {
        final List<Integer> rowInsertionIdxs = getRowInsertionIdxs(data);
        final List<Integer> columnInsertionIdxs = getColumnInsertionIdxs(data);

        final char[][] map = buildMapNoExpansion(data);
        final int mapDepth = data.size();
        final int mapLength = data.get(0).length();
        final List<Position> galaxyPositions = getGalaxyPositions(map, mapLength, mapDepth);

        final Map<Position, Long> depthOffsetMap = new HashMap<>();
        final Map<Position, Long> lengthOffsetMap = new HashMap<>();

        for (Position pos : galaxyPositions) {
            depthOffsetMap.put(pos, 0L);
            lengthOffsetMap.put(pos, 0L);
        }

        for (final int idx : rowInsertionIdxs) {
            for (Position pos : galaxyPositions) {
                if (pos.i() > idx) {
                    depthOffsetMap.put(pos, depthOffsetMap.get(pos) + (offset - 1));
                }
            }
        }

        for (final int idx : columnInsertionIdxs) {
            for (Position pos : galaxyPositions) {
                if (pos.j() > idx) {
                    lengthOffsetMap.put(pos, lengthOffsetMap.get(pos) + (offset - 1));
                }
            }
        }

        for (final Position pos : galaxyPositions) {
            pos.move(depthOffsetMap.get(pos), lengthOffsetMap.get(pos));
        }

        long sum = 0;
        for (int i = 0; i < galaxyPositions.size(); i++) {
            final Position start = galaxyPositions.get(i);
            for (int j = i + 1; j < galaxyPositions.size(); j++) {
                final Position end = galaxyPositions.get(j);
                sum += manhattanDistance(start, end);
            }
        }

        return sum;
    }
    
    public static void main(String[] args) throws IOException {
        final List<String> data = Files.readAllLines(Path.of("src/main/resources/Day11.txt"));
        final List<String> data2 = Files.readAllLines(Path.of("src/main/resources/Day11.txt"));
        System.out.println("Part 1: " + new Day11().findSumOfShortestLengths(data));
        System.out.println("Part 2: " + new Day11().findSumOfShortestLengthsPart2(data2, 1_000_000L));
    }
}
