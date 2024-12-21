package io.camunda.connector.csv.producer;

import io.camunda.connector.api.error.ConnectorException;

public abstract class CsvProducer {
    // produce dataRecord

    public abstract DataRecordContainer getDataRecord();

    public abstract int getNumberOfRecords();

    public void begin() throws ConnectorException {
    }

    public void end() throws ConnectorException {
    }

}
