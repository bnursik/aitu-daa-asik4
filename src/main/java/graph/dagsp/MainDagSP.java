package graph.dagsp;

import common.metrics.Metrics;
import common.metrics.SimpleMetrics;
import graph.model.Graph;
import graph.topo.KahnTopoSort;
import graph.scc.CondensationGraphBuilder;
import graph.scc.TarjanSCC;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public final class MainDagSP {

    private static void usage() {
        System.err.println(
                "Usage: java graph.dagsp.MainDagSP --input data/graph.json [--src S] [--shortest dst] [--longest dst]");
        System.err.println("Notes:");
        System.err.println("  - If --src is omitted, uses JSON 'source' if available, else 0.");
        System.err.println("  - Computations run on the condensation DAG (SCC-compressed).");
    }

    public static void main(String[] args) {
        if (args.length < 2 || !"--input".equals(args[0])) {
            usage();
            System.exit(1);
        }
        Path input = Path.of(args[1]);

        Integer userSrc = null;
        Integer shortestDst = null;
        Integer longestDst = null;

        for (int i = 2; i < args.length; i++) {
            switch (args[i]) {
                case "--src":
                    if (i + 1 >= args.length) {
                        usage();
                        return;
                    }
                    userSrc = Integer.parseInt(args[++i]);
                    break;
                case "--shortest":
                    if (i + 1 >= args.length) {
                        usage();
                        return;
                    }
                    shortestDst = Integer.parseInt(args[++i]);
                    break;
                case "--longest":
                    if (i + 1 >= args.length) {
                        usage();
                        return;
                    }
                    longestDst = Integer.parseInt(args[++i]);
                    break;
                default:
                    usage();
                    return;
            }
        }

        Graph g = Graph.fromJson(input);

        // 1) SCC
        Metrics m = new SimpleMetrics();
        TarjanSCC tarjan = new TarjanSCC();
        m.reset();
        m.start();
        TarjanSCC.Result scc = tarjan.compute(g, m);
        m.stop();
        System.out.println("[SCC] count=" + scc.components.size()
                + " dfsVisits=" + m.getDfsVisits()
                + " dfsEdgeScans=" + m.getDfsEdgeScans()
                + " time(ns)=" + m.elapsedNanos());

        // 2) Condensation DAG
        CondensationGraphBuilder builder = new CondensationGraphBuilder();
        Graph dag = builder.build(g, scc.components.size(), scc.compId);
        int dagM = 0;
        for (int u = 0; u < dag.n; u++)
            dagM += dag.adj.get(u).size();
        System.out.println("[Condensation DAG] nodes=" + dag.n + " edges=" + dagM);

        // Map original source to its component
        int origSrc = (userSrc != null) ? userSrc : (g.source != null ? g.source : 0);
        if (origSrc < 0 || origSrc >= g.n) {
            System.err.println("Invalid --src (or JSON source).");
            System.exit(2);
        }
        int srcComp = scc.compId[origSrc];
        System.out.println("[Source] original=" + origSrc + " -> component=" + srcComp);

        // 3) Topological order on condensation DAG
        KahnTopoSort kahn = new KahnTopoSort();
        m.reset();
        m.start();
        List<Integer> topo = kahn.order(dag, m);
        m.stop();
        if (topo.isEmpty()) {
            System.err.println("Condensation graph not a DAG (should not happen).");
            System.exit(3);
        }
        System.out.println("[Topo] order=" + topo);
        System.out.println("[Topo] metrics: pushes=" + m.getKahnPushes()
                + " pops=" + m.getKahnPops()
                + " time(ns)=" + m.elapsedNanos());

        // 4a) Single-source shortest paths from srcComp
        if (shortestDst != null) {
            int dstComp = scc.compId[Math.max(0, Math.min(shortestDst, g.n - 1))];
            DagShortestPaths sp = new DagShortestPaths();
            m.reset();
            m.start();
            DagShortestPaths.Result res = sp.shortest(dag, srcComp, topo, m);
            m.stop();
            System.out.println("[DAG-SP shortest] relaxations=" + m.getRelaxations()
                    + " time(ns)=" + m.elapsedNanos());
            System.out.println("[DAG-SP shortest] dist (by component): " + Arrays.toString(res.dist));
            int[] path = DagShortestPaths.reconstructPath(srcComp, dstComp, res.parent);
            System.out.println("[DAG-SP shortest] path components: " + Arrays.toString(path));
            if (path.length > 0) {
                System.out.println("[DAG-SP shortest] expanded SCCs (per component in path):");
                for (int c : path) {
                    System.out.println("  C" + c + " -> " + scc.components.get(c));
                }
            }
        }

        // 4b) Longest (critical) path from srcComp
        if (longestDst != null) {
            int dstComp = scc.compId[Math.max(0, Math.min(longestDst, g.n - 1))];
            DagLongestPath lp = new DagLongestPath();
            DagLongestPath.Result cres = lp.longest(dag, srcComp, topo);
            int[] path = DagLongestPath.reconstructPath(srcComp, dstComp, cres.parent);
            int length = (path.length == 0) ? DagLongestPath.NEG_INF : cres.best[dstComp];
            System.out.println("[DAG longest] best (by component): " + Arrays.toString(cres.best));
            System.out.println("[DAG longest] critical length to dstComp=" + dstComp + " -> " + length);
            System.out.println("[DAG longest] path components: " + Arrays.toString(path));
            if (path.length > 0) {
                System.out.println("[DAG longest] expanded SCCs (per component in path):");
                for (int c : path) {
                    System.out.println("  C" + c + " -> " + scc.components.get(c));
                }
            }
        }

        if (shortestDst == null && longestDst == null) {
            System.out.println("(Hint) Add --shortest <dst> or --longest <dst> to compute paths.");
        }
    }
}
