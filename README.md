# Top Path Counter
Stored Procedure to count Paths

Instructions
------------ 

This project uses maven, to build a jar-file with the procedure in this
project, simply package the project with maven:

    mvn clean package

This will produce a jar-file, `target/top-path-counter-1.0-SNAPSHOT.jar`,
that can be copied to the `plugin` directory of your Neo4j instance.

    cp target/top-path-counter-1.0-SNAPSHOT.jar neo4j-enterprise-3.5.8/plugins/.


Restart your Neo4j Server. Your new Stored Procedures are available:


    CALL com.maxdemarzi.top.paths
    CALL com.maxdemarzi.top.paths(top, limit, path) // top x to return, limit is size of traversal
    CALL com.maxdemarzi.top.paths(10, 6, "/tmp/paths.chronicle")
    
    To see the best paths every 5 seconds:
    tail -f ./neo4j-enterprise-3.5.8/logs/neo4j.log 
    
Example:

    create (n:Node)
    create (n2:Node)
    create (n3:Node)
    create (n4:Node)
    create (n5:Node)
    create (n)-[:LINKED_TO]->(n2)
    create (n2)-[:LINKED_TO]->(n3)
    create (n3)-[:LINKED_TO]->(n4)
    create (n5)-[:LINKED_TO]->(n2)    
    
Result:

    ╒═════════╤═══════╕
    │"key"    │"count"│
    ╞═════════╪═══════╡
    │"1-2-3"  │2      │
    ├─────────┼───────┤
    │"4-1-2"  │1      │
    ├─────────┼───────┤
    │"4-1-2-3"│1      │
    ├─────────┼───────┤
    │"0-1-2"  │1      │
    ├─────────┼───────┤
    │"1-2-3"  │1      │
    ├─────────┼───────┤
    │"0-1-2-3"│1      │
    └─────────┴───────┘    