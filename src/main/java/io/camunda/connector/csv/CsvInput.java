package io.camunda.connector.csv;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.cherrytemplate.CherryInput;
import io.camunda.connector.csv.toolbox.CsvError;
import io.camunda.connector.csv.toolbox.ParameterToolbox;
import io.camunda.filestorage.FileVariable;
import io.camunda.filestorage.StorageDefinition;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)

public class CsvInput implements CherryInput {

    public static final String CSV_FUNCTION = "csvFunction";
    public static final String SOURCE_FILE = "sourceFile";
    public static final String SOURCE_FILE_LABEL = "Input source CSV file";
    public static final String SOURCE_FILE_EXPLANATION = "FileStorage definition to access the source file";
    public static final String CHARSET = "charset";
    public static final String CHARSET_LABEL = "Charset used to code the CSV file";
    public static final String CHARSET_EXPLANATION = "File is encode by a specific charset";
    public static final String SEPARATOR = "separator";
    public static final String SEPARATOR_LABEL = "Separator between fields";
    public static final String SEPARATOR_DEFAULT = ";";
    public static final String SEPARATOR_EXPLANATION = "CSV is a collection of fields separated by a separator (; or ,)";
    public static final String FILTER = "filter";
    public static final String FILTER_LABEL = "Filter";
    public static final String FILTER_EXPLANATION = "Only data matching the record are kept";
    public static final String PAGE_NUMBER = "pageNumber";
    public static final String PAGE_NUMBER_LABEL = "page Number";
    public static final String PAGE_NUMBER_EXPLANATION = "Page number start at 0";
    public static final String PAGE_SIZE = "pageSize";
    public static final String PAGE_SIZE_LABEL = "Page size";
    public static final String PAGE_SIZE_EXPLANATION = "Number of records per page";
    public static final String RECORDS = "inputRecords";
    public static final String RECORDS_LABEL = "records";
    public static final String RECORDS_EXPLANATION = "Records to read or write. List of Map";

    public static final String MATCHERS = "matchers";
    public static final String MATCHERS_LABEL = "Matcher";
    public static final String MATCHERS_EXPLANATION = "Records to match the flow of data, using the KeyFields to correlate.";

    public static final String MAPPERS = "mappers";
    public static final String MAPPERS_LABEL = "Mappers";
    public static final String MAPPERS_EXPLANATION = "Give a list of functions to transform the CSV source in Java Object";
    public static final String FIELDS_RESULT = "fieldsResult";
    public static final String FIELDS_RESULT_LABEL = "Fields Result";
    public static final String FIELDS_RESULT_EXPLANATION = "List the field to be produce in the result. if empty, all fields in the source (CSV, ListofObject) are save";
    public static final String OUTPUT_STORAGE_DEFINITION = "outputStorageDefinition";
    public static final String OUTPUT_STORAGE_DEFINITION_LABEL = "Output Storage Definition";
    public static final String OUTPUT_STORAGE_DEFINITION_EXPLANATION = "Where to save CSV file";
    public static final String OUTPUT_FILENAME = "outputFileName";
    public static final String OUTPUT_FILENAME_LABEL = "Output File Name";
    public static final String OUTPUT_FILENAME_EXPLANATION = "File Name used to create the file";
    public static final String UPDATE_POLICY = "updatePolicy";
    public static final String UPDATE_POLICY_LABEL = "Update Policy";
    public static final String UPDATE_POLICY_EXPLANATION = "Choose a policy : "
            + UpdatePolicy.MULTIPLE + ": one item can match one or no records,"
            + UpdatePolicy.SINGLEORNONE + ": one item must match no record or only one record,"
            + UpdatePolicy.SINGLE + ": each item must match one and only one record";
    public static final String KEY_FIELDS = "keyFields";
    public static final String KEY_FIELDS_LABEL = "Key Fields";
    public static final String KEY_FIELDS_EXPLANATION = "Specify the key fields for update";
    public static final String OUTPUT_TYPE_STORAGE = "outputTypeStorage";
    public static final String OUTPUT_TYPE_STORAGE_LABEL = "Output Type Storage";
    public static final String OUTPUT_TYPE_STORAGE_EXPLANATION = "Specify the storage of the output (where the data will be write): "
            + TypeStorage.STORAGE + ","
            + TypeStorage.PROCESSVARIABLE;
    public static final String INPUT_TYPE_STORAGE = "inputTypeStorage";
    public static final String INPUT_TYPE_STORAGE_LABEL = "Input Type Storage";
    public static final String INPUT_TYPE_STORAGE_EXPLANATION = "Specify the storage of the input (from where data are read) : "
            + TypeStorage.STORAGE + ","
            + TypeStorage.PROCESSVARIABLE;


