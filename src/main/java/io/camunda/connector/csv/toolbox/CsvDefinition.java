package io.camunda.connector.csv.toolbox;

import io.camunda.connector.api.error.ConnectorException;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvDefinition {

  public static final String DEFAULT_SEPARATOR = ";";
  List<String> header;
  String separator;
  Map<String, String> transformer = new HashMap<>();

  public static CsvDefinition fromHeader(String line, String separator) {
    CsvDefinition csvDefinition = new CsvDefinition();
    csvDefinition.decodeHeader(line, separator);
    csvDefinition.separator = separator;
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

  public Map<String, String> getTransformer() {
    return transformer;
  }

  public void setTransformer(Map<String, String> transformer) {
    this.transformer = transformer;
  }

  public Map<String, Object> getRecord(String line, int lineNumber) {
    List<String> fields = Arrays.asList(line.split(separator == null ? DEFAULT_SEPARATOR : separator, -1));
    if (fields.size() > header.size())
      throw new ConnectorException(CsvError.TOO_MUCH_FIELDS_IN_LINE,
          "Line " + lineNumber + " Fields detected in the line [" + fields.size() //
              + "] headers [" + header.size() + "]");

    Map<String, Object> dataRecord = new HashMap<>();
    for (int i = 0; i < fields.size(); i++) {
      String key = header.get(i);
      Object value = transformValue(key, fields.get(i));
      dataRecord.put(key, value);
    }
    return dataRecord;
  }

  public Map<String, Object> tranformRecord(Map<String, Object> dataRecord) {
    List<String> listKeys = dataRecord.keySet().stream().toList();

    for (String key : listKeys) {
      if (transformer.containsKey(key))
        dataRecord.put(key, transformValue(key, dataRecord.get(key)));
    }
    return dataRecord;

  }

  private Object transformValue(String key, Object value) {
    Object resultTransformation = value;
    if (transformer.containsKey(key)) {
      String tranformation = transformer.get(key);
      if (tranformation.toUpperCase().startsWith("NOWDATE")) {
        return new Date();
      }
      if (tranformation.toUpperCase().startsWith("NOWLOCALDATE")) {
        return LocalDate.now();
      }
      if (value == null)
        return null;
      if (tranformation.toUpperCase().startsWith("DATE")) {
        String format = tranformation.substring("DATE".length());
        // Formatter formatter = new Fo

      }
      if (tranformation.toUpperCase().startsWith("LONG")) {
        return Long.valueOf(value.toString());
      }
      if (tranformation.toUpperCase().startsWith("INTEGER")) {
        return Integer.valueOf(value.toString());
      }
    }
    return resultTransformation;
  }

  private List<String> decodeLine(String input, String separator) {
    return Arrays.asList(input.split(separator == null ? DEFAULT_SEPARATOR : separator, -1));

  }
}
