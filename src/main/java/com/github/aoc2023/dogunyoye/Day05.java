package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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

    private class SeedRange {
        private long seedStart;
        private long seedEnd;
        private final int recipeIdx;

        private SeedRange(long seedStart, long seedEnd, int recipeIdx) {
            this.seedStart = seedStart;
            this.seedEnd = seedEnd;
            this.recipeIdx = recipeIdx;
        }

        private long seedStart() {
            return this.seedStart;
        }

        private long seedEnd() {
            return this.seedEnd;
        }

        private long length() {
            return this.seedEnd - this.seedStart;
        }

        private int recipeIndex() {
            return this.recipeIdx;
        }

        @Override
        public String toString() {
            return "SeedRange [seedStart=" + seedStart + ", seedEnd=" + seedEnd + ", recipeIdx="
                    + recipeIdx + "]";
        }
    }

    private Iterator<String> processRecipe(Iterator<String> lines, Recipe recipe, Map<Recipe, List<Range>> recipes) {
        String currentLine = lines.next();
        final List<Range> ranges = new ArrayList<>();
        while (!currentLine.isEmpty()) {
            final String[] parts = currentLine.split(" ");
            final long destination = Long.parseLong(parts[0]);
            final long source = Long.parseLong(parts[1]);
            final long range = Long.parseLong(parts[2]);

            ranges.add(new Range(source, destination, range));

            if (!lines.hasNext()) {
                break;
            }

            currentLine = lines.next();
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
                    value = destinationStart + toAdd;
                    break;
                }
            }
        }

        return value;
    }

    private long processSeedRange(SeedRange seedRange, Map<Recipe, List<Range>> recipes) {
        long lowestLocation = Long.MAX_VALUE;
        final Queue<SeedRange> queue = new ArrayDeque<>();
        queue.add(seedRange);

        while (!queue.isEmpty()) {
            final SeedRange sr = queue.poll();
            for (int i = sr.recipeIndex(); i < ORDER.length; i++) {
                final long value = sr.seedStart();

                for (final Range range : recipes.get(ORDER[i])) {

                    final long sourceStart = range.sourceStart();
                    final long sourceLimit = sourceStart + range.range();
                    final long destinationStart = range.destinationStart();
                    final long destinationLimit = destinationStart + range.range();

                    if (value < sourceStart && sr.seedEnd() > sourceLimit) {
                        queue.add(new SeedRange(value, sourceStart, i));
                        queue.add(new SeedRange(sourceLimit, sr.seedEnd(), i));
                        sr.seedStart = destinationStart;
                        sr.seedEnd = destinationLimit;
                        break;
                    }
                    
                    if (value < sourceStart && sr.seedEnd() > sourceStart && sr.seedEnd() < sourceLimit) {
                        queue.add(new SeedRange(value, sourceStart, i));
                        final long diff = sr.seedEnd() - sourceStart;
                        sr.seedStart = destinationStart;
                        sr.seedEnd = destinationStart + diff;
                        break;
                    }
                    
                    // seed range can start in the source
                    if (value >= sourceStart && value < sourceLimit) {
                        final long oldLength = sr.length();
                        final long offset = value - sourceStart;
                        sr.seedStart = destinationStart + offset;

                        // there is overlap
                        // we need to make a new range
                        if (sr.seedEnd() > sourceLimit) {
                            final long length = sr.seedEnd() - sourceLimit;
                            sr.seedEnd = destinationLimit;
                            queue.add(new SeedRange(sourceLimit, sourceLimit + length, i));
                        } else {
                            sr.seedEnd = sr.seedStart() + oldLength;
                        }

                        break;
                    }
                }
            }

            lowestLocation = Math.min(lowestLocation, sr.seedStart());
        }

        return lowestLocation;
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

    public long findLowestLocationForSeedNumberRangeBruteForce(List<String> info) {
        final FarmInfo farmInfo = createFarmInfo(info);
        final List<Long> seeds = farmInfo.seeds();

        long lowestLocation = Long.MAX_VALUE;

        for (int i = 0; i < seeds.size(); i+=2) {
            final long seed = seeds.get(i);
            final long length = seeds.get(i+1);

            final long location =
                LongStream
                    .range(seed, seed + length)
                    .parallel()
                    .map(s -> processSeed(s, farmInfo.recipes()))
                    .min()
                    .getAsLong();

            lowestLocation = Math.min(location, lowestLocation);
        }

        return lowestLocation;
    }

    public long findLowestLocationForSeedNumberRange(List<String> info) {
        final FarmInfo farmInfo = createFarmInfo(info);
        final List<Long> seeds = farmInfo.seeds();

        long lowestLocation = Long.MAX_VALUE;

        for (int i = 0; i < seeds.size(); i+=2) {
            final long seed = seeds.get(i);
            final long length = seeds.get(i+1);

            final SeedRange sr = new SeedRange(seed, seed + length, 0);
            lowestLocation = Math.min(lowestLocation, processSeedRange(sr, farmInfo.recipes()));
        }

        return lowestLocation;
    }

    public static void main(String[] args) throws IOException {
        final List<String> farmInfo = Files.readAllLines(Path.of("src/main/resources/Day05.txt"));
        System.out.println("Part 1: " + new Day05().findLowestLocationNumber(farmInfo));
        System.out.println("Part 2: " + new Day05().findLowestLocationForSeedNumberRange(farmInfo));
    }
}
