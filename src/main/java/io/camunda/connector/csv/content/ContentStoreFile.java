package io.camunda.connector.csv.content;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.csv.toolbox.CsvError;
import io.camunda.filestorage.FileRepoFactory;
import io.camunda.filestorage.FileVariable;
import io.camunda.filestorage.FileVariableReference;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * The content is provided in memory
 */
public class ContentStoreFile extends ContentStore {

  byte[] contentByte;
  InputStreamReader inputStreamReader;
  BufferedReader reader;
  String charSet;

  public ContentStoreFile(FileVariableReference fileVariableReference, String charSet) {
    FileRepoFactory fileRepoFactory = FileRepoFactory.getInstance();
    try {
      FileVariable fileVariable = fileRepoFactory.loadFileVariable(fileVariableReference);

      // For the moment, only implementation is to read the conmplete content
      contentByte = fileVariable.getValue();
      this.charSet = charSet;
    } catch (Exception e) {
      throw new ConnectorException(CsvError.CANT_READ_FILE, e.getMessage());
    }
  }

  public void openReadLine() throws ConnectorException {
    try {
      InputStream byteArrayInputStream = new ByteArrayInputStream(contentByte);
      inputStreamReader = new InputStreamReader(byteArrayInputStream,
          charSet == null ? StandardCharsets.UTF_8.name() : charSet);
      reader = new BufferedReader(inputStreamReader);
    } catch (Exception e) {
      throw new ConnectorException(CsvError.CANT_READ_FILE, e.getMessage());
    }
  }

  public String readLine() throws ConnectorException {

    try {
      return reader.readLine();
    } catch (IOException e) {
      throw new ConnectorException(CsvError.CANT_READ_FILE, e.getMessage());
    }
  }

  public void closeReadLine() throws ConnectorException {
    try {
      reader.close();
      inputStreamReader.close();
    } catch (IOException e) {
      throw new ConnectorException(CsvError.CANT_READ_FILE, e.getMessage());
    }

  }
}
