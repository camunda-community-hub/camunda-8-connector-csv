package io.camunda.connector.csv.filter;

import java.util.Map;

/**
 * The goial of the stream is to say if the record is kepp or not
 */
public abstract class DataRecordFilter {

    private boolean isEnabled;

    protected DataRecordFilter(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public abstract boolean needDataRecordDecode();

    public abstract boolean keepDataRecord(int lineNumber, Map<String, Object> dataRecord);

    public boolean isEnabled() {
        return isEnabled;
    }

}
