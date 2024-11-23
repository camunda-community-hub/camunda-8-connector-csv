package io.camunda.connector.csv.collector;

import io.camunda.connector.api.error.ConnectorException;

import java.util.Map;

/**
 * Operation process the file, and when the record is kep, the collector is called
 */
public abstract class CvsCollector {
    private int numberOfTotalRecords = 0;
    private int numberOfRecordsCollected = 0;

    public void begin() throws ConnectorException {
    }

    /**
     * Call each time a line is processed
     */
    public void processOneRecord() {
        numberOfTotalRecords++;
    }

    /**
     * This method is call every time a record is collected. All records processes may not bne keep.
     */
    public void processRecordCollected() {
        numberOfRecordsCollected++;
    }

    public void end() throws ConnectorException {
    }

    public int getTotalNumberOfRecords() {
        return numberOfTotalRecords;
    }

    public int getCollectedNumberOfRecords() {
        return numberOfRecordsCollected;
    }

    /**
     * Collect a Data record
     *
     * @param dataRecord data record to collect
     */
    public abstract void collect(Map<String, Object> dataRecord);

}
