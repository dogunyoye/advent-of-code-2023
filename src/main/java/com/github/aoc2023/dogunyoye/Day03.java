package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Day03 {

    private record Position (int i, int j) {
        private List<Position> getNeighbours(int length, int depth) {
            final List<Position> neighbours = new ArrayList<>();
            final int i = this.i;
            final int j = this.j;

            neighbours.add(new Position(i-1, j));   // north
            neighbours.add(new Position(i-1, j+1)); // north-east
            neighbours.add(new Position(i, j+1));   // east
            neighbours.add(new Position(i+1, j+1)); // south-east
            neighbours.add(new Position(i+1, j));   // south
            neighbours.add(new Position(i+1, j-1)); // south-west
            neighbours.add(new Position(i, j-1));   // west
            neighbours.add(new Position(i-1, j-1)); // north-west

            return
                neighbours.stream()
                    .filter((pos) -> {
                        // filter out any out of bounds positions
                        return pos.i >= 0 && pos.i < depth && pos.j >= 0 && pos.j < length;  
                    }).toList();
        }
    }

    private record EngineSchematic (int length, int depth, char[][] schematic, List<Position> symbolPositions) { }

    private EngineSchematic buildEngineSchematic(List<String> schematic) {
        final int mapLength = schematic.get(0).length();
        final int mapDepth = schematic.size();
        final char[][] engineSchematic = new char[mapDepth][mapLength];
        final List<Position> symbolPositions = new ArrayList<>();
        
        for (int i = 0; i < mapDepth; i++) {
            for (int j = 0; j < mapLength; j++) {
                final char c = schematic.get(i).charAt(j);
                engineSchematic[i][j] = c;

                if ('.' != c && !Character.isDigit(c)) {
                    symbolPositions.add(new Position(i, j));
                }
            }
        }

        return new EngineSchematic(mapLength, mapDepth, engineSchematic, symbolPositions);
    }

    private static int completeNumber(EngineSchematic engineSchematic, Position pos, Set<Position> mapped) {

        if(mapped.contains(pos)) {
            return 0;
        }

        final char[][] schematic = engineSchematic.schematic();
        final int mapLength = engineSchematic.length();
        String numString = Character.toString(schematic[pos.i()][pos.j()]);

        final int x = pos.i();
        int leftY = pos.j()-1;
        int rightY = pos.j()+1;

        while(leftY >= 0 && Character.isDigit(schematic[x][leftY])) {
            numString = schematic[x][leftY] + numString;
            mapped.add(new Position(x, leftY));
            leftY--;
        }

        while(rightY < mapLength && Character.isDigit(schematic[x][rightY])) {
            numString = numString + schematic[x][rightY];
            mapped.add(new Position(x, rightY));
            rightY++;
        }

        return Integer.parseInt(numString);
    }

    public int sumOfAllPartNumbers(List<String> schematic) {
        final EngineSchematic engineSchematic = buildEngineSchematic(schematic);
        final int mapDepth = engineSchematic.depth();
        final int mapLength = engineSchematic.length();

        int sum = 0;
        for (final Position symPos : engineSchematic.symbolPositions()) {
            final List<Position> neighbours = symPos.getNeighbours(mapLength, mapDepth);
            final Set<Position> mapped = new HashSet<>();
            sum += neighbours.stream()
                    .filter(nPos -> Character.isDigit(engineSchematic.schematic()[nPos.i()][nPos.j()]))
                    .map(pos -> Day03.completeNumber(engineSchematic, pos, mapped))
                    .mapToInt(number -> number)
                    .sum();
        }
        
        return sum;
    }

    public int sumOfAllGearRatios(List<String> schematic) {
        final EngineSchematic engineSchematic = buildEngineSchematic(schematic);
        final int mapDepth = engineSchematic.depth();
        final int mapLength = engineSchematic.length();

        int sum = 0;
        for (final Position symPos : engineSchematic.symbolPositions()) {
            if (engineSchematic.schematic()[symPos.i()][symPos.j()] == '*') {
                final List<Position> neighbours = symPos.getNeighbours(mapLength, mapDepth);
                final Set<Position> mapped = new HashSet<>();
                final List<Integer> numbers = neighbours.stream()
                        .filter(nPos -> Character.isDigit(engineSchematic.schematic()[nPos.i()][nPos.j()]))
                        .map(pos -> Day03.completeNumber(engineSchematic, pos, mapped))
                        .filter(number -> number != 0)
                        .toList();
                
                if (numbers.size() == 2) {
                    sum += numbers.get(0) * numbers.get(1);
                }
            }
        }

        return sum;
    }
    
    public static void main(String[] args) throws IOException {
        final List<String> schematic = Files.readAllLines(Path.of("src/main/resources/Day03.txt"));
        System.out.println("Part 1: " + new Day03().sumOfAllPartNumbers(schematic));
        System.out.println("Part 2: " + new Day03().sumOfAllGearRatios(schematic));
    }
}
