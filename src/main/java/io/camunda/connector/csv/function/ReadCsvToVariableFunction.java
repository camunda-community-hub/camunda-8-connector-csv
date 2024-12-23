package io.camunda.connector.csv.function;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.csv.CsvInput;
import io.camunda.connector.csv.CsvOutput;
import io.camunda.connector.csv.collector.CollectorListMap;
import io.camunda.connector.csv.content.ContentStore;
import io.camunda.connector.csv.content.ContentStoreFile;
import io.camunda.connector.csv.producer.ProducerContentStore;
import io.camunda.connector.csv.streamer.DataRecordStreamer;
import io.camunda.connector.csv.streamer.PaginationStreamer;
import io.camunda.connector.csv.streamer.SelectorStreamer;
import io.camunda.connector.csv.toolbox.CsvError;
import io.camunda.connector.csv.toolbox.CsvProcessor;
import io.camunda.connector.csv.toolbox.CsvSubFunction;
import io.camunda.connector.csv.transformer.DataRecordTransformer;
import io.camunda.connector.csv.transformer.FieldListTransformer;
import io.camunda.connector.csv.transformer.MapperTransformer;
import io.camunda.filestorage.FileVariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ReadCsvToVariableFunction implements CsvSubFunction {

    private final Logger logger = LoggerFactory.getLogger(ReadCsvToVariableFunction.class.getName());

    @Override
    public CsvOutput executeSubFunction(CsvInput csvInput, OutboundConnectorContext context) throws ConnectorException {
        CsvOutput csvOutput = new CsvOutput();
        try {

            // Producer
            FileVariableReference fileVariableReference = FileVariableReference.fromJson(csvInput.getSourceFile());
            ContentStore contentStore = new ContentStoreFile(fileVariableReference, csvInput.getInputCharSet());
            ProducerContentStore producer = new ProducerContentStore(csvInput.getInputSeparator(), contentStore);

            // List Streamers
            List<DataRecordStreamer> listStreamers = new ArrayList<>();
            SelectorStreamer matcher = new SelectorStreamer().addMatcher(csvInput.getFilter());
            if (matcher.isMatcherActive())
                listStreamers.add(matcher);

            // pagination?
            if (csvInput.isPaginationActive()) {
                PaginationStreamer paginationStreamer = new PaginationStreamer(csvInput.getPageNumber(),
                        csvInput.getPageSize());
                listStreamers.add(paginationStreamer);
            }
            // ListTransformers
            List<DataRecordTransformer> listTransformers = new ArrayList<>();

            // the setTransformerMap can throw an ConnectorException if the transformation is not correct
            if (csvInput.getMappersTransformers() != null) {
                MapperTransformer mapperTransformer = new MapperTransformer();
                mapperTransformer.setTransformerMap(csvInput.getMappersTransformers());
                listTransformers.add(mapperTransformer);
            }
            listTransformers.add(new FieldListTransformer(csvInput.getFieldsResult()));

            // Collector
            CollectorListMap collector = new CollectorListMap();

            // ----------- process the file now
            CsvProcessor cvsProcessor = new CsvProcessor();
            cvsProcessor.processProducerToCollector(producer, listStreamers, listTransformers, collector);

            // Read each lines
            csvOutput.csvHeader = producer.getCsvDefinition().getHeader();
            csvOutput.records = collector.listRecords;
            csvOutput.numberOfRecords = collector.getCollectedNumberOfRecords();
            csvOutput.totalNumberOfRecords = collector.getTotalNumberOfRecords();
            csvOutput.fileVariableReference = fileVariableReference.toJson();
            logger.info("ReadToVariable numberOfRecords[{}] Streamers in {} ms, Transformer in {} ms",
                    csvOutput.records.size(), cvsProcessor.getCumulStreamersTime(), cvsProcessor.getCumulTransformersTime());

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
        return Arrays.asList(
                RunnerParameter.getInstance(CsvInput.SOURCE_FILE, //
                                CsvInput.SOURCE_FILE_LABEL, //
                                String.class, //
                                "", //
                                RunnerParameter.Level.REQUIRED, //
                                CsvInput.SOURCE_FILE_EXPLANATION)
                        .setGroup(CsvInput.GROUP_SOURCE),

                RunnerParameter.getInstance(CsvInput.INPUT_CHARSET, //
                        CsvInput.INPUT_CHARSET_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        CsvInput.INPUT_CHARSET_EXPLANATION //
                ).setGroup(CsvInput.GROUP_SOURCE),

                RunnerParameter.getInstance(CsvInput.INPUT_SEPARATOR, //
                        CsvInput.INPUT_SEPARATOR_LABEL, //
                        String.class, //
                        CsvInput.INPUT_SEPARATOR_DEFAULT, //
                        RunnerParameter.Level.OPTIONAL, //
                        CsvInput.INPUT_SEPARATOR_EXPLANATION //
                ).setGroup(CsvInput.GROUP_SOURCE),
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
                RunnerParameter.getInstance(CsvInput.FILTER, //
                        CsvInput.FILTER_LABEL, //
                        String.class, //
                        null, //
                        RunnerParameter.Level.OPTIONAL, //
                        CsvInput.FILTER_EXPLANATION),
                RunnerParameter.getInstance(CsvInput.MAPPERS_TRANSFORMERS, //
                                CsvInput.MAPPERS_TRANSFORMERS_LABEL, //
                                String.class, //
                                null, //
                                RunnerParameter.Level.OPTIONAL, //
                                CsvInput.MAPPERS_TRANSFORMERS_EXPLANATION)
                        .setGroup(CsvInput.GROUP_PROCESSING),
                RunnerParameter.getInstance(CsvInput.FIELDS_RESULT, //
                                CsvInput.FIELDS_RESULT_LABEL, //
                                String.class, //
                                null, //
                                RunnerParameter.Level.OPTIONAL, //
                                CsvInput.FIELDS_RESULT_EXPLANATION)
                        .setGroup(CsvInput.GROUP_PROCESSING));
    }

    @Override
    public List<RunnerParameter> getOutputsParameter() {
        return Arrays.asList(RunnerParameter.getInstance(CsvOutput.RECORDS, //
                        CsvOutput.RECORDS_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.REQUIRED, //
                        CsvOutput.RECORDS_EXPLANATION), //

                RunnerParameter.getInstance(CsvOutput.CSVHEADER, //
                        CsvOutput.CSVHEADER_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        CsvOutput.CSVHEADER_EXPLANATION), //

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
                        CsvOutput.TOTALNUMBEROFRECORDS_EXPLANATION),

                RunnerParameter.getInstance(CsvOutput.FILEVARIABLEREFERENCE, //
                        CsvOutput.FILEVARIABLEREFERENCE_LABEL, //
                        Date.class, //
                        null, //
                        RunnerParameter.Level.OPTIONAL, //
                        CsvOutput.FILEVARIABLEREFERENCE_EXPLANATION));
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

}