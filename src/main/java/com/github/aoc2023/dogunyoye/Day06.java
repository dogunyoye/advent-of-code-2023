package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.LongStream;

public class Day06 {

    private record Race(long time, long recordDistance) { }

    private Race[] createRaces(List<String> data) {
        final String[] timesParts = data.get(0).split("Time:")[1].trim().split(" ");
        final String[] distancesParts = data.get(1).split("Distance:")[1].trim().split(" ");
        final List<Integer> times = new ArrayList<>();
        final List<Integer> distances = new ArrayList<>();

        for (final String time : timesParts) {
            if (!time.isEmpty()) {
                times.add(Integer.parseInt(time));
            }
        }

        for (final String distance : distancesParts) {
            if (!distance.isEmpty()) {
                distances.add(Integer.parseInt(distance));
            }
        }

        final Race[] races = new Race[times.size()];
        for (int i = 0; i < times.size(); i++) {
            races[i] = new Race(times.get(i), distances.get(i));
        }

        return races;
    }

    private static Long findRecordBreakers(Race race) {
        final long time = race.time();
        final long distance = race.recordDistance();

        return
            LongStream
                .range(1, time)
                .parallel()
                .filter((s) -> {
                    final long remainingTime = time - s;
                    final long newDistance = s * remainingTime;
                    return newDistance > distance;
                })
                .count();
    }

    public long calculateMarginOfError(List<String> data) {
        final Race[] races = createRaces(data);
        return Arrays.stream(races).map(Day06::findRecordBreakers).reduce((x,y) -> x * y).get();
    }

    public long findNumberOfWaysToBeatRecordBruteForce(List<String> data) {
        final Race[] races = createRaces(data);
        String timeString = "";
        String distanceString = "";

        for (final Race race : races) {
            timeString += Long.toString(race.time());
            distanceString += Long.toString(race.recordDistance());
        }

        final Race race = new Race(Long.parseLong(timeString), Long.parseLong(distanceString));
        return findRecordBreakers(race);
    }

    /**
     * Mathematical solution to part 2 using the quadratic formula.
     * <p>
     * We know that:
     * <p>
     * {@code speed = distance / time}
     * <p>
     * We want to calculate the record breaking distances so we need to rearrange
     * the formula:
     * <p>
     * {@code distance = speed * time}
     * <p>
     * - The speed is equal to however long we hold the button for
     * - The time is however long we have left after holding the button i.e the difference
     * between the duration of the race and the speed.
     * <p>
     * With this knowledge we can form a quadratic equation:
     * <p>
     * {@code d = s * (t - s)}
     * <p>
     * {@code d = st - s^2}
     * <p>
     * We can rearrange the formula in the form {@code ax^2 + bx + c = 0}:
     * <p>
     * {@code s^2 - st + d = 0}
     * <p>
     * To find the roots, we can use the quadratic formula - {@link https://en.wikipedia.org/wiki/Quadratic_formula}
     * <p>
     * The roots are the values which will <b>match</b> the record, we want to <b>beat</b> it.
     * <p>
     * So we take the integer values of the roots and find the next nearest integer to them.
     * <p>
     * The range of values will simply be the difference between the high root and low root, plus 1.
     * Well explained here: {@link https://www.reddit.com/r/adventofcode/comments/18ccmxr/comment/kcbi67m/?utm_source=share&utm_medium=web2x&context=3}
     */
    public long findNumberOfWaysToBeatRecord(List<String> data) {
        final Race[] races = createRaces(data);
        String timeString = "";
        String distanceString = "";

        for (final Race race : races) {
            timeString += Long.toString(race.time());
            distanceString += Long.toString(race.recordDistance());
        }

        final long a = 1;
        final long b = -Long.parseLong(timeString);;
        final long c = Long.parseLong(distanceString);;

        final double x0 = (-b + Math.sqrt(Math.pow(b, 2) - (4 * a * c))) / (2 * a);
        final double x1 = (-b - Math.sqrt(Math.pow(b, 2) - (4 * a * c))) / (2 * a);

        System.out.println(x0);
        System.out.println(x1);

        return ((long)(Math.floor(x0) - Math.ceil(x1)) + 1);
    }
    
    public static void main(String[] args) throws IOException {
        final List<String> data = Files.readAllLines(Path.of("src/main/resources/Day06.txt"));
        System.out.println("Part 1: " + new Day06().calculateMarginOfError(data));
        System.out.println("Part 2: " + new Day06().findNumberOfWaysToBeatRecord(data));
    }
}
