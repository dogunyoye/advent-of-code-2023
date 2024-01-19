package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

public class Day23 {

    private enum Direction {
        NORTH,
        EAST,
        SOUTH,
        WEST
    }

    private class Position { 
        private final int i;
        private final int j;
        private Direction direction;
        private int steps;
        private boolean isPart2;

        private Position(int i, int j, Direction direction, int steps, boolean isPart2) {
            this.i = i;
            this.j = j;
            this.direction = direction;
            this.steps = steps;
            this.isPart2 = isPart2;
        }

        private Position(int i, int j, Direction direction, int steps) {
            this(i, j, direction, steps, false);
        }

        private void reset() {
            this.direction = null;
            this.steps = 0;
        }

        // Directionality is not needed for part 2
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            result = prime * result + i;
            result = prime * result + j;
            if (!isPart2) {
                result = prime * result + ((direction == null) ? 0 : direction.hashCode());
                result = prime * result + steps;
            }
            return result;
        }

        // Directionality is not needed for part 2
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
            if (!isPart2) {
                if (direction != other.direction)
                    return false;
                if (steps != other.steps)
                    return false;
            }
            return true;
        }

        private Day23 getEnclosingInstance() {
            return Day23.this;
        }

        @Override
        public String toString() {
            return "Position [i=" + i + ", j=" + j + ", steps=" + steps + "]";
        }
    }

    private Direction opposite(Direction dir) {
        switch(dir) {
            case EAST:
                return Direction.WEST;
            case NORTH:
                return Direction.SOUTH;
            case SOUTH:
                return Direction.NORTH;
            case WEST:
                return Direction.EAST;
            default:
                throw new RuntimeException("Invalid direction: " + dir);
        }
    }

    private List<Position> getNeighbours(Position c, char[][] map, boolean isPart2) {
        final List<Position> neighbours = new ArrayList<>();

        switch(map[c.i][c.j]) {
            case '.':
                break;
            case '^':
                neighbours.add(new Position(c.i - 1, c.j, Direction.NORTH, c.steps + 1));
                break;
            case '>':
                neighbours.add(new Position(c.i, c.j + 1, Direction.EAST, c.steps + 1));
                break;
            case 'v':
                neighbours.add(new Position(c.i + 1, c.j, Direction.SOUTH, c.steps + 1));
                break;
            case '<':
                neighbours.add(new Position(c.i, c.j - 1, Direction.WEST, c.steps + 1));
                break;
            default:
                throw new RuntimeException("Unknown character: " + map[c.i][c.j]);
        }

        if (!neighbours.isEmpty()) {
            final Position p = neighbours.get(0);
            if (map[p.i][p.j] == '#' || p.direction == opposite(c.direction)) {
                return Collections.emptyList();
            }

            return neighbours;
        }

        for (final Direction d : Direction.values()) {
            int i;
            int j;

            if (c.direction != null && d == opposite(c.direction)) {
                continue;
            }

            switch(d) {
                case NORTH:
                    if (c.i == 0) {
                        continue;
                    }

                    i = c.i - 1;
                    j = c.j;
                    break;

                case EAST:
                    i = c.i;
                    j = c.j + 1;
                    break;

                case SOUTH:
                    if (c.i == map.length - 1) {
                        continue;
                    }
                    i = c.i + 1;
                    j = c.j;
                    break;

                case WEST:
                    i = c.i;
                    j = c.j - 1;
                    break;
                
                default:
                    throw new RuntimeException("Unknown direction: " + d);
            }

            neighbours.add(new Position(i, j, d, c.steps + 1, isPart2));
        }

        return neighbours.stream().filter((p) -> map[p.i][p.j] != '#').toList();
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

    private void removeSlopes(char[][] map) {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                if (map[i][j] == '^' || map[i][j] == '>' || map[i][j] == 'v' || map[i][j] == '<') {
                    map[i][j] = '.';
                }
            }
        }
    }

    private List<Integer> bfs(Position start, Position end, char[][] map) {
        final List<Integer> distances = new ArrayList<>();
        final Queue<Position> queue = new ArrayDeque<>();
        final Set<Position> visited = new HashSet<>();

        visited.add(start);
        queue.add(start);

        while (!queue.isEmpty()) {
            final Position current = queue.poll();
            if (current.i == end.i && current.j == end.j) {
                distances.add(current.steps);
                continue;
            }

            for (final Position n : getNeighbours(current, map, false)) {
                if (!visited.contains(n)) {
                    visited.add(n);
                    queue.add(n);
                }
            }
        }

        return distances;
    }

    @SuppressWarnings("unused")
    private void printMap(char[][] map) {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                System.out.print(map[i][j]);
            }
            System.out.println();
        }
    }

    /**
     * Recursive DFS
     * 
     * https://en.wikipedia.org/wiki/Depth-first_search#Pseudocode
     * https://thealgorists.com/Algo/AllPathsBetweenTwoNodes
     * 
     * Very simple - this is used to find all paths (from start to end)
     * We add the current node to `visited` before the recursive call (to prevent any cycles)
     * and remove it after the recursive call so other branches can go through that position.
     *
     * The `distances` list is populated when we encounter a path that reaches the end
     * 
     * The maximum in this list will be our answer.
     */
    private void dfs(Position current, Position end,
        Map<Position, List<Position>> distanceMap, Set<Position> visited, List<Integer> distances, int currentSteps) {

        if (current.equals(end)) {
            distances.add(currentSteps);
            return;
        }

        visited.add(current);

        final List<Position> paths = distanceMap.get(current);
        for (Position n : paths) {
            if (!visited.contains(n)) {
                dfs(n, end, distanceMap, visited, distances, currentSteps + n.steps);
            }
        }

        visited.remove(current);
    }

    // https://www.tutorialspoint.com/print-all-paths-from-a-given-source-to-a-destination-using-bfs-in-cplusplus
    // Unused, but may be useful if an iterative solution to collecting all paths (from start to end) is required
    @SuppressWarnings("unused")
    private List<Integer> bfsAllPaths(Position start, Position end, char[][] map) {
        final Queue<List<Position>> queue = new ArrayDeque<>();
        final List<Integer> distances = new ArrayList<>();

        List<Position> path = new ArrayList<>(List.of(start));
        queue.add(path);

        while (!queue.isEmpty()) {
            path = queue.poll();
            final Position lastPos = path.get(path.size() - 1);
            if (lastPos.i == end.i && lastPos.j == end.j) {
                distances.add(path.size() - 1);
            }

            for (Position n : getNeighbours(lastPos, map, true)) {
                if (!path.contains(n)) {
                    final List<Position> newPath = new ArrayList<>(path);
                    newPath.add(n);
                    queue.add(newPath);
                }
            }
        }

        return distances;
    }

    /**
     * Now that we have our points of interest (start, end and all junctions in the map)
     * we can start measuring distances between them.
     *
     * The aim of the game is to keep the search space as minimal as possible. So we must
     * only find the distance between points of interest, which *do not* take a path
     * through another point of interest.
     *
     * The resulting map is effectively a graph of connected nodes, heavily pruned from the
     * original map
     * 
     * To gather the connected nodes between each point of interest, I iterative DFS using
     * a point of interest as the root and any other points of interest the DFS runs into
     * as the destination.
     *
     * https://en.wikipedia.org/wiki/Edge_contraction
     */
    private Map<Position, List<Position>> buildDistanceMap(Set<Position> pointsOfInterest, char[][] map) {
        final Map<Position, List<Position>> distanceMap = new HashMap<>();
        for (final Position poi : pointsOfInterest) {
            final List<Position> reachable = new ArrayList<>();
            final Stack<Position> stack = new Stack<>();
            final Set<Position> visited = new HashSet<>();

            stack.add(poi);
            while (!stack.isEmpty()) {
                final Position current = stack.pop();
                if (pointsOfInterest.contains(current) && !current.equals(poi)) {
                    reachable.add(current);
                    continue;
                }

                if (!visited.contains(current)) {
                    visited.add(current);
                    final List<Position> neighbours = getNeighbours(current, map, true);
                    for (final Position n : neighbours) {
                        stack.add(n);
                    }
                }
            }

            distanceMap.put(poi, reachable);
        }

        return distanceMap;
    }

    /**
     * BFS through the map to find all junctions.
     *
     * These junctions will act as our new nodes in the "reduced" graph
     * This helps us considerably to prune the search space when trying
     * to find the longest path.
     */
    private Set<Position> findJunctions(Position start, char[][] map) {
        final Set<Position> pointsOfInterest = new HashSet<>();
        final Queue<Position> queue = new ArrayDeque<>();
        final Set<Position> visited = new HashSet<>();

        visited.add(start);
        queue.add(start);

        while (!queue.isEmpty()) {
            final Position current = queue.poll();

            final List<Position> neighbours = getNeighbours(current, map, true);
            if (neighbours.size() >= 2) {
                current.reset();
                pointsOfInterest.add(current);
            }

            for (final Position n : neighbours) {
                if (!visited.contains(n)) {
                    visited.add(n);
                    queue.add(n);
                }
            }
        }

        return pointsOfInterest;
    }

    private Position getStart(List<String> data) {
        return new Position(0, data.get(0).indexOf('.'), null, 0);
    }

    private Position getEnd(List<String> data) {
        return new Position(data.size() - 1, data.get(data.size() - 1).indexOf('.'), null, 0);
    }

    public int findMaxStepsToDestination(List<String> data) {
        final char[][] map = buildMap(data);
        final Position start = getStart(data);
        final Position end = getEnd(data);

        return bfs(start, end, map).stream().mapToInt(n -> n).max().getAsInt();
    }

    /**
     * Part 2 effectively turns the exercise into a "longest path problem"
     * 
     * https://en.wikipedia.org/wiki/Longest_path_problem
     * 
     * There is no algorithm to solve this efficiently (in polynomial time)
     * so we need to look for ways to significantly cut down the search space
     *
     * The graph features many "long corridors" and single routes, which fewer
     * "junctions" (decision points).
     * 
     * We will use this to our advantage to collect the straight paths and map
     * them as distances to their nearest junctions.
     * 
     * Inspiration taken from: https://todd.ginsberg.com/post/advent-of-code/2023/day23/ 
     */
    public int findMaxStepsToDestinationWithNoSlopes(List<String> data) {
        final char[][] map = buildMap(data);
        removeSlopes(map);

        final Position start = getStart(data);
        final Position end = getEnd(data);

        start.isPart2 = true;
        end.isPart2 = true;

        final List<Integer> distances = new ArrayList<>();

        final Set<Position> pointsOfInterest = findJunctions(start, map);
        pointsOfInterest.add(start);
        pointsOfInterest.add(end);

        final Map<Position, List<Position>> distanceMap = buildDistanceMap(pointsOfInterest, map);
        dfs(start, end, distanceMap, new HashSet<Position>(), distances, 0);

        return distances.stream().mapToInt(n -> n).max().getAsInt();
    }

    public static void main(String[] args) throws IOException {
        final List<String> data = Files.readAllLines(Path.of("src/main/resources/Day23.txt"));
        System.out.println("Part 1: " + new Day23().findMaxStepsToDestination(data));
        System.out.println("Part 2: " + new Day23().findMaxStepsToDestinationWithNoSlopes(data));
    }
}
