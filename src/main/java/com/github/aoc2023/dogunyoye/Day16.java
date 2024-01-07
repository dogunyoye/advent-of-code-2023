package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Day16 {

    enum Direction {
        NORTH,
        EAST,
        SOUTH,
        WEST
    }

    private record Position(int i, int j) { }

    private boolean isBeamOutOfBounds(Beam beam, char[][] map) {
        final int mapDepth = map.length;
        final int mapLength = map[0].length;

        return beam.i < 0 || beam.i >= mapDepth || beam.j < 0 || beam.j >= mapLength;
    }

    private boolean hasBeamsInBounds(List<Beam> beams, char[][] map) {
        for (final Beam beam : beams) {
            if (!isBeamOutOfBounds(beam, map)) {
                return true;
            }
        }

        return false;
    }

    private class Beam {
        private int i;
        private int j;
        private Direction direction;
        private boolean outOfBounds;
        
        private Beam(int i, int j, Direction direction) {
            this.i = i;
            this.j = j;
            this.direction = direction;
            outOfBounds = false;
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
            return "Beam [i=" + i + ", j=" + j + ", direction=" + direction + ", outOfBounds=" + outOfBounds + "]";
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

    private int traverseContraption(char[][] map, Beam start) {
        List<Beam> beams = new ArrayList<>();
        beams.add(start);

        final Set<Position> energised = new LinkedHashSet<>();
        final Set<Integer> energisedWatch = new LinkedHashSet<>();
        int counter = 0;

        while (hasBeamsInBounds(beams, map)) {
            final List<Beam> newBeams = new ArrayList<>();

            for (int i = 0; i < beams.size(); i++) {
                final Beam beam = beams.get(i);
                if (isBeamOutOfBounds(beam, map)) {
                    beam.outOfBounds = true;
                    continue;
                }

                final int currentI = beam.i;
                final int currentJ = beam.j;

                final Position currentPosition = new Position(currentI, currentJ);
                energised.add(currentPosition);

                final char value = map[currentI][currentJ];
                switch(value) {
                case '.':
                    break;
                case '/':
                    switch(beam.direction) {
                        case EAST:
                            beam.direction = Direction.NORTH;
                            break;
                        case NORTH:
                            beam.direction = Direction.EAST;
                            break;
                        case SOUTH:
                            beam.direction = Direction.WEST;
                            break;
                        case WEST:
                            beam.direction = Direction.SOUTH;
                            break;
                        default:
                            throw new RuntimeException("Invalid direction");
                    }
                    break;
                case '\\':
                    switch(beam.direction) {
                        case EAST:
                            beam.direction = Direction.SOUTH;
                            break;
                        case NORTH:
                            beam.direction = Direction.WEST;
                            break;
                        case SOUTH:
                            beam.direction = Direction.EAST;
                            break;
                        case WEST:
                            beam.direction = Direction.NORTH;
                            break;
                        default:
                            throw new RuntimeException("Invalid direction");
                    }
                    break;
                case '|':
                    switch(beam.direction) {
                        case EAST:
                        case WEST:
                            newBeams.add(new Beam(currentI-1, currentJ, Direction.NORTH));
                            beam.direction = Direction.SOUTH;
                            break;
                        case SOUTH:
                        case NORTH:
                            break;
                        default:
                            throw new RuntimeException("Invalid direction");
                    }
                    break;
                case '-':
                    switch(beam.direction) {
                        case EAST:
                        case WEST:
                            break;
                        case SOUTH:
                        case NORTH:
                            newBeams.add(new Beam(currentI, currentJ-1, Direction.WEST));
                            beam.direction = Direction.EAST;
                            break;
                        default:
                            throw new RuntimeException("Invalid direction");
                    }
                    break;
                default:
                    throw new RuntimeException("Invalid obstacle: " + value);
                }

                beam.move();
            }

            beams.addAll(newBeams);
            beams = new ArrayList<Beam>(beams.stream().filter((b) -> !b.outOfBounds).toList());

            // very hacky loop/cycle termination
            // if I see the same value 5 times in a row, this is our energised count
            boolean notExists = energisedWatch.add(energised.size());
            if (!notExists) {
                ++counter;
                if (counter == 5) {
                    return energised.size();
                }
            } else {
                counter = 0;
            }
        }

        return energised.size();
    }

    public int findNumberOfEnergisedTiles(List<String> data) {
        return traverseContraption(buildMap(data), new Beam(0, 0, Direction.EAST));
    }

    // takes ages
    // TODO: Optimise heavily
    public int findMaxNumberOfEnergisedTiles(List<String> data) {
        final List<Integer> energised = new ArrayList<>();
        final char[][] map = buildMap(data);

        // top row
        for (int i = 0; i < map[0].length; i++) {
            energised.add(traverseContraption(map, new Beam(0, i, Direction.SOUTH)));
        }

        // right most column
        for (int i = 0; i < map.length; i++) {
            energised.add(traverseContraption(map, new Beam(i, map[0].length - 1, Direction.WEST)));
        }

        // bottom row
        for (int i = 0; i < map[0].length; i++) {
            energised.add(traverseContraption(map, new Beam(map.length - 1, i, Direction.NORTH)));
        }

        // left most column
        for (int i = 0; i < map.length; i++) {
            energised.add(traverseContraption(map, new Beam(i, 0, Direction.EAST)));
        }

        return energised.stream().max(Integer::compareTo).get();
    }
    
    public static void main(String[] args) throws IOException {
        final List<String> data = Files.readAllLines(Path.of("src/main/resources/Day16.txt"));
        System.out.println("Part 1: " + new Day16().findNumberOfEnergisedTiles(data));
        System.out.println("Part 2: " + new Day16().findMaxNumberOfEnergisedTiles(data));
    }
}
