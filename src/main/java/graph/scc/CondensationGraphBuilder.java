package graph.scc;

import graph.model.Edge;
import graph.model.Graph;

import java.util.HashSet;
import java.util.Set;

/**
 * Builds condensation DAG: each SCC -> single node; edges between components.
 */
public final class CondensationGraphBuilder {

    public Graph build(Graph g, int compCount, int[] compId) {
        Graph dag = new Graph(compCount, true);
        Set<Long> seen = new HashSet<>();
        for (int u = 0; u < g.n; u++) {
            int cu = compId[u];
            for (Edge e : g.adj.get(u)) {
                int cv = compId[e.v];
                if (cu == cv)
                    continue;
                long key = (((long) cu) << 32) ^ (cv & 0xffffffffL);
                if (seen.add(key)) {
                    dag.addEdge(cu, cv, 1);
                }
            }
        }
        return dag;
    }
}
