package io.camunda.connector.csv.function;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.csv.CsvFunction;
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

public class UpdateCsvFunction implements CsvSubFunction {

  private final Logger logger = LoggerFactory.getLogger(UpdateCsvFunction.class.getName());

  @Override
  public CsvOutput executeSubFunction(CsvInput csvInput, OutboundConnectorContext context) throws ConnectorException {

    CsvOutput csvOutput = new CsvOutput();

    try {

      return csvOutput;
    } catch (ConnectorException ce) {
      throw ce;
    } catch (Exception e) {
      logger.error("Error during UpdateUser on {}", e.getMessage());
      throw new ConnectorException(CsvError.CANT_READ_FILE, "Error during update-user " + e.getMessage());
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
    return Map.of(CsvError.CANT_READ_FILE, CsvError.CANT_READ_FILE_EXPLANATION); //
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