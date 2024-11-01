package io.camunda.connector.csv.collector;

import io.camunda.connector.csv.content.ContentStore;
import io.camunda.connector.csv.toolbox.CsvDefinition;

import java.util.Map;

public class CollectorContentStore extends CvsCollector {

  CsvDefinition cvsDefinition;
  ContentStore contentStore;
  String separator;
  int numberOfRecords = 0;

  public CollectorContentStore(CsvDefinition cvsDefinition, ContentStore contentStore, String separator) {
    this.cvsDefinition = cvsDefinition;
    this.contentStore = contentStore;
    this.separator = separator;
  }

  @Override
  public void begin() {
    contentStore.openWriteLine();
    // Write the header
    contentStore.writeLine(String.join(separator, cvsDefinition.getHeader()));

  }

  @Override
  public void collect(Map<String, Object> dataRecord) {
    StringBuilder line = new StringBuilder();
    for (int i = 0; i < cvsDefinition.getHeader().size(); i++) {
      if (i > 0)
        line.append(separator);
      line.append(dataRecord.get(cvsDefinition.getHeader().get(i)));
    }
    numberOfRecords++;
    contentStore.writeLine(line.toString());
  }

  @Override
  public void end() {
    contentStore.closeWriteLine();
  }

  public int getNumberOfRecords() {
    return numberOfRecords;
  }
}
