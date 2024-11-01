package io.camunda.connector.csv.content;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.csv.toolbox.CsvError;
import io.camunda.filestorage.FileRepoFactory;
import io.camunda.filestorage.FileVariable;
import io.camunda.filestorage.FileVariableReference;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
  FileVariable fileVariable;
  private ByteArrayOutputStream byteArrayOutputStream;

  /**
   * Create the contentStore from an existing fileVariableReference. it's usefull for the Read
   *
   * @param fileVariableReference reference to a file
   * @param charSet charset to read the files as an ASCII file
   */
  public ContentStoreFile(FileVariableReference fileVariableReference, String charSet) {
    FileRepoFactory fileRepoFactory = FileRepoFactory.getInstance();
    try {
      this.fileVariable = fileRepoFactory.loadFileVariable(fileVariableReference);

      // For the moment, only implementation is to read the conmplete content
      contentByte = fileVariable.getValue();
      this.charSet = charSet;
    } catch (Exception e) {
      throw new ConnectorException(CsvError.CANT_READ_FILE, e.getMessage());
    }
  }

  /**
   * ContentStore with no reference. It will collect the
   */
  public ContentStoreFile(FileVariable fileVariable, String charSet) {
    this.fileVariable = fileVariable;
    this.charSet = charSet;
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

  public void openWriteLine() throws ConnectorException {
    byteArrayOutputStream = new ByteArrayOutputStream();
  }

  public void writeLine(String line) throws ConnectorException {

    try {
      line += "\n";
      byteArrayOutputStream.write(line.getBytes(charSet == null ? StandardCharsets.UTF_8.name() : charSet));
    } catch (IOException e) {
      throw new ConnectorException(CsvError.CANT_WRITE_FILE, e.getMessage());
    }
  }

  public void closeWriteLine() throws ConnectorException {
    fileVariable.setValue(byteArrayOutputStream.toByteArray());

  }

}
