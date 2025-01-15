package io.camunda.connector.csv;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.cherrytemplate.CherryInput;
import io.camunda.connector.csv.content.ContentStore;
import io.camunda.connector.csv.content.ContentStoreFile;
import io.camunda.connector.csv.producer.CsvProducer;
import io.camunda.connector.csv.producer.ProducerContentStore;
import io.camunda.connector.csv.producer.ProducerMemory;
import io.camunda.connector.csv.toolbox.CsvDefinition;
import io.camunda.connector.csv.toolbox.CsvError;
import io.camunda.connector.csv.toolbox.ParameterToolbox;
import io.camunda.filestorage.FileVariable;
import io.camunda.filestorage.FileVariableReference;
import io.camunda.filestorage.StorageDefinition;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)

public class CsvInput implements CherryInput {

    public static final String CSV_FUNCTION = "csvFunction";
    public static final String INPUT_TYPE_READER = "inputTypeReader";
    public static final String INPUT_TYPE_READER_LABEL = "Input Type Reader";
    public static final String INPUT_TYPE_READER_EXPLANATION = "Specify the type of reader (from where data are read) : "
            + TypeStorage.FILE + ","
            + TypeStorage.RECORDS;
    public static final String INPUT_RECORDS = "inputRecords";
    public static final String INPUT_RECORDS_LABEL = "Reader Process Variable";
    public static final String INPUT_RECORDS_EXPLANATION = "Name of the process variable where records are accessible";
    public static final String INPUT_READER_FILESTORAGE = "inputReaderFileStorage";
    public static final String INPUT_READER_FILESTORAGE_LABEL = "Reader FileStorage";
    public static final String INPUT_READER_FILESTORAGE_EXPLANATION = "FileStorage definition to access the CSV document";
    public static final String INPUT_CHARSET = "inputCharset";
    public static final String INPUT_CHARSET_LABEL = "Charset used to code the CSV file";
    public static final String INPUT_CHARSET_EXPLANATION = "File is encode by a specific charset";
    public static final String INPUT_CHARSET_DEFAULT = StandardCharsets.UTF_8.name();
    public static final String INPUT_SEPARATOR = "inputSeparator";
    public static final String INPUT_SEPARATOR_LABEL = "Separator between fields";
    public static final String INPUT_SEPARATOR_DEFAULT = ";";
    public static final String INPUT_SEPARATOR_EXPLANATION = "CSV is a collection of fields separated by a separator (; or ,)";
    public static final String FILTER = "filter";
    public static final String FILTER_LABEL = "Filter";
    public static final String FILTER_EXPLANATION = "Only data matching the record are kept";
    public static final String PAGINATION_ENABLED = "paginationEnabled";
    public static final String PAGINATION_ENABLED_LABEL = "pagination Enabled";
    public static final String PAGINATION_ENABLED_EXPLANATION = "if true pagination is enabled and reader return only a page";
    public static final String PAGE_NUMBER = "pageNumber";
    public static final String PAGE_NUMBER_LABEL = "page Number";
    public static final String PAGE_NUMBER_EXPLANATION = "Page number start at 0";
    public static final String PAGE_SIZE = "pageSize";
    public static final String PAGE_SIZE_LABEL = "Page size";
    public static final String PAGE_SIZE_EXPLANATION = "Number of records per page";
    public static final String MATCHER_ENABLED = "matcherEnabled";
    public static final String MATCHER_ENABLED_LABEL = "Matcher";
    public static final String MATCHER_ENABLED_EXPLANATION = "Enable the matcher operation, to update records";
    public static final String MATCHER_KEY_FIELDS = "matcherKeyFields";
    public static final String MATCHER_KEY_FIELDS_LABEL = "Key Fields";
    public static final String MATCHER_KEY_FIELDS_EXPLANATION = "Specify the key fields (list of fields used for the correlation) for update";
    public static final String MATCHERS_RECORDS = "matchersRecords";
    public static final String MATCHERS_RECORDS_LABEL = "Matchers";
    public static final String MATCHERS_RECORDS_EXPLANATION = "Records to match the flow of data, using the KeyFields to correlate.";
    public static final String MATCHER_POLICY = "matcherPolicy";
    public static final String MATCHER_POLICY_LABEL = "Update Policy";
    public static final String MATCHER_POLICY_EXPLANATION = "Choose a policy : "
            + MatcherPolicy.MULTIPLE + ": one item can match one or no records,"
            + MatcherPolicy.SINGLEORNONE + ": one item must match no record or only one record,"
            + MatcherPolicy.SINGLE + ": each item must match one and only one record";
    public static final String OPERATIONS_TRANSFORMER = "operationsTransformer";
    public static final String OPERATIONS_TRANSFORMER_LABEL = "Operations";
    public static final String OPERATIONS_TRANSFORMER_EXPLANATION = "Give a list of operations to transform the CSV source in Java Object";
    public static final String FIELDS_RESULT = "fieldsResult";
    public static final String FIELDS_RESULT_LABEL = "Fields Result";
    public static final String FIELDS_RESULT_EXPLANATION = "List the field to be produce in the result. if empty, all fields in the source (CSV, ListofObject) are save";
    public static final String OUTPUT_TYPE_WRITER = "outputTypeWriter";
    public static final String OUTPUT_TYPE_STORAGE_LABEL = "Output Type Storage";
    public static final String OUTPUT_TYPE_STORAGE_EXPLANATION = "Specify the storage of the output (where the data will be write): "
            + TypeStorage.FILE + ","
            + TypeStorage.RECORDS;
    public static final String OUTPUT_WRITER_FILESTORAGE = "outputWriterFileStorage";
    public static final String OUTPUT_WRITER_FILESTORAGE_LABEL = "Writer FileStorage";
    public static final String OUTPUT_WRITER_FILESTORAGE_EXPLANATION = "File Storage definition to save the CSV document";
    public static final String OUTPUT_FILENAME = "outputFileName";
    public static final String OUTPUT_FILENAME_LABEL = "Output File Name";
    public static final String OUTPUT_FILENAME_EXPLANATION = "File Name used to create the file";
    public static final String OUTPUT_SEPARATOR = "outputSeparator";
    public static final String OUTPUT_SEPARATOR_LABEL = "Separator between fields";
    public static final String OUTPUT_SEPARATOR_DEFAULT = ";";
    public static final String OUTPUT_SEPARATOR_EXPLANATION = "CSV is a collection of fields separated by a separator (; or ,)";
    public static final String OUTPUT_CHARSET = "outputCharset";
    public static final String OUTPUT_CHARSET_LABEL = "Charset used to code the CSV file";
    public static final String OUTPUT_CHARSET_EXPLANATION = "File is encode by a specific charset";
    /**
     * Group definition
     */
    public static final String GROUP_PAGINATION = "Pagination";
    public static final String GROUP_SOURCE = "Source";
    public static final String GROUP_PROCESSING = "Processing";
    public static final String GROUP_UPDATE = "Update";
    public static final String GROUP_OUTCOME = "Outcome";
    public String inputReaderFileStorage;
    private String csvFunction;
    private String inputTypeReader;
    private List<Map<String, Object>> inputRecords;
    private String inputCharSet;
    private String inputSeparator;
    private Map<String, Object> filter;
    private Boolean paginationEnabled;
    private int pageNumber;
    private int pageSize;
    private Boolean matcherEnabled;
    private List<String> matcherKeyFields;
    private List<Map<String, Object>> matchersRecords;
    private String matcherPolicy;
    private Map<String, String> operationsTransformer;
    private List<String> fieldsResult;
    private String outputTypeWriter;
    private String outputWriterFileStorage;
    private String outputFileName;
    private String outputSeparator;
    private String outputCharSet;

