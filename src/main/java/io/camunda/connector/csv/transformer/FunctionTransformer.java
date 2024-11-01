package io.camunda.connector.csv.transformer;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.csv.toolbox.CsvError;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FunctionTransformer extends DataRecordTransformer {

  public static final String FUNCTION_DATETOSTRING = "DateToString";
  private static final String matchFunction = "(\\w+)\\(([^)]+)\\)";
  private static final String matchParameter = "\"([^\"]*)\"|([^,]+)";
  private final Map<String, String> transformerFunctionMap;
  private final Map<String, List<String>> cacheFunction = new HashMap<>();

  public FunctionTransformer(Map<String, String> transformerFnctionMap) {
    this.transformerFunctionMap = transformerFnctionMap;
  }

  public Map<String, Object> transform(Map<String, Object> dataRecord) throws ConnectorException {
    if (transformerFunctionMap == null || transformerFunctionMap.isEmpty())
      return dataRecord;

    List<String> listKeys = dataRecord.keySet().stream().toList();
    for (String key : listKeys) {
      if (transformerFunctionMap.containsKey(key))
        dataRecord.put(key, transformValue(key, dataRecord.get(key)));
    }
    return dataRecord;

  }

  private Object transformValue(String key, Object value) {
    Object resultTransformation = value;
    if (transformerFunctionMap.containsKey(key)) {
      String tranformation = transformerFunctionMap.get(key);
      if (tranformation.toUpperCase().startsWith("NOWDATE")) {
        return new Date();
      }
      if (tranformation.toUpperCase().startsWith("NOWLOCALDATE")) {
        return LocalDate.now();
      }
      if (value == null)
        return null;

      if (isFunction(tranformation, "STRINGTODATE")) {
        String format = tranformation.substring("DATE".length());
        // Formatter formatter = new Fo
      }

      // DateToString( format )
      if (isFunction(tranformation, FUNCTION_DATETOSTRING)) {

        List<String> parameters = extractParameters(tranformation, 1);
        SimpleDateFormat simpleFormatter = new SimpleDateFormat(parameters.get(0));
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(parameters.get(0));

        if (value instanceof Date valueDate)
          return simpleFormatter.format(valueDate);
        if (value instanceof LocalDateTime valueLocalDateTime)
          return valueLocalDateTime.format(dateTimeFormatter);
        if (value instanceof LocalDate valueLocalDate)
          return valueLocalDate.format(dateTimeFormatter);
        if (value instanceof LocalTime valueLocalTime)
          return valueLocalTime.format(dateTimeFormatter);
        if (value instanceof ZonedDateTime zonedDateTime)
          return zonedDateTime.format(dateTimeFormatter);

        return value.toString();
      }
      if (tranformation.toUpperCase().startsWith("LONG")) {
        return Long.valueOf(value.toString());
      }
      if (tranformation.toUpperCase().startsWith("DOUBLE")) {

        // DOUBLE(<decimal>)
        // source: 133,545,335.33355
        return Long.valueOf(value.toString());
      }
      if (tranformation.toUpperCase().startsWith("INTEGER")) {
        return Integer.valueOf(value.toString());
      }
      if (tranformation.toUpperCase().startsWith("EMAIL")) {
        long count = value.toString().chars().filter(ch -> ch == '@').count();
        return count == 1 ? value : ""; // not an email, clean it
      }
      if (tranformation.toUpperCase().startsWith("FROMUNIT")) {
        // FROMUNIT(<decimal>, position(BEFORE,AFTER), fileToSaveTheUnit)
        // soure: 234,465.332 kw/h
        // ex : TOUNIT(".", AFTER, EnergyUnit)
        // result: <field>: 234465.332, EnergyUnit: kw/h
        return value;
      }
      if (tranformation.toUpperCase().startsWith("TOUNIT")) {
        // FROMUNIT(<decimal>, Separator, position(BEFORE,AFTER), fileToSaveTheUnit)
        // source: 234465.332
        // ex : TOUNIT(".", "," AFTER, EnergyUnit)
        // result: 144,313.33654 kw/h
        return value;
      }
      if (tranformation.toUpperCase().startsWith("CURRENCY")) {
        return value;
      }
    }
    return resultTransformation;
  }

  private boolean isFunction(String transformation, String function) {
    return transformation.toUpperCase().startsWith(function.toUpperCase() + "(");
  }

  /**
   * Extract parameters from the string
   *
   * @param function string
   * @return list of parameters
   */
  private List<String> extractParameters(String function, int numberOfParameters) {
    List<String> parametersList = cacheFunction.get(function);
    if (parametersList != null) {
      if (parametersList.size() != numberOfParameters)
        throw new ConnectorException(CsvError.BAD_TRANSFORMATION_DEFINITION,
            "Function " + function + " expect " + numberOfParameters + " parameters");
      return cacheFunction.get(function);
    }

    int firstParenthesis = function.indexOf("(");
    if (firstParenthesis == -1)
      throw new ConnectorException(CsvError.BAD_TRANSFORMATION_DEFINITION, "No ( in the function");
    String parameters = function.substring((firstParenthesis + 1));
    if (parameters.length() < 1)
      throw new ConnectorException(CsvError.BAD_TRANSFORMATION_DEFINITION, "No ) in the function");
    parameters = parameters.substring(0, parameters.length() - 1);

    parametersList = extractParameters(function, parameters);
    if (parametersList.size() != numberOfParameters)
      throw new ConnectorException(CsvError.BAD_TRANSFORMATION_DEFINITION,
          "Function " + function + " expect " + numberOfParameters + " parameters");
    cacheFunction.put(function, parametersList);

    return parametersList;
  }

  private List<String> extractParameters(String function, String parameters) {
    return Arrays.stream(parameters.split(",")).map(String::trim) // Trim spaces around each element
        .toList();
  }

  private List<String> extractParametersWithRegexp(String function, String parameters) {
    try {
      List<String> parametersList = new ArrayList<>();
      Pattern patternParameters = Pattern.compile(matchParameter);
      Matcher matcher = patternParameters.matcher(parameters);
      if (!matcher.matches())
        return Collections.emptyList();

      for (int i = 1; i <= matcher.groupCount(); i++) {
        parametersList.add(matcher.group(i));
      }
      return parametersList;
    } catch (Exception e) {
      throw new ConnectorException(CsvError.BAD_TRANSFORMATION_EXECUTION,
          "Function " + function + " error " + e.getMessage());
    }
  }
}
