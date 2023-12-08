package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class Day07 {

    enum HandType {
        HIGH_CARD,
        ONE_PAIR,
        TWO_PAIR,
        THREE_OF_A_KIND,
        FULL_HOUSE,
        FOUR_OF_A_KIND,
        FIVE_OF_A_KIND
    }

    private static final List<Character> CARD_ORDER =
        Arrays.asList('2', '3', '4', '5', '6', '7', '8', '9', 'T', 'J', 'Q', 'K', 'A');

    private static final List<Character> NEW_CARD_ORDER =
        Arrays.asList('J', '2', '3', '4', '5', '6', '7', '8', '9', 'T', 'Q', 'K', 'A');

    private record Pair<K, V>(K first, V second) { }

    private class Hand implements Comparable<Hand> {
        private final char[] cards;
        private final int bid;
        private HandType type;
        private final char strongestCard;
        private final List<Character> order;

        private Hand(char[] cards, int bid, HandType type, char strongestCard, List<Character> order) {
            this.cards = cards;
            this.bid = bid;
            this.type = type;
            this.strongestCard = strongestCard;
            this.order = order;
        }

        private int getBid() {
            return bid;
        }

        private void levelUp() {
            final String cardString = new String(this.cards);
            if (cardString.contains("J")) {
                final char[] leveledUp = cardString.replaceAll("J", Character.toString(this.strongestCard)).toCharArray();
                final Pair<HandType, Character> pair = determineHandType(leveledUp, this.order);
                this.type = pair.first();
            }
        }

        @Override
        public int compareTo(Hand other) {
            if (this.type.ordinal() > other.type.ordinal()) {
                return 1;
            } else if (this.type.ordinal() < other.type.ordinal()) {
                return -1;
            }

            // same hand type

            for (int i = 0; i < 5; i++) {
                if (this.order.indexOf(this.cards[i]) > this.order.indexOf(other.cards[i])) {
                    return 1;
                } else if (this.order.indexOf(this.cards[i]) < this.order.indexOf(other.cards[i])) {
                    return -1;
                }
            }

            return 0;
        }

        @Override
        public String toString() {
            return String.format("Hand: %s", new String(this.cards));
        }
    }

    /*
     * We want to maximise the use of our Joker for part 2.
     * Can't do that if it deemed the strongest
     * So we set the strongest card as the next strongest (in accordance to `order`)
     * for it to be correctly leveled up
     */
    private char correctStrongestIfJoker(char strongest, char[] cards, List<Character> order) {
        if (strongest == 'J') {
            int maxIdx = Integer.MIN_VALUE;
            for (int i = 0; i < 5; i++) {
                maxIdx = Math.max(maxIdx, order.indexOf(cards[i]));
            }

            return order.get(maxIdx);
        }

        return strongest;
    }

    private Pair<HandType, Character> determineHandType(char[] cards, List<Character> order) {
        final Map<Character, Integer> characterCount = new HashMap<>();
        for (final char card : cards) {
            if (characterCount.containsKey(card)) {
                int count = characterCount.get(card) + 1;
                characterCount.put(card, count);
            } else {
                characterCount.put(card, 1);
            }
        }

        final Set<Character> keys = characterCount.keySet();
        final Set<Entry<Character, Integer>> entries = characterCount.entrySet();
        final char strongest;

        if (keys.size() == 1) {
            return new Pair<HandType, Character>(HandType.FIVE_OF_A_KIND, keys.stream().findFirst().get());
        }

        if (keys.size() == 5) {
            int maxIdx = Integer.MIN_VALUE;
            for (int i = 0; i < 5; i++) {
                maxIdx = Math.max(maxIdx, order.indexOf(cards[i]));
            }
            return new Pair<HandType, Character>(HandType.HIGH_CARD, order.get(maxIdx));
        }

        if (keys.size() == 2) {
            if (characterCount.values().stream().anyMatch(n -> n == 4)) {
                strongest = entries.stream()
                    .filter((entry) -> entry.getValue() == 4).findFirst().get().getKey();
                return new Pair<HandType, Character>(HandType.FOUR_OF_A_KIND, strongest);
            }

            if (characterCount.values().stream().anyMatch(n -> n == 3)) {
                strongest = entries.stream()
                    .filter((entry) -> entry.getValue() == 3).findFirst().get().getKey();
                return new Pair<HandType, Character>(HandType.FULL_HOUSE, strongest);
            }
        }

        if (keys.size() == 3) {
            if (characterCount.values().stream().anyMatch(n -> n == 3)) {
                strongest = entries.stream()
                    .filter((entry) -> entry.getValue() == 3).findFirst().get().getKey();
                return new Pair<HandType, Character>(HandType.THREE_OF_A_KIND, strongest);
            }

            final List<Entry<Character, Integer>> pairs = entries.stream()
                .filter((entry) -> entry.getValue() == 2).toList();
            
            final int maxIdx = Math.max(order.indexOf(pairs.get(0).getKey()), order.indexOf(pairs.get(1).getKey()));

            return new Pair<HandType, Character>(HandType.TWO_PAIR, order.get(maxIdx));
        }

        strongest = entries.stream().filter((entry) -> entry.getValue() == 2).findFirst().get().getKey();
        return new Pair<HandType, Character>(HandType.ONE_PAIR, strongest);
    }

    private List<Hand> createHands(List<String> data, List<Character> order) {
        return 
            data.stream()
                .map((line) -> {
                    final String[] parts = line.split(" ");
                    final char[] cards = parts[0].toCharArray();
                    final int bid = Integer.parseInt(parts[1]);
                    final Pair<HandType, Character> pair = determineHandType(cards, order);
                    final char strongest = correctStrongestIfJoker(pair.second(), cards, order);
                    return new Hand(cards, bid, pair.first(), strongest, order);
                })
                .toList();
    }

    public int calculateTotalWinnings(List<String> data) {
        final List<Hand> hands = new ArrayList<>(createHands(data, CARD_ORDER));

        hands.sort(Hand::compareTo);

        int result = 0;
        for (int i = 0; i < hands.size(); i++) {
            result += (i+1) * hands.get(i).getBid();
        }

        return result;
    }

    public int calculateTotalWinningsWithJokerRule(List<String> data) {
        final List<Hand> hands = new ArrayList<>(createHands(data, NEW_CARD_ORDER));

        for (final Hand hand : hands) {
            hand.levelUp();
        }

        hands.sort(Hand::compareTo);

        int result = 0;
        for (int i = 0; i < hands.size(); i++) {
            result += (i+1) * hands.get(i).getBid();
        }

        return result;
    }
    
    public static void main(String[] args) throws IOException {
        final List<String> data = Files.readAllLines(Path.of("src/main/resources/Day07.txt"));
        System.out.println("Part 1: " + new Day07().calculateTotalWinnings(data));
        System.out.println("Part 2: " + new Day07().calculateTotalWinningsWithJokerRule(data));
    }
}
