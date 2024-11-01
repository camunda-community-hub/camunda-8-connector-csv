package io.camunda.connector.csv.streamer;

import java.util.Map;

/**
 * The goial of the stream is to say if the record is kepp or not
 */
public abstract class DataRecordStreamer {

  public abstract boolean needDataRecordDecode();

  public abstract boolean keepDataRecord(int lineNumber, Map<String, Object> dataRecord);

}
