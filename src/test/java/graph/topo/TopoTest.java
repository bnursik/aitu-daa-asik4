package graph.topo;

import common.metrics.Metrics;
import common.metrics.SimpleMetrics;
import graph.model.Graph;
import graph.model.Edge;
import graph.scc.CondensationGraphBuilder;
import graph.scc.TarjanSCC;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Kahn topological sort on a classic diamond DAG: 0->{1,2}->{3}. */
public class TopoTest {

    private Graph dag() {
        Graph g = new Graph(4, true);
        g.addEdge(0, 1, 1);
        g.addEdge(0, 2, 1);
        g.addEdge(1, 3, 1);
        g.addEdge(2, 3, 1);
        return g;
    }

    @Test
    void topoOrderIsValid() {
        Graph g = dag();
        TarjanSCC tarjan = new TarjanSCC();
        TarjanSCC.Result scc = tarjan.compute(g, new SimpleMetrics());

        // For a pure DAG, condensation should be identical in structure (4 nodes)
        Graph cg = new CondensationGraphBuilder().build(g, scc.components.size(), scc.compId);
        assertEquals(4, cg.n);

        KahnTopoSort kahn = new KahnTopoSort();
        Metrics m = new SimpleMetrics();
        List<Integer> order = kahn.order(cg, m);
        assertEquals(4, order.size(), "All nodes must appear once");
        assertFalse(order.isEmpty(), "Non-empty topo order");

        // Validate topo: for every edge u->v, pos(u) < pos(v)
        Map<Integer, Integer> pos = new HashMap<>();
        for (int i = 0; i < order.size(); i++)
            pos.put(order.get(i), i);
        for (int u = 0; u < cg.n; u++) {
            for (Edge e : cg.adj.get(u)) {
                assertTrue(pos.get(u) < pos.get(e.v), "Topological violation at " + u + "->" + e.v);
            }
        }

        // Metrics sanity
        assertTrue(m.getKahnPushes() >= 1);
        assertTrue(m.getKahnPops() >= 1);
    }
}
