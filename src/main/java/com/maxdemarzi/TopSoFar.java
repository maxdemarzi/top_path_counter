package com.maxdemarzi;

import org.neo4j.logging.Log;

import java.util.*;

public class TopSoFar extends TimerTask {

    Log log;

    public TopSoFar(Log log) {
        this.log = log;
    }

    @Override
    public void run() {
        try {
            log.info("========================================================================================");
            Procedures.topCounts.forEach(x -> {
                log.info("Path: " + x.getKey() + " Count: " + x.getValue());
            });
        } catch (ConcurrentModificationException e) {
            log.info("topCounts is busy, will try again in 5 seconds.");
        }
    }
}
