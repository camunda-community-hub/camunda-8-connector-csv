package io.camunda.connector.csv.function;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.api.outbound.OutboundConnectorContext;
import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.csv.CsvFunction;
import io.camunda.connector.csv.CsvInput;
import io.camunda.connector.csv.CsvOutput;
import io.camunda.connector.csv.collector.CollectorProperty;
import io.camunda.connector.csv.filter.DataRecordFilter;
import io.camunda.connector.csv.filter.SelectorFilter;
import io.camunda.connector.csv.toolbox.CsvError;
import io.camunda.connector.csv.toolbox.CsvProcessor;
import io.camunda.connector.csv.toolbox.CsvSubFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class GetCsvPropertiesFunction implements CsvSubFunction {

    private final Logger logger = LoggerFactory.getLogger(GetCsvPropertiesFunction.class.getName());

    @Override
    public CsvOutput executeSubFunction(CsvInput csvInput, OutboundConnectorContext context) throws ConnectorException {

        CsvOutput csvOutput = new CsvOutput();
        try {
            CsvInput.ReaderEngine readerEngine = csvInput.initializeInputReader();

            List<DataRecordFilter> listFilters = new ArrayList<>();
            SelectorFilter filter = new SelectorFilter().addFilter(csvInput.getFilter());
            if (filter.isEnabled())
                listFilters.add(filter);

            CollectorProperty collector = new CollectorProperty();

            // ----------- process the file now
            CsvProcessor cvsProcessor = new CsvProcessor();
            cvsProcessor.processProducerToCollector(readerEngine.csvProducer(), listFilters, Collections.emptyList(), collector);

            csvOutput.csvHeader = readerEngine.csvDefinition().getHeader();
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
        return List.of(
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

                RunnerParameter.getInstance(CsvInput.FILTER, //
                                CsvInput.FILTER_LABEL, //
                                String.class, //
                                null, //
                                RunnerParameter.Level.OPTIONAL, //
                                CsvInput.FILTER_EXPLANATION)
                        .setGroup(CsvInput.GROUP_PROCESSING)
        );
    }

    @Override
    public List<RunnerParameter> getOutputsParameter() {
        return Arrays.asList(RunnerParameter.getInstance(CsvOutput.CSVHEADER, //
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
                        CsvOutput.TOTALNUMBEROFRECORDS_EXPLANATION)


        ); //
    }

    @Override
    public Map<String, String> getSubFunctionListBpmnErrors() {
        Map<String, String> combinedMap = new HashMap<>();

        combinedMap.put(CsvFunction.ERROR_UNKNOWN_FUNCTION, CsvFunction.ERROR_UNKNOWN_FUNCTION_LABEL);
        combinedMap.putAll(CsvInput.getBpmnErrors());
        return combinedMap;

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
