package io.camunda.connector.csv.toolbox;

import java.util.Map;

/**
 * Operation process the file, and when the record is kep, the collector is called
 */
public abstract class CvsCollector {
  private int totalRecord = 0;

  /**
   * Call each time a line is processed
   */
  void processOneRecord() {
    totalRecord++;
  }

  public int getTotalNumberOfRecords() {
    return totalRecord;
  }

  public abstract void collect(Map<String, Object> dataRecord);
}
