package com.maxdemarzi;

import com.maxdemarzi.results.KeyCountResult;
import net.openhft.chronicle.map.ChronicleMap;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.*;
import org.neo4j.logging.Log;
import org.neo4j.procedure.*;
import org.roaringbitmap.longlong.LongIterator;
import org.roaringbitmap.longlong.Roaring64NavigableMap;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public class Procedures {

    // This field declares that we need a GraphDatabaseService
    // as context when any procedure in this class is invoked
    @Context
    public GraphDatabaseService db;

    // This gives us a log instance that outputs messages to the
    // standard log, normally found under `data/log/neo4j.log`
    @Context
    public Log log;

    static final RelationshipType LINKED_TO = RelationshipType.withName("LINKED_TO");
    public static PriorityQueue<Map.Entry<String, Long>> topCounts;

    @Procedure(name = "com.maxdemarzi.top.paths", mode = Mode.READ)
    @Description("CALL com.maxdemarzi.top.paths(top, limit, path)")
    public Stream<KeyCountResult> topPaths(@Name(value = "top", defaultValue = "10") Long top,
                                           @Name(value = "limit", defaultValue = "6") Long limit,
                                           @Name(value = "path", defaultValue = "/tmp/paths.chronicle") String path) throws IOException {

        ChronicleMap<String,Long> chronicleMap = ChronicleMap
                .of(String.class, Long.class)
                .name("counts")
                .averageKey("100000-100000-100000-100000-100000-100000-100000")
                .entries(1_000_000)
                .createPersistedTo(new File(path));

        ResourceIterator<Node> iterator = db.findNodes(Label.label("Node"));

        Roaring64NavigableMap startingNodes = new Roaring64NavigableMap();
        Roaring64NavigableMap endingNodes = new Roaring64NavigableMap();

        while(iterator.hasNext()) {
            Node node = iterator.next();
            if(node.getDegree(LINKED_TO, Direction.INCOMING) == 0) {
                startingNodes.add(node.getId());
            }
            if(node.getDegree(LINKED_TO, Direction.OUTGOING) == 0) {
                endingNodes.add(node.getId());
            }
        }

        TraversalDescription myTraversal = db.traversalDescription()
                .depthFirst()
                .expand(new TopPathExpander(limit))
                .evaluator(new TopPathEvaluator(endingNodes))
                .uniqueness(Uniqueness.NONE);

        LongIterator longIterator = startingNodes.getLongIterator();

        HashMap<String, Long> keyCounts = new HashMap<>();
        topCounts = new PriorityQueue<>(top.intValue(), Map.Entry.comparingByValue());
        Map.Entry<String, Long> least;


        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TopSoFar(log), 5000, 5000);

        while (longIterator.hasNext()) {
            long nodeId = longIterator.next();
            Node node = db.getNodeById(nodeId);
            for (Path p : myTraversal.traverse(node)) {
                ArrayList<Long> nodeIds = new ArrayList<>();
                for(Node x : p.nodes()) {
                    nodeIds.add(x.getId());
                }

                for (int start = 0; start < nodeIds.size(); start++) {
                    for (int end = start + 3; end <= nodeIds.size(); end++) {
                        String key = StringUtils.join(nodeIds.subList(start,end), "-");
                        long counts = chronicleMap.merge(key, 1L, Long::sum);

                        keyCounts.put(key, counts);
                        topCounts.clear();
                        keyCounts.entrySet().forEach(x -> {
                            topCounts.add(new AbstractMap.SimpleEntry<>(x.getKey(), x.getValue()));
                        });

                        if (topCounts.size() > top.intValue()) {
                            least = topCounts.poll();
                            keyCounts.remove(least.getKey());
                        }

                        //keyCounts.clear();
                        //topCounts.forEach(x -> { keyCounts.put(x.getKey(), x.getValue());});
                    }
                }
            }
        }
        chronicleMap.clear();
        timer.cancel();
        return topCounts.stream().map(x -> new KeyCountResult(x.getKey(), x.getValue()));

    }
}