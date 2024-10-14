package io.camunda.connector.csv.toolbox;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.csv.CsvInput;
import io.camunda.connector.csv.CsvOutput;

import java.util.List;
import java.util.Map;

public interface CsvSubFunction {
  CsvOutput executeSubFunction(CsvInput pdfInput,
                               OutboundConnectorContext context) throws ConnectorException;

  List<RunnerParameter> getInputsParameter();

  List<RunnerParameter> getOutputsParameter();

  Map<String, String> getSubFunctionListBpmnErrors();

  String getSubFunctionName();

  String getSubFunctionDescription();

  String getSubFunctionType();

}
