package io.camunda.connector.csv.function;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ReadCsvToVariableFunction implements CsvSubFunction {

  private final Logger logger = LoggerFactory.getLogger(ReadCsvToVariableFunction.class.getName());

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

      // Read each lines
      csvOutput.csvHeader = csvDefinition.getHeader();

      CsvMatcher matcher = new CsvMatcher();
      if (csvInput.getFilter() != null)
        matcher.addMatcher(csvInput.getFilter());

      // pagination?

      CollectorVariable collector = new CollectorVariable();
      collector.pageNumber = csvInput.getPageNumber();
      collector.pageSize = csvInput.getPageSize();

      sourceFile.processFile(matcher, collector, true, true);
      csvOutput.records = collector.listRecords;
      csvOutput.numberOfRecords = collector.getNumberOfRecords();
      csvOutput.totalNumberOfRecords = collector.getTotalNumberOfRecords();
      logger.info("ReadToVariable numberOfRecords[{}]", csvOutput.records.size());

      return csvOutput;
    } catch (ConnectorException ce) {
      throw ce;
    } catch (Exception e) {
      logger.error("Error during ReadCsvToVariableFunction  {}", e.getMessage());
      throw new ConnectorException(CsvError.CANT_READ_FILE, "Error during update-user " + e.getMessage());
    }
  }

  @Override
  public List<RunnerParameter> getInputsParameter() {
    return Arrays.asList();//

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
            CsvOutput.OUTPUT_DATE_OPERATION_EXPLANATION));
  }

  @Override
  public Map<String, String> getSubFunctionListBpmnErrors() {
    return Map.of(CsvError.CANT_READ_FILE, CsvError.CANT_READ_FILE_EXPLANATION); //

  }

  @Override
  public String getSubFunctionName() {
    return "ReadCSV";
  }

  @Override
  public String getSubFunctionDescription() {
    return "Read a CSV file to a process variable";
  }

  @Override
  public String getSubFunctionType() {
    return "read-csv";
  }

  /**
   * Collector to count the number of records which pass the filter
   */
  private class CollectorVariable extends CvsCollector {
    public List<Map<String, Object>> listRecords = new ArrayList();

    public int pageNumber;
    public int pageSize;
    public int numberOfRecords = 0;

    @Override
    public void collect(Map<String, Object> record) {
      numberOfRecords++;
      if (numberOfRecords <= pageSize * pageNumber)
        return;
      if (numberOfRecords > (pageSize) * (pageNumber+1))
        return;
      listRecords.add(record);
    }

    public int getNumberOfRecords() {
      return numberOfRecords;
    }
  }

}