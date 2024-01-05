package com.github.aoc2023.dogunyoye;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.jgrapht.Graph;
import org.jgrapht.alg.StoerWagnerMinimumCut;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

public class Day25 {

    private Map<String, Set<String>> buildConnectedMap(List<String> data) {
        final Map<String, Set<String>> connected = new HashMap<>();
        for (final String line : data) {
            final String[] parts = line.split(": ");
            final String key = parts[0];
            final String[] children = parts[1].split(" ");
            final Set<String> components = connected.get(key);
            if (components != null) {
                for (final String child : children) {
                    components.add(child);
                }
            } else {
                connected.put(key, new HashSet<>(Arrays.asList(children)));
            }

            for (final String child : children) {
                final Set<String> childSet = connected.get(child);
                if (childSet != null) {
                    childSet.add(key);
                } else {
                    connected.put(child, new HashSet<>(List.of(key)));
                }
            }
        }

        return connected;
    }

    private Graph<String, DefaultEdge> buildConnectedGraph(List<String> data) {
        final Graph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        for (final String line : data) {
            final String[] parts = line.split(": ");
            final String key = parts[0];
            final String[] children = parts[1].split(" ");

            if (!g.vertexSet().contains(key)) {
                g.addVertex(key);
            }

            for (final String child : children) {
                if (!g.vertexSet().contains(child)) {
                    g.addVertex(child);
                }
                g.addEdge(key, child);
            }
        }

        return g;
    }

    private List<List<String>> buildConnectedPairs(Map<String, Set<String>> connected) {
        final List<List<String>> pairs = new ArrayList<>();
        connected
            .entrySet()
            .forEach((e) -> {
                for (final String child : e.getValue()) {
                    pairs.add(new ArrayList<String>(List.of(e.getKey(), child)));
                }
            });
        
        return pairs;
    }

    private Map<String, Set<String>> copyConnectedMap(Map<String, Set<String>> connected) {
        final Map<String, Set<String>> copy = new HashMap<>();
        connected
            .entrySet()
            .forEach((e) -> {
                copy.put(e.getKey(), new HashSet<>(e.getValue().stream().toList()));
            });

        return copy;
    }

    private Set<String> dfs(Map<String, Set<String>> connected, String start) {
        final Stack<String> stack = new Stack<>();
        final Set<String> visited = new HashSet<>();

        final Set<String> path = new HashSet<>();
        path.add(start);

        stack.add(start);
        while(!stack.isEmpty()) {
            final String current = stack.pop();
            if (!visited.contains(current)) {
                visited.add(current);
                for (final String child : connected.get(current)) {
                    stack.push(child);
                    path.add(child);
                }
            }
        }

        return path;
    }

    private Set<Set<String>> checkAllComponentsAreDisconnected(Map<String, Set<String>> connected, List<List<String>> pairs) {

        final List<String> allDisconnected = new ArrayList<>();
        for (List<String> pair : pairs) {
            final Set<String> components0 = connected.get(pair.get(0));
            components0.remove(pair.get(1));

            final Set<String> components1 = connected.get(pair.get(1));
            components1.remove(pair.get(0));

            allDisconnected.addAll(pair);
        }

        final Set<Set<String>> paths = new HashSet<>();

        for (List<String> pair : pairs) {
            for (final String component : pair) {
                final Set<String> path = dfs(connected, component);
                paths.add(path);
                if (path.containsAll(allDisconnected)) {
                    return null;
                }
            }
        }

        return paths;
    }

    // Naive solution which checks all 3 cut pair combinations
    // works for the example but will take a very long time for the actual input
    public int findProductOfDisconnectedComponentsNaive(List<String> data) {
        final Map<String, Set<String>> connected = buildConnectedMap(data);
        final List<List<String>> pairs = buildConnectedPairs(connected);

        for (int i = 0; i < pairs.size() - 2; i++) {
            for (int j = i + 1; j < pairs.size() - 1; j++) {
                for (int k = j + 1; k < pairs.size(); k++) {
                    final Map<String, Set<String>> copy = copyConnectedMap(connected);
                    final List<String> pair0 = pairs.get(i);
                    final List<String> pair1 = pairs.get(j);
                    final List<String> pair2 = pairs.get(k);

                    final Set<Set<String>> disconnectedPaths = checkAllComponentsAreDisconnected(copy, List.of(pair0, pair1, pair2));
                    if (disconnectedPaths != null) {
                        return disconnectedPaths.stream().mapToInt((s) -> s.size()).reduce(1, (a, b) -> a * b);
                    }
                }
            }
        }

        throw new RuntimeException("No solution found!");
    }

    /**
     * https://en.wikipedia.org/wiki/Stoer%E2%80%93Wagner_algorithm
     * 
     * Uses an open source lib (https://jgrapht.org/) to build a graph
     * and perform a min cut (using the above algorithm).
     * 
     * The `minCut()` method returns the size of one half of the min cut.
     * Simply substract this from the number of nodes (vertices) to obtain
     * the size of the other half.
     * 
     * Multiply these values together, job's a good'un
     */
    public int findProductOfDisconnectedComponents(List<String> data) {
        final Graph<String, DefaultEdge> graph = buildConnectedGraph(data);
        final StoerWagnerMinimumCut<String, DefaultEdge> swMinCut = new StoerWagnerMinimumCut<>(graph);
        final int minCut = swMinCut.minCut().size();
        return minCut * (graph.vertexSet().size() - minCut);
    }
    
    public static void main(String[] args) throws IOException {
        final List<String> data = Files.readAllLines(Path.of("src/main/resources/Day25.txt"));
        System.out.println("Part 1: " + new Day25().findProductOfDisconnectedComponents(data));
    }
}
