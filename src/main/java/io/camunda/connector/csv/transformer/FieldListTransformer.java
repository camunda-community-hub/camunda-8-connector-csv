package io.camunda.connector.csv.transformer;

import io.camunda.connector.api.error.ConnectorException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldListTransformer extends DataRecordTransformer {

  List<String> fieldsList;

  public FieldListTransformer(List<String> fieldsList) {
    this.fieldsList = fieldsList;
  }

  @Override
  public Map<String, Object> transform(Map<String, Object> dataRecord) throws ConnectorException {
    if (fieldsList == null || fieldsList.isEmpty()) {
      return dataRecord;
    }
    Map<String, Object> result = new HashMap<>();
    for (String field : fieldsList) {
      result.put(field, dataRecord.get(field));
    }
    return result;
  }
}
