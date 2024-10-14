package io.camunda.connector.csv.toolbox;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvDefinition {

  public static final String DEFAULT_SEPARATOR = ";";
  List<String> header;

  Map<String, String> transformer = new HashMap<>();

  public static CsvDefinition fromHeader(String line, String separator) {
    CsvDefinition csvDefinition = new CsvDefinition();
    csvDefinition.decodeHeader(line, separator);
    return csvDefinition;
  }

  public void decodeHeader(String line, String separator) {
    header = decodeLine(line, separator);
  }

  public String codeHeader(String separator) {
    return String.join(separator, header);
  }

  public List<String> getHeader() {
    return header;
  }

  public void setHeader(List<String> header) {
    this.header = header;
  }

  public void setTransformer(Map<String, String> transformer) {
    this.transformer = transformer;
  }

  public Map<String, String> getTransformer() {
    return transformer;
  }

  private List<String> decodeLine(String input, String separator) {
    return Arrays.asList(input.split(separator==null? DEFAULT_SEPARATOR :separator, -1));

  }
}
