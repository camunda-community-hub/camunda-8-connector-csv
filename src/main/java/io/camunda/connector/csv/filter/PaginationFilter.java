package io.camunda.connector.csv.filter;

import io.camunda.connector.csv.CsvInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class PaginationFilter extends DataRecordFilter {
    private final Logger logger = LoggerFactory.getLogger(PaginationFilter.class.getName());

    private int pageNumber;
    private int pageSize;
    private int numberOfRecords = 0;


    public PaginationFilter(CsvInput csvInput) {
        super(csvInput.isPaginationEnabled());
        this.pageNumber = csvInput.getPageNumber();
        this.pageSize = csvInput.getPageSize();
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
