package graph.scc;

import common.metrics.Metrics;
import graph.model.Edge;
import graph.model.Graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Tarjan's algorithm for strongly connected components (SCC) in a directed
 * graph.
 * Usage: new TarjanSCC().compute(graph, metrics) -> components + compId map.
 */

public class TarjanSCC {

    public static final class Result {
        public final List<List<Integer>> components; // each SCC as list of vertices
        public final int[] compId; // compId[v] = component index [0..k-1]

        public Result(List<List<Integer>> comps, int[] id) {
            this.components = comps;
            this.compId = id;
        }
    }

    public Result compute(Graph g, Metrics m) {
        int n = g.n;
        int[] idx = new int[n]; // discovery time (0 = unvisited)
        int[] low = new int[n];
        boolean[] onSt = new boolean[n];
        Deque<Integer> st = new ArrayDeque<>();
        int[] time = { 0 };
        List<List<Integer>> comps = new ArrayList<>();
        int[] compId = new int[n];
        for (int i = 0; i < n; i++)
            compId[i] = -1;

        for (int v = 0; v < n; v++) {
            if (idx[v] == 0)
                dfs(v, g, m, idx, low, onSt, st, time, comps, compId);
        }
        return new Result(comps, compId);
    }

    private void dfs(int u, Graph g, Metrics m,
            int[] idx, int[] low, boolean[] onSt, Deque<Integer> st, int[] time,
            List<List<Integer>> comps, int[] compId) {
        m.incDfsVisits();
        idx[u] = low[u] = ++time[0];
        st.push(u);
        onSt[u] = true;

        for (Edge e : g.adj.get(u)) {
            m.incDfsEdgeScans();
            int v = e.v;
            if (idx[v] == 0) {
                dfs(v, g, m, idx, low, onSt, st, time, comps, compId);
                low[u] = Math.min(low[u], low[v]);
            } else if (onSt[v]) {
                low[u] = Math.min(low[u], idx[v]);
            }
        }

        if (low[u] == idx[u]) {
            List<Integer> comp = new ArrayList<>();
            while (true) {
                int v = st.pop();
                onSt[v] = false;
                compId[v] = comps.size();
                comp.add(v);
                if (v == u)
                    break;
            }
            comps.add(comp);
        }
    }
}
