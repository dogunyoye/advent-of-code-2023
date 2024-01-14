package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.LongStream;

public class Day05 {

    enum MapKey {
        SEED,
        SOIL,
        FERTILISER,
        WATER,
        LIGHT,
        TEMPERATURE,
        HUMIDITY,
        LOCATION
    }

    private static final Recipe[] ORDER =
        new Recipe[]{
            new Recipe(MapKey.SEED, MapKey.SOIL),
            new Recipe(MapKey.SOIL, MapKey.FERTILISER),
            new Recipe(MapKey.FERTILISER, MapKey.WATER),
            new Recipe(MapKey.WATER, MapKey.LIGHT),
            new Recipe(MapKey.LIGHT, MapKey.TEMPERATURE),
            new Recipe(MapKey.TEMPERATURE, MapKey.HUMIDITY),
            new Recipe(MapKey.HUMIDITY, MapKey.LOCATION)
        };

    private record Recipe(MapKey source, MapKey destination) { }

    private record Range(long sourceStart, long destinationStart, Long range) { }

    private record FarmInfo(List<Long> seeds, Map<Recipe, List<Range>> recipes) { }

    private Iterator<String> processRecipe(Iterator<String> lines, Recipe recipe, Map<Recipe, List<Range>> recipes) {
        String curr = lines.next();
        final List<Range> ranges = new ArrayList<>();
        while (!curr.isEmpty()) {
            final String[] parts = curr.split(" ");
            final long destination = Long.parseLong(parts[0]);
            final long source = Long.parseLong(parts[1]);
            final long range = Long.parseLong(parts[2]);

            ranges.add(new Range(source, destination, range));

            if (!lines.hasNext()) {
                break;
            }

            curr = lines.next();
        }

        recipes.put(recipe, ranges);
        return lines;
    }
    
    private FarmInfo createFarmInfo(List<String> farmInfo) {
        final List<Long> seeds = new ArrayList<>();
        final Map<Recipe, List<Range>> recipes = new HashMap<>();

        Iterator<String> lines = farmInfo.iterator();

        while (lines.hasNext()) {
            final String currentLine = lines.next();
            if (currentLine.isEmpty()) {
                continue;
            }

            if (currentLine.contains("seeds:")) {
                final String[] parts = currentLine.split(" ");
                for (int i = 1; i < parts.length; i++) {
                    seeds.add(Long.parseLong(parts[i]));
                }
                continue;
            }

            lines = switch(currentLine.split(" ")[0]) {
                    case "seed-to-soil" -> processRecipe(lines, new Recipe(MapKey.SEED, MapKey.SOIL), recipes);
                    case "soil-to-fertilizer" -> processRecipe(lines, new Recipe(MapKey.SOIL, MapKey.FERTILISER), recipes);
                    case "fertilizer-to-water" -> processRecipe(lines, new Recipe(MapKey.FERTILISER, MapKey.WATER), recipes);
                    case "water-to-light" -> processRecipe(lines, new Recipe(MapKey.WATER, MapKey.LIGHT), recipes);
                    case "light-to-temperature" -> processRecipe(lines, new Recipe(MapKey.LIGHT, MapKey.TEMPERATURE), recipes);
                    case "temperature-to-humidity" -> processRecipe(lines, new Recipe(MapKey.TEMPERATURE, MapKey.HUMIDITY), recipes);
                    case "humidity-to-location" -> processRecipe(lines, new Recipe(MapKey.HUMIDITY, MapKey.LOCATION), recipes);
                    default -> throw new RuntimeException("Unknown mapping");
            };
        }
    
        return new FarmInfo(seeds, recipes);
    }

    private long processSeed(long seed, Map<Recipe, List<Range>> recipes) {

        long value = seed;
        for (final Recipe recipe : ORDER) {
            final List<Range> ranges = recipes.get(recipe);
            for (final Range range : ranges) {
                final long sourceStart = range.sourceStart();
                final long sourceLimit = sourceStart + range.range();

                if (value >= sourceStart && value < sourceLimit) {
                    final long toAdd = Math.abs(value - range.sourceStart());

                    final long destinationStart = range.destinationStart();
                    final long destinationLimit = destinationStart + range.range();

                    final long newValue = destinationStart + toAdd;

                    if (newValue >= destinationStart && newValue < destinationLimit) {
                        value = newValue;
                    }

                    break;
                }
            }
        }

        return value;
    }

    public long findLowestLocationNumber(List<String> info) {
        final FarmInfo farmInfo = createFarmInfo(info);
        return
            farmInfo.seeds().stream()
                .map(seed -> new Day05().processSeed(seed, farmInfo.recipes()))
                .mapToLong(location -> location)
                .min()
                .getAsLong();
    }

    public long findLowestLocationForSeedNumberRange(List<String> info) {
        final FarmInfo farmInfo = createFarmInfo(info);
        final List<Long> seeds = farmInfo.seeds();

        long min = Long.MAX_VALUE;

        for (int i = 0; i < seeds.size(); i+=2) {
            final long seed = seeds.get(i);
            final long nextSeed = seeds.get(i+1);

            final long location =
                LongStream
                    .range(seed, seed + nextSeed)
                    .parallel()
                    .map(s -> processSeed(s, farmInfo.recipes()))
                    .min()
                    .getAsLong();

            min = Math.min(location, min);
        }

        return min;
    }

    public static void main(String[] args) throws IOException {
        final List<String> farmInfo = Files.readAllLines(Path.of("src/main/resources/Day05.txt"));
        System.out.println("Part 1: " + new Day05().findLowestLocationNumber(farmInfo));
        System.out.println("Part 2: " + new Day05().findLowestLocationForSeedNumberRange(farmInfo));
    }
}
