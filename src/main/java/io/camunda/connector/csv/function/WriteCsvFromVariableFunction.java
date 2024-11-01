package io.camunda.connector.csv.function;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.csv.CsvInput;
import io.camunda.connector.csv.CsvOutput;
import io.camunda.connector.csv.collector.CollectorContentStore;
import io.camunda.connector.csv.content.ContentStore;
import io.camunda.connector.csv.content.ContentStoreFile;
import io.camunda.connector.csv.producer.ProducerMemory;
import io.camunda.connector.csv.streamer.CompositeMatcherStreamer;
import io.camunda.connector.csv.streamer.DataRecordStreamer;
import io.camunda.connector.csv.streamer.PaginationStreamer;
import io.camunda.connector.csv.toolbox.CsvDefinition;
import io.camunda.connector.csv.toolbox.CsvError;
import io.camunda.connector.csv.toolbox.CsvProcessor;
import io.camunda.connector.csv.toolbox.CsvSubFunction;
import io.camunda.connector.csv.transformer.DataRecordTransformer;
import io.camunda.connector.csv.transformer.FieldListTransformer;
import io.camunda.connector.csv.transformer.FunctionTransformer;
import io.camunda.filestorage.FileRepoFactory;
import io.camunda.filestorage.FileVariable;
import io.camunda.filestorage.FileVariableReference;
import io.camunda.filestorage.StorageDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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

    StorageDefinition storageDefinition;
    try {
      storageDefinition = StorageDefinition.getFromString(csvInput.getStorageDefinition());
      storageDefinition.complement = csvInput.getStorageDefinitionFolderComplement();
      if (storageDefinition.complement != null && storageDefinition.complement.isEmpty())
        storageDefinition.complement = null;

      storageDefinition.complementInObject = csvInput.getStorageDefinitionCmisComplement();
    } catch (Exception e) {
      throw new ConnectorException(CsvError.BAD_STORAGE_DEFINITION);
    }

    try {
      FileVariable fileVariable = new FileVariable();

      fileVariable.setStorageDefinition(storageDefinition);
      fileVariable.setName(csvInput.getFileName());
      fileVariable.setMimeType("text/csv");
      FileRepoFactory fileRepoFactory = FileRepoFactory.getInstance();

      List<DataRecordStreamer> listStreamers = new ArrayList<>();
      CompositeMatcherStreamer matcher = CompositeMatcherStreamer.getFromRecord(csvInput.getFilter());
      if (matcher.isMatcherActive())
        listStreamers.add(matcher);

      if (csvInput.isPaginationActive()) {
        PaginationStreamer paginationStreamer = new PaginationStreamer(csvInput.getPageNumber(),
            csvInput.getPageSize());
        listStreamers.add(paginationStreamer);
      }
      // ListTransformer
      List<DataRecordTransformer> listTransformers = new ArrayList<>();
      listTransformers.add(new FunctionTransformer(csvInput.getTransformers()));
      listTransformers.add(new FieldListTransformer(csvInput.getFieldsResult()));

      CsvDefinition csvDefinition = CsvDefinition.fromFields(csvInput.getFieldsResult(), csvInput.getSeparator());
      if (csvDefinition.getHeader().isEmpty())
        throw new ConnectorException(CsvError.NO_HEADER, "No fieldResult in the input");
      // Process the file

      ProducerMemory producer = new ProducerMemory(csvDefinition, csvInput.getRecords());

      ContentStore contentStore = new ContentStoreFile(fileVariable, csvInput.getCharSet());
      CollectorContentStore collector = new CollectorContentStore(csvDefinition, contentStore, csvInput.getSeparator());

      // process data now
      CsvProcessor csvProcessor = new CsvProcessor();
      csvProcessor.processProducerToCollector(producer, listStreamers, listTransformers, collector);

      // Write the file now
      try {
        FileVariableReference fileVariableReference = fileRepoFactory.saveFileVariable(fileVariable);
        csvOutput.fileVariableReference = fileVariableReference.toJson();
      } catch (Exception e) {
        logger.error("Can't store CSV {]", e.getMessage());
        throw new ConnectorException(CsvError.CANT_STORE_CSV, e.getMessage());

      }
      csvOutput.numberOfRecords = collector.getNumberOfRecords();
      csvOutput.totalNumberOfRecords = producer.getNumberOfRecords();
    } catch (ConnectorException ce) {
      throw ce;
    } catch (Exception e) {
      logger.error("Error during CSV ReadFile {]", e.getMessage());

      throw new ConnectorException(CsvError.CANT_PROCESS_CSV, e.getMessage());
    }

    return csvOutput;
  }

  @Override
  public List<RunnerParameter> getInputsParameter() {
    return Collections.emptyList();
  }

  @Override
  public List<RunnerParameter> getOutputsParameter() {
    return Arrays.asList(RunnerParameter.getInstance(CsvOutput.OUTPUT_FILEVARIABLEREFERENCE, //
            CsvOutput.OUTPUT_FILEVARIABLEREFERENCE_LABEL, //
            String.class, //
            "", //
            RunnerParameter.Level.REQUIRED, //
            CsvOutput.OUTPUT_FILEVARIABLEREFERENCE_EXPLANATION),
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
            CsvOutput.OUTPUT_TOTALNUMBEROFRECORDS_EXPLANATION)//
    );

  }

  @Override
  public Map<String, String> getSubFunctionListBpmnErrors() {
    return Map.of(CsvError.CANT_READ_FILE, CsvError.CANT_READ_FILE_EXPLANATION); //
  }

  @Override
  public String getSubFunctionName() {
    return "writeCsv";
  }

  @Override
  public String getSubFunctionDescription() {
    return "Write a new CSV document";
  }

  @Override
  public String getSubFunctionType() {
    return "write-csv";
  }

}
