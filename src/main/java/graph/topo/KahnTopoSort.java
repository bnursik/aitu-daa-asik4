package graph.topo;

import common.metrics.Metrics;
import graph.model.Edge;
import graph.model.Graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Kahn's algorithm for topological sorting (BFS over in-degrees). Returns a
 * valid order or empty if cyclic.
 */

public final class KahnTopoSort {

    public List<Integer> order(Graph dag, Metrics m) {
        int n = dag.n;
        int[] indeg = new int[n];
        for (int u = 0; u < n; u++) {
            for (Edge e : dag.adj.get(u))
                indeg[e.v]++;
        }

        Deque<Integer> q = new ArrayDeque<>();
        for (int v = 0; v < n; v++) {
            if (indeg[v] == 0) {
                q.add(v);
                m.incKahnPushes();
            }
        }

        List<Integer> topo = new ArrayList<>(n);
        while (!q.isEmpty()) {
            int u = q.removeFirst();
            m.incKahnPops();
            topo.add(u);
            for (Edge e : dag.adj.get(u)) {
                if (--indeg[e.v] == 0) {
                    q.addLast(e.v);
                    m.incKahnPushes();
                }
            }
        }
        if (topo.size() != n)
            return List.of(); // not a DAG
        return topo;
    }
}
