package io.camunda.connector.csv.producer;

import io.camunda.connector.csv.toolbox.CsvDefinition;

import java.util.List;
import java.util.Map;

public class ProducerMemory extends CsvProducer {
    List<Map<String, Object>> records;
    int lineNumber;
    CsvDefinition csvDefinition;

    public ProducerMemory(CsvDefinition csvDefinition, List<Map<String, Object>> records) {
        this.csvDefinition = csvDefinition;
        this.records = records;
        lineNumber = -1;
    }

    @Override
    public DataRecordContainer getDataRecord() {
        lineNumber++;
        if (records == null || lineNumber >= records.size())
            return null;
        return new DataRecordContainer(records.get(lineNumber), lineNumber, csvDefinition);
    }

    @Override
    public int getNumberOfRecords() {
        return records.size();
    }
}
