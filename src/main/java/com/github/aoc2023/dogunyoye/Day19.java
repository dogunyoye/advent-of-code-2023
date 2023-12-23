package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Day19 {

    private record PartRating(int x, int m, int a, int s) { }

    private record Order(String s, int idx) { }

    private record Workflow(String name, List<Function<Integer, String>> processes, List<Order> order) { }

    private boolean isFunction(String s) {
        return
            s.contains("x<") || s.contains("x>") ||
            s.contains("m<") || s.contains("m>") ||
            s.contains("a<") || s.contains("a>") ||
            s.contains("s<") || s.contains("s>");
    }

    private Map<String, Workflow> buildWorkflows(List<String> data, int idx) {
        final Map<String, Workflow> workflows = new HashMap<>();

        for (int i = 0; i < idx; i++) {

            final String line = data.get(i).replaceAll("\\{", " ").replaceAll("\\}", "");
            final String[] parts = line.split(" ");
            final String name = parts[0];
            final String[] functionsAndResults = parts[1].split(",");
            final List<Order> order = new ArrayList<>();
            final List<Function<Integer, String>> processes = new ArrayList<>();

            for (int j = 0; j < functionsAndResults.length; j++) {
                final String fr = functionsAndResults[j];
                if (isFunction(fr)) { // immediate function (a>1716)
                    order.add(new Order(Character.toString(fr.charAt(0)), j));
                    final String[] parts2 = fr.split(":");
                    final String test = parts2[0];
                    final String result = parts2[1];

                    final char lessThanOrGreaterThan = test.charAt(1);
                    final int testNumber = Integer.parseInt(test.substring(2, test.length()));
                    final Function<Integer, String> f;

                    if (lessThanOrGreaterThan == '>') {
                        f = (number) -> {
                            if (number > testNumber) {
                                return result;
                            }
                            return null;
                        };
                    } else {
                        f = (number) -> {
                            if (number < testNumber) {
                                return result;
                            }
                            return null;
                        };
                    }

                    processes.add(f);
                } else { // reference of a function (rfg) or terminal result (A or R)
                    order.add(new Order(fr, -1));
                }
            }

            workflows.put(name, new Workflow(name, processes, order));
        }

        return workflows;
    }

    private List<PartRating> buildPartRatings(List<String> data, int idx) {
        final List<PartRating> partRatings = new ArrayList<>();
        for (int i = idx; i < data.size(); i++) {
            final String line = data.get(i).substring(1, data.get(i).length() - 1);
            final String[] parts = line.split(",");
            final List<Integer> nums = new ArrayList<>();
            for (String n : parts) {
                nums.add(Integer.parseInt(n.split("=")[1]));
            }
            partRatings.add(new PartRating(nums.get(0), nums.get(1), nums.get(2), nums.get(3)));
        }

        return partRatings;
    }

    private static int findEmptyLine(List<String> data) {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).isEmpty()) {
                return i;
            }
        }

        throw new RuntimeException("No blank line found!");
    }

    private boolean testPartRating(Map<String, Workflow> workflows, PartRating pr) {
        String wfName = "in";
        int i = 0;

        while (true) {

            String result = "";
            // terminating result (A)ccept or (R)eject
            if ("A".equals(wfName) || "R".equals(wfName)) {
                return wfName.equals("A");
            }

            final Workflow wf = workflows.get(wfName);

            // function, function reference or result
            final Order order = wf.order().get(i);
            final String s = order.s();

            // terminating result (A)ccept or (R)eject
            if ("A".equals(s) || "R".equals(s)) {
                return s.equals("A");
            }

            // function
            if (s.length() == 1) {
                final Function<Integer, String> f = wf.processes.get(order.idx());
                switch(s) {
                    case "x":
                        result = f.apply(pr.x());
                        break;
                    case "m":
                        result = f.apply(pr.m());
                        break;
                    case "a":
                        result = f.apply(pr.a());
                        break;
                    case "s":
                        result = f.apply(pr.s());
                        break;
                    default:
                        throw new RuntimeException("invalid letter: " + s);
                }

                // null result means the function returned false
                if (result == null) {
                    // stay in the same workflow
                    final Order nextOrder = wf.order().get(i+1);
                    if (nextOrder.s().matches("[xmas]")) {
                        ++i;
                    } else {
                        wfName = nextOrder.s();
                        i = 0;
                    }
                } else {
                        wfName = result;
                        i = 0;
                }
            } else {
                // function reference
                wfName = s;
                i = 0;
            }
        }
    }

    public int findSumOfAcceptedParts(List<String> data) {
        final int idx = findEmptyLine(data);
        final Map<String, Workflow> workflows = buildWorkflows(data, idx);
        final List<PartRating> partRatings = buildPartRatings(data, idx + 1);

        int sum = 0;
        for (final PartRating pr : partRatings) {
            if (testPartRating(workflows, pr)) {
                sum += pr.x() + pr.m() + pr.a() + pr.s();
            }
        }

        return sum;
    }

    public long findDistinctNumberOfAcceptedCombinations(List<String> data) {
        final int idx = findEmptyLine(data);
        final Map<String, Workflow> workflows = buildWorkflows(data, idx);

        return 0;
    }
    
    public static void main(String[] args) throws IOException {
        final List<String> data = Files.readAllLines(Path.of("src/main/resources/Day19.txt"));
        System.out.println("Part 1: " + new Day19().findSumOfAcceptedParts(data));
        System.out.println("Part 2: " + new Day19().findDistinctNumberOfAcceptedCombinations(data));
    }
}
