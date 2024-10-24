package io.camunda.connector.csv;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.camunda.connector.cherrytemplate.CherryOutput;
import io.camunda.connector.csv.toolbox.ParameterToolbox;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)

public class CsvOutput implements CherryOutput {

  public static final String OUTPUT_STATUS = "status";
  public static final String OUTPUT_STATUS_LABEL = "Status";
  public static final String OUTPUT_STATUS_EXPLANATION = "Status of operation";
  public static final String OUTPUT_DATE_OPERATION = "dateOperation";
  public static final String OUTPUT_DATE_OPERATION_LABEL = "Date of operation";
  public static final String OUTPUT_DATE_OPERATION_EXPLANATION = "Date when the operation is done in Keycloak";
  public static final String OUTPUT_USER_ID = "userId";
  public static final String OUTPUT_USER_ID_LABEL = "User Id";
  public static final String OUTPUT_USER_ID_EXPLANATION = "User Id created by keycloak";
  public static final String OUTPUT_USER = "user";
  public static final String OUTPUT_USER_LABEL = "User";
  public static final String OUTPUT_USER_EXPLANATION = "Keycloak user";
  public static final String OUTPUT_LIST_USERS = "listUsers";
  public static final String OUTPUT_LIST_USERS_LABEL = "List Users";
  public static final String OUTPUT_LIST_USERS_EXPLANATION = "List of users found by the search";

  public List<Map<String, Object>> records;

  public List<String> csvHeader;
  /* number of records which pass the filter */
  public int numberOfRecords;
  /* number of lines in the file */
  public int totalNumberOfRecords;

  @Override
  public List<Map<String, Object>> getOutputParameters() {
    return ParameterToolbox.getOutputParameters();
  }

}
