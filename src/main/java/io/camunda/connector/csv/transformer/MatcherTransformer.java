package io.camunda.connector.csv.transformer;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.csv.CsvInput;
import io.camunda.connector.csv.filter.SelectorFilter;
import io.camunda.connector.csv.producer.DataRecordContainer;
import io.camunda.connector.csv.toolbox.CsvError;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MatcherTransformer extends DataRecordTransformer {

    SelectorFilter selectorUpdateRecord;
    CsvInput.MatcherPolicy matcherPolicy;

    private int nbRecordsUpdated;

    public MatcherTransformer(CsvInput csvInput) {
        super(csvInput.isMatcherEnabled());
        if (csvInput.isMatcherEnabled()) {
            selectorUpdateRecord = new SelectorFilter();
            if (csvInput.getMatchersRecords() != null) {
                for (Map<String, Object> updateRecord : csvInput.getMatchersRecords()) {
                    selectorUpdateRecord.addFilter(updateRecord);
                }
            }
            if (csvInput.getMatcherKeyFields() == null)
                throw new ConnectorException(CsvError.BAD_MATCHER_DEFINITION, "No KeyFields defined");
            selectorUpdateRecord.addKeyFields(csvInput.getMatcherKeyFields().stream().collect(Collectors.toSet()));
            this.matcherPolicy = csvInput.getMatcherPolicy();
        }
        this.nbRecordsUpdated = 0;
    }

    public static Map<String, String> getBpmnErrors() {
        return Map.of(CsvError.BAD_MATCHER_DEFINITION, CsvError.BAD_MATCHER_DEFINITION_EXPLANATION,
                CsvError.ONE_RECORD_DOES_NOT_MATCH_UNIQUEEACH, CsvError.ONE_RECORD_DOES_NOT_MATCH_UNIQUEEACH_EXPLANATION,
                CsvError.ONE_RECORD_DOES_NOT_MATCH_UNIQUE, CsvError.ONE_RECORD_DOES_NOT_MATCH_UNIQUE_EXPLANATION);

    }

    public int getNbRecordsUpdated() {
        return nbRecordsUpdated;
    }

    @Override
    public void transform(DataRecordContainer dataRecordContainer) throws ConnectorException {
        List<Map<String, Object>> searchMatch = selectorUpdateRecord.searchMatch(dataRecordContainer.getDataRecord());
        if (matcherPolicy == CsvInput.MatcherPolicy.SINGLE && searchMatch.size() != 1)
            throw new ConnectorException(CsvError.ONE_RECORD_DOES_NOT_MATCH_UNIQUEEACH, "Line [" + dataRecordContainer.lineNumber + "]");
        if (matcherPolicy == CsvInput.MatcherPolicy.SINGLEORNONE && searchMatch.size() > 1)
            throw new ConnectorException(CsvError.ONE_RECORD_DOES_NOT_MATCH_UNIQUE, "Line [" + dataRecordContainer.lineNumber + "]");

        nbRecordsUpdated++;
        for (Map<String, Object> matchRecord : searchMatch) {
            dataRecordContainer.putAll(matchRecord);

        }

    }
}
