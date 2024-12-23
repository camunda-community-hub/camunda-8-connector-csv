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

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)

public class CsvInput implements CherryInput {

    public static final String CSV_FUNCTION = "csvFunction";
    public static final String SOURCE_FILE = "sourceFile";
    public static final String SOURCE_FILE_LABEL = "Input source CSV file";
    public static final String SOURCE_FILE_EXPLANATION = "FileStorage definition to access the source file";

    public static final String INPUT_TYPE_STORAGE = "inputTypeStorage";
    public static final String INPUT_TYPE_STORAGE_LABEL = "Input Type Storage";
    public static final String INPUT_TYPE_STORAGE_EXPLANATION = "Specify the storage of the input (from where data are read) : "
            + TypeStorage.STORAGE + ","
            + TypeStorage.PROCESSVARIABLE;
    public static final String INPUT_STORAGE_FILE = "inputStorageFile";
    public static final String INPUT_STORAGE_FILE_LABEL = "Input source CSV file";
    public static final String INPUT_STORAGE_FILE_EXPLANATION = "FileStorage definition to access the source file";

    public static final String INPUT_STORAGE_RECORDS = "inputStorageRecords";
    public static final String INPUT_STORAGE_RECORDS_LABEL = "Input Records";
    public static final String INPUT_STORAGE_RECORDS_EXPLANATION = "Records are saved in a variable";

    public static final String INPUT_CHARSET = "inputCharset";
    public static final String INPUT_STORAGE_CHARSET = "inputStorageCharset";
    public static final String INPUT_CHARSET_LABEL = "Charset used to code the CSV file";
    public static final String INPUT_CHARSET_EXPLANATION = "File is encode by a specific charset";
    public static final String INPUT_SEPARATOR = "inputSeparator";
    public static final String INPUT_STORAGE_SEPARATOR = "inputStorageSeparator";
    public static final String INPUT_SEPARATOR_LABEL = "Separator between fields";
    public static final String INPUT_SEPARATOR_DEFAULT = ";";
    public static final String INPUT_SEPARATOR_EXPLANATION = "CSV is a collection of fields separated by a separator (; or ,)";

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

    public static final String UPDATE_KEY_FIELDS = "updateKeyFields";
    public static final String UPDATE_KEY_FIELDS_LABEL = "Key Fields";
    public static final String UPDATE_KEY_FIELDS_EXPLANATION = "Specify the key fields (list of fields used for the correlation) for update";
    public static final String UPDATE_MATCHERS_RECORDS = "updateMatchersRecords";
    public static final String UPDATE_MATCHERS_RECORDS_LABEL = "Matchers";
    public static final String UPDATE_MATCHERS_RECORDS_EXPLANATION = "Records to match the flow of data, using the KeyFields to correlate.";
    public static final String UPDATE_POLICY = "updatePolicy";
    public static final String UPDATE_POLICY_LABEL = "Update Policy";
    public static final String UPDATE_POLICY_EXPLANATION = "Choose a policy : "
            + UpdatePolicy.MULTIPLE + ": one item can match one or no records,"
            + UpdatePolicy.SINGLEORNONE + ": one item must match no record or only one record,"
            + UpdatePolicy.SINGLE + ": each item must match one and only one record";

    public static final String MAPPERS_TRANSFORMERS = "mappersTransformers";
    public static final String MAPPERS_TRANSFORMERS_LABEL = "Mappers Function";
    public static final String MAPPERS_TRANSFORMERS_EXPLANATION = "Give a list of functions to transform the CSV source in Java Object";

    public static final String FIELDS_RESULT = "fieldsResult";
    public static final String FIELDS_RESULT_LABEL = "Fields Result";
    public static final String FIELDS_RESULT_EXPLANATION = "List the field to be produce in the result. if empty, all fields in the source (CSV, ListofObject) are save";

    public static final String OUTPUT_STORAGE_DEFINITION = "outputStorageDefinition";
    public static final String OUTPUT_STORAGE_DEFINITION_LABEL = "Output Storage Definition";
    public static final String OUTPUT_STORAGE_DEFINITION_EXPLANATION = "Where to save CSV file";
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


