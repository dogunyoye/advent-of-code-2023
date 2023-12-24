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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day19 {

    private enum WorkflowParams {
        // an immediate function (s>537:gd)
        FUNCTION,
        // a reference to another function (qqz)
        // or a result (A/R)
        FUNCTION_REFERENCE,
    }

    private enum Operand {
        LESS_THAN,
        GREATER_THAN
    }

    private record Param(WorkflowParams type, Operand op, Character xmas, Integer testNumber, String result) { }

    private class Range {
        private int min;
        private int max;

        private Range(int min, int max) {
            this.min = min;
            this.max = max;
        }

        private Range setMin(int value) {
            return new Range(value, max);
        }

        private Range setMax(int value) {
            return new Range(min, value);
        }

        private Range invert(Operand op) {
            if (op == Operand.LESS_THAN) {
                return new Range(max + 1, 4000);
            }

            return new Range(1, min-1);
        }

        private Range intersect(Range r) {
            final Set<Integer> s1 = IntStream.rangeClosed(min, max).boxed().collect(Collectors.toSet());
            final Set<Integer> s2 = IntStream.rangeClosed(r.min, r.max).boxed().collect(Collectors.toSet());
            s1.retainAll(s2);
            if (s1.isEmpty()) {
                // no intersection between the ranges
                return null;
            }

            final int[] intersect = s1.stream().mapToInt(n -> n).toArray();
            Arrays.sort(intersect);

            return new Range(intersect[0], intersect[intersect.length-1]);
        }

        private long rangeOfValues() {
            return max - min + 1;
        }

        @Override
        public String toString() {
            return "Range [min=" + min + ", max=" + max + "]";
        }
    }

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

    private Map<String, List<Param>> buildWorkflowsForPart2(List<String> data, int idx) {
        final Map<String, List<Param>> workflows = new HashMap<>();

        for (int i = 0; i < idx; i++) {

            final String line = data.get(i).replaceAll("\\{", " ").replaceAll("\\}", "");
            final String[] parts = line.split(" ");
            final String name = parts[0];
            final String[] functionsAndResults = parts[1].split(",");

            final List<Param> paramsList = new ArrayList<>();

            for (int j = 0; j < functionsAndResults.length; j++) {
                final String fr = functionsAndResults[j];
                if (isFunction(fr)) {
                    final String[] parts2 = fr.split(":");
                    final String test = parts2[0];
                    final String result = parts2[1];

                    final Operand op = test.charAt(1) == '>' ? Operand.GREATER_THAN : Operand.LESS_THAN;
                    final int testNumber = Integer.parseInt(test.substring(2, test.length()));

                    paramsList.add(new Param(WorkflowParams.FUNCTION, op, test.charAt(0), testNumber, result));
                } else {
                    paramsList.add(new Param(WorkflowParams.FUNCTION_REFERENCE, null, null, null, fr));
                }
            }

            workflows.put(name, paramsList);
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
                // function reference or A/R
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

    private long findApprovedCombos(Map<String, List<Param>> workflows,
        Range xRange, Range mRange, Range aRange, Range sRange, String workFlowName) {

            if ("R".equals(workFlowName)) {
                return 0;
            }

            if ("A".equals(workFlowName)) {
                return xRange.rangeOfValues() * mRange.rangeOfValues() * aRange.rangeOfValues() * sRange.rangeOfValues();
            }

            long r = 0;
            final Map<Character, Range> rangesMap = new HashMap<>();
            rangesMap.put('x', xRange);
            rangesMap.put('m', mRange);
            rangesMap.put('a', aRange);
            rangesMap.put('s', sRange);

            final List<Param> params = workflows.get(workFlowName);
            for (int i = 0; i < params.size(); i++) {
                final Param p = params.get(i);

                final Range x = rangesMap.get('x');
                final Range m = rangesMap.get('m');
                final Range a = rangesMap.get('a');
                final Range s = rangesMap.get('s');

                if (p.type() == WorkflowParams.FUNCTION_REFERENCE) {
                    r += findApprovedCombos(workflows, x, m, a, s, p.result());
                    continue;
                }
                
                final int testNumber = p.testNumber();
                final String result = p.result();
                final Range newRange;

                switch(p.xmas()) {
                    case 'x':
                        newRange = p.op == Operand.LESS_THAN ? x.setMax(testNumber - 1) : x.setMin(testNumber + 1);
                        r += findApprovedCombos(workflows, newRange, m, a, s, result);
                        break;
                    case 'm':
                        newRange = p.op == Operand.LESS_THAN ? m.setMax(testNumber - 1) : m.setMin(testNumber + 1);
                        r += findApprovedCombos(workflows, x, newRange, a, s, result);
                        break;
                    case 'a':
                        newRange = p.op == Operand.LESS_THAN ? a.setMax(testNumber - 1) : a.setMin(testNumber + 1);
                        r += findApprovedCombos(workflows, x, m, newRange, s, result);
                        break;
                    case 's':
                        newRange = p.op == Operand.LESS_THAN ? s.setMax(testNumber - 1) : s.setMin(testNumber + 1);
                        r += findApprovedCombos(workflows, x, m, a, newRange, result);
                        break;
                    default:
                        throw new RuntimeException("Invalid function character: " + p.xmas());
                }

                final Range inversion = newRange.invert(p.op);
                final Range currentRange = rangesMap.get(p.xmas());
                final Range intersection = currentRange.intersect(inversion);
                if (intersection == null) {
                    throw new RuntimeException("No intersection found!");
                }

                rangesMap.put(p.xmas(), intersection);
            }

            return r;
    }

    public long findDistinctNumberOfAcceptedCombinations(List<String> data) {
        final int idx = findEmptyLine(data);
        final Map<String, List<Param>> workflows = buildWorkflowsForPart2(data, idx);

        final Range xRange = new Range(1, 4000);
        final Range mRange = new Range(1, 4000);
        final Range aRange = new Range(1, 4000);
        final Range sRange = new Range(1, 4000);

        return findApprovedCombos(workflows, xRange, mRange, aRange, sRange, "in");
    }
    
    public static void main(String[] args) throws IOException {
        final List<String> data = Files.readAllLines(Path.of("src/main/resources/Day19.txt"));
        System.out.println("Part 1: " + new Day19().findSumOfAcceptedParts(data));
        System.out.println("Part 2: " + new Day19().findDistinctNumberOfAcceptedCombinations(data));
    }
}
