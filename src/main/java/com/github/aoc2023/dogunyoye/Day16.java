package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Advent of Code 2023, Day 16. */
public class Day16 {

    enum Direction {
        NORTH,
        EAST,
        SOUTH,
        WEST
    }

    private record Position(int i, int j) { }

    private static boolean isBeamOutOfBounds(Beam beam, char[][] map) {
        final int mapDepth = map.length;
        final int mapLength = map[0].length;

        return beam.i < 0 || beam.i >= mapDepth || beam.j < 0 || beam.j >= mapLength;
    }

    private static class Beam {
        private int i;
        private int j;
        private Direction direction;

        private Beam(int i, int j, Direction direction) {
            this.i = i;
            this.j = j;
            this.direction = direction;
        }

        private void move() {
            switch(this.direction) {

            case Direction.NORTH:
                --this.i;
                break;

            case Direction.EAST:
                ++this.j;
                break;

            case Direction.SOUTH:
                ++this.i;
                break;

            case Direction.WEST:
                --this.j;
                break;
            }
        }

        @Override
        public String toString() {
            return "Beam [i=" + i + ", j=" + j + ", direction=" + direction + "]";
        }
    }

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

    private record BeamState(int i, int j, Direction direction) { }

    private static Direction reflectSlash(Direction direction) {
        return switch (direction) {
            case EAST -> Direction.NORTH;
            case NORTH -> Direction.EAST;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.SOUTH;
        };
    }

    private static Direction reflectBackslash(Direction direction) {
        return switch (direction) {
            case EAST -> Direction.SOUTH;
            case NORTH -> Direction.WEST;
            case SOUTH -> Direction.EAST;
            case WEST -> Direction.NORTH;
        };
    }

    private static int traverseContraption(char[][] map, Beam start) {
        final Set<Position> energised = new HashSet<>();
        // A beam's behaviour is fully determined by its position and direction,
        // so a repeated state means a loop - terminate that beam. This guarantees
        // termination and visits every reachable tile exactly once.
        final Set<BeamState> visited = new HashSet<>();
        final Deque<Beam> beams = new ArrayDeque<>();
        beams.push(new Beam(start.i, start.j, start.direction));

        while (!beams.isEmpty()) {
            final Beam beam = beams.pop();

            while (!isBeamOutOfBounds(beam, map)) {
                if (!visited.add(new BeamState(beam.i, beam.j, beam.direction))) {
                    break;
                }
                energised.add(new Position(beam.i, beam.j));

                final char value = map[beam.i][beam.j];
                switch (value) {
                    case '.':
                        break;
                    case '/':
                        beam.direction = reflectSlash(beam.direction);
                        break;
                    case '\\':
                        beam.direction = reflectBackslash(beam.direction);
                        break;
                    case '|':
                        if (beam.direction == Direction.EAST || beam.direction == Direction.WEST) {
                            beams.push(new Beam(beam.i - 1, beam.j, Direction.NORTH));
                            beam.direction = Direction.SOUTH;
                        }
                        break;
                    case '-':
                        if (beam.direction == Direction.NORTH || beam.direction == Direction.SOUTH) {
                            beams.push(new Beam(beam.i, beam.j - 1, Direction.WEST));
                            beam.direction = Direction.EAST;
                        }
                        break;
                    default:
                        throw new RuntimeException("Invalid obstacle: " + value);
                }

                beam.move();
            }
        }

        return energised.size();
    }

    public int findNumberOfEnergisedTiles(List<String> data) {
        return traverseContraption(buildMap(data), new Beam(0, 0, Direction.EAST));
    }

    public int findMaxNumberOfEnergisedTiles(List<String> data) {
        final char[][] map = buildMap(data);
        final List<Beam> beams = new ArrayList<>();

        // top row
        for (int i = 0; i < map[0].length; i++) {
            beams.add(new Beam(0, i, Direction.SOUTH));
        }

        // right most column
        for (int i = 0; i < map.length; i++) {
            beams.add(new Beam(i, map[0].length - 1, Direction.WEST));
        }

        // bottom row
        for (int i = 0; i < map[0].length; i++) {
            beams.add(new Beam(map.length - 1, i, Direction.NORTH));
        }

        // left most column
        for (int i = 0; i < map.length; i++) {
            beams.add(new Beam(i, 0, Direction.EAST));
        }

        return beams
                .stream()
                .parallel()
                .map((b) -> Day16.traverseContraption(map, b))
                .max(Integer::compareTo)
                .get();
    }

    public static void main(String[] args) throws IOException {
        final List<String> data = Files.readAllLines(Path.of("src/main/resources/Day16.txt"));
        System.out.println("Part 1: " + new Day16().findNumberOfEnergisedTiles(data));
        System.out.println("Part 2: " + new Day16().findMaxNumberOfEnergisedTiles(data));
    }
}
