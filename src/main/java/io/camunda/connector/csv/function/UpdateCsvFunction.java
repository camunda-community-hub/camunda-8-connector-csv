package io.camunda.connector.csv.function;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.csv.CsvFunction;
import io.camunda.connector.csv.CsvInput;
import io.camunda.connector.csv.CsvOutput;
import io.camunda.connector.csv.toolbox.KeycloakOperation;
import io.camunda.connector.csv.toolbox.CsvSubFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class UpdateCsvFunction implements CsvSubFunction {

  private final Logger logger = LoggerFactory.getLogger(UpdateCsvFunction.class.getName());

  @Override
  public CsvOutput executeSubFunction(CsvInput csvInput,
                                      OutboundConnectorContext context) throws ConnectorException {

    CsvOutput csvOutput = new CsvOutput();

    try {

      csvOutput.status = "SUCCESS";
      csvOutput.dateOperation = new Date();
      return csvOutput;
    } catch (ConnectorException ce) {
      throw ce;
    } catch (Exception e) {
      logger.error("Error during UpdateUser on {}", e.getMessage());
      throw new ConnectorException(KeycloakOperation.ERROR_UPDATE_USER, "Error during update-user " + e.getMessage());
    }
  }

  @Override
  public List<RunnerParameter> getInputsParameter() {
    return Collections.emptyList();
  }

  @Override
  public List<RunnerParameter> getOutputsParameter() {
    return Arrays.asList(RunnerParameter.getInstance(CsvOutput.OUTPUT_STATUS, //
            CsvOutput.OUTPUT_STATUS_LABEL, //
            String.class, //
            "", //
            RunnerParameter.Level.OPTIONAL, //
            CsvOutput.OUTPUT_STATUS_EXPLANATION), //

        RunnerParameter.getInstance(CsvOutput.OUTPUT_DATE_OPERATION, //
            CsvOutput.OUTPUT_DATE_OPERATION_LABEL, //
            Date.class, //
            null, //
            RunnerParameter.Level.OPTIONAL, //
            CsvOutput.OUTPUT_DATE_OPERATION_EXPLANATION)); //
  }

  @Override
  public Map<String, String> getSubFunctionListBpmnErrors() {
    return Map.of(KeycloakOperation.ERROR_KEYCLOAK_CONNECTION, KeycloakOperation.ERROR_KEYCLOAK_CONNECTION_LABEL, //
        CsvFunction.ERROR_UNKNOWN_FUNCTION, CsvFunction.ERROR_UNKNOWN_FUNCTION_LABEL, //
        KeycloakOperation.ERROR_UPDATE_USER, KeycloakOperation.ERROR_UPDATE_USER_LABEL, //
        KeycloakOperation.ERROR_CANT_UPDATE_USERNAME, KeycloakOperation.ERROR_CANT_UPDATE_USERNAME_LABEL, //
        KeycloakOperation.ERROR_UNKNOWN_USERID, KeycloakOperation.ERROR_UNKNOWN_USERID_LABEL, //
        KeycloakOperation.ERROR_USER_SET_PASSWORD, KeycloakOperation.ERROR_USER_SET_PASSWORD_LABEL);
  }

  @Override
  public String getSubFunctionName() {
    return "updateUser";
  }

  @Override
  public String getSubFunctionDescription() {
    return "Update a user in Keycloak";
  }

  @Override
  public String getSubFunctionType() {
    return "update-user";
  }

  private boolean containsInformation(Object value) {
    if (value == null)
      return false;
    if (value instanceof String valueSt && valueSt.trim().isEmpty())
      return false;
    return true;
  }
}