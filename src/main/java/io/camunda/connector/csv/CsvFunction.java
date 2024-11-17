package io.camunda.connector.csv;

import io.camunda.connector.api.annotation.OutboundConnector;
import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.api.outbound.OutboundConnectorFunction;
import io.camunda.connector.cherrytemplate.CherryConnector;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.csv.function.GetCsvPropertiesFunction;
import io.camunda.connector.csv.function.ReadCsvToVariableFunction;
import io.camunda.connector.csv.function.UpdateCsvFunction;
import io.camunda.connector.csv.function.WriteCsvFromVariableFunction;
import io.camunda.connector.csv.toolbox.CsvSubFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OutboundConnector(name = "CVSFunction", inputVariables = { CsvInput.INPUT_CSV_FUNCTION, CsvInput.INPUT_CHARSET, //
    CsvInput.INPUT_SEPARATOR, //
    CsvInput.INPUT_SOURCE_FILE, //
    CsvInput.INPUT_FILTER, //
    CsvInput.INPUT_FUNCTION_TRANSFORMERS, //
    CsvInput.INPUT_FIELDS_RESULT, //
    CsvInput.INPUT_PAGE_NUMBER, //
    CsvInput.INPUT_PAGE_SIZE, CsvInput.INPUT_STORAGEDEFINITION_RESULT, //
    CsvInput.INPUT_STORAGEDEFINITION_FOLDER_COMPLEMENT_RESULT, //
    CsvInput.INPUT_RECORDS, //
    CsvInput.INPUT_FILENAME, //
    CsvInput.INPUT_STORAGEDEFINITION_CMIS_COMPLEMENT_RESULT }, type = "c-csv-function")

public class CsvFunction implements OutboundConnectorFunction, CherryConnector {
  public static final String ERROR_UNKNOWN_FUNCTION = "UNKNOWN_FUNCTION";
  public static final String ERROR_UNKNOWN_FUNCTION_LABEL = "The function is unknown. There is a limited number of operation";
  private static final List<Class<?>> allFunctions = Arrays.asList(GetCsvPropertiesFunction.class, // add User
      ReadCsvToVariableFunction.class, // removeUser
      WriteCsvFromVariableFunction.class, // Search User
      UpdateCsvFunction.class); // Update user
  private static final String WORKER_LOGO = "data:image/svg+xml;base64,PHN2ZyBoZWlnaHQ9IjY0IiB2aWV3Qm94PSIwIDAgNTYgNjQiIHdpZHRoPSI1NiIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cGF0aCBjbGlwLXJ1bGU9ImV2ZW5vZGQiIGQ9Im01LjEwNiAwYy0yLjgwMiAwLTUuMDczIDIuMjcyLTUuMDczIDUuMDc0djUzLjg0MWMwIDIuODAzIDIuMjcxIDUuMDc0IDUuMDczIDUuMDc0aDQ1Ljc3NGMyLjgwMSAwIDUuMDc0LTIuMjcxIDUuMDc0LTUuMDc0di0zOC42MDVsLTE4LjkwMy0yMC4zMWgtMzEuOTQ1eiIgZmlsbD0iIzQ1YjA1OCIgZmlsbC1ydWxlPSJldmVub2RkIi8+PHBhdGggZD0ibTIwLjMwNiA0My4xOTdjLjEyNi4xNDQuMTk4LjMyNC4xOTguNTIyIDAgLjM3OC0uMzA2LjcyLS43MDMuNzItLjE4IDAtLjM3OC0uMDcyLS41MDQtLjIzNC0uNzAyLS44NDYtMS44OTEtMS4zODctMy4wMDctMS4zODctMi42MjkgMC00LjYyNyAyLjAxNy00LjYyNyA0Ljg4IDAgMi44NDUgMS45OTkgNC44NzkgNC42MjcgNC44NzkgMS4xMzQgMCAyLjI1LS40ODYgMy4wMDctMS4zNjkuMTI1LS4xNDQuMzI0LS4yMzMuNTA0LS4yMzMuNDE1IDAgLjcwMy4zNTkuNzAzLjczOCAwIC4xOC0uMDcyLjM2LS4xOTguNTA0LS45MzcuOTcyLTIuMjE1IDEuNjkzLTQuMDE1IDEuNjkzLTMuNDU3IDAtNi4xNzYtMi41MjEtNi4xNzYtNi4yMTJzMi43MTktNi4yMTIgNi4xNzYtNi4yMTJjMS44LjAwMSAzLjA5Ni43MjEgNC4wMTUgMS43MTF6bTYuODAyIDEwLjcxNGMtMS43ODIgMC0zLjE4Ny0uNTk0LTQuMjEzLTEuNDk1LS4xNjItLjE0NC0uMjM0LS4zNDItLjIzNC0uNTQgMC0uMzYxLjI3LS43NTcuNzAyLS43NTcuMTQ0IDAgLjMwNi4wMzYuNDMyLjE0NC44MjguNzM5IDEuOTggMS4zMTQgMy4zNjcgMS4zMTQgMi4xNDMgMCAyLjgyNy0xLjE1MiAyLjgyNy0yLjA3MSAwLTMuMDk3LTcuMTEyLTEuMzg2LTcuMTEyLTUuNjcyIDAtMS45OCAxLjc2NC0zLjMzMSA0LjEyMy0zLjMzMSAxLjU0OCAwIDIuODgxLjQ2NyAzLjg1MyAxLjI3OC4xNjIuMTQ0LjI1Mi4zNDIuMjUyLjU0IDAgLjM2LS4zMDYuNzItLjcwMy43Mi0uMTQ0IDAtLjMwNi0uMDU0LS40MzItLjE2Mi0uODgyLS43Mi0xLjk4LTEuMDQ0LTMuMDc5LTEuMDQ0LTEuNDQgMC0yLjQ2Ny43NzQtMi40NjcgMS45MDkgMCAyLjcwMSA3LjExMiAxLjE1MiA3LjExMiA1LjYzNi4wMDEgMS43NDgtMS4xODcgMy41MzEtNC40MjggMy41MzF6bTE2Ljk5NC0xMS4yNTQtNC4xNTkgMTAuMzM1Yy0uMTk4LjQ4Ni0uNjg1LjgxLTEuMTg4LjgxaC0uMDM2Yy0uNTIyIDAtMS4wMDgtLjMyNC0xLjIwNy0uODFsLTQuMTQyLTEwLjMzNWMtLjAzNi0uMDktLjA1NC0uMTgtLjA1NC0uMjg4IDAtLjM2LjMyMy0uNzkzLjgxLS43OTMuMzA2IDAgLjU5NC4xOC43Mi40ODZsMy44ODkgOS45OTIgMy44ODktOS45OTJjLjEwOC0uMjg4LjM5Ni0uNDg2LjcyLS40ODYuNDY4IDAgLjgxLjM3OC44MS43OTMuMDAxLjA5LS4wMTcuMTk4LS4wNTIuMjg4eiIgZmlsbD0iI2ZmZiIvPjxnIGNsaXAtcnVsZT0iZXZlbm9kZCIgZmlsbC1ydWxlPSJldmVub2RkIj48cGF0aCBkPSJtNTYuMDAxIDIwLjM1N3YxaC0xMi44cy02LjMxMi0xLjI2LTYuMTI4LTYuNzA3YzAgMCAuMjA4IDUuNzA3IDYuMDAzIDUuNzA3eiIgZmlsbD0iIzM0OWM0MiIvPjxwYXRoIGQ9Im0zNy4wOTguMDA2djE0LjU2MWMwIDEuNjU2IDEuMTA0IDUuNzkxIDYuMTA0IDUuNzkxaDEyLjhsLTE4LjkwNC0yMC4zNTJ6IiBmaWxsPSIjZmZmIiBvcGFjaXR5PSIuNSIvPjwvZz48L3N2Zz4=";
  private final Logger logger = LoggerFactory.getLogger(CsvFunction.class.getName());

