package io.camunda.connector.csv;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.camunda.connector.cherrytemplate.CherryInput;
import io.camunda.connector.csv.toolbox.ParameterToolbox;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)

public class CsvInput implements CherryInput {

  public static final String INPUT_CSV_FUNCTION = "csvFunction";
  public static final String INPUT_SOURCE_FILE = "sourceFile";
  public static final String INPUT_SOURCE_FILE_LABEL = "Input source CSV file";
  public static final String INPUT_SOURCE_FILE_EXPLANATION = "FileStorage definition to access the source file";

  public static final String INPUT_CHARSET = "charset";
  public static final String INPUT_CHARSET_LABEL = "Charset used to code the CSV file";
  public static final String INPUT_CHARSET_EXPLANATION = "File is encode by a specific charset";

  public static final String INPUT_SEPARATOR = "separator";
  public static final String INPUT_SEPARATOR_LABEL = "Separator between fields";
  public static final String INPUT_SEPARATOR_EXPLANATION = "CSV is a collection of fields separated by a separator (; or ,)";

  public static final String INPUT_FILTER = "filter";
  public static final String INPUT_FILTER_LABEL = "Filter";
  public static final String INPUT_FILTER_EXPLANATION = "Only data matching the record are kept";

  public static final String INPUT_PAGE_NUMBER = "pageNumber";
  public static final String INPUT_PAGE_NUMBER_LABEL = "page Number";
  public static final String INPUT_PAGE_NUMBER_EXPLANATION = "Page number start at 0";

  public static final String INPUT_PAGE_SIZE = "pageSize";
  public static final String INPUT_PAGE_SIZE_LABEL = "Page size";
  public static final String INPUT_PAGE_SIZE_EXPLANATION = "Number of records per page";

  public static final String INPUT_FUNCTION_TRANSFORMERS = "transformers";
  public static final String INPUT_FUNCTION_TRANSFORMERS_LABEL = "Transformers";
  public static final String INPUT_FUNCTION_TRANSFORMERS_EXPLANATION = "Give a list of functions to transform the CSV source in Java Object";

  public static final String INPUT_FIELDS_RESULT = "fieldsResult";
  public static final String INPUT_FIELDS_RESULT_LABEL = "Fields Result";
  public static final String INPUT_FIELDS_RESULT_EXPLANATION = "List the field to be produce in the result. if empty, all fields in the source (CSV, ListofObject) are save";

  public static final String INPUT_STORAGEDEFINITION_RESULT = "storageDefinition";
  public static final String INPUT_STORAGEDEFINITION_LABEL = "Storage Definition";
  public static final String INPUT_STORAGEDEFINITION_EXPLANATION = "Where to save CSV file";

  public static final String INPUT_STORAGEDEFINITION_FOLDER_COMPLEMENT_RESULT = "storageDefinitionFolderComplement";
  public static final String INPUT_STORAGEDEFINITION_FOLDER_COMPLEMENT_LABEL = "Storage Folder Complement";
  public static final String INPUT_STORAGEDEFINITION_FOLDER_COMPLEMENT_EXPLANATION = "In case of a Folder Storage, information on the folder";

  public static final String INPUT_STORAGEDEFINITION_CMIS_COMPLEMENT_RESULT = "storageDefinitionCmisFolder";
  public static final String INPUT_STORAGEDEFINITION_CMIS_COMPLEMENT_LABEL = "Storage Cmis complement";
  public static final String INPUT_STORAGEDEFINITION_CMIS_COMPLEMENT_EXPLANATION = "In case of CMIS storage, information to connect the CMIS repository";

  public static final String INPUT_FILENAME = "fileName";
  public static final String INPUT_FILENAME_LABEL = "File Name";
  public static final String INPUT_FILENAME_EXPLANATION = "File Name used to create the file";

  public static final String INPUT_RECORDS = "records";
  public static final String INPUT_RECORDS_LABEL = "records";
  public static final String INPUT_RECORDS_EXPLANATION = "Records to write to the CSV. List of Map";

  private String csvFunction;
  private String sourceFile;
  private String charSet;
  private String separator;
  private Map<String, Object> filter;
  private int pageNumber;
  private int pageSize;

  private List<Map<String, Object>> records;

  private Map<String, String> transformers;
  private List<String> fieldsResult;
  private String storageDefinition;
  private String storageDefinitionFolderComplement;
  private String storageDefinitionCmisComplement;

  private String fileName;

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

  public Map<String, String> getTransformers() {
    return transformers;
  }

  public List<String> getFieldsResult() {
    return fieldsResult;
  }

  public String getStorageDefinition() {
    return storageDefinition;
  }

  public String getStorageDefinitionFolderComplement() {
    return storageDefinitionFolderComplement;
  }

  public String getStorageDefinitionCmisComplement() {
    return storageDefinitionCmisComplement;
  }

  public String getFileName() {
    return fileName;
  }

  public List<Map<String, Object>> getRecords() {
    return records;
  }

  @Override
  public List<Map<String, Object>> getInputParameters() {
    return ParameterToolbox.getInputParameters();
  }
}
