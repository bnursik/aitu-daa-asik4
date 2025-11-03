package common.metrics;

public interface Metrics {
    // SCC
    void incDfsVisits();

    void incDfsEdgeScans();

    long getDfsVisits();

    long getDfsEdgeScans();

    // Topo (Kahn)
    void incKahnPushes();

    void incKahnPops();

    long getKahnPushes();

    long getKahnPops();

    // DAG-SP
    void incRelaxations();

    long getRelaxations();

    // Timing
    void start();

    void stop();

    long elapsedNanos();

    void reset();
}
