package io.camunda.connector.csv.transformer;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.csv.producer.DataRecordContainer;

public abstract class DataRecordTransformer {

    private boolean isEnabled;

    protected DataRecordTransformer(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public abstract void transform(DataRecordContainer dataRecordContainer) throws ConnectorException;

    public boolean isEnabled() {
        return isEnabled;
    }
}