    public static Map<String, String> getBpmnErrors() {
        return Map.of(CsvError.CANT_ACCESS_INPUTRECORDS, CsvError.CANT_ACCESS_INPUTRECORDS_EXPLANATION,
                CsvError.CANT_ACCESS_FILE, CsvError.CANT_ACCESS_FILE_EXPLANATION,
                CsvError.UNSUPPORTED_TYPE_STORAGE, CsvError.UNSUPPORTED_TYPE_STORAGE_EXPLANATION);
    }

    public String getCsvFunction() {
        return csvFunction;
    }

    public String getInputCharSet() {
        return inputCharSet;
    }

    public String getInputSeparator() {
        return inputSeparator == null ? ";" : inputSeparator;
    }

    public Map<String, Object> getFilter() {
        return filter;
    }

    public boolean isPaginationEnabled() {
        return Boolean.TRUE.equals(paginationEnabled);
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public Map<String, String> getOperationTransformers() {
        return operationsTransformer;
    }

    public List<String> getFieldsResult() {
        return fieldsResult;
    }

    public String getOutputCharSet() {
        return outputCharSet;
    }

    public String getOutputSeparator() {
        return outputSeparator == null ? ";" : outputSeparator;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public List<Map<String, Object>> getInputRecords() {
        return inputRecords;
    }

    public String getInputReaderFileStorage() {
        return inputReaderFileStorage;
    }

    public Map<String, String> getOperationsTransformer() {
        return operationsTransformer;
    }

    public String getOutputWriterFileStorage() {
        return outputWriterFileStorage;
    }

    @Override
    public List<Map<String, Object>> getInputParameters() {
        return ParameterToolbox.getInputParameters();
    }

    public boolean isMatcherEnabled() {
        return Boolean.TRUE.equals(matcherEnabled);
    }

    public List<String> getMatcherKeyFields() {
        return matcherKeyFields;
    }

    public List<Map<String, Object>> getMatchersRecords() {
        return matchersRecords;
    }

    public MatcherPolicy getMatcherPolicy() {
        try {
            return MatcherPolicy.valueOf(matcherPolicy);
        } catch (Exception e) {
            return MatcherPolicy.MULTIPLE;
        }
    }

    public TypeStorage getOutputTypeWriter() {
        try {
            return TypeStorage.valueOf(outputTypeWriter);
        } catch (Exception e) {
            return TypeStorage.FILE;
        }
    }

    public TypeStorage getInputTypeReader() {
        try {
            return TypeStorage.valueOf(inputTypeReader);
        } catch (Exception e) {
            return TypeStorage.FILE;
        }
    }

    public FileVariable initializeOutputFileVariable() throws ConnectorException {
        StorageDefinition storageOutputDefinition;
        try {
            storageOutputDefinition = StorageDefinition.getFromString(outputWriterFileStorage);
        } catch (ConnectorException ce) {
            throw ce;
        } catch (Exception e) {
            throw new ConnectorException(CsvError.BAD_STORAGE_DEFINITION, e.getMessage());
        }


        FileVariable fileVariable = new FileVariable();

        fileVariable.setStorageDefinition(storageOutputDefinition);
        fileVariable.setName(getOutputFileName());
        fileVariable.setMimeType("text/csv");
        return fileVariable;
    }

    public ReaderEngine initializeInputReader() throws ConnectorException {
        // Producer (the reader)
        CsvProducer producer;
        if (getInputTypeReader() == CsvInput.TypeStorage.RECORDS) {
            CsvDefinition csvDefinition = CsvDefinition.fromFields(getFieldsResult(), getInputSeparator());
            if (getInputRecords() == null) {
                throw new ConnectorException(CsvError.CANT_ACCESS_INPUTRECORDS, "The Input record is null (variable does not exist)");
            }
            producer = new ProducerMemory(csvDefinition, getInputRecords());
            return new ReaderEngine(producer, csvDefinition);
        } else if (getInputTypeReader() == CsvInput.TypeStorage.FILE) {
            FileVariableReference fileVariableReference = null;
            try {
                fileVariableReference = FileVariableReference.fromJson(inputReaderFileStorage);
                ContentStore contentStore = new ContentStoreFile(fileVariableReference, inputCharSet);
                producer = new ProducerContentStore(inputSeparator, contentStore);
                producer.begin();
                CsvDefinition csvDefinition = ((ProducerContentStore) producer).getCsvDefinition();
                return new ReaderEngine(producer, csvDefinition);
            } catch (Exception e) {
                throw new ConnectorException(CsvError.CANT_ACCESS_FILE, e.getMessage());
            }
        } else {
            throw new ConnectorException(CsvError.UNSUPPORTED_TYPE_STORAGE, "TypeSource[" + getInputTypeReader() + "]. Expect ["
                    + CsvInput.TypeStorage.RECORDS + "," + CsvInput.TypeStorage.FILE);
        }
    }

    public enum MatcherPolicy {MULTIPLE, SINGLEORNONE, SINGLE}


    public enum TypeStorage {FILE, RECORDS}

    public record ReaderEngine(
            CsvProducer csvProducer,
            CsvDefinition csvDefinition) {
    }

}
