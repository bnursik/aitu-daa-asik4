package graph.scc;

import common.metrics.Metrics;
import common.metrics.SimpleMetrics;
import graph.model.Graph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Tarjan SCC on a tiny mixed graph: cycle (0,1,2) + tail 3->4. */
public class SccTest {

    private Graph makeGraph() {
        Graph g = new Graph(5, true);
        // 3-cycle
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 0, 1);
        // tail
        g.addEdge(3, 4, 1);
        // connect tail to cycle
        g.addEdge(4, 0, 1);
        return g;
    }

    @Test
    void tarjanFindsExpectedComponents() {
        Graph g = makeGraph();
        Metrics m = new SimpleMetrics();
        TarjanSCC tarjan = new TarjanSCC();

        TarjanSCC.Result res = tarjan.compute(g, m);

        // Expect 3 SCCs: {0,1,2}, {3}, {4}
        assertEquals(3, res.components.size(), "SCC count");

        int c0 = res.compId[0], c1 = res.compId[1], c2 = res.compId[2];
        int c3 = res.compId[3], c4 = res.compId[4];

        assertEquals(c0, c1);
        assertEquals(c1, c2);
        assertNotEquals(c3, c4);
        assertNotEquals(c0, c3);
        assertNotEquals(c0, c4);

        // Condensation should have 3 nodes; edges: {4}->{cycle}, {3}->{4}
        CondensationGraphBuilder builder = new CondensationGraphBuilder();
        Graph dag = builder.build(g, res.components.size(), res.compId);
        assertEquals(3, dag.n);

        int mcount = 0;
        for (int u = 0; u < dag.n; u++)
            mcount += dag.adj.get(u).size();
        assertTrue(mcount >= 2, "Expected at least 2 edges in condensation");
    }
}
