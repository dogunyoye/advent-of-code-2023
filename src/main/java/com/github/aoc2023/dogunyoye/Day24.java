package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Day24 {

    private record Position2D(long x, long y) { }

    private record Position3D(long x, long y, long z) { }

    private record Velocity(long xVel, long yVel, long zVel) { }

    private record TestArea(long min, long max) { }

    private record Hail(Position3D position, Velocity velocity) { }

    private static Hail createHail(String line) {
        final String[] parts = line.split(" @ ");
        final String[] pos = parts[0].split(",");
        final String[] vel = parts[1].split(",");

        final Position3D p = new Position3D(Long.parseLong(pos[0].trim()), Long.parseLong(pos[1].trim()), Long.parseLong(pos[2].trim()));
        final Velocity v = new Velocity(Long.parseLong(vel[0].trim()), Long.parseLong(vel[1].trim()), Long.parseLong(vel[2].trim()));

        return new Hail(p, v);
    }

    private List<Hail> createHail(List<String> data) {
        return data.stream().map(Day24::createHail).toList();
    }

    private double[] gradientAndYIntercept(long x, long y, long xVel, long yVel) {
        final double m = (double) yVel / xVel;
        final double c = (y - (m * x));
        return new double[]{m, c};
    }

    // https://en.wikipedia.org/wiki/Line%E2%80%93line_intersection#Given_two_line_equations
    // https://www.ncl.ac.uk/webtemplate/ask-assets/external/maths-resources/core-mathematics/geometry/equation-of-a-straight-line.html
    // x = (d -c)/(a - b)
    // y = (a * (d - c)/(a - b)) + c
    private double[] intersects(Hail h, Hail next, TestArea area) {

        final double[] ac =
            gradientAndYIntercept(h.position().x(), h.position().y(), h.velocity().xVel(), h.velocity().yVel());
        final double a = ac[0];
        final double c = ac[1];

        final double[] bd =
            gradientAndYIntercept(next.position().x(), next.position().y(), next.velocity().xVel(), next.velocity().yVel());
        final double b = bd[0];
        final double d = bd[1];

        // intersection coordinates
        final double x = ((d - c)/(a - b));
        final double y = ((a * (d - c)/(a - b)) + c);

        // outside the test area (part 1)
        if (area != null && ( x < area.min() || x > area.max() || y < area.min() || y > area.max())) {
            return null;
        }

        // check x and y, if either are in hailstone A or B's past
        if (x < h.position().x() && h.velocity().xVel() > 0 ||
            x > h.position().x() && h.velocity().xVel() < 0 ||
            x < next.position().x() && next.velocity().xVel() > 0 ||
            x > next.position().x() && next.velocity().xVel() < 0) {
            return null;
        }

        // System.out.println(Arrays.toString(ac));
        // System.out.println(Arrays.toString(bd));
        // System.out.println("x: " + x + " y: " + y);
        // System.out.println("++++++++++++++");

        return new double[]{x, y};
    }

    long findNumberOfIntersectionsInTheTestArea(List<String> data, long min, long max) {
        final List<Hail> hailstones = createHail(data);
        final TestArea area = new TestArea(min, max);
        int intersectCount = 0;

        for (int i = 0; i < hailstones.size() - 1; i++) {
            for (int j = i + 1; j < hailstones.size(); j++) {
                if (intersects(hailstones.get(i), hailstones.get(j), area) != null) {
                    ++intersectCount;
                }
            }
        }

        return intersectCount;
    }

    public long findNumberOfIntersectionsInTheTestArea(List<String> data) {
        return findNumberOfIntersectionsInTheTestArea(data, 200000000000000L, 400000000000000L);
    }

    // private Position2D findIntersectWithNewVelocity2D(List<Hail> hailstones, int toCheck, int x, int y, int z) {
    //     final Map<Position2D, Integer> positionCount = new HashMap<>();
    //     for (int i = 0; i < toCheck; i++) {
    //         final Hail h = hailstones.get(i);
    //         final Velocity v =
    //             new Velocity(h.velocity().xVel - x, h.velocity().yVel() - y, h.velocity().zVel() - z);

    //         Position2D p = new Position2D(h.position().x(), h.position().y());
    //         for (int count = 0; count < 250; count++) {
    //             // apply velocity
    //             p = new Position2D(p.x() + v.xVel(), p.y() + v.yVel());
    //             if (positionCount.containsKey(p)) {
    //                 positionCount.put(p, positionCount.get(p) + 1);
    //             } else {
    //                 positionCount.put(p, 1);
    //             }
    //         }
    //     }

    //     if (!positionCount.values().stream().anyMatch((c) -> c == toCheck)) {
    //         return null;
    //     }

    //     return positionCount.entrySet().stream().filter((e) -> e.getValue() == toCheck).map((e) -> e.getKey()).findFirst().get();
    // }

    // private Position3D findIntersectWithNewVelocity3D(List<Hail> hailstones, int toCheck, int x, int y, int z) {
    //     final Map<Position3D, Integer> positionCount = new HashMap<>();
    //     for (int i = 0; i < toCheck; i++) {
    //         final Hail h = hailstones.get(i);
    //         final Velocity v =
    //             new Velocity(h.velocity().xVel - x, h.velocity().yVel() - y, h.velocity().zVel() - z);

    //         Position3D p = new Position3D(h.position().x(), h.position().y(), h.position().z());
    //         for (int count = 0; count < 250; count++) {
    //             // apply velocity
    //             p = new Position3D(p.x() + v.xVel(), p.y() + v.yVel(), p.z() + v.zVel());
    //             if (positionCount.containsKey(p)) {
    //                 positionCount.put(p, positionCount.get(p) + 1);
    //             } else {
    //                 positionCount.put(p, 1);
    //             }
    //         }
    //     }

    //     if (!positionCount.values().stream().anyMatch((c) -> c == toCheck)) {
    //         return null;
    //     }

    //     return positionCount.entrySet().stream().filter((e) -> e.getValue() == toCheck).map((e) -> e.getKey()).findFirst().get();
    // }

    private Set<Double> intersectsWithNewVelocityXY(List<Hail> hailstones, int x, int y) {
        final Set<Double> xy = new HashSet<>();
        final int toCheck =hailstones.size();

        for (int i = 0; i < toCheck; i++) {
            final Hail h = hailstones.get(i);
            for (int j = i + 1; j < hailstones.size(); j++) {
                final Velocity newV = new Velocity(h.velocity().xVel - x, h.velocity().yVel() - y, 0);

                final Hail nextH = hailstones.get(j);
                final Velocity nextHNewV = new Velocity(nextH.velocity().xVel - x, nextH.velocity().yVel() - y, 0);

                final Hail h0 = new Hail(h.position(), newV);
                final Hail h1 = new Hail(nextH.position(), nextHNewV);

                final double[] intersection = intersects(h0, h1, null);
                if (intersection == null) {
                    return null;
                }

                if (Double.isNaN(intersection[0]) || Double.isNaN(intersection[1])) {
                    continue;
                }

                xy.add(intersection[0]);
                xy.add(intersection[1]);
            } 
        }

        //System.out.println(xy);
        return xy.size() == 2 ? xy : null;
    }

    private Set<Double> intersectsWithNewVelocityXZ(List<Hail> hailstones, int x, int z) {
        final Set<Double> xz = new HashSet<>();
        final int toCheck = hailstones.size();

        for (int i = 0; i < toCheck; i++) {
            final Hail h = hailstones.get(i);
            for (int j = i + 1; j < hailstones.size(); j++) {
                final Velocity newV = new Velocity(h.velocity().xVel - x, h.velocity().zVel() - z, 0);

                final Hail nextH = hailstones.get(j);
                final Velocity nextHNewV = new Velocity(nextH.velocity().xVel - x, nextH.velocity().zVel() - z, 0);

                final Hail h0 = new Hail(new Position3D(h.position.x(), h.position.z(), 0), newV);
                final Hail h1 = new Hail(new Position3D(nextH.position.x(), nextH.position().z(), 0), nextHNewV);

                final double[] intersection = intersects(h0, h1, null);
                if (intersection == null) {
                    return null;
                }

                if (Double.isNaN(intersection[0]) || Double.isNaN(intersection[1])) {
                    continue;
                }

                xz.add(intersection[0]);
                xz.add(intersection[1]);
            } 
        }

        return xz.size() == 2 ? xz : null;
    }

    public long sumOfCoordinatesThatHitAllHailstones(List<String> data) {
        final List<Hail> hailstones =
            createHail(data).stream()
            //.filter((h) -> Math.sqrt(Math.pow(h.velocity().xVel(), 2) + Math.pow(h.velocity().yVel(), 2) + Math.pow(h.velocity().zVel, 2)) < 500)
            .toList();
        final Set<Double> answer = new HashSet<>();

        for (int x = -1000; x <= 1000; x++) {
            for (int y = -1000; y <= 1000; y++) {
                final Set<Double> xyCandidate = intersectsWithNewVelocityXY(hailstones, x, y);
                if (xyCandidate == null) {
                    continue;
                }
                answer.addAll(xyCandidate);

                // final Position2D pxy = findIntersectWithNewVelocity2D(hailstones, hailstones.size(), x, y, 0);
                // if (pxy == null) {
                //     continue;
                // }

                for (int z = -1000; z <= 1000; z++) {
                    System.out.println("in here for, x: " + x + " y: " + y );
                    //System.out.println("found position: " + pxy);

                    final Set<Double> xzCandidate = intersectsWithNewVelocityXZ(hailstones, x, z);
                    if (xzCandidate != null) {
                        answer.addAll(xzCandidate);
                        System.out.println(answer);
                        return Double.valueOf(answer.stream().mapToDouble(n -> n).sum()).longValue();
                    }

                    // final Position3D pxyz = findIntersectWithNewVelocity3D(hailstones, hailstones.size(), x, y, z);
                    // if (pxyz != null) {
                    //     System.out.println("found answer: " + pxyz);
                    //     return pxyz.x() + pxyz.y() + pxyz.z();
                    // }
                }
            }
        }

        return -1;
    }

    public static void main(String[] args) throws IOException {
        final List<String> data = Files.readAllLines(Path.of("src/main/resources/Day24.txt"));
        //System.out.println("Part 1: " + new Day24().findNumberOfIntersectionsInTheTestArea(data));
        System.out.println("Part 2: " + new Day24().sumOfCoordinatesThatHitAllHailstones(data));
    }
}
