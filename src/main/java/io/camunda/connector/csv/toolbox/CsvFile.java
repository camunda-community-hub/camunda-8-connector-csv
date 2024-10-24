package io.camunda.connector.csv.toolbox;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.csv.content.ContentStore;
import io.camunda.connector.csv.content.ContentStoreFile;
import io.camunda.filestorage.FileRepoFactory;
import io.camunda.filestorage.FileVariable;
import io.camunda.filestorage.FileVariableReference;

import java.util.Map;

public class CsvFile {

  String separator;
  String charSet;
  CsvDefinition csvDefinition = null;
  ContentStore contentStore;

  public void readSourceFile(FileVariableReference fileVariableReference, String charSet, String separator)
      throws Exception {
    FileRepoFactory fileRepoFactory = FileRepoFactory.getInstance();
    try {
      FileVariable fileVariable = fileRepoFactory.loadFileVariable(fileVariableReference);
      if (fileVariable.getValue() != null)
        this.contentStore = new ContentStoreFile(fileVariableReference, charSet);
      this.separator = separator;
      this.charSet = charSet;
      this.contentStore.openReadLine();

    } catch (Exception e) {
      throw new ConnectorException("CANT_READ_FILE", e.getMessage());
    }
  }

  public CsvDefinition getDefinition() throws ConnectorException {
    if (csvDefinition != null)
      return csvDefinition;
    // if the file is inside the contentStr, read it from the top
    try {
      String line = contentStore.readLine();
      csvDefinition = CsvDefinition.fromHeader(line, separator);

      return csvDefinition;
    } catch (ConnectorException ce) {
      throw ce;
    }
  }

  /**
   * Process all records, and send the result via the interface ProcessRecord.
   * If the definition is null, we suppose the Stream is set on the begining of the file, and the header is read
   * else we suppose we are after the header, at the first position of records
   */
  public void processFile(CsvMatcher matcher, CvsCollector collector, boolean decode, boolean playTransformation)
      throws ConnectorException {
    try {

      String line;
      if (csvDefinition == null) {
        line = contentStore.readLine();
        csvDefinition = CsvDefinition.fromHeader(line, separator);
      }
      int lineNumber = 1;
      // Read and process lines one by one
      while ((line = contentStore.readLine()) != null) {
        // we ignore empty line
        if (line.isEmpty())
          continue;

        lineNumber++;
        collector.processOneRecord();

        // if there is a matcher, we must decode the line
        Map<String, Object> dataRecord = null;
        if (matcher.isMatcherActive()) {
          dataRecord = csvDefinition.getRecord(line, lineNumber);
          if (matcher.getMatchers(dataRecord).isEmpty())
            continue; // skip the line
        }
        // if the decoder is required, decode the line if it's not done
        if (decode && dataRecord == null)
          dataRecord = csvDefinition.getRecord(line, lineNumber);

        // transdformer can be done only if dataRecord is present
        if (dataRecord != null && playTransformation)
          dataRecord = csvDefinition.tranformRecord(dataRecord);
        collector.collect(dataRecord);
      }
    } catch (ConnectorException ce) {
      throw ce;
    } finally {
      contentStore.closeReadLine();
    }
  }

}