    /**
     * Group definition
     */
    public static final String GROUP_PAGINATION = "Pagination";
    public static final String GROUP_SOURCE = "Source";
    public static final String GROUP_PROCESSING = "Processing";
    public static final String GROUP_PRODUCER = "Producer";
    private String csvFunction;
    private String sourceFile;
    private String charSet;
    private String separator;
    private Map<String, Object> filter;
    private int pageNumber;
    private int pageSize;
    private List<Map<String, Object>> inputRecords;
    private Map<String, String> mappers;
    private List<String> fieldsResult;
    private String outputStorageDefinition;
    private String outputFileName;
    private String updatePolicy;
    private List<Map<String,Object>> matchers;
    private List<String> keyFields;
    private String outputTypeStorage;

    private String inputTypeStorage;


    public String getCsvFunction() {
        return csvFunction;
    }

    public String getCharSet() {
        return charSet;
    }

    public String getSeparator() {
        return separator == null ? ";" : separator;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public Map<String, Object> getFilter() {
        return filter;
    }

    public boolean isPaginationActive() {
        return pageSize > 0 && pageNumber >= 0;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public Map<String, String> getMappers() {
        return mappers;
    }

    public List<String> getKeyFields() {
        return keyFields;
    }

    public List<String> getFieldsResult() {
        return fieldsResult;
    }

    public String getOutputStorageDefinition() {
        return outputStorageDefinition;
    }


    public String getOutputFileName() {
        return outputFileName;
    }

    public List<Map<String, Object>> getInputRecords() {
        return inputRecords;
    }

    @Override
    public List<Map<String, Object>> getInputParameters() {
        return ParameterToolbox.getInputParameters();
    }

    public List<Map<String, Object>> getMatchers() {
        return matchers;
    }

    public UpdatePolicy getUpdatePolicy() {
        try {
            return UpdatePolicy.valueOf(updatePolicy);
        } catch (Exception e) {
            return UpdatePolicy.MULTIPLE;
        }
    }

    public TypeStorage getOutputTypeStorage() {
        try {
            return TypeStorage.valueOf(outputTypeStorage);
        } catch (Exception e) {
            return TypeStorage.STORAGE;
        }
    }

    public TypeStorage getInputTypeStorage() {
        try {
            return TypeStorage.valueOf(inputTypeStorage);
        } catch (Exception e) {
            return TypeStorage.STORAGE;
        }
    }

    public FileVariable initializeOutputFileVariable() throws ConnectorException {
        StorageDefinition storageOutputDefinition;
        try {
            storageOutputDefinition = StorageDefinition.getFromString(getOutputStorageDefinition());
        } catch (Exception e) {
            throw new ConnectorException(CsvError.BAD_STORAGE_DEFINITION);
        }


        FileVariable fileVariable = new FileVariable();

        fileVariable.setStorageDefinition(storageOutputDefinition);
        fileVariable.setName(getOutputFileName());
        fileVariable.setMimeType("text/csv");
        return fileVariable;
    }

    public enum UpdatePolicy {MULTIPLE, SINGLEORNONE, SINGLE}

    public enum TypeStorage {STORAGE, PROCESSVARIABLE}

}
