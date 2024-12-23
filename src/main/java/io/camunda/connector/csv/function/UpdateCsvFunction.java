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
import io.camunda.connector.csv.producer.CsvProducer;
import io.camunda.connector.csv.producer.DataRecordContainer;
import io.camunda.connector.csv.producer.ProducerContentStore;
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
import io.camunda.connector.csv.transformer.OperationDefinition;
import io.camunda.filestorage.FileRepoFactory;
import io.camunda.filestorage.FileVariable;
import io.camunda.filestorage.FileVariableReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class UpdateCsvFunction implements CsvSubFunction {

    private final Logger logger = LoggerFactory.getLogger(UpdateCsvFunction.class.getName());

    @Override
    public CsvOutput executeSubFunction(CsvInput csvInput, OutboundConnectorContext context) throws ConnectorException {CsvOutput csvOutput = new CsvOutput();
        try {
            // Producer (the reader)
            CsvInput.ProducerRecord producerRecord = csvInput.initializeInputProducer();


            //-------------- List Streamers
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


            //--------------- ListTransformers
            List<DataRecordTransformer> listTransformers = new ArrayList<>();

            // the setTransformerMap can throw an ConnectorException if the transformation is not correct
            if (csvInput.getMappersTransformers() != null) {
                MapperTransformer mapperTransformer = new MapperTransformer();
                mapperTransformer.setTransformerMap(csvInput.getMappersTransformers());
                listTransformers.add(mapperTransformer);
            }

            if (csvInput.getFieldsResult() != null)
                listTransformers.add(new FieldListTransformer(csvInput.getFieldsResult()));


            //--- -- instantiate the preprocessor Read the variables who will update the flow
            SelectorStreamer selectorUpdateRecord = new SelectorStreamer();
            if (csvInput.getUpdateMatchersRecords() != null)
                for (Map<String, Object> updateRecord : csvInput.getUpdateMatchersRecords()) {
                    selectorUpdateRecord.addMatcher(updateRecord);
                }
            selectorUpdateRecord.addKeyFields(csvInput.getKeyFields().stream().collect(Collectors.toSet()));
            CsvProcessorUpdate cvsProcessor = new CsvProcessorUpdate(selectorUpdateRecord, csvInput.getUpdatePolicy());


            //------ Collector
            FileRepoFactory fileRepoFactory = FileRepoFactory.getInstance();
            FileVariable fileVariableOutput = null;
            CvsCollector collector;
            if (csvInput.getOutputTypeStorage() == CsvInput.TypeStorage.PROCESSVARIABLE) {
                collector = new CollectorListMap();
                csvOutput.records = ((CollectorListMap) collector).listRecords;
            } else if (csvInput.getOutputTypeStorage() == CsvInput.TypeStorage.STORAGE) {
                // Collect: where we write the result
                fileVariableOutput = csvInput.initializeOutputFileVariable();
                ContentStore contentStoreOutput = new ContentStoreFile(fileVariableOutput, csvInput.getOutputCharSet());
                collector = new CollectorContentStore(producerRecord.csvDefinition(), contentStoreOutput, csvInput.getOutputSeparator());
            } else {
                throw new ConnectorException(CsvError.UNSUPPORTED_TYPE_STORAGE, "TypeStorage[" + csvInput.getOutputTypeStorage() + "]");
            }
            // ----------- process the file now
            cvsProcessor.processProducerToCollector(producerRecord.csvProducer(), listStreamers, listTransformers, collector);


            producerRecord.csvProducer().end();

            //---------------- write the file now
            if (csvInput.getOutputTypeStorage() == CsvInput.TypeStorage.PROCESSVARIABLE) {
                csvOutput.records = ((CollectorListMap) collector).listRecords;
            } else if (csvInput.getOutputTypeStorage() == CsvInput.TypeStorage.STORAGE) {
                try {
                    FileVariableReference fileVariableOutputReference = fileRepoFactory.saveFileVariable(fileVariableOutput);
                    csvOutput.fileVariableReference = fileVariableOutputReference.toJson();
                } catch (Exception e) {
                    logger.error("Can't store CSV {}", e.getMessage());
                    throw new ConnectorException(CsvError.CANT_STORE_CSV, e.getMessage());
                }
            }

            // Collect statistics
            csvOutput.csvHeader = producerRecord.csvDefinition().getHeader();
            csvOutput.numberOfRecords = cvsProcessor.getNbRecordsUpdated();
            csvOutput.totalNumberOfRecords = collector.getTotalNumberOfRecords();
            logger.info("ReadToVariable numberOfRecords[{}] Streamers in {} ms, Transformer in {} ms",
                    collector.getTotalNumberOfRecords(), cvsProcessor.getCumulStreamersTime(), cvsProcessor.getCumulTransformersTime());

            return csvOutput;
        } catch (ConnectorException ce) {
            throw ce;
        } catch (Exception e) {
            logger.error("Error during ReadCsvToVariableFunction  {}", e.getMessage());
            throw new ConnectorException(CsvError.CANT_PROCESS_CSV, "Error during update-user " + e.getMessage());
        }
    }

    @Override
    public List<RunnerParameter> getInputsParameter() {
        return Arrays.asList(
                RunnerParameter.getInstance(CsvInput.INPUT_TYPE_STORAGE, //
                                CsvInput.INPUT_TYPE_STORAGE_LABEL, //
                                String.class, //
                                CsvInput.TypeStorage.STORAGE.name(), //
                                RunnerParameter.Level.REQUIRED, //
                                CsvInput.INPUT_TYPE_STORAGE_EXPLANATION //
                        ).addChoice(CsvInput.TypeStorage.STORAGE.name(), CsvInput.TypeStorage.STORAGE.name())
                        .addChoice(CsvInput.TypeStorage.PROCESSVARIABLE.name(), CsvInput.TypeStorage.PROCESSVARIABLE.name())
                        .setGroup(CsvInput.GROUP_SOURCE),

                RunnerParameter.getInstance(CsvInput.INPUT_STORAGE_FILE, //
                                CsvInput.INPUT_STORAGE_FILE_LABEL, //
                                String.class, //
                                "", //
                                RunnerParameter.Level.REQUIRED, //
                                CsvInput.INPUT_STORAGE_FILE_EXPLANATION //
                        ).addCondition(CsvInput.INPUT_TYPE_STORAGE, List.of(CsvInput.TypeStorage.STORAGE.name()))
                        .setGroup(CsvInput.GROUP_SOURCE),

                RunnerParameter.getInstance(CsvInput.INPUT_STORAGE_RECORDS, //
                                CsvInput.INPUT_STORAGE_RECORDS_LABEL, //
                                List.class, //
                                "", //
                                RunnerParameter.Level.REQUIRED, //
                                CsvInput.INPUT_STORAGE_RECORDS_EXPLANATION //
                        ).addCondition(CsvInput.INPUT_TYPE_STORAGE, List.of(CsvInput.TypeStorage.PROCESSVARIABLE.name()))
                        .setGroup(CsvInput.GROUP_SOURCE),


                RunnerParameter.getInstance(CsvInput.INPUT_STORAGE_CHARSET, //
                        CsvInput.INPUT_CHARSET_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        CsvInput.INPUT_CHARSET_EXPLANATION //
                ).addCondition(CsvInput.INPUT_TYPE_STORAGE, List.of(CsvInput.TypeStorage.STORAGE.name()))
                        .setGroup(CsvInput.GROUP_SOURCE),

                RunnerParameter.getInstance(CsvInput.INPUT_STORAGE_SEPARATOR, //
                        CsvInput.INPUT_SEPARATOR_LABEL, //
                        String.class, //
                        CsvInput.INPUT_SEPARATOR_DEFAULT, //
                        RunnerParameter.Level.OPTIONAL, //
                        CsvInput.INPUT_SEPARATOR_EXPLANATION //
                ).addCondition(CsvInput.INPUT_TYPE_STORAGE, List.of(CsvInput.TypeStorage.STORAGE.name()))
                        .setGroup(CsvInput.GROUP_SOURCE),

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

                RunnerParameter.getInstance(CsvInput.FIELDS_RESULT, //
                        CsvInput.FIELDS_RESULT_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        CsvInput.FIELDS_RESULT_EXPLANATION //
                ).setGroup(CsvInput.GROUP_PROCESSING),

                RunnerParameter.getInstance(CsvInput.MAPPERS_TRANSFORMERS, //
                        CsvInput.MAPPERS_TRANSFORMERS_LABEL, //
                        List.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        CsvInput.MAPPERS_TRANSFORMERS_EXPLANATION + " " + MapperTransformer.getMapperExplanation() //
                ).setGroup(CsvInput.GROUP_PROCESSING),

                RunnerParameter.getInstance(CsvInput.UPDATE_MATCHERS_RECORDS, //
                                CsvInput.UPDATE_MATCHERS_RECORDS_LABEL, //
                                List.class, //
                                "", //
                                RunnerParameter.Level.OPTIONAL, //
                                CsvInput.UPDATE_MATCHERS_RECORDS_EXPLANATION //
                        )
                        .setGroup(CsvInput.GROUP_UPDATE),

                RunnerParameter.getInstance(CsvInput.UPDATE_KEY_FIELDS, //
                        CsvInput.UPDATE_KEY_FIELDS_LABEL, //
                        List.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        CsvInput.UPDATE_KEY_FIELDS_EXPLANATION //
                ).setGroup(CsvInput.GROUP_UPDATE),

                RunnerParameter.getInstance(CsvInput.UPDATE_POLICY, //
                        CsvInput.UPDATE_POLICY_LABEL, //
                        String.class, //
                        CsvInput.UpdatePolicy.MULTIPLE.name(), //
                        RunnerParameter.Level.REQUIRED, //
                        CsvInput.UPDATE_POLICY_EXPLANATION //
                ).addChoice(CsvInput.UpdatePolicy.MULTIPLE.name(),CsvInput.UpdatePolicy.MULTIPLE.name() )
                        .addChoice(CsvInput.UpdatePolicy.SINGLEORNONE.name(), CsvInput.UpdatePolicy.SINGLEORNONE.name())
                        .addChoice(CsvInput.UpdatePolicy.SINGLE.name(),CsvInput.UpdatePolicy.SINGLE.name())
                        .setGroup(CsvInput.GROUP_UPDATE),

                RunnerParameter.getInstance(CsvInput.OUTPUT_TYPE_STORAGE, //
                                CsvInput.OUTPUT_TYPE_STORAGE_LABEL, //
                                String.class, //
                                CsvInput.TypeStorage.STORAGE.name(), //
                                RunnerParameter.Level.REQUIRED, //
                                CsvInput.OUTPUT_TYPE_STORAGE_EXPLANATION //
                        ).addChoice(CsvInput.TypeStorage.STORAGE.name(), CsvInput.TypeStorage.STORAGE.name())
                        .addChoice(CsvInput.TypeStorage.PROCESSVARIABLE.name(), CsvInput.TypeStorage.PROCESSVARIABLE.name())
                        .setGroup(CsvInput.GROUP_OUTCOME),

                RunnerParameter.getInstance(CsvInput.OUTPUT_STORAGE_DEFINITION, //
                                CsvInput.OUTPUT_STORAGE_DEFINITION_LABEL, //
                                String.class, //
                                "", //
                                RunnerParameter.Level.REQUIRED, //
                                CsvInput.OUTPUT_STORAGE_DEFINITION_EXPLANATION //
                        ).addCondition(CsvInput.OUTPUT_TYPE_STORAGE, List.of(CsvInput.TypeStorage.STORAGE.name()))
                        .setGroup(CsvInput.GROUP_OUTCOME),


                RunnerParameter.getInstance(CsvInput.OUTPUT_FILENAME, //
                                CsvInput.OUTPUT_FILENAME_LABEL, //
                                String.class, //
                                "", //
                                RunnerParameter.Level.OPTIONAL, //
                                CsvInput.OUTPUT_FILENAME_EXPLANATION //
                        ).addCondition(CsvInput.OUTPUT_TYPE_STORAGE, List.of(CsvInput.TypeStorage.STORAGE.name()))
                        .setGroup(CsvInput.GROUP_OUTCOME),

                RunnerParameter.getInstance(CsvInput.OUTPUT_CHARSET, //
                        CsvInput.OUTPUT_CHARSET_LABEL, //
                        String.class, //
                        "", //
                        RunnerParameter.Level.OPTIONAL, //
                        CsvInput.OUTPUT_CHARSET_EXPLANATION //
                ).setGroup(CsvInput.GROUP_OUTCOME),

                RunnerParameter.getInstance(CsvInput.OUTPUT_SEPARATOR, //
                        CsvInput.OUTPUT_SEPARATOR_LABEL, //
                        String.class, //
                        CsvInput.OUTPUT_SEPARATOR_DEFAULT, //
                        RunnerParameter.Level.OPTIONAL, //
                        CsvInput.OUTPUT_SEPARATOR_EXPLANATION //
                ).setGroup(CsvInput.GROUP_OUTCOME)


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
                        CsvOutput.RECORDS_EXPLANATION), //

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
        combinedMap.put(
                CsvError.UNSUPPORTED_TYPE_STORAGE, CsvError.UNSUPPORTED_TYPE_STORAGE_EXPLANATION); //
        combinedMap.putAll(CsvProcessor.getBpmnErrors());
        combinedMap.putAll(CsvProcessorUpdate.getBpmnErrors());
        combinedMap.putAll(ContentStoreFile.getBpmnErrors());
        combinedMap.putAll(CsvDefinition.getBpmnErrors());
        combinedMap.putAll(OperationDefinition.getBpmnErrors());
        combinedMap.putAll(MapperTransformer.getBpmnErrors());
        return combinedMap;
    }


    @Override
    public String getSubFunctionName() {
        return "updateCsv";
    }

    @Override
    public String getSubFunctionDescription() {
        return "Update a CSF file";
    }

    @Override
    public String getSubFunctionType() {
        return "update-csv";
    }

    private boolean containsInformation(Object value) {
        if (value == null)
            return false;
        return !(value instanceof String valueSt) || !valueSt.trim().isEmpty();
    }


    /* ******************************************************************** */
    /*                                                                      */
    /*  CsvProcessorUpdate                                                  */
    /*                                                                      */
    /*  Implement the preprocessing : search match and complete             */
    /* ******************************************************************** */
    private class CsvProcessorUpdate extends CsvProcessor {
        SelectorStreamer selectorUpdateRecord;
        CsvInput.UpdatePolicy updatePolicy;
        int nbRecordsUpdated = 0;

        public CsvProcessorUpdate(SelectorStreamer selectorUpdateRecord, CsvInput.UpdatePolicy updatePolicy) {
            this.selectorUpdateRecord = selectorUpdateRecord;
            this.updatePolicy = updatePolicy;
        }

        public static Map<String, String> getBpmnErrors() {
            return Map.of(CsvError.ONE_RECORD_DOES_NOT_MATCH_UNIQUEEACH, CsvError.ONE_RECORD_DOES_NOT_MATCH_UNIQUEEACH_EXPLANATION,
                    CsvError.ONE_RECORD_DOES_NOT_MATCH_UNIQUE, CsvError.ONE_RECORD_DOES_NOT_MATCH_UNIQUE_EXPLANATION);
        }

        public int getNbRecordsUpdated() {
            return nbRecordsUpdated;
        }

        /**
         * Method is call before any transformation - so we can match and complete it on demande
         *
         * @param dataRecordContainer data to be process
         * @return the dataRecordContainer completed if needed
         */
        @Override
        public DataRecordContainer preTransformData(DataRecordContainer dataRecordContainer) {
            super.preTransformData(dataRecordContainer);
            List<Map<String, Object>> searchMatch = selectorUpdateRecord.searchMatch(dataRecordContainer.getDataRecord());
            if (updatePolicy == CsvInput.UpdatePolicy.SINGLE && searchMatch.size() != 1)
                throw new ConnectorException(CsvError.ONE_RECORD_DOES_NOT_MATCH_UNIQUEEACH, "Line [" + dataRecordContainer.lineNumber + "]");
            if (updatePolicy == CsvInput.UpdatePolicy.SINGLEORNONE && searchMatch.size() > 1)
                throw new ConnectorException(CsvError.ONE_RECORD_DOES_NOT_MATCH_UNIQUE, "Line [" + dataRecordContainer.lineNumber + "]");

            nbRecordsUpdated++;
            for (Map<String, Object> matchRecord : searchMatch) {
                Map<String, Object> dataRecord = dataRecordContainer.getDataRecord();
                dataRecord.putAll(matchRecord);
                dataRecordContainer.setDataRecord(dataRecord);
            }
            return dataRecordContainer;
        }
    }

}