  /**
   * Return the common parameters to all sub functions
   *
   * @return list of common parameters
   */
  public static List<RunnerParameter> getInputCommonParameters() {
    return Collections.emptyList();
  }

  public static List<Class<?>> getAllFunctions() {
    return allFunctions;
  }

  @Override
  public CsvOutput execute(OutboundConnectorContext outboundConnectorContext) throws ConnectorException {

    CsvInput csvInput = outboundConnectorContext.bindVariables(CsvInput.class);
    // search the sub-function referenced
    String function = csvInput.getCsvFunction();
    long beginTime = System.currentTimeMillis();

    // connect (a Connector exception will be thrown in case of issue)
    for (CsvSubFunction inputSubFunction : getListSubFunctions()) {
      if (inputSubFunction.getSubFunctionType().equals(function)) {
        CsvOutput csvOutput = inputSubFunction.executeSubFunction(csvInput, outboundConnectorContext);
        logger.info("CsvFunction End function [{}] in {} ms", function, System.currentTimeMillis() - beginTime);
        return csvOutput;
      }
    }

    throw new ConnectorException(ERROR_UNKNOWN_FUNCTION, "CSV Function Unknown [" + function + "]");
  }

  @Override
  public String getDescription() {
    return "CSV operation (add/remove user";
  }

  @Override
  public String getLogo() {
    return WORKER_LOGO;
  }

  @Override
  public String getCollectionName() {
    return "CSV";
  }

  @Override
  public Map<String, String> getListBpmnErrors() {
    Map<String, String> allErrors = new HashMap<>();
    allErrors.put(ERROR_UNKNOWN_FUNCTION, ERROR_UNKNOWN_FUNCTION_LABEL);

    for (CsvSubFunction subFunction : getListSubFunctions()) {
      allErrors.putAll(subFunction.getSubFunctionListBpmnErrors());
    }

    return allErrors;
  }

  @Override
  public Class<?> getInputParameterClass() {
    return CsvInput.class;
  }

  @Override
  public Class<?> getOutputParameterClass() {
    return CsvOutput.class;
  }

  @Override
  public List<String> getAppliesTo() {
    return List.of("bpmn:ServiceTask");
  }

  public List<CsvSubFunction> getListSubFunctions() {
    List<CsvSubFunction> listSubFunction = new ArrayList<>();

    for (Class<?> classFunction : allFunctions) {
      try {
        Constructor<?> constructor = classFunction.getConstructor();
        CsvSubFunction inputSubFunction = (CsvSubFunction) constructor.newInstance();
        listSubFunction.add(inputSubFunction);
      } catch (Exception e) {
        logger.error("Can't call a constructor on {} : {}", classFunction.getName(), e.toString());
      }

    }
    return listSubFunction;
  }
}
