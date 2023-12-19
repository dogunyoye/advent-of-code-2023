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
    }

    private char[][] buildMap(List<String> data) {
        final int mapDepth = data.size();
        final int mapLength = data.get(0).length();
        final char[][] map = new char[mapDepth][mapLength];

        for (int i = 0; i < mapDepth; i++) {
            for (int j = 0; j < mapLength; j++) {
                map[i][j] = data.get(i).charAt(j);
                System.out.print(map[i][j]);
            }
            System.out.println();
        }

        return map;
    }

    public int findNumberOfEnergisedTiles(List<String> data) {
        final char[][] map = buildMap(data);
        final List<Beam> beams = new ArrayList<>();
        beams.add(new Beam(0, 0, Direction.EAST));

        final Set<Position> energised = new LinkedHashSet<>();
        energised.add(new Position(0, 0));

        while (hasBeamsInBounds(beams, map)) {
            for (int i = 0; i < beams.size(); i++) {
                final Beam beam = beams.get(i);
                if (isBeamOutOfBounds(beam, map)) {
                    continue;
                }
                beam.move();

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
                    break;
                case '|':
                    break;
                case '-':
                    break;
                default:
                    throw new RuntimeException("Invalid obstacle: " + value);
                }

            }
        }

        return 0;
    }
    
    public static void main(String[] args) throws IOException {
        final List<String> data = Files.readAllLines(Path.of("src/main/resources/Day16.txt"));
        new Day16().buildMap(data);
    }
}
