package io.camunda.connector.csv.producer;

import io.camunda.connector.csv.toolbox.CsvDefinition;

import java.util.Map;

/**
 * We want to decode the data only when it is necessary. This class does that and decode it only when it's needed
 */
public class DataRecordContainer {
    public String line;
    public Map<String, Object> dataRecord = null;
    public int lineNumber;
    CsvDefinition csvDefinition;

    public DataRecordContainer(String line, int lineNumber, CsvDefinition csvDefinition) {
        this.line = line;
        this.lineNumber = lineNumber;
        this.csvDefinition = csvDefinition;
    }

    public DataRecordContainer(Map<String, Object> dataRecord, int lineNumber, CsvDefinition csvDefinition) {
        this.dataRecord = dataRecord;
        this.lineNumber = lineNumber;
        this.csvDefinition = csvDefinition;

    }

    public Map<String, Object> getDataRecord() {
        if (dataRecord == null)
            dataRecord = csvDefinition.getRecord(line, lineNumber);
        return dataRecord;
    }

    public void setDataRecord(Map<String, Object> dataRecord) {
        this.dataRecord = dataRecord;
    }

    public void put(String key, Object value) {
        this.dataRecord.put(key, value);
    }

    public void putAll(Map<String, Object> dataRecord) {
        this.dataRecord.putAll(dataRecord);
    }
}
