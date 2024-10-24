package io.camunda.connector.csv.toolbox;

public class CsvError {

  public final static String CANT_READ_FILE = "CANT_READ_FILE";
  public final static String CANT_READ_FILE_EXPLANATION = "Csv File can't be read";

  public final static String GET_PROPERTIES = "GET_PROPERTIES";
  public final static String GET_PROPERTIES_EXPLANATION = "Error during the get-properties";

  public final static String ERROR_IN_FILE = "ERROR_IN_FILE";
  public final static String ERROR_IN_FILE_EXPLANATION = "Error during reading a new line in the file";

  public final static String NO_HEADER = "NO_HEADER";
  public final static String NO_HEADER_EXPLANATION =
      "A Csv file must have one line, to describe the list of " + "fields in " + "the the CSV file";

  public final static String TOO_MUCH_FIELDS_IN_LINE = "TOO_MUCH_FIELDS_IN_LINE";
  public final static String TOO_MUCH_FIELDS_IN_LINE_EXPLANATION = "The header describe some fields, and a line describe more fields";

}
