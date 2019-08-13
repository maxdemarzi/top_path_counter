package com.maxdemarzi;

import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.*;
import org.roaringbitmap.longlong.Roaring64NavigableMap;

public class TopPathEvaluator implements PathEvaluator {

    Roaring64NavigableMap endingNodes;

    public TopPathEvaluator(Roaring64NavigableMap endingNodes) {
        this.endingNodes = endingNodes;
    }
    @Override
    public Evaluation evaluate(Path path, BranchState branchState) {
        if (endingNodes.contains(path.endNode().getId())) {
            return Evaluation.INCLUDE_AND_PRUNE;
        }
        return Evaluation.EXCLUDE_AND_CONTINUE;
    }

    @Override
    public Evaluation evaluate(Path path) {
        return null;
    }
}
