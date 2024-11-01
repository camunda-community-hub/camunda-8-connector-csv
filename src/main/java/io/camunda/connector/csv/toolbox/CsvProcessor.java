package io.camunda.connector.csv.toolbox;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.csv.collector.CvsCollector;
import io.camunda.connector.csv.producer.CvsProducer;
import io.camunda.connector.csv.producer.DataRecordContainer;
import io.camunda.connector.csv.streamer.DataRecordStreamer;
import io.camunda.connector.csv.transformer.DataRecordTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CsvProcessor {
  private final Logger logger = LoggerFactory.getLogger(CsvProcessor.class.getName());

  /**
   * Processs a flux of dataRecord, coming from the producer, to the collector.
   *
   * @param producer         producer to have DataRecord
   * @param listStreamers    list of streamers, to reduce the dataRecord
   * @param listTransformers list of transformers, to transform the dataRecord
   * @param collector        where the result is sent
   */
  public void processProducerToCollector(CvsProducer producer,
                                         List<DataRecordStreamer> listStreamers,
                                         List<DataRecordTransformer> listTransformers,
                                         CvsCollector collector) {
    try {
      producer.begin();
      collector.begin();

      int lineNumber = 1;
      // Read and process lines one by one
      DataRecordContainer dataRecordContainer;
      while ((dataRecordContainer = producer.getDataRecord()) != null) {
        lineNumber++;
        collector.processOneRecord();

        boolean processDataRecord = true;
        for (DataRecordStreamer streamer : listStreamers) {
          if (!streamer.keepDataRecord(lineNumber, dataRecordContainer.getDataRecord())) {
            processDataRecord = false;
            break;
          }
        }
        if (!processDataRecord)
          continue;

        // transformer can be done only if dataRecord is present
        for (DataRecordTransformer transformer : listTransformers)
          dataRecordContainer.setDataRecord(transformer.transform(dataRecordContainer.getDataRecord()));

        // Collect as a String
        collector.collect(dataRecordContainer.getDataRecord());

      }
      producer.end();
      collector.end();

    } catch (ConnectorException ce) {
      throw ce;
    } catch (Exception e) {
      logger.error("During process file {}", e.getMessage());
      throw new ConnectorException(CsvError.CANT_PROCESS_CSV, e.getMessage());
    }
  }

}