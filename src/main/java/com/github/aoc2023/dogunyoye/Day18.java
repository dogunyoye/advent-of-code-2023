package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Day18 {

    private record Position(long i, long j) { }

    private record Instruction(char direction, long metres, String hexColour) { }

    private List<Instruction> createInstructionsList(List<String> data) {
        final List<Instruction> instructions = new ArrayList<>();
        for (final String line : data) {
            final String[] parts = line.split(" ");
            final char direction = parts[0].charAt(0);
            final int metres = Integer.parseInt(parts[1]);
            final String hexColour = parts[2].replaceAll("[()]", "");
            instructions.add(new Instruction(direction, metres, hexColour));
        }

        return instructions;
    }

    private List<Instruction> createInstructionsListPart2(List<String> data) {
        final List<Instruction> instructions = new ArrayList<>();
        for (final String line : data) {
            final String[] parts = line.split(" ");

            char direction;
            final String hexColour = parts[2].replaceAll("[()]", "");
            final char directionNumber = hexColour.charAt(hexColour.length()-1);
            switch(directionNumber) {
                case '0':
                    direction = 'R';
                    break;
                case '1':
                    direction = 'D';
                    break;
                case '2':
                    direction = 'L';
                    break;
                case '3':
                    direction = 'U';
                    break;
                default:
                    throw new RuntimeException("invalid number: " + directionNumber);
            }

            final int metres = Integer.parseInt(hexColour.substring(1, hexColour.length()-1), 16);
            instructions.add(new Instruction(direction, metres, hexColour));
        }

        return instructions; 
    }

    private long manhattanDistance(Position first, Position second) {
        return Math.abs(first.i() - second.i()) + Math.abs(first.j() - second.j());
    }

    private List<Position> findCorners(List<Instruction> instructions) {
        int i = 0;
        int j = 0;

        List<Position> corners = new ArrayList<>();
        corners.add(new Position(0, 0));

        for (final Instruction ins : instructions) {
            switch(ins.direction()) {
                case 'U':
                    i -= ins.metres();
                    break;
                case 'D':
                    i += ins.metres();
                    break;
                case 'L':
                    j -= ins.metres();
                    break;
                case 'R':
                    j += ins.metres();
                    break;
                default:
                    throw new RuntimeException("Invalid direction: " + ins.direction());
            }
            corners.add(new Position(i, j));
        }

        return corners;
    }

    // https://www.geeksforgeeks.org/area-of-a-polygon-with-given-n-ordered-vertices/
    // https://www.101computing.net/the-shoelace-algorithm/
    private long shoeLaceFormula(List<Position> corners) {
        final List<Long> xs = new ArrayList<>();
        final List<Long> ys = new ArrayList<>();

        final int length = corners.size();

        for (int i = 0; i < corners.size(); i++) {
            final Position pos = corners.get(i);
            xs.add(pos.j());
            ys.add(pos.i());
        }

        // Initialize area
        long area = 0;

        // Calculate value of shoelace formula
        int j = length - 1;
        for (int i = 0; i < length; i++) {
            area += (xs.get(j) + xs.get(i)) * (ys.get(j) - ys.get(i));
                
            // j is previous vertex to i
            j = i; 
        }
        
        // Return absolute value
        return Math.abs(area) / 2;
    }

    private long findArea(List<Instruction> instructions) {
        int perimeter = 0;
        final List<Position> corners = findCorners(instructions);
        for (int i = 0; i < corners.size() - 1; i++) {
            final Position first = corners.get(i);
            final Position second = corners.get(i+1);
            perimeter += manhattanDistance(first, second);
        }

        return shoeLaceFormula(corners) + (perimeter/2) + 1;
    }

    public long findDigPlanArea(List<String> data) {
        final List<Instruction> instructions = createInstructionsList(data);
        return findArea(instructions);
    }

    public long findDigPlanAreaPart2(List<String> data) {
        final List<Instruction> instructions = createInstructionsListPart2(data);
        return findArea(instructions);
    }
    
    public static void main(String[] args) throws IOException {
        final List<String> data = Files.readAllLines(Path.of("src/main/resources/Day18.txt"));
        System.out.println("Part 1: " + new Day18().findDigPlanArea(data));
        System.out.println("Part 2: " + new Day18().findDigPlanAreaPart2(data));
    }
}
