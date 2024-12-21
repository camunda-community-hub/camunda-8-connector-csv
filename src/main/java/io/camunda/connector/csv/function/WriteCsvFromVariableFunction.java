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
import io.camunda.connector.csv.streamer.DataRecordStreamer;
import io.camunda.connector.csv.streamer.PaginationStreamer;
import io.camunda.connector.csv.streamer.SelectorStreamer;
import io.camunda.connector.csv.toolbox.CsvDefinition;
import io.camunda.connector.csv.toolbox.CsvError;
import io.camunda.connector.csv.toolbox.CsvProcessor;
import io.camunda.connector.csv.toolbox.CsvSubFunction;
import io.camunda.connector.csv.transformer.DataRecordTransformer;
import io.camunda.connector.csv.transformer.FieldListTransformer;
import io.camunda.connector.csv.transformer.MapperTransformer;
import io.camunda.filestorage.FileRepoFactory;
import io.camunda.filestorage.FileVariable;
import io.camunda.filestorage.FileVariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class WriteCsvFromVariableFunction implements CsvSubFunction {

    private final Logger logger = LoggerFactory.getLogger(WriteCsvFromVariableFunction.class.getName());

    @Override
    public CsvOutput executeSubFunction(CsvInput csvInput, OutboundConnectorContext context) throws ConnectorException {

        CsvOutput csvOutput = new CsvOutput();

        try {
            FileVariable fileVariable = csvInput.initializeOutputFileVariable();

            FileRepoFactory fileRepoFactory = FileRepoFactory.getInstance();

            List<DataRecordStreamer> listStreamers = new ArrayList<>();
            SelectorStreamer matcher = new SelectorStreamer().addMatcher(csvInput.getFilter());
            if (matcher.isMatcherActive())
                listStreamers.add(matcher);

            if (csvInput.isPaginationActive()) {
                PaginationStreamer paginationStreamer = new PaginationStreamer(csvInput.getPageNumber(),
                        csvInput.getPageSize());
                listStreamers.add(paginationStreamer);
            }
            // ListTransformers
            List<DataRecordTransformer> listTransformers = new ArrayList<>();
            if (csvInput.getMappers() != null) {
                MapperTransformer mapperTransformer = new MapperTransformer();
                mapperTransformer.setTransformerMap(csvInput.getMappers());
                listTransformers.add(mapperTransformer);
            }
            listTransformers.add(new FieldListTransformer(csvInput.getFieldsResult()));

            CsvDefinition csvDefinition = CsvDefinition.fromFields(csvInput.getFieldsResult(), csvInput.getSeparator());

            // Process the file
            ProducerMemory producer = new ProducerMemory(csvDefinition, csvInput.getInputRecords());

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
                logger.error("Can't store CSV {}", e.getMessage());
                throw new ConnectorException(CsvError.CANT_STORE_CSV, e.getMessage());

            }
            csvOutput.numberOfRecords = collector.getNumberOfRecords();
            csvOutput.totalNumberOfRecords = producer.getNumberOfRecords();
        } catch (ConnectorException ce) {
            throw ce;
        } catch (Exception e) {
            logger.error("Error during CSV ReadFile {}", e.getMessage());

            throw new ConnectorException(CsvError.CANT_PROCESS_CSV, e.getMessage());
        }

        return csvOutput;
    }

    @Override
    public List<RunnerParameter> getInputsParameter() {
        return Arrays.asList(
                RunnerParameter.getInstance(CsvInput.FIELDS_RESULT, //
                        CsvInput.FIELDS_RESULT_LABEL, //
                        String.class, //
                        null, //
                        RunnerParameter.Level.OPTIONAL, //
                        CsvInput.FIELDS_RESULT_EXPLANATION),
                RunnerParameter.getInstance(CsvInput.MAPPERS, //
                        CsvInput.MAPPERS_LABEL, //
                        String.class, //
                        null, //
                        RunnerParameter.Level.OPTIONAL, //
                        CsvInput.MAPPERS_EXPLANATION),
                RunnerParameter.getInstance(CsvInput.PAGE_NUMBER, //
                        CsvInput.PAGE_NUMBER_LABEL, //
                        Integer.class, //
                        "0", //
                        RunnerParameter.Level.OPTIONAL, //
                        CsvInput.PAGE_NUMBER_EXPLANATION //
                ).setGroup(CsvInput.GROUP_PAGINATION),

                RunnerParameter.getInstance(CsvInput.PAGE_SIZE, //
                        CsvInput.PAGE_SIZE_LABEL, //
                        Integer.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        CsvInput.PAGE_SIZE_EXPLANATION //
                ).setGroup(CsvInput.GROUP_PAGINATION),
                RunnerParameter.getInstance(CsvInput.SEPARATOR, //
                        CsvInput.SEPARATOR_LABEL, //
                        String.class, //
                        CsvInput.SEPARATOR_DEFAULT, //
                        RunnerParameter.Level.OPTIONAL, //
                        CsvInput.SEPARATOR_EXPLANATION //
                ).setGroup(CsvInput.GROUP_PRODUCER),
                RunnerParameter.getInstance(CsvInput.CHARSET, //
                        CsvInput.CHARSET_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        CsvInput.CHARSET_EXPLANATION //
                ).setGroup(CsvInput.GROUP_PRODUCER),
                RunnerParameter.getInstance(CsvInput.OUTPUT_STORAGE_DEFINITION, //
                        CsvInput.OUTPUT_STORAGE_DEFINITION_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.REQUIRED, //
                        CsvInput.OUTPUT_STORAGE_DEFINITION_EXPLANATION)

                );

    }

    @Override
    public List<RunnerParameter> getOutputsParameter() {
        return Arrays.asList(RunnerParameter.getInstance(CsvOutput.FILEVARIABLEREFERENCE, //
                        CsvOutput.FILEVARIABLEREFERENCE_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.REQUIRED, //
                        CsvOutput.FILEVARIABLEREFERENCE_EXPLANATION),
                RunnerParameter.getInstance(CsvOutput.NUMBEROFRECORDS, //
                        CsvOutput.NUMBEROFRECORDS_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        CsvOutput.NUMBEROFRECORDS_EXPLANATION), //

                RunnerParameter.getInstance(CsvOutput.TOTALNUMBEROFRECORDS, //
                        CsvOutput.TOTALNUMBEROFRECORDS_LABEL, //
                        Date.class, //
                        null, //
                        RunnerParameter.Level.OPTIONAL, //
                        CsvOutput.TOTALNUMBEROFRECORDS_EXPLANATION)//
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
