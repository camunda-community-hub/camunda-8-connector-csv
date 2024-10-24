package io.camunda.connector.csv.function;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.csv.CsvFunction;
import io.camunda.connector.csv.CsvInput;
import io.camunda.connector.csv.CsvOutput;
import io.camunda.connector.csv.toolbox.CsvDefinition;
import io.camunda.connector.csv.toolbox.CsvError;
import io.camunda.connector.csv.toolbox.CsvFile;
import io.camunda.connector.csv.toolbox.CsvMatcher;
import io.camunda.connector.csv.toolbox.CsvSubFunction;
import io.camunda.connector.csv.toolbox.CvsCollector;
import io.camunda.filestorage.FileVariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class GetCsvPropertiesFunction implements CsvSubFunction {

  private final Logger logger = LoggerFactory.getLogger(GetCsvPropertiesFunction.class.getName());

  @Override
  public CsvOutput executeSubFunction(CsvInput csvInput, OutboundConnectorContext context) throws ConnectorException {

    CsvOutput csvOutput = new CsvOutput();
    try {
      FileVariableReference fileVariableReference = FileVariableReference.fromJson(csvInput.getSourceFile());
      CsvFile sourceFile = new CsvFile();
      sourceFile.readSourceFile(fileVariableReference, csvInput.getCharSet(), csvInput.getSeparator());
      CsvDefinition csvDefinition = sourceFile.getDefinition();
      if (csvDefinition.getHeader() == null)
        throw new ConnectorException(CsvError.NO_HEADER);
      csvOutput.csvHeader = sourceFile.getDefinition().getHeader();
      CsvMatcher matcher = CsvMatcher.getFromRecord(csvInput.getFilter());

      CollectorProperty collector = new CollectorProperty();
      sourceFile.processFile(matcher, collector, false, false);
      csvOutput.numberOfRecords = collector.getNbRecords();
      csvOutput.totalNumberOfRecords = collector.getTotalNumberOfRecords();
      logger.info("GetCsvPropertiesFunction TotalNumberOfRecords[{}] RecordsFiltered[{}]",
          csvOutput.totalNumberOfRecords ,
          csvOutput.numberOfRecords);

      return csvOutput;
    } catch (ConnectorException ce) {
      throw ce;
    } catch (Exception e) {
      logger.error("Error during CSVGetProperties on  {}", e.getMessage());
      throw new ConnectorException(CsvError.GET_PROPERTIES, "Error during get-properties " + e.getMessage());
    }
  }

  @Override
  public List<RunnerParameter> getInputsParameter() {
    return Arrays.asList(RunnerParameter.getInstance(CsvInput.INPUT_SOURCE_FILE, // name
        CsvInput.INPUT_SOURCE_FILE_LABEL, // label
        Object.class, // class
        RunnerParameter.Level.REQUIRED, // level
        CsvInput.INPUT_SOURCE_FILE_EXPLANATION)

    );
  }

  @Override
  public List<RunnerParameter> getOutputsParameter() {
    return Arrays.asList(RunnerParameter.getInstance(CsvOutput.OUTPUT_USER_ID, //
            CsvOutput.OUTPUT_USER_ID_LABEL, //
            String.class, //
            "", //
            RunnerParameter.Level.OPTIONAL, //
            CsvOutput.OUTPUT_USER_ID_EXPLANATION), //

        RunnerParameter.getInstance(CsvOutput.OUTPUT_STATUS, //
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
    return Map.of(CsvFunction.ERROR_UNKNOWN_FUNCTION, CsvFunction.ERROR_UNKNOWN_FUNCTION_LABEL);

  }

  @Override
  public String getSubFunctionName() {
    return "Get properties";
  }

  @Override
  public String getSubFunctionDescription() {
    return "Get properties on the file";
  }

  @Override
  public String getSubFunctionType() {
    return "get-properties";
  }

  /**
   * Collector to count the number of records which pass the filter
   */
  private static class CollectorProperty extends CvsCollector {
    private int nbRecords = 0;

    @Override
    public void collect(Map<String, Object> recordCsv) {
      nbRecords++;
    }

    public int getNbRecords() {
      return nbRecords;
    }
  }

}
