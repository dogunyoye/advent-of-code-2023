package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Day04 {

    private record ScratchCard(int cardNumber, Set<Integer> winningNumbers, Set<Integer> myNumbers) { }

    private static ScratchCard createCard(String card) {
        // Card 1: 41 48 83 86 17 | 83 86  6 31 17  9 48 53

        // Card 1
        // 41 48 83 86 17 | 83 86  6 31 17  9 48 53
        final String[] parts = card.split(": ");
        final String[] parts2 = parts[0].split(" ");
        final int cardNumber = Integer.parseInt(parts2[parts2.length-1].trim());

        // 41 48 83 86 17 | 83 86  6 31 17  9 48 53
        final String[] numbersParts = parts[1].split("\\|");
        final Set<Integer> winningSet = new HashSet<>();
        final Set<Integer> myNumbers = new HashSet<>();

        // 41 48 83 86 17
        final String[] winningNums = numbersParts[0].trim().split(" ");
        for (final String winningNum : winningNums) {
            if (!winningNum.isEmpty()) {
                winningSet.add(Integer.parseInt(winningNum));
            }
        }

        // 83 86  6 31 17  9 48 53
        final String[] myNums = numbersParts[1].trim().split(" ");
        for (final String myNum : myNums) {
            if (!myNum.isEmpty()) {
                myNumbers.add(Integer.parseInt(myNum));
            }
        }

        final ScratchCard sc = new ScratchCard(cardNumber, winningSet, myNumbers);
        return sc;
    }

    private List<ScratchCard> createScratchCards(List<String> cards) {
        return cards.stream().map(Day04::createCard).toList();
    }

    public int calculatePoints(List<String> cards) {
        int sum = 0;

        final List<ScratchCard> scratchCards = createScratchCards(cards);
        for (final ScratchCard sc : scratchCards) {
            final long winningNums =
                sc.winningNumbers().stream().filter(num -> sc.myNumbers().contains(num)).count();
            sum += Math.pow(2, winningNums-1);
        }

        return sum;
    }

    public int processCard(int cardId, Map<Integer, List<Integer>> cardsMap) {
        if (cardsMap.get(cardId).size() == 0) {
            return 0;
        }

        int result = 0;
        for (final int copy : cardsMap.get(cardId)) {
            result += processCard(copy, cardsMap) + 1;
        }

        return result;
    }

    public int totalScratchCards(List<String> cards) {
        final Map<Integer, List<Integer>> cardCopiesMap = new HashMap<>();

        final List<ScratchCard> scratchCards = createScratchCards(cards);
        for (final ScratchCard sc : scratchCards) {
            final long cardsWon =
                sc.winningNumbers().stream().filter(num -> sc.myNumbers().contains(num)).count();

            final List<Integer> cardsWonSet = new ArrayList<>();
            for (int i = 0; i < cardsWon; i++) {
                final int cardNumberWon = sc.cardNumber() + i + 1;
                cardsWonSet.add(cardNumberWon);
            }

            cardCopiesMap.put(sc.cardNumber(), cardsWonSet);
        }

        int result = scratchCards.size();
        for (final ScratchCard sc : scratchCards) {
            result += processCard(sc.cardNumber(), cardCopiesMap);
        }

        return result;
    }
    
    public static void main(String[] args) throws IOException {
        final List<String> cards = Files.readAllLines(Path.of("src/main/resources/Day04.txt"));
        System.out.println("Part 1: " + new Day04().calculatePoints(cards));
        System.out.println("Part 2: " + new Day04().totalScratchCards(cards));
    }
}
