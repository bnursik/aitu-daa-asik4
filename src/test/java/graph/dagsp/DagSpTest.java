package graph.dagsp;

import common.metrics.SimpleMetrics;
import graph.model.Graph;
import graph.topo.KahnTopoSort;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Weighted DAG:
 * 0->1(2), 0->2(5), 1->2(1), 1->3(3), 2->3(1)
 * Shortest 0->3: 0-1-2-3 cost 4
 * Longest 0->3: 0-2-3 cost 6
 */
public class DagSpTest {

    private Graph weightedDag() {
        Graph g = new Graph(4, true);
        g.addEdge(0, 1, 2);
        g.addEdge(0, 2, 5);
        g.addEdge(1, 2, 1);
        g.addEdge(1, 3, 3);
        g.addEdge(2, 3, 1);
        return g;
    }

    @Test
    void shortestAndLongestPathsMatchExpectations() {
        Graph g = weightedDag();

        // Topo order on original graph (it is a DAG)
        KahnTopoSort kahn = new KahnTopoSort();
        List<Integer> topo = kahn.order(g, new SimpleMetrics());
        assertEquals(4, topo.size());

        // Shortest from 0 to 3 on original DAG (preserves weights)
        DagShortestPaths sp = new DagShortestPaths();
        DagShortestPaths.Result r = sp.shortest(g, 0, topo, new SimpleMetrics());
        assertEquals(4, r.dist[3], "Shortest distance 0->3 should be 4");
        int[] shortestPath = DagShortestPaths.reconstructPath(0, 3, r.parent);
        assertArrayEquals(new int[] { 0, 1, 2, 3 }, shortestPath);

        // Longest (critical) from 0 to 3 on original DAG (max-DP)
        DagLongestPath lp = new DagLongestPath();
        DagLongestPath.Result L = lp.longest(g, 0, topo);
        assertEquals(6, L.best[3], "Longest value 0->3 should be 6");
        int[] longestPath = DagLongestPath.reconstructPath(0, 3, L.parent);
        assertTrue(Arrays.equals(new int[] { 0, 2, 3 }, longestPath));
    }
}
