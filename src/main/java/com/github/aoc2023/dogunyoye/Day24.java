package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Day24 {

    private record Position(long x, long y, long z) { }

    private record Velocity(long xVel, long yVel, long zVel) { }

    private record TestArea(long min, long max) { }

    private record Hail(Position position, Velocity velocity) { }

    private static Hail createHail(String line) {
        final String[] parts = line.split(" @ ");
        final String[] pos = parts[0].split(",");
        final String[] vel = parts[1].split(",");

        final Position p = new Position(Long.parseLong(pos[0].trim()), Long.parseLong(pos[1].trim()), Long.parseLong(pos[2].trim()));
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

        return new double[]{x, y};
    }

    // https://en.wikipedia.org/wiki/Intersection_(geometry)#Two_lines
    private long[] intersectsPart2(Hail first, Hail second) {
        final BigInteger a0 = BigInteger.valueOf(first.velocity().yVel());
        final BigInteger b0 = BigInteger.valueOf(-first.velocity().xVel());
        final BigInteger c0 = BigInteger.valueOf(first.velocity().yVel() * first.position().x() - first.velocity().xVel() * first.position().y());

        final BigInteger a1 = BigInteger.valueOf(second.velocity().yVel());
        final BigInteger b1 = BigInteger.valueOf(-second.velocity().xVel());
        final BigInteger c1 = BigInteger.valueOf(second.velocity().yVel() * second.position().x() - second.velocity().xVel() * second.position().y());

        final BigInteger d = b1.multiply(a0).subtract(b0.multiply(a1));

        if (d.longValue() == 0) {
            return null;
        }

        final BigInteger x = (b1.multiply(c0).subtract(b0.multiply(c1))).divide(d);
        final BigInteger y = (c1.multiply(a0).subtract(c0.multiply(a1))).divide(d);
    
        return new long[]{x.longValue(), y.longValue()};
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

    private Set<Long> intersectsWithNewVelocityXY(List<Hail> hailstones, int x, int y) {
        final Set<Long> xy = new HashSet<>();
        final int toCheck = 0;

        for (int i = 0; i <= toCheck; i++) {
            final Hail h = hailstones.get(i);
            for (int j = i + 1; j < 5; j++) {
                final Velocity newV = new Velocity(h.velocity().xVel - x, h.velocity().yVel() - y, 0);

                final Hail nextH = hailstones.get(j);
                final Velocity nextHNewV = new Velocity(nextH.velocity().xVel - x, nextH.velocity().yVel() - y, 0);

                final Hail h0 = new Hail(h.position(), newV);
                final Hail h1 = new Hail(nextH.position(), nextHNewV);

                final long[] intersection = intersectsPart2(h0, h1);
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

        return xy.size() == 2 ? xy : null;
    }

    private Set<Long> intersectsWithNewVelocityXZ(List<Hail> hailstones, int x, int z) {
        final Set<Long> xz = new HashSet<>();
        final int toCheck = 0;

        for (int i = 0; i <= toCheck; i++) {
            final Hail h = hailstones.get(i);
            for (int j = i + 1; j < 5; j++) {
                final Velocity newV = new Velocity(h.velocity().xVel - x, h.velocity().zVel() - z, 0);

                final Hail nextH = hailstones.get(j);
                final Velocity nextHNewV = new Velocity(nextH.velocity().xVel - x, nextH.velocity().zVel() - z, 0);

                final Hail h0 = new Hail(new Position(h.position.x(), h.position.z(), 0), newV);
                final Hail h1 = new Hail(new Position(nextH.position.x(), nextH.position().z(), 0), nextHNewV);

                final long[] intersection = intersectsPart2(h0, h1);
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
        final List<Hail> hailstones = createHail(data);
        final Set<Long> answer = new HashSet<>();

        for (int x = -500; x <= 500; x++) {
            for (int y = -500; y <= 500; y++) {
                final Set<Long> xyCandidate = intersectsWithNewVelocityXY(hailstones, x, y);
                if (xyCandidate == null) {
                    continue;
                }
                answer.addAll(xyCandidate);
                for (int z = -500; z <= 500; z++) {
                    final Set<Long> xzCandidate = intersectsWithNewVelocityXZ(hailstones, x, z);
                    if (xzCandidate != null) {
                        answer.addAll(xzCandidate);
                        return answer.stream().mapToLong(n -> n).sum();
                    }
                }
            }
        }

        throw new RuntimeException("No solution found!");
    }

    public static void main(String[] args) throws IOException {
        final List<String> data = Files.readAllLines(Path.of("src/main/resources/Day24.txt"));
        System.out.println("Part 1: " + new Day24().findNumberOfIntersectionsInTheTestArea(data));
        System.out.println("Part 2: " + new Day24().sumOfCoordinatesThatHitAllHailstones(data));
    }
}
