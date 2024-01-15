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
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

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
            connected.put(parent, new HashSet<>(Arrays.asList(children)));
        }

        return connected;
    }

    private Graph<String, DefaultEdge> buildConnectedGraph(List<String> data) {
        final Graph<String, DefaultEdge> g = new SimpleGraph<>(DefaultEdge.class);
        for (final String line : data) {
            final String[] parts = line.split(": ");
            final String parent = parts[0];
            final String[] children = parts[1].split(" ");

            g.addVertex(parent);
            for (final String child : children) {
                g.addVertex(child);
                g.addEdge(parent, child);
            }
        }

        return g;
    }

    private Set<String> vertices(List<String> data) {
        final Set<String> vertices = new HashSet<>();
        for (final String line : data) {
            final String[] parts = line.split(": ");
            final String parent = parts[0];
            final String[] children = parts[1].split(" ");

            vertices.add(parent);
            for (final String child : children) {
                vertices.add(child);
            }
        }

        return vertices;
    }

    private List<List<String>> edges(List<String> data) {
        final List<List<String>> edges = new ArrayList<>();
        for (final String line : data) {
            final String[] parts = line.split(": ");
            final String parent = parts[0];
            final String[] children = parts[1].split(" ");

            for (final String child : children) {
                edges.add(new ArrayList<String>(List.of(parent, child)));
            }
        }

        return edges;
    }

    private Map<String, Set<String>> copyConnectedMap(Map<String, Set<String>> connected) {
        final Map<String, Set<String>> copy = new HashMap<>();
        connected
            .entrySet()
            .forEach((e) -> {
                copy.put(new String(e.getKey()), new HashSet<>(e.getValue().stream().toList()));
            });

        return copy;
    }

    private Set<String> getConnected(String v, Map<String, Set<String>> connected) {
        final Set<String> connectedNodes = new HashSet<>();
        if (connected.containsKey(v)) {
            connectedNodes.addAll(connected.get(v));
        }

        connectedNodes.addAll(
            connected.entrySet()
            .stream()
            .filter((e) -> e.getValue().contains(v))
            .map((e) -> e.getKey())
            .collect(Collectors.toSet())
        );

        return connectedNodes;
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
                for (final String child : getConnected(current, connected)) {
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
            final Set<String> connectedNodes = connected.get(edge.get(0));
            connectedNodes.remove(edge.get(1));
            allDisconnected.addAll(edge);
        }

        final Set<Set<String>> paths = new HashSet<>();

        for (final List<String> edge : edges) {
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

    private Map<String, Set<String>> contract(Set<String> vertices, List<List<String>> edges) {
        final Random rand = new Random();
        final Map<String, Set<String>> verticesGroups = new HashMap<>();
        vertices.forEach((v) -> {
            verticesGroups.put(v, new HashSet<>(List.of(v)));
        });

        while (verticesGroups.size() > 2) {
            final List<String> edge = edges.get(rand.nextInt(edges.size()));
            final String from = edge.get(0);
            final String to = edge.get(1);

            final Set<String> fromGroup = verticesGroups.get(from);
            final Set<String> toGroup = verticesGroups.get(to);
            fromGroup.addAll(toGroup);

            verticesGroups.remove(to);

            edges.removeIf((e) -> (e.get(0).equals(to) && e.get(1).equals(from)) || (e.get(0).equals(from) && e.get(1).equals(to)));
            
            edges
                .forEach((e) -> {
                    if (e.get(0).equals(to)) {
                        e.set(0, from);
                    }

                    if (e.get(1).equals(to)) {
                        e.set(1, from);
                    }
                });
        }

        return verticesGroups;
    }

    // Naive solution which checks all 3 cut pair combinations
    // works for the example but will take a very long time for the actual input
    public int findProductOfDisconnectedComponentsBruteForce(List<String> data) {
        final Map<String, Set<String>> connected = buildConnectedMap(data);
        final List<List<String>> edges = edges(data);

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
     * {@link https://en.wikipedia.org/wiki/Stoer%E2%80%93Wagner_algorithm}
     * <p>
     * Uses an open source lib ({@link https://jgrapht.org/}) to build a graph
     * and perform a min cut (using the above algorithm).
     * <p>
     * The {@code minCut()} method returns the set of vertices on one half
     * of the min cut. Simply substract this value from the total number of
     * vertices to obtain the size of the other half.
     * <p>
     * Multiply these values together, job's a good'un
     */
    public int findProductOfDisconnectedComponents(List<String> data) {
        final Graph<String, DefaultEdge> graph = buildConnectedGraph(data);
        final StoerWagnerMinimumCut<String, DefaultEdge> swMinCut = new StoerWagnerMinimumCut<>(graph);
        final int minCut = swMinCut.minCut().size();
        return minCut * (graph.vertexSet().size() - minCut);
    }

    /**
     * Used Graphviz ({@link https://graphviz.org/}) to build a visual representation of
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

    /**
     * Implementation of Karger's algorithm ({@link https://en.wikipedia.org/wiki/Karger%27s_algorithm})
     * Non-deterministic algorithm as it takes random edges until we find a selection of grouped
     * edges and nodes that fit our constraint (2 separate graphs/groups, 3 edges to cut). Once
     * our constraint is met, we have our answer.
     */
    public int findProductOfDisconnectedComponentsKarger(List<String> data) {
        Set<String> vertices = vertices(data);

        while (true) {
            final List<List<String>> edges = edges(data);
            vertices = vertices.stream().collect(Collectors.toSet());

            final Map<String, Set<String>> groups = contract(vertices, edges);

            if (groups.values().stream().allMatch((v) -> v.size() > 1) && edges.size() == 3) {
                return groups.values().stream().mapToInt((v) -> v.size()).reduce(1, (a, b) -> a * b);
            }
        }
    }
    
    public static void main(String[] args) throws IOException {
        final List<String> data = Files.readAllLines(Path.of("src/main/resources/Day25.txt"));
        System.out.println("Part 1: " + new Day25().findProductOfDisconnectedComponents(data));
    }
}
