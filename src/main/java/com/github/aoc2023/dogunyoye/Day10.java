package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Day10 {

    // | - L J 7 F

    private static Map<Character, List<Character>> validNorthAdjacentNodes;
    private static Map<Character, List<Character>> validEastAdjacentNodes;
    private static Map<Character, List<Character>> validSouthAdjacentNodes;
    private static Map<Character, List<Character>> validWestAdjacentNodes;

    static {
        validNorthAdjacentNodes = new HashMap<>();
        validEastAdjacentNodes = new HashMap<>();
        validSouthAdjacentNodes = new HashMap<>();
        validWestAdjacentNodes = new HashMap<>();

        validNorthAdjacentNodes.put('|', List.of('|', '7', 'F'));
        validNorthAdjacentNodes.put('L', List.of('|', '7', 'F'));
        validNorthAdjacentNodes.put('J', List.of('|', '7', 'F'));

        validEastAdjacentNodes.put('-', List.of('-', 'J', '7'));
        validEastAdjacentNodes.put('F', List.of('-', 'J', '7'));
        validEastAdjacentNodes.put('L', List.of('-', 'J', '7'));

        validSouthAdjacentNodes.put('|', List.of('|', 'J', 'L'));
        validSouthAdjacentNodes.put('7', List.of('|', 'J', 'L'));
        validSouthAdjacentNodes.put('F', List.of('|', 'J', 'L'));

        validWestAdjacentNodes.put('-', List.of('-', 'L', 'F'));
        validWestAdjacentNodes.put('J', List.of('-', 'L', 'F'));
        validWestAdjacentNodes.put('7', List.of('-', 'L', 'F'));
    }

    private record Position(int i, int j) { }

    private List<Position> getConnectingNodes(char[][] map, int length, int depth, int i, int j) {
        final char pipe = map[i][j];
        final List<Character> validNorth = validNorthAdjacentNodes.get(pipe);
        final List<Character> validEast = validEastAdjacentNodes.get(pipe);
        final List<Character> validSouth = validSouthAdjacentNodes.get(pipe);
        final List<Character> validWest = validWestAdjacentNodes.get(pipe);

        final List<Position> connecting = new ArrayList<>();

        // NORTH
        if (i - 1 >= 0 && validNorth != null) {
            if (validNorth.contains(map[i-1][j])) {
                connecting.add(new Position(i-1, j));
            }
        }

        // EAST
        if (j + 1 < length && validEast != null) {
            if (validEast.contains(map[i][j+1])) {
                connecting.add(new Position(i, j+1));
            }
        }

        // SOUTH
        if (i + 1 < depth && validSouth != null) {
            if (validSouth.contains(map[i+1][j])) {
                connecting.add(new Position(i+1, j));
            }
        }

        // WEST
        if (j - 1 >= 0 && validWest != null) {
            if (validWest.contains(map[i][j-1])) {
                connecting.add(new Position(i, j-1));
            }
        }

        if (connecting.size() == 0 || connecting.size() > 2) {
            throw new RuntimeException("invalid connection");
        }

        return connecting;
    }

    private List<Position> connectingNodesFromStart(Position start, char[][] map, int length, int depth) {
        final List<Position> connecting = new ArrayList<>();

        // NORTH
        if ((start.i() - 1 >= 0) && "|7F".indexOf(Character.toString(map[start.i() - 1][start.j()])) != -1) {
            connecting.add(new Position(start.i() - 1, start.j()));
        }

        // EAST
        if ((start.j() + 1 < length) && "-7J".indexOf(Character.toString(map[start.i()][start.j() + 1])) != -1) {
            connecting.add(new Position(start.i(), start.j() + 1));
        }

        // SOUTH
        if ((start.i() + 1 < depth) &&"|JL".indexOf(Character.toString(map[start.i() + 1][start.j()])) != -1) {
            connecting.add(new Position(start.i() + 1, start.j()));
        }

        // WEST
        if ((start.j() - 1 >= 0) && "-FL".indexOf(Character.toString(map[start.i()][start.j() - 1])) != -1) {
            connecting.add(new Position(start.i(), start.j() - 1));
        }

        if (connecting.size() != 2) {
            throw new RuntimeException("Incorrect connections from start");
        }

        return connecting;
    }

    private Position findStartPosition(char[][] map, int length, int depth) {
        for (int i = 0; i < depth; i++) {
            for (int j = 0; j < length; j++) {
                if (map[i][j] == 'S') {
                    return new Position(i, j);
                }
            }
        }

        throw new RuntimeException("Unable to find start position");
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

    private Set<Position> traversePipes(List<String> data) {
        final char[][] pipesMap = buildMap(data);
        final int mapLength = data.get(0).length();
        final int mapDepth = data.size();

        final Position start = findStartPosition(pipesMap, mapLength, mapDepth);
        List<Position> connecting = connectingNodesFromStart(start, pipesMap, mapLength, mapDepth);

        // LinkedHashSet used here to maintain insertion order
        // Makes it easier for us to later build a clockwise ordering of the boundary points
        final Set<Position> visited = new LinkedHashSet<>();
        visited.add(start);

        while(!connecting.isEmpty()) {
            final List<Position> next = new ArrayList<>();
            for (final Position p : connecting) {
                final List<Position> adjacent =
                    getConnectingNodes(pipesMap, mapLength, mapDepth, p.i(), p.j());
                next.addAll(adjacent.stream().filter((pos) -> !visited.contains(pos)).toList());
                visited.add(p);
            }

            connecting = next;
        }

        return visited;
    }

    public int findStepsToTheFurthestPosition(List<String> data) {
        return traversePipes(data).size() / 2;
    }

    // https://www.geeksforgeeks.org/area-of-a-polygon-with-given-n-ordered-vertices/
    // https://www.101computing.net/the-shoelace-algorithm/
    private int shoeLaceFormula(List<Position> corners) {
        final List<Integer> xs = new ArrayList<>();
        final List<Integer> ys = new ArrayList<>();

        final int length = corners.size();

        for (int i = 0; i < corners.size(); i++) {
            final Position pos = corners.get(i);
            xs.add(pos.j());
            ys.add(pos.i());
        }

        // Initialize area
        int area = 0;

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

    public int findNumberOfTilesEnclosedByLoop(List<String> data) {
        final Set<Position> boundary = traversePipes(data);
        final char[][] pipesMap = buildMap(data);

        // `path` is a clockwise ordering of the loop boundary points
        final List<Position> path = new ArrayList<>();
        final List<Position> secondPath = new ArrayList<>();

        final List<Position> boundaryList = boundary.stream().toList();

        path.add(boundaryList.get(0));

        for (int i = 1; i < boundary.size(); i+=2) {
            path.add(boundaryList.get(i));
        }

        for (int i = 2; i < boundary.size(); i+=2) {
            secondPath.add(boundaryList.get(i));
        }

        path.addAll(secondPath.reversed());

        final List<Position> corners =
            path.stream()
                .filter((pos) -> {
                    final char v = pipesMap[pos.i()][pos.j()];
                    return Character.toString(v).matches("[SFL7J]");
                })
                .toList();

        // Pick's Theorem - https://en.wikipedia.org/wiki/Pick%27s_theorem
        return (shoeLaceFormula(corners) + 1) - (boundary.size() / 2);
    }
    
    public static void main(String[] args) throws IOException {
        final List<String> data = Files.readAllLines(Path.of("src/main/resources/Day10.txt"));
        System.out.println("Part 1: " + new Day10().findStepsToTheFurthestPosition(data));
        System.out.println("Part 2: " + new Day10().findNumberOfTilesEnclosedByLoop(data));
    }
}
