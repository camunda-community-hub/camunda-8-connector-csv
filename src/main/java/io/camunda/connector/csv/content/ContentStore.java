package io.camunda.connector.csv.content;

import io.camunda.connector.api.error.ConnectorException;

public abstract class ContentStore {

  public abstract void openReadLine() throws ConnectorException;

  /**
   * return a line. Null if the stream is finished
   *
   * @return a line
   * @throws ConnectorException
   */
  public abstract String readLine() throws ConnectorException;

  public abstract void closeReadLine() throws ConnectorException;

}
