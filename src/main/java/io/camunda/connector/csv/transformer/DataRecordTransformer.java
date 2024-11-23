package io.camunda.connector.csv.transformer;

import io.camunda.connector.api.error.ConnectorException;

import java.util.Map;

public abstract class DataRecordTransformer {

    public abstract Map<String, Object> transform(Map<String, Object> dataRecord) throws ConnectorException;

}
