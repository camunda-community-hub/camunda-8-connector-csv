package io.camunda.connector.csv.streamer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class PaginationStreamer extends DataRecordStreamer {
    private final Logger logger = LoggerFactory.getLogger(PaginationStreamer.class.getName());

    public int pageNumber;
    public int pageSize;
    public int numberOfRecords = 0;

    public PaginationStreamer(int pageNumber, int pageSize) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    @Override
    public boolean needDataRecordDecode() {
        return false;
    }

    @Override
    public boolean keepDataRecord(int lineNumber, Map<String, Object> dataRecord) {
        numberOfRecords++;
        boolean keepData = numberOfRecords > pageSize * pageNumber;
        if (numberOfRecords > (pageSize) * (pageNumber + 1))
            keepData = false;
        logger.debug("Pagination record {} (pageNumber: {} pageSize {} keep? {}", numberOfRecords, pageNumber, pageSize,
                keepData);
        return keepData;
    }

    /**
     * Return the number of record the pagination streamer see.
     * This is not the number of record view and keep, but the total number of records
     *
     * @return total number of record
     */
    public int getTotalNumberOfRecords() {
        return numberOfRecords;
    }

}
