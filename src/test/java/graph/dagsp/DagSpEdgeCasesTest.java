package graph.dagsp;

import common.metrics.SimpleMetrics;
import graph.model.Graph;
import graph.topo.KahnTopoSort;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class DagSpEdgeCasesTest {
    @Test
    void unreachableDestinationGivesInfAndEmptyPath() {
        Graph g = new Graph(3, true);
        g.addEdge(0, 1, 2); // node 2 is disconnected
        List<Integer> topo = new KahnTopoSort().order(g, new SimpleMetrics());
        DagShortestPaths.Result r = new DagShortestPaths().shortest(g, 0, topo, new SimpleMetrics());
        assertEquals(DagShortestPaths.INF, r.dist[2]);
        assertArrayEquals(new int[0], DagShortestPaths.reconstructPath(0, 2, r.parent));
    }

    @Test
    void singleNodeGraph() {
        Graph g = new Graph(1, true);
        List<Integer> topo = new KahnTopoSort().order(g, new SimpleMetrics());
        assertEquals(1, topo.size());
        DagShortestPaths.Result r = new DagShortestPaths().shortest(g, 0, topo, new SimpleMetrics());
        assertEquals(0, r.dist[0]);
    }
}
