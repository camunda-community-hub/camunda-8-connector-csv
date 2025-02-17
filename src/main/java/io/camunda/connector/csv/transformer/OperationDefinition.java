package io.camunda.connector.csv.transformer;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.csv.toolbox.CsvError;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The function transformer operation is reading from a string, and contains different parameters
 */
public class OperationDefinition {

    public static final String PARAMETER_TYPE_DATA = "typeData";
    public static final String PARAMETER_FORMAT = "format";
    public static final String PARAMETER_ERROR = "error";
    public static final String PARAMETER_UNIT_FIELD = "unitField";
    public static final String PARAMETER_UNIT_FIELD_PREFIX = "unitFieldPrefix";
    public static final String PARAMETER_UNIT_FIELD_SUFFIX = "unitFieldSuffix";
    public static final String PARAMETER_LOCALE = "locale";
    public static final String PARAMETER_FUNCTION = "function";

    private Operation operation;
    private Locale locale = Locale.getDefault();
    private String errorValue;
    private String unitField;
    private String unitFieldPrefix;
    private String unitFieldSuffix;

    private TypeDataNumber typeDataNumber;
    private TypeDataDate typeDataDate;
    private String format;

    public static OperationDefinition decodeFromString(String operationString) throws ConnectorException {
        OperationDefinition operationDefinition = new OperationDefinition();
        // format is name(<fields°. for example
        // stringToDate(format:yyyy-MM-dd,type:LocalDate,error:null)",
        Map<String, String> parameters = extractParameters(operationString);

        operationDefinition.errorValue = parameters.get(PARAMETER_ERROR);
        operationDefinition.unitField = parameters.get(PARAMETER_UNIT_FIELD);
        operationDefinition.unitFieldPrefix = parameters.get(PARAMETER_UNIT_FIELD_PREFIX);
        operationDefinition.unitFieldSuffix = parameters.get(PARAMETER_UNIT_FIELD_SUFFIX);
        if (parameters.get(PARAMETER_TYPE_DATA) != null) {
            // it maybe an TypeData integer or Date
            try {
                operationDefinition.typeDataNumber = TypeDataNumber.valueOf(parameters.get(PARAMETER_TYPE_DATA));
            } catch (Exception e) {
                // do nothing, let's try the second
            }
            try {
                operationDefinition.typeDataDate = TypeDataDate.valueOf(parameters.get(PARAMETER_TYPE_DATA));
            } catch (Exception e) {
                // do nothing
            }
            // If now both are null, this is a problem
            if (operationDefinition.typeDataNumber == null && operationDefinition.typeDataDate == null)
                throw CsvError.throwAndLog(CsvError.BAD_TRANSFORMATION_DEFINITION,
                        "TypeData[" + parameters.get(PARAMETER_TYPE_DATA) + "] can't be decoded");

        }

        operationDefinition.format = parameters.get(PARAMETER_FORMAT);

        if (parameters.get(PARAMETER_LOCALE) != null)
            try {
                operationDefinition.locale = new Locale(parameters.get(PARAMETER_LOCALE));
            } catch (Exception e) {
                throw CsvError.throwAndLog(CsvError.BAD_TRANSFORMATION_DEFINITION,
                        "Locale [" + parameters.get("locale") + "] can't be decoded");

            }

        try {
            operationDefinition.operation = Operation.valueOf(parameters.get(PARAMETER_FUNCTION).toUpperCase());
        } catch (Exception e) {
            throw CsvError.throwAndLog(CsvError.BAD_TRANSFORMATION_DEFINITION,
                    "Function [" + parameters.get(PARAMETER_FUNCTION) + "] can't be decoded : expect [" //
                            + Arrays.stream(Operation.values()).map(Enum::name).collect(Collectors.joining(", ")) //
                            + "]");
        }

        if (operationDefinition.operation == Operation.StringToCurrency)
            operationDefinition.typeDataNumber = TypeDataNumber.DOUBLE;

        checkOperation(operationDefinition, operationString);
        return operationDefinition;
    }

