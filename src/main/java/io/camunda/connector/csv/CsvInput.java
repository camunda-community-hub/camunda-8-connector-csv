package io.camunda.connector.csv;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.camunda.connector.cherrytemplate.CherryInput;
import io.camunda.connector.csv.toolbox.ParameterToolbox;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)

public class CsvInput implements CherryInput {

  public static final String INPUT_CSV_FUNCTION = "csvFunction";
  public static final String INPUT_SOURCE_FILE = "sourceFile";
  public static final String INPUT_SOURCE_FILE_LABEL = "Input source CSV file";
  public static final String INPUT_SOURCE_FILE_EXPLANATION = "FileStorage definition to access the source file";

  public static final String INPUT_CHARSET = "charset";
  public static final String INPUT_CHARSET_LABEL = "Charset used to code the CSV file";
  public static final String INPUT_CHARSET_EXPLANATION = "File is encode by a specific charset";

  public static final String INPUT_SEPARATOR = "separator";
  public static final String INPUT_SEPARATOR_LABEL = "Separator between fields";
  public static final String INPUT_SEPARATOR_EXPLANATION = "CSV is a collection of fields separated by a separator (; or ,)";

  private String csvFunction;
   private String sourceFile;
  private String charSet;
private String separator;

  public String getCsvFunction() {
    return csvFunction;
  }

  public String getCharSet() {
    return charSet;
  }

  public String getSeparator() {
    return separator;
  }

  public String getSourceFile() {
    return sourceFile;
  }


  @Override
  public List<Map<String, Object>> getInputParameters() {
    return ParameterToolbox.getInputParameters();
  }
}
