package io.camunda.connector.csv.collector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Collector to count the number of records which pass the filter
 */
public class CollectorListMap extends CvsCollector {
  public List<Map<String, Object>> listRecords = new ArrayList<>();

  @Override
  public void collect(Map<String, Object> record) {
    listRecords.add(record);
  }

  public int getNumberOfRecords() {
    return listRecords.size();
  }
}
