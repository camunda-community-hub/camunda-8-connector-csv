package io.camunda.connector.csv.function;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.csv.CsvFunction;
import io.camunda.connector.csv.CsvInput;
import io.camunda.connector.csv.CsvOutput;
import io.camunda.connector.csv.collector.CollectorProperty;
import io.camunda.connector.csv.content.ContentStore;
import io.camunda.connector.csv.content.ContentStoreFile;
import io.camunda.connector.csv.producer.ProducerContentStore;
import io.camunda.connector.csv.streamer.CompositeMatcherStreamer;
import io.camunda.connector.csv.streamer.DataRecordStreamer;
import io.camunda.connector.csv.toolbox.CsvError;
import io.camunda.connector.csv.toolbox.CsvProcessor;
import io.camunda.connector.csv.toolbox.CsvSubFunction;
import io.camunda.filestorage.FileVariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

      List<DataRecordStreamer> listStreamers = new ArrayList<>();
      CompositeMatcherStreamer matcher = CompositeMatcherStreamer.getFromRecord(csvInput.getFilter());
      listStreamers.add(matcher);

      // ----------- process the file now
      ContentStore contentStore = new ContentStoreFile(fileVariableReference, csvInput.getCharSet());
      ProducerContentStore producer = new ProducerContentStore(csvInput.getSeparator(), contentStore);

      CollectorProperty collector = new CollectorProperty();

      CsvProcessor cvsFile = new CsvProcessor();
      cvsFile.processProducerToCollector(producer, listStreamers, Collections.emptyList(),
          collector);

      csvOutput.csvHeader = producer.getCsvDefinition().getHeader();

      csvOutput.numberOfRecords = collector.getNbRecords();
      csvOutput.totalNumberOfRecords = collector.getTotalNumberOfRecords();
      logger.info("GetCsvPropertiesFunction TotalNumberOfRecords[{}] RecordsFiltered[{}]",
          csvOutput.totalNumberOfRecords, csvOutput.numberOfRecords);

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
    return Arrays.asList(RunnerParameter.getInstance(CsvOutput.OUTPUT_CSVHEADER, //
            CsvOutput.OUTPUT_CSVHEADER_LABEL, //
            String.class, //
            "", //
            RunnerParameter.Level.OPTIONAL, //
            CsvOutput.OUTPUT_CSVHEADER_EXPLANATION), //

        RunnerParameter.getInstance(CsvOutput.OUTPUT_NUMBEROFRECORDS, //
            CsvOutput.OUTPUT_NUMBEROFRECORDS_LABEL, //
            String.class, //
            "", //
            RunnerParameter.Level.OPTIONAL, //
            CsvOutput.OUTPUT_NUMBEROFRECORDS_EXPLANATION), //

        RunnerParameter.getInstance(CsvOutput.OUTPUT_TOTALNUMBEROFRECORDS, //
            CsvOutput.OUTPUT_TOTALNUMBEROFRECORDS_LABEL, //
            Date.class, //
            null, //
            RunnerParameter.Level.OPTIONAL, //
            CsvOutput.OUTPUT_TOTALNUMBEROFRECORDS_EXPLANATION)); //
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

}
