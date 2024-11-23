package io.camunda.connector.csv.producer;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.csv.content.ContentStore;
import io.camunda.connector.csv.toolbox.CsvDefinition;
import io.camunda.connector.csv.toolbox.CsvError;

public class ProducerContentStore extends CvsProducer {

    String separator;

    ContentStore contentStore;
    CsvDefinition csvDefinition;
    int lineNumber = 0;

    public ProducerContentStore(String separator, ContentStore contentStore) {
        this.separator = separator;
        this.contentStore = contentStore;
    }

    @Override
    public void begin() throws ConnectorException {
        this.contentStore.openReadLine();
        String header = contentStore.readLine();
        if (header == null)
            throw new ConnectorException(CsvError.NO_HEADER);
        csvDefinition = CsvDefinition.fromHeader(header, separator);
        lineNumber = 1;
    }

    @Override
    public void end() throws ConnectorException {
        this.contentStore.closeReadLine();
    }

    public CsvDefinition getCsvDefinition() {
        return csvDefinition;
    }

    @Override
    public DataRecordContainer getDataRecord() {
        String line = contentStore.readLine();
        // no more data
        if (line == null)
            return null;
        lineNumber++;
        return new DataRecordContainer(line, lineNumber, csvDefinition);
    }

    @Override
    public int getNumberOfRecords() {
        return lineNumber;
    }

}
