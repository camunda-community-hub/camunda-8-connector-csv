package io.camunda.connector.csv.toolbox;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.filestorage.FileRepoFactory;
import io.camunda.filestorage.FileVariable;
import io.camunda.filestorage.FileVariableReference;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class CsvFile {

  byte[] contentByte;

  String separator;
  String charSet;
  CsvDefinition csvDefinition = null;

  public void readSourceFile(FileVariableReference fileVariableReference, String charSet, String separator)
      throws Exception {
    FileRepoFactory fileRepoFactory = FileRepoFactory.getInstance();
    try {
      FileVariable fileVariable = fileRepoFactory.loadFileVariable(fileVariableReference);

      // For the moment, only implementation is to read the conmplete content
      contentByte = fileVariable.getValue();
      this.separator = separator;
      this.charSet = charSet;
    } catch (Exception e) {
      throw new ConnectorException("CANT_READ_FILE", e.getMessage());
    }
  }

  public CsvDefinition getDefinition() throws ConnectorException {
    if (csvDefinition != null)
      return csvDefinition;
    // if the file is inside the contentStr, read it from the top
    try {
      BufferedReader reader = new BufferedReader(getContentStream());
      String line = reader.readLine();
      csvDefinition = CsvDefinition.fromHeader(line, separator);

      return csvDefinition;
    } catch (ConnectorException ce) {
      throw ce;
    } catch (IOException e) {
      throw new ConnectorException(CsvError.ERROR_CANT_READ_FILE, e);
    }
  }

  public int getNumberOfRecords() throws ConnectorException {
    try {
      BufferedReader reader = new BufferedReader(getContentStream());

      int lineCount = 0;

      // Read and process lines one by one
      while ((reader.readLine()) != null) {
        lineCount++;
      }
      return lineCount-1;
    } catch (ConnectorException ce) {
      throw ce;
    } catch (IOException e) {
      throw new ConnectorException(CsvError.ERROR_CANT_READ_FILE, e);
    }
  }

  private InputStreamReader getContentStream() {
    try {
      InputStream byteArrayInputStream = new ByteArrayInputStream(contentByte);

      InputStreamReader inputStreamReader = new InputStreamReader(byteArrayInputStream,
          charSet==null? StandardCharsets.UTF_8.name(): charSet);
      return inputStreamReader;
    } catch (IOException e) {
      throw new ConnectorException(CsvError.ERROR_CANT_READ_FILE, e);
    }
  }

}