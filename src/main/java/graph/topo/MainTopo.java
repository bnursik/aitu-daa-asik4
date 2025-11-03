package graph.topo;

import common.metrics.Metrics;
import common.metrics.SimpleMetrics;
import graph.model.Graph;
import graph.scc.CondensationGraphBuilder;
import graph.scc.TarjanSCC;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * CLI:
 * java graph.topo.MainTopo --input data/your.json
 *
 * Prints:
 * - SCC summary
 * - Condensation DAG size
 * - Component topological order (Kahn)
 * - Derived original task order (by component order, stable within each SCC
 * list)
 * - Metrics
 */
public final class MainTopo {
    public static void main(String[] args) {
        if (args.length < 2 || !"--input".equals(args[0])) {
            System.err.println("Usage: java graph.topo.MainTopo --input data/your.json");
            System.exit(1);
        }
        Path input = Path.of(args[1]);
        Graph g = Graph.fromJson(input);

        Metrics m = new SimpleMetrics();

        // 1) SCC + condensation
        TarjanSCC tarjan = new TarjanSCC();
        m.reset();
        m.start();
        TarjanSCC.Result sccRes = tarjan.compute(g, m);
        m.stop();
        System.out.println("[SCC] count=" + sccRes.components.size()
                + " dfsVisits=" + m.getDfsVisits()
                + " dfsEdgeScans=" + m.getDfsEdgeScans()
                + " time(ns)=" + m.elapsedNanos());

        CondensationGraphBuilder builder = new CondensationGraphBuilder();
        Graph dag = builder.build(g, sccRes.components.size(), sccRes.compId);

        int dagEdges = 0;
        for (int u = 0; u < dag.n; u++)
            dagEdges += dag.adj.get(u).size();
        System.out.println("[Condensation DAG] nodes=" + dag.n + " edges=" + dagEdges);

        // 2) Kahn topological sort on condensation DAG
        KahnTopoSort kahn = new KahnTopoSort();
        m.reset();
        m.start();
        List<Integer> compTopo = kahn.order(dag, m);
        m.stop();
        if (compTopo.isEmpty()) {
            System.out.println("[Topo] graph is not a DAG (unexpected for condensation)");
            System.exit(2);
        }
        System.out.println("[Topo] component order: " + compTopo);
        System.out.println("[Topo] metrics: pushes=" + m.getKahnPushes()
                + " pops=" + m.getKahnPops()
                + " time(ns)=" + m.elapsedNanos());

        // 3) Derived original order: expand components in topo order
        List<Integer> derived = deriveOriginalOrder(compTopo, sccRes.components);
        System.out.println("[Derived original task order] " + derived);
    }

    /**
     * Expand components in topological component order.
     * Inside each SCC we keep the component list order (stable and deterministic
     * from Tarjan).
     */
    static List<Integer> deriveOriginalOrder(List<Integer> compTopo, List<List<Integer>> components) {
        List<Integer> out = new ArrayList<>();
        for (int c : compTopo) {
            out.addAll(components.get(c));
        }
        return out;
    }
}
