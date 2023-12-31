package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class Day13 {

    private record Note(int id, List<String> rows, List<String> columns, boolean cache) { }

    private record Replacement(int idx, String replacement) { }

    private static final Map<Integer, String> REFLECTION_POINTS = new HashMap<>();

    private Note createNoteFromRows(int id, List<String> data, boolean cacheValue) {
        final int lineLength = data.get(0).length();
        final List<String> columns = new ArrayList<>();
        for (int i = 0; i < lineLength; i++) {
            String column = "";
            for (final String line : data) {
                column += line.charAt(i);
            }
            columns.add(column);
        }

        return new Note(id, new ArrayList<>(data), columns, cacheValue);
    }

    private List<Note> createNotes(List<String> data, boolean cacheValue) {
        final List<Note> notes = new ArrayList<>();

        // https://stackoverflow.com/a/62670286/2981152
        final Collection<List<String>> n =
            Arrays.stream(
                data.stream()
                .collect(Collectors.joining("\n")).split("\n{2}") // split by double 'empty lines'
            )
            .map(s -> Arrays.stream(s.split("\n")).collect(Collectors.toList()))
            .collect(Collectors.toList());

        int id = 0;
        for (final List<String> rows : n) {
            notes.add(createNoteFromRows(id, rows, cacheValue));
            ++id;
        }

        return notes;
    }

    private static int getDiffCount(String a, String b) {
        if (a.equals(b)) {
            return 0;
        }

        int count = 0;
        for (int i = 0; i < a.length(); i++) {
            if (a.charAt(i) != b.charAt(i)) {
                count++;
            }
        }
        return count;
    }

    private static Map<Note, List<Replacement>> findCandidates(List<Note> notes, boolean row) {
        final Map<Note, List<Replacement>> candidatesMap = new HashMap<>();
        for (final Note note : notes) {

            final List<String> data;
            if (row) {
                data = note.rows();
            } else {
                data = note.columns();
            }

            final List<Replacement> replacements = new ArrayList<>();

            for (int i = 0; i < data.size() - 1; i++) {
                final String current = data.get(i);
                final String next = data.get(i+1);

                int diffCount = getDiffCount(current, next);
                if (diffCount == 1) {
                    replacements.add(new Replacement(i+1, current));
                    continue;
                }

                if (current.equals(next)) {
                    int upOrLeft = i - 1;
                    int belowOrRight = i + 2;

                    while (upOrLeft >= 0 && belowOrRight < data.size()) {

                        if (!data.get(upOrLeft).equals(data.get(belowOrRight))) {
                            diffCount = getDiffCount(data.get(upOrLeft), data.get(belowOrRight));
                            if (diffCount == 1) {
                                replacements.add(new Replacement(belowOrRight, data.get(upOrLeft)));
                            }
                            break;
                        }

                        --upOrLeft;
                        ++belowOrRight;
                    }
                }
            }

            if (!replacements.isEmpty()) {
                candidatesMap.put(note, replacements);
            }
        }

        return candidatesMap;
    }

    private static int checkSymmetry(Note n, List<String> note, boolean isVertical) {
        final String value = REFLECTION_POINTS.get(n.id());
        for (int i = 0; i < note.size() - 1; i++) {
            final String current = note.get(i);
            final String next = note.get(i + 1);
            final String reflectionLine = isVertical ? "x=" + i : "y=" + i;

            if (value != null) {
                // for part 2, skip this reflection line
                // if it is the previously kn
                if (value.equals(reflectionLine)) {
                    continue;
                }
            }

            if (current.equals(next)) {
                    boolean isReflectionPoint = true;

                    int upOrLeft = i - 1;
                    int belowOrRight = i + 2;

                    while (upOrLeft >= 0 && belowOrRight < note.size()) {

                        if (!note.get(upOrLeft).equals(note.get(belowOrRight))) {
                            isReflectionPoint = false;
                            break;
                        }

                        --upOrLeft;
                        ++belowOrRight;
                    }

                    if (isReflectionPoint) {
                        if (n.cache()) {
                            REFLECTION_POINTS.put(n.id(), reflectionLine);
                        }
                        return i + 1;
                    }
            }
        }

        return 0;
    }

    private static int checkVerticalSymmetry(Note note) {
        return checkSymmetry(note, note.columns(), true);
    }

    private static int checkHorizontalSymmetry(Note note) {
        return checkSymmetry(note, note.rows(), false);
    }

    public long summariseAllNotes(List<String> data) {
        final List<Note> notes = createNotes(data, true);
        final long leftColumns = notes.stream().mapToInt(Day13::checkVerticalSymmetry).sum();
        final long rowsAbove = notes.stream().mapToInt(Day13::checkHorizontalSymmetry).sum();

        return leftColumns + (100 * rowsAbove);
    }

    public long summariseAllNotesPart2(List<String> data) {
        final List<Note> notes = createNotes(data, true);
        final Map<Note, List<Replacement>> rowReplacements = findCandidates(notes, true);
        final Map<Note, List<Replacement>> columnReplacements = findCandidates(notes, false);

        long leftColumns = 0;
        long rowsAbove = 0;

        final Set<Note> found = new HashSet<>();

        for (final Entry<Note, List<Replacement>> e : rowReplacements.entrySet()) {
            final Note note = e.getKey();
            final List<Replacement> replacements = e.getValue();

            for (final Replacement r : replacements) {
                final List<String> newRow = new ArrayList<String>(List.copyOf(note.rows()));
                newRow.set(r.idx(), r.replacement());

                final Note newNote = createNoteFromRows(note.id(), newRow, false);
                int l = checkVerticalSymmetry(newNote);
                int a = checkHorizontalSymmetry(newNote);

                if (l > 0 && a > 0) {
                    throw new RuntimeException("Multiple reflection points");
                }

                if (l != 0) {
                    leftColumns += l;
                    found.add(note);
                    break;
                }

                if (a != 0) {
                    rowsAbove += a;
                    found.add(note);
                    break;
                }
            }
        }

        for (final Entry<Note, List<Replacement>> e : columnReplacements.entrySet()) {

            final Note note = e.getKey();
            if (found.contains(note)) {
                // skip
                continue;
            }

            final List<Replacement> replacements = e.getValue();

            for (final Replacement r : replacements) {
                final List<String> newRow = new ArrayList<String>(List.copyOf(note.rows()));
                for (int i = 0; i < newRow.size(); i++) {
                    final StringBuilder sb = new StringBuilder(newRow.get(i));
                    sb.setCharAt(r.idx(), r.replacement().charAt(i));
                    newRow.set(i, sb.toString());
                }

                final Note newNote = createNoteFromRows(note.id(), newRow, false);
                int l = checkVerticalSymmetry(newNote);
                int a = checkHorizontalSymmetry(newNote);

                if (l > 0 && a > 0) {
                    throw new RuntimeException("Multiple reflection points");
                }

                if (l != 0) {
                    leftColumns += l;
                    found.add(note);
                    break;
                }

                if (a != 0) {
                    rowsAbove += a;
                    found.add(note);
                    break;
                }
            }
        }

        return leftColumns + (100 * rowsAbove);
    }
 
    public static void main(String[] args) throws IOException {
        final List<String> data = Files.readAllLines(Path.of("src/main/resources/Day13.txt"));
        System.out.println("Part 1: " + new Day13().summariseAllNotes(data));
        System.out.println("Part 2: " + new Day13().summariseAllNotesPart2(data));
    }
}
