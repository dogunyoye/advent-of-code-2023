package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class Day17 {

    private enum Direction {
        NORTH,
        EAST,
        SOUTH,
        WEST
    }

    private class NodeComparator implements Comparator<Node> {
        @Override
        public int compare(Node o1, Node o2) {
            return Integer.compare(o1.cost, o2.cost);
        }   
    }

    private class Node {
        private int x;
        private int y;
        private Direction direction;
        private int cost;
        private int steps;

        private Node(int x, int y, Direction direction, int steps) {
            this.x = x;
            this.y = y;
            this.direction = direction;
            this.cost = 0;
            this.steps = steps;
        }

        public void setCost(int cost) {
            this.cost = cost;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            result = prime * result + x;
            result = prime * result + y;
            result = prime * result + ((direction == null) ? 0 : direction.hashCode());
            result = prime * result + steps;
            return result;
        }

        private Day17 getEnclosingInstance() {
            return Day17.this;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;

            Node other = (Node) obj;
            if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
                return false;
            if (x != other.x)
                return false;
            if (y != other.y)
                return false;
            if (direction != other.direction)
                return false;
            if (steps != other.steps)
                return false;
            return true;
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

    private List<Node> getNeighbours(Node c, int[][] map, boolean isPart2) {
        final List<Node> neighbours = new ArrayList<>();
        final int length = map[0].length;
        final int depth = map.length;
        Direction excluded = null;
        boolean canTurn = true;

        if ((!isPart2 && c.steps == 3) || (isPart2 && c.steps == 10)) {
            // exclude the "forward" direction as a neighbour
            // the forward direction is the one with the same value as the node
            excluded = c.direction;
        }

        // for part 2, can't turn if you haven't moved 4 steps in the same direction
        if (isPart2 && c.steps < 4) {
            canTurn = false;
        }

        for (final Direction d : Direction.values()) {
            int x = 0;
            int y = 0;
            int steps = 1;

            if (c.direction != null && d == opposite(c.direction)) {
                continue;
            }

            // if you can't turn (`canTurn` is false),
            // then you have to go in a straight (`d == c.direction`)
            // If you're the starting node (`c.direction` is `null`),
            // then we also permit turning from stationary
            if (canTurn || d == c.direction || c.direction == null) {
                switch(d) {
                    case NORTH:
                        x = c.x - 1;
                        y = c.y;

                        if (x < 0 || excluded == d) {
                            continue;
                        }
                        break;

                    case EAST:
                        x = c.x;
                        y = c.y + 1;

                        if (y >= length || excluded == d) {
                            continue;
                        }
                        break;

                    case SOUTH:
                        x = c.x + 1;
                        y = c.y;

                        if (x >= depth || excluded == d) {
                            continue;
                        }
                        break;

                    case WEST:
                        x = c.x;
                        y = c.y - 1;

                        if (y < 0 || excluded == d) {
                            continue;
                        }
                        break;
                }

                if (c.direction == d) {
                    steps += c.steps;
                }

                final Node neighbour = new Node(x, y, d, steps);
                neighbours.add(neighbour); 
            }
        }

        return neighbours;
    }

    private int[][] buildMap(List<String> data) {
        final int mapDepth = data.size();
        final int mapLength = data.get(0).length();
        final int[][] map = new int[mapDepth][mapLength];

        for (int i = 0; i < mapDepth; i++) {
            for (int j = 0; j < mapLength; j++) {
                map[i][j] = Integer.parseInt(Character.toString(data.get(i).charAt(j)));
            }
        }

        return map;
    }

    private long djikstra(int[][] map, Node start, Node end, boolean isPart2) {
        final PriorityQueue<Node> frontier = new PriorityQueue<>(map[0].length * map.length, new NodeComparator());
        start.setCost(0);
        frontier.add(start);

        final Map<Node, Integer> costSoFar = new HashMap<>();
        costSoFar.put(start, 0);

        while (!frontier.isEmpty()) {
            final Node current = frontier.remove();

            if (current.x == end.x && current.y == end.y) {
                return current.cost;
            }

            final int currentCost = current.cost;

            if (currentCost <= costSoFar.get(current)) {
                for (final Node n : getNeighbours(current, map, isPart2)) {
    
                    final int w = map[n.x][n.y];
                    final int newCost = currentCost + w;

                    if (!costSoFar.containsKey(n) || newCost < costSoFar.get(n)) {
                        n.setCost(newCost);
                        costSoFar.put(n, newCost);
    
                        frontier.add(n);
                    }
                }
            }
        }

        throw new RuntimeException("no solution!");
    }

    private long findMinimalHeatLoss(List<String> data, boolean isPart2) {
        final int[][] map = buildMap(data);
        final Node start = new Node(0, 0, null, 1);
        final Node end = new Node(map.length - 1, map[0].length - 1, null, 1);

        return djikstra(map, start, end, isPart2); 
    }

    public long findMinimalHeatLossOfCrucible(List<String> data) {
        return findMinimalHeatLoss(data, false);
    }

    public long findMinimalHeatLossOfUltraCrucible(List<String> data) {
        return findMinimalHeatLoss(data, true);
    }
    
    public static void main(String[] args) throws IOException {
        final List<String> data = Files.readAllLines(Path.of("src/main/resources/Day17.txt"));
        System.out.println("Part 1: " + new Day17().findMinimalHeatLossOfCrucible(data));
        System.out.println("Part 2: " + new Day17().findMinimalHeatLossOfUltraCrucible(data));
    }
}
