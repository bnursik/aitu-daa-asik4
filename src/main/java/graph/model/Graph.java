package graph.model;

import common.io.JsonLoader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Directed adjacency-list graph. We keep it simple and explicit.
 */
public class Graph {
    public final int n;
    public final List<List<Edge>> adj; // adj[u] = list of outgoing edges from u
    public final boolean directed;

    public final Integer source;

    public Graph(int n, boolean directed) {
        this(n, directed, null);
    }

    public Graph(int n, boolean directed, Integer source) {
        this.n = n;
        this.directed = directed;
        this.source = source;
        this.adj = new ArrayList<>(n);
        for (int i = 0; i < n; i++)
            adj.add(new ArrayList<>());
    }

    public void addEdge(int u, int v, int w) {
        adj.get(u).add(new Edge(u, v, w));
    }

    /** Convenience loader that reads our dataset JSON format. */
    public static Graph fromJson(Path path) {
        return JsonLoader.loadGraph(path);
    }
}
