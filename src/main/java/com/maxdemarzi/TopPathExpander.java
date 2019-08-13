package com.maxdemarzi;

import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;

import java.util.Collections;

import static com.maxdemarzi.Procedures.LINKED_TO;

public class TopPathExpander implements PathExpander {

    private int limit;

    public TopPathExpander(Long limit) {
        this.limit = limit.intValue();
    }

    @Override
    public Iterable<Relationship> expand(Path path, BranchState branchState) {
        if(path.length() >= limit) {
            return Collections.emptyList();
        }
        return path.endNode().getRelationships(LINKED_TO, Direction.OUTGOING);

    }

    @Override
    public PathExpander reverse() {
        return null;
    }
}
