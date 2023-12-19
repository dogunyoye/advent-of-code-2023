package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class Day15 {

    enum Operation {
        PUT,
        REMOVE
    }

    private class Lens {
        private final String label;
        private final int focalLength;
        private final Operation op;

        private Lens(String label, int focalLength, Operation op) {
            this.label = label;
            this.focalLength = focalLength;
            this.op = op;
        }

        private String label() {
            return this.label;
        }

        private Operation op() {
            return this.op;
        }

        private int focalLength() {
            return this.focalLength;
        }

        // For the purposes of this exercise, lens equality only accounts for the label
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            result = prime * result + ((label == null) ? 0 : label.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            Lens other = (Lens) obj;
            return this.label.equals(other.label);
        }

        private Day15 getEnclosingInstance() {
            return Day15.this;
        }

        @Override
        public String toString() {
            return "Lens [label=" + label + ", focalLength=" + focalLength + ", op=" + op + "]";
        }
    }

    private record Box(int id, LinkedList<Lens> values) { 
        private void put(Lens lens) {
            this.values.add(lens);
        }

        private void put(int index, Lens lens) {
            this.values.remove(index);
            this.values.add(index, lens);
        }

        private int find(Lens lens) {
            return this.values.indexOf(lens);
        }

        private void remove(Lens lens) {
            this.values.remove(lens);
        }

        private List<Lens> lens() {
            return new ArrayList<>(this.values);
        }
    }

    private static int applyHASHalgorithm(String s) {

        int currentValue = 0;
        for (final int c : s.chars().toArray()) {
            currentValue += c;
            currentValue *= 17;
            currentValue %= 256;
        }

        return currentValue;
    }

    private void processLenses(Lens lens, Box[] boxes) {

        final int boxIdx = applyHASHalgorithm(lens.label());
        Box box = boxes[boxIdx];

        switch(lens.op()) {
        
        case Operation.PUT:
            if (box == null) {
                boxes[boxIdx] = new Box(boxIdx, new LinkedList<>());
                box = boxes[boxIdx];
            }

            final int existing = box.find(lens);
            if (existing == -1) {
                box.put(lens);
            } else {
                box.put(existing, lens);
            }

            break;
        
        case Operation.REMOVE:
            if (box == null) {
                return;
            }

            box.remove(lens);

            break;
        
        default:
            throw new RuntimeException("Unknown operation: " + lens.op());
        }
    }
    
    private List<String> getSteps(List<String> data) {
        return Arrays.stream(data.get(0).split(",")).toList();
    }

    private List<Lens> getLenses(List<String> data) {
        final List<String> list = getSteps(data);
        final List<Lens> lenses = new ArrayList<>();

        for (final String step : list) {
            if (step.contains("=")) {
                final String[] parts = step.split("=");
                lenses.add(new Lens(parts[0], Integer.parseInt(parts[1]), Operation.PUT));
            } else {
                lenses.add(new Lens(step.substring(0, step.length()-1), 0, Operation.REMOVE));
            }
        }

        return lenses;
    }

    private static int sumBox(Box box) {
        int sum = 0;
        final List<Lens> lenses = box.lens();
        for (int i = 0; i < lenses.size(); i++) {
            sum += (i + 1) * (box.id() + 1) *lenses.get(i).focalLength();
        }

        return sum;
    }

    public int sumOfHashAlgorithmRuns(List<String> data) {
        final List<String> steps = getSteps(data);
        return steps.stream().map(Day15::applyHASHalgorithm).mapToInt(n -> n).sum();
    }

    public int calculateFocusingPower(List<String> data) {
        final List<Lens> lenses = getLenses(data);
        final Box[] boxes = new Box[256];

        for (final Lens lens : lenses) {
            processLenses(lens, boxes);
        }

        return Arrays.stream(boxes).map((b) -> b == null ? 0 : sumBox(b)).mapToInt(n -> n).sum();
    }

    public static void main(String[] args) throws IOException {
        final List<String> data = Files.readAllLines(Path.of("src/main/resources/Day15.txt"));
        System.out.println("Part 1: " + new Day15().sumOfHashAlgorithmRuns(data));
        System.out.println("Part 2: " + new Day15().calculateFocusingPower(data));
    }
}