    public static final String OUTPUT_TYPE_STORAGE = "outputTypeStorage";
    public static final String OUTPUT_TYPE_STORAGE_LABEL = "Output Type Storage";
    public static final String OUTPUT_TYPE_STORAGE_EXPLANATION = "Specify the storage of the output (where the data will be write): "
            + TypeStorage.STORAGE + ","
            + TypeStorage.PROCESSVARIABLE;


    /**
     * Group definition
     */
    public static final String GROUP_PAGINATION = "Pagination";
    public static final String GROUP_SOURCE = "Source";
    public static final String GROUP_PROCESSING = "Processing";
    public static final String GROUP_UPDATE = "Update";
    public static final String GROUP_OUTCOME = "Outcome";
    private String csvFunction;
    private String sourceFile;
    public String inputStorageFile;
    private List<Map<String, Object>> inputStorageRecords;

    private String inputCharSet;
    private String inputSeparator;
    private String inputStorageCharSet;
    private String inputStorageSeparator;

    private Map<String, Object> filter;
    private int pageNumber;
    private int pageSize;
    private Map<String, String> mappersTransformer;
    private List<String> fieldsResult;
    private String outputStorageDefinition;
    private String outputFileName;
    private String updatePolicy;
    private List<Map<String, Object>> updateMatchersRecords;
    private List<String> keyFields;
    private String outputTypeStorage;
    private String outputCharSet;
    private String outputSeparator;

    private String inputTypeStorage;


    public String getCsvFunction() {
        return csvFunction;
    }

    public String getInputCharSet() {
        return inputCharSet;
    }

    public String getInputSeparator() {
        return inputSeparator == null ? ";" : inputSeparator;
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

    public Map<String, String> getMappersTransformers() {
        return mappersTransformer;
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

    public String getOutputCharSet() {
        return outputCharSet;
    }

    public String getOutputSeparator() {
        return outputSeparator == null ? ";" : outputSeparator;
    }


    public String getOutputFileName() {
        return outputFileName;
    }

    public List<Map<String, Object>> getInputStorageRecords() {
        return inputStorageRecords;
    }

    @Override
    public List<Map<String, Object>> getInputParameters() {
        return ParameterToolbox.getInputParameters();
    }

    public List<Map<String, Object>> getUpdateMatchersRecords() {
        return updateMatchersRecords;
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


    public String getInputStorageCharSet() {
        return inputStorageCharSet;
    }

    public String getInputStorageSeparator() {
        return getInputStorageSeparator;
    }

    public FileVariable initializeOutputFileVariable() throws ConnectorException {
        StorageDefinition storageOutputDefinition;
        try {
            storageOutputDefinition = StorageDefinition.getFromString(getOutputStorageDefinition());
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


    public record ProducerRecord(
            CsvProducer csvProducer,
            CsvDefinition csvDefinition) {
    }

    public ProducerRecord initializeInputProducer() throws ConnectorException {
        // Producer (the reader)
        CsvProducer producer;
        if (getInputTypeStorage() == CsvInput.TypeStorage.PROCESSVARIABLE) {
            CsvDefinition csvDefinition = CsvDefinition.fromFields(getFieldsResult(), getInputSeparator());
            producer = new ProducerMemory(csvDefinition, getInputStorageRecords());
            return new ProducerRecord(producer, csvDefinition);
        } else if (getInputTypeStorage() == CsvInput.TypeStorage.STORAGE) {
            FileVariableReference fileVariableReference = null;
            try {
                fileVariableReference = FileVariableReference.fromJson(getSourceFile());
                ContentStore contentStore = new ContentStoreFile(fileVariableReference, getInputStorageCharSet());
                producer = new ProducerContentStore(getInputStorageSeparator(), contentStore);
                producer.begin();
                CsvDefinition csvDefinition = ((ProducerContentStore) producer).getCsvDefinition();
                return new ProducerRecord(producer, csvDefinition);
            } catch (Exception e) {
                throw new ConnectorException(CsvError.CANT_PROCESS_CSV, e.getMessage());
            }
        } else {
            throw new ConnectorException(CsvError.UNSUPPORTED_TYPE_STORAGE, "TypeSource[" + getInputTypeStorage() + "]");
        }

    }

    public enum UpdatePolicy {MULTIPLE, SINGLEORNONE, SINGLE}

    public enum TypeStorage {STORAGE, PROCESSVARIABLE}

}
