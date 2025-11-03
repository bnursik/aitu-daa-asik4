package graph.dagsp;

import common.metrics.Metrics;
import graph.model.Edge;
import graph.model.Graph;

import java.util.Arrays;
import java.util.List;

/** Single-source shortest paths on a DAG given a valid topological order. */
public final class DagShortestPaths {

    public static final int INF = 1_000_000_000;

    public static final class Result {
        public final int[] dist;
        public final int[] parent;

        public Result(int[] dist, int[] parent) {
            this.dist = dist;
            this.parent = parent;
        }
    }

    public Result shortest(Graph dag, int src, List<Integer> topo, Metrics m) {
        int n = dag.n;
        int[] dist = new int[n];
        int[] parent = new int[n];
        Arrays.fill(dist, INF);
        Arrays.fill(parent, -1);
        dist[src] = 0;

        // Relax edges in topological order
        for (int u : topo) {
            if (dist[u] == INF)
                continue;
            for (Edge e : dag.adj.get(u)) {
                int v = e.v;
                int cand = dist[u] + e.w;
                if (cand < dist[v]) {
                    dist[v] = cand;
                    parent[v] = u;
                    m.incRelaxations();
                }
            }
        }
        return new Result(dist, parent);
    }

    /**
     * Reconstruct path [src ... dst] using parent[], returns empty if unreachable.
     */
    public static int[] reconstructPath(int src, int dst, int[] parent) {
        if (src == dst && parent[dst] == -1)
            return new int[] { src };
        if (dst < 0 || dst >= parent.length)
            return new int[0];
        int[] stack = new int[parent.length];
        int k = 0;
        int cur = dst;
        while (cur != -1) {
            stack[k++] = cur;
            if (cur == src)
                break;
            cur = parent[cur];
        }
        if (k == 0 || stack[k - 1] != src)
            return new int[0];
        int[] path = new int[k];
        for (int i = 0; i < k; i++)
            path[i] = stack[k - 1 - i];
        return path;
    }
}
