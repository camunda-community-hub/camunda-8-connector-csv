package io.camunda.connector.csv.collector;

import java.util.Map;

/**
 * Collector to count the number of records which pass the filter
 */
public class CollectorProperty extends CvsCollector {
    private int nbRecords = 0;

    @Override
    public void collect(Map<String, Object> recordCsv) {
        nbRecords++;
    }

    public int getNbRecords() {
        return nbRecords;
    }
}
