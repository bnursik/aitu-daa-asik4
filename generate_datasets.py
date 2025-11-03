#!/usr/bin/env python3
"""
Generate 9 JSON graph datasets for SCC/Topo/DAG-SP:
- 3 small  (6–10 nodes): mix of pure DAG and 1–2 cycles
- 3 medium (10–20 nodes): mixed with several SCCs
- 3 large  (20–50 nodes): performance & timing (varied density + SCCs)

Output files are written to ./data/.
Each file format:
{
  "directed": true,
  "n": N,
  "edges": [{"u":..., "v":..., "w":...}, ...],
  "source": s,
  "weight_model": "edge"
}
"""

import json
import os
import random
from dataclasses import dataclass
from typing import List, Tuple, Set

# ---------- helpers ----------

def ensure_dir(path: str) -> None:
    os.makedirs(path, exist_ok=True)

def write_json(path: str, payload: dict) -> None:
    with open(path, "w", encoding="utf-8") as f:
        json.dump(payload, f, indent=2, ensure_ascii=False)

def rng(seed: int) -> random.Random:
    r = random.Random()
    r.seed(seed)
    return r

@dataclass
class Spec:
    name: str
    n: int
    density: str  # "sparse" | "medium" | "dense"
    cyclic: bool
    scc_hint: int  # approximate number of SCCs to try forming when cyclic

# Map density label to edge probability multiplier for DAG backbone
DENSITY_P = {
    "sparse": 0.10,
    "medium": 0.20,
    "dense": 0.35,
}

def gen_dag_edges(n: int, p: float, R: random.Random) -> Set[Tuple[int, int]]:
    """
    Generate an acyclic backbone by sampling edges from a random topological order.
    """
    order = list(range(n))
    R.shuffle(order)
    pos = {v: i for i, v in enumerate(order)}
    edges: Set[Tuple[int, int]] = set()
    for i in range(n):
        for j in range(i + 1, n):
            u, v = order[i], order[j]
            if R.random() < p:
                edges.add((u, v))
    # Guarantee weak connectivity-ish by adding at least a chain
    for i in range(n - 1):
        u, v = order[i], order[i + 1]
        edges.add((u, v))
    return edges

def add_cycles(edges: Set[Tuple[int, int]], n: int, scc_hint: int, R: random.Random) -> None:
    """
    Inject scc_hint small cycles by adding a few back-edges inside random windows.
    """
    if scc_hint <= 0:
        return
    for _ in range(scc_hint):
        # choose a window size 3..5 if possible
        w = min(n, R.randint(3, 5))
        start = R.randint(0, max(0, n - w))
        nodes = list(range(start, start + w))
        R.shuffle(nodes)
        # Make a simple cycle within these nodes
        for i in range(len(nodes)):
            u = nodes[i]
            v = nodes[(i + 1) % len(nodes)]
            edges.add((u, v))
        # Random extra chord
        if len(nodes) >= 4 and R.random() < 0.6:
            a, b = R.sample(nodes, 2)
            edges.add((a, b))

def assign_weights(edges: Set[Tuple[int, int]], R: random.Random) -> List[Tuple[int, int, int]]:
    """
    Positive integer weights in a modest range (1..9), suitable for DAG-SP.
    """
    return [(u, v, R.randint(1, 9)) for (u, v) in edges]

def build_payload(n: int, edges_w: List[Tuple[int, int, int]], source: int) -> dict:
    return {
        "directed": True,
        "n": n,
        "edges": [{"u": u, "v": v, "w": w} for (u, v, w) in sorted(edges_w)],
        "source": source,
        "weight_model": "edge",
    }

def generate_one(spec: Spec, seed: int) -> dict:
    R = rng(seed)
    p = DENSITY_P[spec.density]
    edges = gen_dag_edges(spec.n, p, R)
    if spec.cyclic:
        # try to form several SCCs by injecting cycles
        add_cycles(edges, spec.n, spec.scc_hint, R)
    edges_w = assign_weights(edges, R)
    # Pick a reasonable source: if cyclic, choose a random node; else prefer a node with small indegree
    indeg = [0] * spec.n
    for u, v, _ in edges_w:
        indeg[v] += 1
    candidate_sources = [i for i in range(spec.n) if indeg[i] == 0]
    src = R.choice(candidate_sources) if candidate_sources else R.randint(0, spec.n - 1)
    return build_payload(spec.n, edges_w, src)

# ---------- plan: 9 datasets ----------

def plan_specs(R: random.Random) -> List[Spec]:
    # Small: 6–10 nodes
    small_ns = [R.randint(6, 10) for _ in range(3)]
    # Medium: 10–20 nodes
    medium_ns = [R.randint(10, 20) for _ in range(3)]
    # Large: 20–50 nodes
    large_ns = [R.randint(20, 50) for _ in range(3)]

    specs: List[Spec] = []
    # Small variants: one pure DAG (sparse), two with 1–2 SCCs
    specs += [
        Spec("small-1-dag-sparse", small_ns[0], "sparse", False, 0),
        Spec("small-2-cyclic-medium", small_ns[1], "medium", True, 1),
        Spec("small-3-cyclic-dense", small_ns[2], "dense", True, 2),
    ]
    # Medium: mixed densities, several SCCs in two of them
    specs += [
        Spec("medium-1-dag-medium", medium_ns[0], "medium", False, 0),
        Spec("medium-2-cyclic-sparse", medium_ns[1], "sparse", True, 2),
        Spec("medium-3-cyclic-dense", medium_ns[2], "dense", True, 3),
    ]
    # Large: timing stress, vary density and SCC count
    specs += [
        Spec("large-1-dag-dense", large_ns[0], "dense", False, 0),
        Spec("large-2-cyclic-medium", large_ns[1], "medium", True, 4),
        Spec("large-3-cyclic-dense", large_ns[2], "dense", True, 5),
    ]
    return specs

def main():
    base_seed = 20251103  # deterministic across runs
    R = rng(base_seed)

    out_dir = os.path.join(".", "data")
    ensure_dir(out_dir)

    specs = plan_specs(R)
    rows = []
    for i, spec in enumerate(specs, start=1):
        seed = base_seed + i
        payload = generate_one(spec, seed)
        path = os.path.join(out_dir, f"{spec.name}.json")
        write_json(path, payload)

        n = spec.n
        m = len(payload["edges"])
        cyclic = spec.cyclic
        rows.append((spec.name, n, m, spec.density, "cyclic" if cyclic else "DAG", payload["source"]))

    # Print a compact summary for your README report copy-paste
    print("Generated datasets (copy this table to README):")
    print("| file | n | m | density | type | source |")
    print("|------|---|---|---------|------|--------|")
    for name, n, m, density, typ, src in rows:
        print(f"| {name}.json | {n} | {m} | {density} | {typ} | {src} |")

if __name__ == "__main__":
    main()
