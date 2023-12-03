package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class Day02 {

    private record Round (int blueCubes, int redCubes, int greenCubes) { }

    private record Game (int id, List<Round> rounds) { }

    private static final int RED_CUBE_LIMIT = 12;
    private static final int GREEN_CUBE_LIMIT = 13;
    private static final int BLUE_CUBE_LIMIT = 14;

    private Game createGame(String game) {
        // Game 1: 3 blue, 4 red; 1 red, 2 green, 6 blue; 2 green
        final String[] split = game.split(": ");
        // Game 1
        final int gameId = Integer.parseInt(split[0].split(" ")[1]);
        final List<Round> rounds = new ArrayList<>();

        // 3 blue, 4 red; 1 red, 2 green, 6 blue; 2 green
        final String[] gameRounds = split[1].split("; ");

        for (final String r : gameRounds) {
            int blueCubes = 0;
            int redCubes = 0;
            int greenCubes = 0;

            // 3 blue, 4 red
            final String[] cubes = r.split(", ");
            for (final String cube : cubes) {
                // 3 blue
                final String[] cubeParts = cube.split(" ");
                final int numberOfCubes = Integer.parseInt(cubeParts[0]);
                final String cubeColour = cubeParts[1];
                
                switch (cubeColour) {
                    case "blue":
                        blueCubes = numberOfCubes;
                        break;
                    case "red":
                        redCubes = numberOfCubes;
                        break;
                    case "green":
                        greenCubes = numberOfCubes;
                        break;
                    default:
                        throw new RuntimeException("Unknown cube colour");
                }
            }

            rounds.add(new Round(blueCubes, redCubes, greenCubes));
        }

        return new Game(gameId, rounds);
    }

    private List<Game> createGames(List<String> gamesList) {
        final List<Game> games = new ArrayList<>();

        gamesList.stream().forEach((game) -> {
            games.add(createGame(game));
        });

        return games;
    }

    public int sumCandidateGameIds(List<String> gamesList) {
        final List<Game> games = createGames(gamesList);

        return
            games.stream().filter((game) -> {
                for (final Round round : game.rounds()) {
                    if (round.blueCubes() <= BLUE_CUBE_LIMIT &&
                        round.greenCubes() <= GREEN_CUBE_LIMIT &&
                        round.redCubes() <= RED_CUBE_LIMIT) {
                        continue;
                    }

                    return false;
                }

                return true;
            })
            .flatMapToInt(game -> IntStream.of(game.id()))
            .sum();
    }

    public int sumMaxCubesPerGame(List<String> gamesList) {
        final List<Game> games = createGames(gamesList);

        return
            games.stream().map((game) -> {
                final int maxRedCubes =
                    game.rounds().stream().map(Round::redCubes).max(Integer::compare).get();
                final int maxGreenCubes =
                    game.rounds().stream().map(Round::greenCubes).max(Integer::compare).get();
                final int maxBlueCubes =
                    game.rounds().stream().map(Round::blueCubes).max(Integer::compare).get();

                return maxRedCubes * maxGreenCubes * maxBlueCubes;
            })
            .flatMapToInt(IntStream::of)
            .sum();
    }

    public static void main(String[] args) throws IOException {
        final List<String> gamesList = Files.readAllLines(Path.of("src/main/resources/Day02.txt"));
        System.out.println("Part 1: " + new Day02().sumCandidateGameIds(gamesList));
        System.out.println("Part 2: " + new Day02().sumMaxCubesPerGame(gamesList));
    }
}
