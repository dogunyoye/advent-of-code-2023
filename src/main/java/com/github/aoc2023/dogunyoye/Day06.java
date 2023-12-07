package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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

    public int calculateMarginOfError(List<String> data) {
        final Race[] races = createRaces(data);
        int result = 1;

        for (final Race race : races) {
            result *= findRecordBreakers(race);
        }

        return result;
    }

    public long calculateMarginOfErrorPart2(List<String> data) {
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
    
    public static void main(String[] args) throws IOException {
        final List<String> data = Files.readAllLines(Path.of("src/main/resources/Day06.txt"));
        System.out.println("Part 1: " + new Day06().calculateMarginOfError(data));
        System.out.println("Part 2: " + new Day06().calculateMarginOfErrorPart2(data));
    }
}
