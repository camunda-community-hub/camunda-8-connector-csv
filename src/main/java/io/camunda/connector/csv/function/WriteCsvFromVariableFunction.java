package io.camunda.connector.csv.function;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.csv.CsvInput;
import io.camunda.connector.csv.CsvOutput;
import io.camunda.connector.csv.toolbox.CsvError;
import io.camunda.connector.csv.toolbox.CsvSubFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class WriteCsvFromVariableFunction implements CsvSubFunction {

  private final Logger logger = LoggerFactory.getLogger(WriteCsvFromVariableFunction.class.getName());

  @Override
  public CsvOutput executeSubFunction(CsvInput csvInput, OutboundConnectorContext context) throws ConnectorException {

    CsvOutput csvOutput = new CsvOutput();

    return csvOutput;
  }

  @Override
  public List<RunnerParameter> getInputsParameter() {
    return Collections.emptyList();
  }

  @Override
  public List<RunnerParameter> getOutputsParameter() {
    return Arrays.asList(RunnerParameter.getInstance(CsvOutput.OUTPUT_LIST_USERS, //
            CsvOutput.OUTPUT_LIST_USERS_LABEL, //
            String.class, //
            "", //
            RunnerParameter.Level.OPTIONAL, //
            CsvOutput.OUTPUT_LIST_USERS_EXPLANATION), //

        RunnerParameter.getInstance(CsvOutput.OUTPUT_USER_ID, //
            CsvOutput.OUTPUT_USER_ID_LABEL, //
            Date.class, //
            null, //
            RunnerParameter.Level.OPTIONAL, //
            CsvOutput.OUTPUT_USER_ID_EXPLANATION), //

        RunnerParameter.getInstance(CsvOutput.OUTPUT_USER, //
            CsvOutput.OUTPUT_USER_LABEL, //
            Date.class, //
            null, //
            RunnerParameter.Level.OPTIONAL, //
            CsvOutput.OUTPUT_USER_EXPLANATION));

  }

  @Override
  public Map<String, String> getSubFunctionListBpmnErrors() {
    return Map.of(CsvError.CANT_READ_FILE, CsvError.CANT_READ_FILE_EXPLANATION); //
  }

  @Override
  public String getSubFunctionName() {
    return "searchUser";
  }

  @Override
  public String getSubFunctionDescription() {
    return "Search users in Keycloak by it's ID, or name/firstName/lastName/email";
  }

  @Override
  public String getSubFunctionType() {
    return "search-users";
  }

}
