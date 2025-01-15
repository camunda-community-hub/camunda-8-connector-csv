package io.camunda.connector.csv.transformer;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.csv.CsvInput;
import io.camunda.connector.csv.producer.DataRecordContainer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldListTransformer extends DataRecordTransformer {

    List<String> fieldsList;

    public FieldListTransformer(CsvInput csvInput) {
        super(csvInput.getFieldsResult() != null);
        this.fieldsList = csvInput.getFieldsResult();
    }

    @Override
    public void transform(DataRecordContainer dataRecordContainer) throws ConnectorException {
        if (fieldsList == null || fieldsList.isEmpty()) {
            return;
        }
        Map<String, Object> result = new HashMap<>();
        for (String field : fieldsList) {
            result.put(field, dataRecordContainer.getDataRecord().get(field));
        }
        dataRecordContainer.setDataRecord(result);
    }
}
