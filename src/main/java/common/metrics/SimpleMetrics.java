package common.metrics;

public class SimpleMetrics implements Metrics {
    private long dfsVisits, dfsEdgeScans;
    private long kahnPushes, kahnPops;
    private long relaxations;
    private long t0, t1;

    @Override
    public void incDfsVisits() {
        dfsVisits++;
    }

    @Override
    public void incDfsEdgeScans() {
        dfsEdgeScans++;
    }

    @Override
    public long getDfsVisits() {
        return dfsVisits;
    }

    @Override
    public long getDfsEdgeScans() {
        return dfsEdgeScans;
    }

    @Override
    public void incKahnPushes() {
        kahnPushes++;
    }

    @Override
    public void incKahnPops() {
        kahnPops++;
    }

    @Override
    public long getKahnPushes() {
        return kahnPushes;
    }

    @Override
    public long getKahnPops() {
        return kahnPops;
    }

    @Override
    public void incRelaxations() {
        relaxations++;
    }

    @Override
    public long getRelaxations() {
        return relaxations;
    }

    @Override
    public void start() {
        t0 = System.nanoTime();
    }

    @Override
    public void stop() {
        t1 = System.nanoTime();
    }

    @Override
    public long elapsedNanos() {
        return t1 - t0;
    }

    @Override
    public void reset() {
        dfsVisits = dfsEdgeScans = kahnPushes = kahnPops = relaxations = 0L;
        t0 = t1 = 0L;
    }
}
