package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Day24 {

    private record Position(long x, long y, long z) { }

    private record Velocity(long xVel, long yVel, long zVel) { }

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
    long findNumberOfIntersectionsInTheTestArea(List<String> data, long min, long max) {
        final List<Hail> hailstones = createHail(data);
        int interceptCount = 0;

        for (int i = 0; i < hailstones.size() - 1; i++) {
            final Hail h = hailstones.get(i);
            final double[] a1c1 =
                gradientAndYIntercept(h.position().x(), h.position().y(), h.velocity().xVel(), h.velocity().yVel());

            for (int j = i + 1; j < hailstones.size(); j++) {
                final Hail next = hailstones.get(j);
                final double[] a2c2 =
                    gradientAndYIntercept(next.position().x(), next.position().y(), next.velocity().xVel(), next.velocity().yVel());

                final double x = (a2c2[1] - a1c1[1])/(a1c1[0] - a2c2[0]);
                final double y = (a1c1[0] * (a2c2[1] - a1c1[1])/(a1c1[0] - a2c2[0])) + a1c1[1];

                // first check that the intersection point is inside
                // the test range
                if ((x >= min && x <= max) && (y >= min && y <= max)) {

                    // check x
                    if (x < h.position().x() && h.velocity().xVel() > 0) {
                        continue;
                    }

                    if (x > h.position().x() && h.velocity().xVel() < 0) {
                        continue;
                    }

                    if (x < next.position().x() && next.velocity().xVel() > 0) {
                        continue;
                    }

                    if (x > next.position().x() && next.velocity().xVel() < 0) {
                        continue;
                    }

                    // check y
                    if (y < h.position().y() && h.velocity().yVel() > 0) {
                        continue;
                    }

                    if (y > h.position().y() && h.velocity().yVel() < 0) {
                        continue;
                    }

                    if (y < next.position().y() && next.velocity().yVel() > 0) {
                        continue;
                    }

                    if (y > next.position().y() && next.velocity().yVel() < 0) {
                        continue;
                    }

                    ++interceptCount;
                }
            }
        }

        return interceptCount;
    }

    public long findNumberOfIntersectionsInTheTestArea(List<String> data) {
        return findNumberOfIntersectionsInTheTestArea(data, 200000000000000L, 400000000000000L);
    }

    public long sumOfCoordinatesThatHitAllHailstones(List<String> data) {
        return 0;
    }

    public static void main(String[] args) throws IOException {
        final List<String> data = Files.readAllLines(Path.of("src/main/resources/Day24.txt"));
        System.out.println("Part 1: " + new Day24().findNumberOfIntersectionsInTheTestArea(data));
    }
}