    private static void checkOperation(OperationDefinition operationDefinition, String operationString) throws ConnectorException {
        if (operationDefinition.operation == null)
            throw CsvError.throwAndLog(CsvError.BAD_TRANSFORMATION_DEFINITION,
                    "Function without operation[" + operationString + "]");

        if ((operationDefinition.operation == Operation.StringToDate)
                && operationDefinition.typeDataDate == null)
            throw CsvError.throwAndLog(CsvError.BAD_TRANSFORMATION_DEFINITION,
                    "Parameter " + PARAMETER_TYPE_DATA + " missing for operation[" + operationDefinition.operation + "] source[" + operationString
                            + "] waits one of [" //
                            + Arrays.stream(TypeDataDate.values()).map(Enum::name).collect(Collectors.joining(", "))
                            + "]");
        if ((operationDefinition.operation == Operation.DateToString)
                && operationDefinition.format == null)
            throw CsvError.throwAndLog(CsvError.BAD_TRANSFORMATION_DEFINITION,
                    "Parameter " + PARAMETER_FORMAT + " missing for operation[" + operationDefinition.operation + "] source[" + operationString
                            + "]");


        if (operationDefinition.operation == Operation.StringToUnit
                && operationDefinition.typeDataNumber == null)
            throw CsvError.throwAndLog(CsvError.BAD_TRANSFORMATION_DEFINITION,
                    "Format DataNumber missing for operation[" + operationDefinition.operation + "] source[" + operationString
                            + "] waits one of [" //
                            + Arrays.stream(TypeDataNumber.values()).map(Enum::name).collect(Collectors.joining(", "))
                            + "] like " + PARAMETER_TYPE_DATA + ":" + TypeDataNumber.DOUBLE);
    }

    /**
     * Extract parameters from the string. Example: stringToLong(locale:US,error:null) will return
     * Map("Locale": "US", "error": "null")
     *
     * @param function string containing parameters
     * @return Map of parameters
     */
    private static Map<String, String> extractParameters(String function) throws ConnectorException {

        Map<String, String> result = new HashMap<>();
        int firstParenthesis = function.indexOf("(");
        if (firstParenthesis == -1)
            throw new ConnectorException(CsvError.BAD_TRANSFORMATION_DEFINITION, "No ( in the function");
        String parameters = function.substring((firstParenthesis + 1));
        if (parameters.isEmpty())
            throw new ConnectorException(CsvError.BAD_TRANSFORMATION_DEFINITION, "No ) in the function");

        result.put("function", function.substring(0, firstParenthesis));

        parameters = parameters.substring(0, parameters.length() - 1);

        List<String> parametersList = Arrays.stream(parameters.split(","))
                .map(String::trim) // Trim spaces around each element
                .toList();
        // Each parameters is a string <label>:<value>
        for (String parameter : parametersList) {
            int firstDot = parameter.indexOf(":");
            if (firstDot == -1)
                throw CsvError.throwAndLog(CsvError.BAD_TRANSFORMATION_DEFINITION,
                        "Function [" + function + "] on parameter [" + parameter + "] format 'label:value' expected");
            String label = parameter.substring(0, firstDot);
            String value = parameter.substring(firstDot + 1);
            result.put(label, value);
        }
        return result;
    }

    public static Map<String, String> getBpmnErrors() {
        return Map.of(CsvError.BAD_TRANSFORMATION_DEFINITION, CsvError.BAD_TRANSFORMATION_DEFINITION_EXPLANATION);
    }

    public String getSynthesis() {
        return operation.toString();
    }

    public Operation getOperation() {
        return operation;
    }

    /**
     * Return the local of the operation
     *
     * @return Locale defined in the operation (null if no locale is defined)
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Return the local, or the DefaultLocale if no local is defined in the operation
     *
     * @return Locale, never null
     */
    public Locale getLocaleOrDefault() {
        return locale == null ? Locale.getDefault() : locale;
    }

    public boolean catchError() {
        return errorValue != null;
    }

    public String getErrorValue() {
        return errorValue;
    }

    public Double getErrorNumberValue() {
        try {
            return Double.parseDouble(errorValue);
        } catch (Exception e) {
            // the error has an error... ok, return 0
            return Double.valueOf(0.0);
        }
    }

    public String getUnitField() {
        return unitField;
    }

    public String getUnitFieldPrefix() {
        return unitFieldPrefix;
    }

    public String getUnitFieldSuffix() {
        return unitFieldSuffix;
    }

    public TypeDataNumber getTypeDataNumber() {
        return typeDataNumber;
    }

    public TypeDataNumber getTypeDataNumber(TypeDataNumber defaultValue) {
        return typeDataNumber == null ? defaultValue : typeDataNumber;
    }

    public TypeDataDate getTypeDataDate(TypeDataDate defaultData) {
        return typeDataDate == null ? defaultData : typeDataDate;
    }

    public String getFormat() {
        return format;
    }

    /**
     * We keep this way because we prefer the designer use StringToDate as name and not STRINGTODATE or STRING_TO_DATE
     */
    public enum Operation {Now, StringToDate, DateToString, StringToInteger, StringToLong, StringToDouble, StringToFloat, StringToEmail, StringToUnit, StringToCurrency, UnitToString, CurrencyToString, NumberToString}

    public enum TypeDataNumber {INTEGER, LONG, DOUBLE, FLOAT}

    public enum TypeDataDate {Date, LocalDate, LocalDateTime, ZonedDateTime}

}
