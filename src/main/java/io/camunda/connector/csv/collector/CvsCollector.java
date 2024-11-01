package io.camunda.connector.csv.collector;

import io.camunda.connector.api.error.ConnectorException;

import java.util.Map;

/**
 * Operation process the file, and when the record is kep, the collector is called
 */
public abstract class CvsCollector {
  private int totalRecord = 0;

  public void begin() throws ConnectorException {
  }

  /**
   * Call each time a line is processed
   */
  public void processOneRecord() {
    totalRecord++;
  }

  public void end() throws ConnectorException {
  }

  public int getTotalNumberOfRecords() {
    return totalRecord;
  }

  /**
   * Collect a Data record
   *
   * @param dataRecord data record to collect
   */
  public abstract void collect(Map<String, Object> dataRecord);

}
