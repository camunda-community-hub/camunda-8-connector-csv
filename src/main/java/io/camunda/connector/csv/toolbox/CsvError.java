package io.camunda.connector.csv.toolbox;

import io.camunda.connector.api.error.ConnectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsvError {
    public static final String CANT_READ_FILE = "CANT_READ_FILE";
    public static final String CANT_READ_FILE_EXPLANATION = "Csv File can't be read";

    public static final String CANT_ACCESS_FILE = "CANT_ACCESS_FILE";
    public static final String CANT_ACCESS_FILE_EXPLANATION = "Csv File can't be accessible";

    public static final String CANT_ACCESS_INPUTRECORDS = "CANT_ACCESS_INPUTRECORDS";
    public static final String CANT_ACCESS_INPUTRECORDS_EXPLANATION = "InputRecord variable is not defined";

    public static final String GET_PROPERTIES = "GET_PROPERTIES";
    public static final String GET_PROPERTIES_EXPLANATION = "Error during the get-properties";

//  public static final String ERROR_IN_FILE = "ERROR_IN_FILE";
//  public static final String ERROR_IN_FILE_EXPLANATION = "Error during reading a new line in the file";

    public static final String NO_HEADER = "NO_HEADER";
    public static final String NO_HEADER_EXPLANATION =
            "A Csv file must have one line, to describe the list of fields in the CSV file";

    public static final String TOO_MUCH_FIELDS_IN_LINE = "TOO_MUCH_FIELDS_IN_LINE";
    public static final String TOO_MUCH_FIELDS_IN_LINE_EXPLANATION = "The header describe some fields, and a line describe more fields";

    public static final String BAD_STORAGE_DEFINITION = "BAD_STORAGE_DEFINITION";
    public static final String BAD_STORAGE_DEFINITION_EXPLANATION = "Storage definition is not correct";

    public static final String CANT_WRITE_FILE = "CANT_WRITE_FILE";
    public static final String CANT_WRITE_FILE_EXPLANATION = "Csv File can't be write";

    public static final String CANT_PROCESS_CSV = "CANT_PROCESS_CSV";
    public static final String CANT_PROCESS_CSV_EXPLANATION = "Error during processing the CSV";

    public static final String CANT_STORE_CSV = "CANT_STORE_CSV";
    public static final String CANT_STORE_CSV_EXPLANATION = "Error during saving the CSV";

    public static final String BAD_TRANSFORMATION_DEFINITION = "BAD_TRANSFORMATION_DEFINITION";
    public static final String BAD_TRANSFORMATION_DEFINITION_EXPLANATION = "The operation given does not have the expected number of parameters";

    public static final String BAD_TRANSFORMATION_EXECUTION = "BAD_TRANSFORMATION_EXECUTION";
    public static final String BAD_TRANSFORMATION_EXECUTION_EXPLANATION = "Error during execute an operation";

    public static final String BAD_MATCHER_DEFINITION = "BAD_MATCHER_DEFINITION";
    public static final String BAD_MATCHER_DEFINITION_EXPLANATION = "A matcher must define the keys used for the correlation";


    public static final String ONE_RECORD_DOES_NOT_MATCH_UNIQUEEACH = "ONE_RECORD_DOES_NOT_MATCH_UNIQUEEACH";
    public static final String ONE_RECORD_DOES_NOT_MATCH_UNIQUEEACH_EXPLANATION = "The update policy specify each item must match one and only one record";

    public static final String ONE_RECORD_DOES_NOT_MATCH_UNIQUE = "ONE_RECORD_DOES_NOT_MATCH_UNIQUE";
    public static final String ONE_RECORD_DOES_NOT_MATCH_UNIQUE_EXPLANATION = "The update policy specify each item must match one, or no record";


    public static final String UNSUPPORTED_TYPE_STORAGE = "UNSUPPORTED_TYPE_STORAGE";
    public static final String UNSUPPORTED_TYPE_STORAGE_EXPLANATION = "The Type storage is not supported by the function";


    private static final Logger logger = LoggerFactory.getLogger(CsvError.class.getName());

    public static ConnectorException throwAndLog(String errorCode, String message) {
        logger.error("Error [{}] : {}", errorCode, message);
        return new ConnectorException(errorCode, message);
    }
}
