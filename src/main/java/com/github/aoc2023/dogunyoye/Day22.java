package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Day22 {

    private class BrickComparator implements Comparator<Brick> {
        @Override
        public int compare(Brick o1, Brick o2) {
            // sort by the lowest point on the brick
            // which will be the first element of it brick positions
            final int o1Level = o1.brickPoints().get(0).z();
            final int o2Level = o2.brickPoints().get(0).z();
            return Integer.compare(o1Level, o2Level);
        }
    }

    private static int id = 0;
    
    private record Position(int x, int y, int z) { }

    private record Brick(int id, Position[] faces, List<Position> brickPoints, boolean isVertical) { 
        private void setBrickPoints(List<Position> newBrickPoints) {
            this.brickPoints.clear();
            this.brickPoints.addAll(new ArrayList<>(newBrickPoints));
        }
    }

    private static Position createPosition(String posLine) {
        final String[] parts = posLine.split(",");
        return new Position(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }

    private static Brick createBrick(String line) {
        final String[] parts = line.split("~");
        final Position[] faces = new Position[]{createPosition(parts[0]), createPosition(parts[1])};
        final List<Position> points = new ArrayList<>();

        for (int i = faces[0].x(); i <= faces[1].x(); i++) {
            for (int j = faces[0].y(); j <= faces[1].y(); j++) {
                for (int k = faces[0].z(); k <= faces[1].z(); k++) {
                    points.add(new Position(i, j, k));
                }
            }
        }

        final boolean isVertical = faces[0].z() != faces[1].z();
        return new Brick(id++, faces, points, isVertical);
    }

    private List<Brick> createBricks(List<String> data) {
        final List<Brick> bricks = new ArrayList<>(data.stream().map(Day22::createBrick).toList());
        // sort the list from lowest bricks (to the ground)
        // to highest. This is so we drop the bricks in the
        // correct order (one by one, tetris style)
        bricks.sort(new BrickComparator());
        return bricks;
    }

    private void printMap(char[][] map) {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                System.out.print(map[i][j]);
            }
            System.out.println();
        }
    }

    private void initialiseMap(char[][] map) {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                map[i][j] = '.';
            }
        }
    }

    private boolean hasRested(List<Position> droppedBrickPoints, Set<Position> allBrickPoints) {
        // hit the ground
        if (droppedBrickPoints.stream().anyMatch((p) -> p.z() == 0)) {
            return true;
        }

        for (final Position p : droppedBrickPoints) {
            // hit another brick
            if (allBrickPoints.contains(p)) {
                return true;
            }
        }

        return false;
    }

    private List<Position> dropBrick(List<Position> brickPoints) {
        final List<Position> dropped = new ArrayList<>();
        for (final Position p : brickPoints) {
            dropped.add(new Position(p.x(), p.y(), p.z() - 1));
        }
        return dropped;
    }

    private void visualiseBricks(List<Brick> bricks) {
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        int maxZ = Integer.MIN_VALUE;

        for (final Brick b : bricks) {
            for (final Position p : b.brickPoints()) {
                maxX = Math.max(maxX, p.x());
                maxY = Math.max(maxY, p.y());
                maxZ = Math.max(maxZ, p.z());
            }
        }

        ++maxX;
        ++maxY;
        ++maxZ;

        final char[][] xView = new char[maxZ][maxX];
        final char[][] yView = new char[maxZ][maxY];

        initialiseMap(xView);
        initialiseMap(yView);

        for (final Brick b : bricks) {
            for (final Position p : b.brickPoints()) {
                int depth = maxZ - p.z() - 1;
                xView[depth][p.x()] = '#';
                yView[depth][p.y()] = '#';
            }
        }

        printMap(xView);
        System.out.println();
        printMap(yView);
    }

    private Map<Brick, Set<Brick>> buildSupportMap(List<Brick> bricks) {
        final Set<Position> allBrickPoints = new HashSet<>();
        final Map<Position, Brick> positionBrickMap = new HashMap<>();

        for (final Brick b : bricks) {
            for (final Position p : b.brickPoints()) {
                allBrickPoints.add(p);
            }
        }

        for (final Brick b : bricks) {
            if (b.faces[0].z() == 1) {
                // ground level bricks can't be dropped further
                continue;
            }

            final List<Position> current = b.brickPoints();
            List<Position> prev = null;

            // vertical bricks would "rest" with
            // themselves if they aren't removed first
            allBrickPoints.removeAll(current);

            List<Position> dropped = dropBrick(current);

            while (!hasRested(dropped, allBrickPoints)) {
                prev = dropped;
                dropped = dropBrick(dropped);
            }

            if (prev != null) {
                allBrickPoints.addAll(new ArrayList<>(prev));
                b.setBrickPoints(prev);
            } else {
                // Brick hasn't moved, just add its positions
                // back to the set of positions
                allBrickPoints.addAll(new ArrayList<>(current)); 
            }
        }

        // associate every brick position to its corresponding brick
        for (final Brick brick : bricks) {
            for (final Position p : brick.brickPoints()) {
                positionBrickMap.put(p, brick);
            }
        }

        //visualiseBricks(bricks);

        final Map<Brick, Set<Brick>> supportedByMap = new HashMap<>();

        for (final Brick brick : bricks) {
            List<Position> pointsToCheck = brick.brickPoints();

            if (brick.isVertical()) {
                // get the highest position in the vertical brick
                final Position highest = brick.brickPoints().get(brick.brickPoints().size() - 1);
                pointsToCheck = new ArrayList<>(List.of(highest));
            }

            for (final Position p : pointsToCheck){
                final Brick above = positionBrickMap.get(new Position(p.x(), p.y(), p.z() + 1));

                // no brick north of/above this position
                // skip and move on to the next point
                if (above == null) {
                    continue;
                }

                final Set<Brick> supportedBy = supportedByMap.get(above);
                if (supportedBy == null) {
                    supportedByMap.put(above, new HashSet<Brick>(List.of(brick)));
                } else {
                    supportedBy.add(brick);
                }
            }
        }

        return supportedByMap;
    }

    /**
     * Every brick which is supported by only one brick
     * cannot be disintegrated.
     */
    private Set<Brick> findNonDisintegratedBricks(Map<Brick, Set<Brick>> supportMap) {
        final Set<Brick> cannotDisintegrate = new HashSet<>();
        supportMap.values().stream().filter((s) -> s.size() == 1).forEach((s) -> cannotDisintegrate.addAll(s));
        return cannotDisintegrate;
    }

    public long findNumberOfBricksToDisintegrate(List<String> data) {
        final List<Brick> bricks = createBricks(data);
        final Map<Brick, Set<Brick>> supportMap = buildSupportMap(bricks);
        final Set<Brick> nonDisintegratedBricks = findNonDisintegratedBricks(supportMap);
        return bricks.size() - nonDisintegratedBricks.size();
    }

    public static void main(String[] args) throws IOException {
        final List<String> data = Files.readAllLines(Path.of("src/main/resources/Day22.txt"));
        System.out.println("Part 1: " + new Day22().findNumberOfBricksToDisintegrate(data));
    }
}
