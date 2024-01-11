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
            final String parent = parts[0];
            final String[] children = parts[1].split(" ");
            final Set<String> components = connected.get(parent);
            if (components != null) {
                for (final String child : children) {
                    components.add(child);
                }
            } else {
                connected.put(parent, new HashSet<>(Arrays.asList(children)));
            }

            for (final String child : children) {
                final Set<String> childSet = connected.get(child);
                if (childSet != null) {
                    childSet.add(parent);
                } else {
                    connected.put(child, new HashSet<>(List.of(parent)));
                }
            }
        }

        return connected;
    }

    private Graph<String, DefaultEdge> buildConnectedGraph(List<String> data) {
        final Graph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        for (final String line : data) {
            final String[] parts = line.split(": ");
            final String parent = parts[0];
            final String[] children = parts[1].split(" ");

            if (!g.vertexSet().contains(parent)) {
                g.addVertex(parent);
            }

            for (final String child : children) {
                if (!g.vertexSet().contains(child)) {
                    g.addVertex(child);
                }
                g.addEdge(parent, child);
            }
        }

        return g;
    }

    private List<List<String>> collectEdges(Map<String, Set<String>> connected) {
        final List<List<String>> edges = new ArrayList<>();
        connected
            .entrySet()
            .forEach((e) -> {
                for (final String child : e.getValue()) {
                    edges.add(new ArrayList<String>(List.of(e.getKey(), child)));
                }
            });
        
        return edges;
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

    private Set<Set<String>> checkAllComponentsAreDisconnected(Map<String, Set<String>> connected, List<List<String>> edges) {

        final List<String> allDisconnected = new ArrayList<>();
        for (final List<String> edge : edges) {
            final Set<String> components0 = connected.get(edge.get(0));
            components0.remove(edge.get(1));

            final Set<String> components1 = connected.get(edge.get(1));
            components1.remove(edge.get(0));

            allDisconnected.addAll(edge);
        }

        final Set<Set<String>> paths = new HashSet<>();

        for (List<String> edge : edges) {
            for (final String component : edge) {
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
        final List<List<String>> edges = collectEdges(connected);

        for (int i = 0; i < edges.size() - 2; i++) {
            for (int j = i + 1; j < edges.size() - 1; j++) {
                for (int k = j + 1; k < edges.size(); k++) {
                    final Map<String, Set<String>> copy = copyConnectedMap(connected);
                    final List<String> edge0 = edges.get(i);
                    final List<String> edge1 = edges.get(j);
                    final List<String> edge2 = edges.get(k);

                    final Set<Set<String>> disconnectedPaths = checkAllComponentsAreDisconnected(copy, List.of(edge0, edge1, edge2));
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
     * The {@code minCut()} method returns the set of vertices on one half
     * of the min cut. Simply substract this value from the total number of
     * vertices to obtain the size of the other half.
     * 
     * Multiply these values together, job's a good'un
     */
    public int findProductOfDisconnectedComponents(List<String> data) {
        final Graph<String, DefaultEdge> graph = buildConnectedGraph(data);
        final StoerWagnerMinimumCut<String, DefaultEdge> swMinCut = new StoerWagnerMinimumCut<>(graph);
        final int minCut = swMinCut.minCut().size();
        return minCut * (graph.vertexSet().size() - minCut);
    }

    /**
     * Used Graphviz (https://graphviz.org/) to build a visual representation of
     * the network. From this, we can easily identify the 3 edges which need to
     * be severed in order to form 2 separate graphs.
     */
    public int findProductOfDisconnectedComponentsGraphViz(List<String> data) {
        final Map<String, Set<String>> connected = buildConnectedMap(data);
        // the three edges specific to my input
        final List<String> edge0 = List.of("xsl", "tpb");
        final List<String> edge1 = List.of("bmx", "zlv");
        final List<String> edge2 = List.of("qpg", "lrd");
        final Set<Set<String>> disconnectedPaths = checkAllComponentsAreDisconnected(connected, List.of(edge0, edge1, edge2));
        if (disconnectedPaths != null) {
            return disconnectedPaths.stream().mapToInt((s) -> s.size()).reduce(1, (a, b) -> a * b);
        }

        throw new RuntimeException("No solution found!");
    }
    
    public static void main(String[] args) throws IOException {
        final List<String> data = Files.readAllLines(Path.of("src/main/resources/Day25.txt"));
        System.out.println("Part 1: " + new Day25().findProductOfDisconnectedComponents(data));
    }
}
