package graph.dagsp;

import graph.model.Edge;
import graph.model.Graph;

import java.util.Arrays;
import java.util.List;

/**
 * Longest path on a DAG via max-DP over topological order (weights assumed >=
 * 1).
 */
public final class DagLongestPath {

    public static final int NEG_INF = -1_000_000_000;

    public static final class Result {
        public final int[] best;
        public final int[] parent;
        public final int src;

        public Result(int[] best, int[] parent, int src) {
            this.best = best;
            this.parent = parent;
            this.src = src;
        }
    }

    public Result longest(Graph dag, int src, List<Integer> topo) {
        int n = dag.n;
        int[] best = new int[n];
        int[] parent = new int[n];
        Arrays.fill(best, NEG_INF);
        Arrays.fill(parent, -1);
        best[src] = 0;

        for (int u : topo) {
            if (best[u] == NEG_INF)
                continue;
            for (Edge e : dag.adj.get(u)) {
                int v = e.v;
                int cand = best[u] + e.w;
                if (cand > best[v]) {
                    best[v] = cand;
                    parent[v] = u;
                }
            }
        }
        return new Result(best, parent, src);
    }

    /**
     * Reconstructs path from src to dst using parent[], or empty if unreachable.
     */
    public static int[] reconstructPath(int src, int dst, int[] parent) {
        if (src == dst && parent[dst] == -1)
            return new int[] { src };
        int[] stack = new int[parent.length];
        int k = 0, cur = dst;
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
