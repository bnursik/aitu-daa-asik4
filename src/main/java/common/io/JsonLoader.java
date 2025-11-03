package common.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import graph.model.Graph;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Loads the assignment's graph JSON format.
 */
public final class JsonLoader {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonLoader() {
    }

    public static Graph loadGraph(Path path) {
        try {
            Dataset ds = MAPPER.readValue(path.toFile(), Dataset.class);
            boolean directed = ds.directed == null || ds.directed; // default true
            Graph g = new Graph(ds.n, directed, ds.source);
            if (ds.edges != null) {
                for (DEdge e : ds.edges) {
                    g.addEdge(e.u, e.v, e.w);
                }
            }
            return g;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load graph JSON: " + path, e);
        }
    }

    static class Dataset {
        public Boolean directed;
        public int n;
        public List<DEdge> edges;
        public Integer source;
        public String weight_model;
    }

    static class DEdge {
        public int u, v, w;
    }
}
