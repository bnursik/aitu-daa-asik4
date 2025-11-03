package graph.scc;

import common.metrics.Metrics;
import common.metrics.SimpleMetrics;
import graph.model.Graph;

import java.nio.file.Path;
import java.util.List;

public final class MainSCC {
    public static void main(String[] args) {
        if (args.length < 2 || !"--input".equals(args[0])) {
            System.err.println("Usage: java MainSCC --input data/your.json");
            System.exit(1);
        }
        Path input = Path.of(args[1]);
        Graph g = Graph.fromJson(input);

        Metrics m = new SimpleMetrics();
        TarjanSCC tarjan = new TarjanSCC();

        m.reset();
        m.start();
        TarjanSCC.Result res = tarjan.compute(g, m);
        m.stop();

        System.out.println("SCC count: " + res.components.size());
        for (int i = 0; i < res.components.size(); i++) {
            List<Integer> comp = res.components.get(i);
            System.out.println("  C" + i + " size=" + comp.size() + " -> " + comp);
        }
        System.out.println("Metrics: dfsVisits=" + m.getDfsVisits()
                + " dfsEdgeScans=" + m.getDfsEdgeScans()
                + " time(ns)=" + m.elapsedNanos());

        CondensationGraphBuilder builder = new CondensationGraphBuilder();
        Graph dag = builder.build(g, res.components.size(), res.compId);
        System.out.println("Condensation DAG nodes=" + dag.n);
        int mcount = 0;
        for (int u = 0; u < dag.n; u++)
            mcount += dag.adj.get(u).size();
        System.out.println("Condensation DAG edges=" + mcount);
    }
}
