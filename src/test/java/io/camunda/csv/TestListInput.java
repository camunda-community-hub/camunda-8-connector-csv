package io.camunda.csv;

import io.camunda.connector.cherrytemplate.CherryInput;
import io.camunda.connector.csv.CsvInput;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TestListInput {

    @Test
    public void testInput() {
        CsvInput input = new CsvInput();
        List<Map<String, Object>> listInputs = input.getInputParameters();
        assert (listInputs != null);
        checkExistName(listInputs, CsvInput.MAPPERS_TRANSFORMERS, CsvInput.GROUP_PROCESSING);checkExistName(listInputs, CsvInput.UPDATE_MATCHERS_RECORDS, CsvInput.GROUP_UPDATE);
        checkExistName(listInputs, CsvInput.UPDATE_KEY_FIELDS, CsvInput.GROUP_UPDATE);
        checkExistName(listInputs, CsvInput.UPDATE_POLICY, CsvInput.GROUP_UPDATE);

        checkExistName(listInputs, CsvInput.OUTPUT_STORAGE_DEFINITION, CsvInput.GROUP_OUTCOME);

        checkExistName(listInputs, CsvInput.INPUT_CHARSET, CsvInput.GROUP_SOURCE);
        checkExistName(listInputs, CsvInput.OUTPUT_CHARSET, CsvInput.GROUP_OUTCOME);

    }

    private void checkExistName(List<Map<String, Object>> listInputs, String name, String group) {
        Optional<Map<String, Object>> input = listInputs.stream().filter(t -> t.get("name").equals(name)).findFirst();
        assert (input.isPresent());
        if (group != null)
            assert (input.get().get(CherryInput.PARAMETER_MAP_GROUP_LABEL).equals(group));

    }
}
