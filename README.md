# Assignment 4 — Smart City / Smart Campus Scheduling (Java)

## Goal

Consolidate two course topics in one practical case (“Smart City / Smart Campus Scheduling”):

1. Strongly Connected Components (SCC) & Topological Ordering
2. Shortest Paths in DAGs

## Features

- SCC detection (Tarjan) + Condensation DAG builder
- Topological Sort (Kahn)
- DAG Shortest Path (DP on topo order)
- DAG Longest / Critical Path (max-DP)
- Metrics: dfsVisits, dfsEdgeScans, kahnPushes, kahnPops, relaxations, and execution time

## Weight Model

**Edge weights** (positive integers 1–9) are used for all graphs.

## Project Structure

```
src/main/java/
├── common/
│   ├── io/JsonLoader.java
│   └── metrics/{Metrics.java, SimpleMetrics.java}
├── graph/
│   ├── model/{Graph.java, Edge.java}
│   ├── scc/{TarjanSCC.java, CondensationGraphBuilder.java, MainSCC.java}
│   ├── topo/{KahnTopoSort.java, MainTopo.java}
│   └── dagsp/{DagShortestPaths.java, DagLongestPath.java, MainDagSP.java}
data/
├── small-*.json
├── medium-*.json
└── large-*.json
scripts/
└── generate_datasets.py
```

## Build & Run

```bash
mvn clean package
# SCC
java -cp target/asik4.jar graph.scc.MainSCC --input data/small-1-dag-sparse.json
# Topo + derived order
java -cp target/asik4.jar graph.topo.MainTopo --input data/medium-2-cyclic-sparse.json
# DAG-SP: auto-chooses original DAG vs condensation DAG
java -cp target/asik4.jar graph.dagsp.MainDagSP --input data/large-2-cyclic-medium.json --src 0 --shortest 7 --longest 7
```

## Dataset Generation

Run this to regenerate 9 test datasets:

```bash
python3 generate_datasets.py
```

It will create files in `data/` and print a table of:  
`| file | n | m | density | type | source |`

Example generated datasets:
| file | n | m | density | type | source |
|------|---|---|---------|------|--------|
| small-1-dag-sparse.json | 7 | 12 | sparse | DAG | 0 |
| small-2-cyclic-medium.json | 8 | 17 | medium | cyclic | 1 |
| ... | ... | ... | ... | ... | ... |

## Results (Example Format)

### SCC + Condensation

| dataset                | n   | m   | #SCC | dfsVisits | dfsEdgeScans | time(ns) |
| ---------------------- | --- | --- | ---- | --------- | ------------ | -------- |
| small-1-dag-sparse     | 7   | 12  | 7    | 7         | 12           | 35,000   |
| medium-2-cyclic-sparse | 14  | 24  | 5    | 14        | 30           | 62,000   |

### Topological Sort (Kahn)

| dataset            | compNodes | compEdges | pushes | pops | time(ns) |
| ------------------ | --------- | --------- | ------ | ---- | -------- |
| small-1-dag-sparse | 7         | 12        | 7      | 7    | 12,000   |

### DAG Shortest/Longest Paths

| dataset             | src | dst | shortestDist | relaxations | time(ns) | longestLen | pathType   |
| ------------------- | --- | --- | ------------ | ----------- | -------- | ---------- | ---------- |
| medium-1-dag-medium | 0   | 7   | 13           | 24          | 80,000   | 32         | components |

## Analysis

- **SCC**: denser graphs → more dfsEdgeScans; larger SCCs reduce condensation size.
- **Topo**: Kahn scales with edges; nearly linear for DAGs.
- **DAG-SP**: relaxations ≈ edges; topological order dominates setup time.
- **Practical**: compress cycles, then plan on DAG; longest path = critical path for scheduling.

## Tests

JUnit 5 tests under `src/test/java`:

- `SccTest.java` — validates Tarjan and condensation graph
- `TopoTest.java` — validates Kahn topo correctness
- `DagSpTest.java` — validates shortest and longest paths
- `DagSpEdgeCasesTest.java` — edge cases (unreachable, single node)

Run tests:

```bash
mvn test
```

## Code Quality & Comments

- Package structure follows assignment spec (`graph.scc`, `graph.topo`, `graph.dagsp`).
- Each class includes concise Javadoc.
- Metrics instrumentation implemented for all algorithms.
- Readable variable naming, modular functions, minimal inline comments.

## Conclusions

- SCC detection + condensation is essential for cyclic task graphs.
- Kahn’s topological order gives linear-time scheduling over DAGs.
- DP on topological order efficiently computes both shortest and critical paths.
- For Smart City / Smart Campus scheduling, this design supports mixed cyclic–acyclic dependencies.
