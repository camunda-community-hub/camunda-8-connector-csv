package io.camunda.connector.csv.function;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.csv.CsvInput;
import io.camunda.connector.csv.CsvOutput;
import io.camunda.connector.csv.collector.CollectorContentStore;
import io.camunda.connector.csv.collector.CollectorListMap;
import io.camunda.connector.csv.collector.CvsCollector;
import io.camunda.connector.csv.content.ContentStore;
import io.camunda.connector.csv.content.ContentStoreFile;
import io.camunda.connector.csv.filter.DataRecordFilter;
import io.camunda.connector.csv.filter.PaginationFilter;
import io.camunda.connector.csv.filter.SelectorFilter;
import io.camunda.connector.csv.toolbox.CsvDefinition;
import io.camunda.connector.csv.toolbox.CsvError;
import io.camunda.connector.csv.toolbox.CsvProcessor;
import io.camunda.connector.csv.toolbox.CsvSubFunction;
import io.camunda.connector.csv.transformer.*;
import io.camunda.filestorage.FileRepoFactory;
import io.camunda.filestorage.FileVariable;
import io.camunda.filestorage.FileVariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ProcessCsvFunction implements CsvSubFunction {

    private final Logger logger = LoggerFactory.getLogger(ProcessCsvFunction.class.getName());

    @Override
    public CsvOutput executeSubFunction(CsvInput csvInput, OutboundConnectorContext context) throws ConnectorException {
        CsvOutput csvOutput = new CsvOutput();
        try {
            // Reader
            CsvInput.ReaderEngine readerEngine = csvInput.initializeInputReader();

            //-------------- List Filters
            List<DataRecordFilter> listFilters = new ArrayList<>();

            SelectorFilter filter = new SelectorFilter().addFilter(csvInput.getFilter());

            if (filter.isEnabled())
                listFilters.add(filter);

            // pagination?
            PaginationFilter paginationFilter = new PaginationFilter(csvInput);
            if (paginationFilter.isEnabled()) {
                listFilters.add(paginationFilter);
            }


            //--------------- ListTransformers
            List<DataRecordTransformer> listTransformers = new ArrayList<>();

            // the setTransformerMap can throw an ConnectorException if the transformation is not correct
            OperationsTransformer operationsTransformer = new OperationsTransformer(csvInput);
            if (operationsTransformer.isEnabled()) {
                listTransformers.add(operationsTransformer);
            }

            FieldListTransformer fieldListTransformer = new FieldListTransformer(csvInput);
            if (fieldListTransformer.isEnabled())
                listTransformers.add(fieldListTransformer);


            //--- -- instantiate the preprocessor Read the variables who will update the flow
            MatcherTransformer matcherTransformer = new MatcherTransformer(csvInput);
            if (matcherTransformer.isEnabled())
                listTransformers.add(matcherTransformer);


            //------ Collector
            FileRepoFactory fileRepoFactory = FileRepoFactory.getInstance();
            FileVariable fileVariableOutput = null;
            CvsCollector collector;
            if (csvInput.getOutputTypeWriter() == CsvInput.TypeStorage.RECORDS) {
                collector = new CollectorListMap();
                csvOutput.records = ((CollectorListMap) collector).listRecords;
            } else if (csvInput.getOutputTypeWriter() == CsvInput.TypeStorage.FILE) {
                // Collect: where we write the result
                fileVariableOutput = csvInput.initializeOutputFileVariable();
                ContentStore contentStoreOutput = new ContentStoreFile(fileVariableOutput, csvInput.getOutputCharSet());
                collector = new CollectorContentStore(readerEngine.csvDefinition(), contentStoreOutput, csvInput.getOutputSeparator());
            } else {
                throw new ConnectorException(CsvError.UNSUPPORTED_TYPE_STORAGE, "TypeStorage[" + csvInput.getOutputTypeWriter() + "]");
            }
            // ----------- process the file now
            CsvProcessor csvProcessor = new CsvProcessor();
            csvProcessor.processProducerToCollector(readerEngine.csvProducer(), listFilters, listTransformers, collector);


            readerEngine.csvProducer().end();

            //---------------- write the file now
            if (csvInput.getOutputTypeWriter() == CsvInput.TypeStorage.RECORDS) {
                csvOutput.records = ((CollectorListMap) collector).listRecords;
            } else if (csvInput.getOutputTypeWriter() == CsvInput.TypeStorage.FILE) {
                try {
                    FileVariableReference fileVariableOutputReference = fileRepoFactory.saveFileVariable(fileVariableOutput);
                    csvOutput.fileVariableReference = fileVariableOutputReference.toJson();
                } catch (Exception e) {
                    // Log here because we get more information to log
                    logger.error("Can't store CSV {}", e.getMessage());
                    throw new ConnectorException(CsvError.CANT_STORE_CSV, e.getMessage());
                }
            }

            // Collect statistics
            csvOutput.csvHeader = readerEngine.csvDefinition().getHeader();
            csvOutput.numberOfRecords = matcherTransformer.getNbRecordsUpdated();
            csvOutput.totalNumberOfRecords = collector.getTotalNumberOfRecords();
            logger.info("ReadToVariable numberOfRecords[{}] Streamers in {} ms, Transformer in {} ms",
                    collector.getTotalNumberOfRecords(), csvProcessor.getCumulStreamersTime(), csvProcessor.getCumulTransformersTime());

            return csvOutput;
        } catch (ConnectorException ce) {
            throw ce;
        } catch (Exception e) {
            // Log here because we get more information to log
            logger.error("Error during ReadCsvToVariableFunction  {}", e.getMessage());
            throw new ConnectorException(CsvError.CANT_PROCESS_CSV, "Error during process-csv " + e.getMessage());
        }
    }

    @Override
    public List<RunnerParameter> getInputsParameter() {
        return Arrays.asList(
                RunnerParameter.getInstance(CsvInput.INPUT_TYPE_READER, //
                                CsvInput.INPUT_TYPE_READER_LABEL, //
                                String.class, //
                                CsvInput.TypeStorage.FILE.name(), //
                                RunnerParameter.Level.REQUIRED, //
                                CsvInput.INPUT_TYPE_READER_EXPLANATION //
                        ).addChoice(CsvInput.TypeStorage.FILE.name(), CsvInput.TypeStorage.FILE.name())
                        .addChoice(CsvInput.TypeStorage.RECORDS.name(), CsvInput.TypeStorage.RECORDS.name())
                        .setGroup(CsvInput.GROUP_SOURCE),

                RunnerParameter.getInstance(CsvInput.INPUT_READER_FILESTORAGE, //
                                CsvInput.INPUT_READER_FILESTORAGE_LABEL, //
                                String.class, //
                                "", //
                                RunnerParameter.Level.REQUIRED, //
                                CsvInput.INPUT_READER_FILESTORAGE_EXPLANATION //
                        ).addCondition(CsvInput.INPUT_TYPE_READER, List.of(CsvInput.TypeStorage.FILE.name()))
                        .setGroup(CsvInput.GROUP_SOURCE),

                RunnerParameter.getInstance(CsvInput.INPUT_RECORDS, //
                                CsvInput.INPUT_RECORDS_LABEL, //
                                List.class, //
                                "", //
                                RunnerParameter.Level.REQUIRED, //
                                CsvInput.INPUT_RECORDS_EXPLANATION //
                        ).addCondition(CsvInput.INPUT_TYPE_READER, List.of(CsvInput.TypeStorage.RECORDS.name()))
                        .setGroup(CsvInput.GROUP_SOURCE),


                RunnerParameter.getInstance(CsvInput.INPUT_CHARSET, //
                                CsvInput.INPUT_CHARSET_LABEL, //
                                String.class, //
                                CsvInput.INPUT_CHARSET_DEFAULT, //
                                RunnerParameter.Level.REQUIRED, //
                                CsvInput.INPUT_CHARSET_EXPLANATION //
                        ).addCondition(CsvInput.INPUT_TYPE_READER, List.of(CsvInput.TypeStorage.FILE.name()))
                        .setGroup(CsvInput.GROUP_SOURCE),

                RunnerParameter.getInstance(CsvInput.INPUT_SEPARATOR, //
                                CsvInput.INPUT_SEPARATOR_LABEL, //
                                String.class, //
                                CsvInput.INPUT_SEPARATOR_DEFAULT, //
                                RunnerParameter.Level.REQUIRED, //
                                CsvInput.INPUT_SEPARATOR_EXPLANATION //
                        ).addCondition(CsvInput.INPUT_TYPE_READER, List.of(CsvInput.TypeStorage.FILE.name()))
                        .setGroup(CsvInput.GROUP_SOURCE),

                RunnerParameter.getInstance(CsvInput.PAGINATION_ENABLED, //
                        CsvInput.PAGINATION_ENABLED_LABEL, //
                        Boolean.class, //
                        Boolean.FALSE, //
                        RunnerParameter.Level.REQUIRED, //
                        CsvInput.PAGINATION_ENABLED_EXPLANATION //
                ).setGroup(CsvInput.GROUP_PAGINATION),


                RunnerParameter.getInstance(CsvInput.PAGE_NUMBER, //
                                CsvInput.PAGE_NUMBER_LABEL, //
                                Integer.class, //
                                "0", //
                                RunnerParameter.Level.REQUIRED, //
                                CsvInput.PAGE_NUMBER_EXPLANATION //
                        ).addCondition(CsvInput.PAGINATION_ENABLED, List.of("true"))
                        .setGroup(CsvInput.GROUP_PAGINATION),

                RunnerParameter.getInstance(CsvInput.PAGE_SIZE, //
                                CsvInput.PAGE_SIZE_LABEL, //
                                Integer.class, //
                                "", //
                                RunnerParameter.Level.REQUIRED, //
                                CsvInput.PAGE_SIZE_EXPLANATION //
                        ).addCondition(CsvInput.PAGINATION_ENABLED, List.of("true"))
                        .setGroup(CsvInput.GROUP_PAGINATION),

                RunnerParameter.getInstance(CsvInput.FILTER, //
                                CsvInput.FILTER_LABEL, //
                                String.class, //
                                null, //
                                RunnerParameter.Level.OPTIONAL, //
                                CsvInput.FILTER_EXPLANATION)
                        .setGroup(CsvInput.GROUP_PROCESSING),

                RunnerParameter.getInstance(CsvInput.FIELDS_RESULT, //
                        CsvInput.FIELDS_RESULT_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        CsvInput.FIELDS_RESULT_EXPLANATION //
                ).setGroup(CsvInput.GROUP_PROCESSING),

                RunnerParameter.getInstance(CsvInput.OPERATIONS_TRANSFORMER, //
                        CsvInput.OPERATIONS_TRANSFORMER_LABEL, //
                        List.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        CsvInput.OPERATIONS_TRANSFORMER_EXPLANATION + " " + OperationsTransformer.getMapperExplanation() //
                ).setGroup(CsvInput.GROUP_PROCESSING),

                RunnerParameter.getInstance(CsvInput.MATCHER_ENABLED, //
                                CsvInput.MATCHER_ENABLED_LABEL, //
                                Boolean.class, //
                                "", //
                                RunnerParameter.Level.REQUIRED, //
                                CsvInput.MATCHER_ENABLED_EXPLANATION //
                        )
                        .setGroup(CsvInput.GROUP_UPDATE),

                RunnerParameter.getInstance(CsvInput.MATCHERS_RECORDS, //
                                CsvInput.MATCHERS_RECORDS_LABEL, //
                                List.class, //
                                "", //
                                RunnerParameter.Level.REQUIRED, //
                                CsvInput.MATCHERS_RECORDS_EXPLANATION //
                        ).setGroup(CsvInput.GROUP_UPDATE)
                        .addCondition(CsvInput.MATCHER_ENABLED, List.of("true")),

                RunnerParameter.getInstance(CsvInput.MATCHER_KEY_FIELDS, //
                                CsvInput.MATCHER_KEY_FIELDS_LABEL, //
                                List.class, //
                                "", //
                                RunnerParameter.Level.REQUIRED, //
                                CsvInput.MATCHER_KEY_FIELDS_EXPLANATION //
                        ).setGroup(CsvInput.GROUP_UPDATE)
                        .addCondition(CsvInput.MATCHER_ENABLED, List.of("true")),

                RunnerParameter.getInstance(CsvInput.MATCHER_POLICY, //
                                CsvInput.MATCHER_POLICY_LABEL, //
                                String.class, //
                                CsvInput.MatcherPolicy.MULTIPLE.name(), //
                                RunnerParameter.Level.REQUIRED, //
                                CsvInput.MATCHER_POLICY_EXPLANATION //
                        ).addChoice(CsvInput.MatcherPolicy.MULTIPLE.name(), CsvInput.MatcherPolicy.MULTIPLE.name())
                        .addChoice(CsvInput.MatcherPolicy.SINGLEORNONE.name(), CsvInput.MatcherPolicy.SINGLEORNONE.name())
                        .addChoice(CsvInput.MatcherPolicy.SINGLE.name(), CsvInput.MatcherPolicy.SINGLE.name())
                        .setGroup(CsvInput.GROUP_UPDATE)
                        .addCondition(CsvInput.MATCHER_ENABLED, List.of("true")),

                RunnerParameter.getInstance(CsvInput.OUTPUT_TYPE_WRITER, //
                                CsvInput.OUTPUT_TYPE_STORAGE_LABEL, //
                                String.class, //
                                CsvInput.TypeStorage.FILE.name(), //
                                RunnerParameter.Level.REQUIRED, //
                                CsvInput.OUTPUT_TYPE_STORAGE_EXPLANATION //
                        ).addChoice(CsvInput.TypeStorage.FILE.name(), CsvInput.TypeStorage.FILE.name())
                        .addChoice(CsvInput.TypeStorage.RECORDS.name(), CsvInput.TypeStorage.RECORDS.name())
                        .setGroup(CsvInput.GROUP_OUTCOME),

                RunnerParameter.getInstance(CsvInput.OUTPUT_WRITER_FILESTORAGE, //
                                CsvInput.OUTPUT_WRITER_FILESTORAGE_LABEL, //
                                String.class, //
                                "", //
                                RunnerParameter.Level.REQUIRED, //
                                CsvInput.OUTPUT_WRITER_FILESTORAGE_EXPLANATION //
                        ).addCondition(CsvInput.OUTPUT_TYPE_WRITER, List.of(CsvInput.TypeStorage.FILE.name()))
                        .setGroup(CsvInput.GROUP_OUTCOME),


                RunnerParameter.getInstance(CsvInput.OUTPUT_FILENAME, //
                                CsvInput.OUTPUT_FILENAME_LABEL, //
                                String.class, //
                                "", //
                                RunnerParameter.Level.REQUIRED, //
                                CsvInput.OUTPUT_FILENAME_EXPLANATION //
                        ).addCondition(CsvInput.OUTPUT_TYPE_WRITER, List.of(CsvInput.TypeStorage.FILE.name()))
                        .setGroup(CsvInput.GROUP_OUTCOME),

                RunnerParameter.getInstance(CsvInput.OUTPUT_CHARSET, //
                                CsvInput.OUTPUT_CHARSET_LABEL, //
                                String.class, //
                                CsvInput.INPUT_CHARSET_DEFAULT, //
                                RunnerParameter.Level.REQUIRED, //
                                CsvInput.OUTPUT_CHARSET_EXPLANATION //
                        ).addCondition(CsvInput.OUTPUT_TYPE_WRITER, List.of(CsvInput.TypeStorage.FILE.name()))
                        .setGroup(CsvInput.GROUP_OUTCOME),

                RunnerParameter.getInstance(CsvInput.OUTPUT_SEPARATOR, //
                                CsvInput.OUTPUT_SEPARATOR_LABEL, //
                                String.class, //
                                CsvInput.OUTPUT_SEPARATOR_DEFAULT, //
                                RunnerParameter.Level.REQUIRED, //
                                CsvInput.OUTPUT_SEPARATOR_EXPLANATION //
                        ).addCondition(CsvInput.OUTPUT_TYPE_WRITER, List.of(CsvInput.TypeStorage.FILE.name()))
                        .setGroup(CsvInput.GROUP_OUTCOME)


        );
    }

    @Override
    public List<RunnerParameter> getOutputsParameter() {
        return Arrays.asList(
                RunnerParameter.getInstance(CsvOutput.RECORDS, //
                                CsvOutput.RECORDS_LABEL, //
                                List.class, //
                                "", //
                                RunnerParameter.Level.REQUIRED, //
                                CsvOutput.RECORDS_EXPLANATION)
                        .addCondition(CsvInput.OUTPUT_TYPE_WRITER, List.of(CsvInput.TypeStorage.RECORDS.name())), //

                RunnerParameter.getInstance(CsvOutput.CSVHEADER, //
                        CsvOutput.CSVHEADER_LABEL, //
                        List.class, //
                        "", //
                        RunnerParameter.Level.REQUIRED, //
                        CsvOutput.CSVHEADER_EXPLANATION),

                RunnerParameter.getInstance(CsvOutput.NUMBEROFRECORDS, //
                        CsvOutput.NUMBEROFRECORDS_LABEL, //
                        Integer.class, //
                        "", //
                        RunnerParameter.Level.REQUIRED, //
                        CsvOutput.NUMBEROFRECORDS_EXPLANATION),

                RunnerParameter.getInstance(CsvOutput.TOTALNUMBEROFRECORDS, //
                        CsvOutput.TOTALNUMBEROFRECORDS_LABEL, //
                        Integer.class, //
                        "", //
                        RunnerParameter.Level.REQUIRED, //
                        CsvOutput.TOTALNUMBEROFRECORDS_EXPLANATION));


    }

    @Override
    public Map<String, String> getSubFunctionListBpmnErrors() {
        Map<String, String> combinedMap = new HashMap<>();

        combinedMap.putAll(CsvInput.getBpmnErrors());
        combinedMap.putAll(CsvProcessor.getBpmnErrors());
        combinedMap.putAll(ContentStoreFile.getBpmnErrors());
        combinedMap.putAll(CsvDefinition.getBpmnErrors());
        combinedMap.putAll(OperationDefinition.getBpmnErrors());
        combinedMap.putAll(OperationsTransformer.getBpmnErrors());
        combinedMap.putAll(MatcherTransformer.getBpmnErrors());
        return combinedMap;
    }


    @Override
    public String getSubFunctionName() {
        return "Process CSV";
    }

    @Override
    public String getSubFunctionDescription() {
        return "Process a CSV document";
    }

    @Override
    public String getSubFunctionType() {
        return "process-csv";
    }



}