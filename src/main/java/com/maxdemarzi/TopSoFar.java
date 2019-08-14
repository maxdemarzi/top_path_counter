package com.maxdemarzi;

import org.neo4j.logging.Log;

import java.util.TimerTask;

public class TopSoFar extends TimerTask {

    Log log;

    public TopSoFar(Log log) {
        this.log = log;
    }

    @Override
    public void run() {
        log.info("========================================================================================");
        Procedures.topCounts.forEach(x-> {
            log.info("Path: "+ x.getKey() + " Count: " + x.getValue());
        });
    }
}
