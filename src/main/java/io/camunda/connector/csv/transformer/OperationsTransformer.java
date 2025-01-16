package io.camunda.connector.csv.transformer;

import io.camunda.connector.api.error.ConnectorException;
import io.camunda.connector.csv.CsvInput;
import io.camunda.connector.csv.producer.DataRecordContainer;
import io.camunda.connector.csv.toolbox.CsvError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class OperationsTransformer extends DataRecordTransformer {

    public static final String ALL_TYPEDATA_DATE = OperationDefinition.TypeDataDate.Date + "," //
            + OperationDefinition.TypeDataDate.LocalDate + "," //
            + OperationDefinition.TypeDataDate.LocalDateTime + "," //
            + OperationDefinition.TypeDataDate.ZonedDateTime;
    public static final String ALL_TYPE8DATA_NUMBER = OperationDefinition.TypeDataNumber.INTEGER + ","
            + OperationDefinition.TypeDataNumber.LONG + "," //
            + OperationDefinition.TypeDataNumber.DOUBLE + "," //
            + OperationDefinition.TypeDataNumber.FLOAT;

    public static final String FUNCTION_NOW_EXPLANATION =
            OperationDefinition.Operation.Now + "(typeData:" + ALL_TYPEDATA_DATE + "]) to create an object dated now";
    public static final String FUNCTION_STRINGTODATE_EXPLANATION =
            OperationDefinition.Operation.StringToDate + "(format:<FormatterString>, typeData:"
                    + ALL_TYPEDATA_DATE
                    + ",error:<value>) where FormatterString is something like 'ex yyyy-MM-dd'. defaultTypeData is "
                    + OperationDefinition.TypeDataDate.Date;
    public static final String FUNCTION_DATETOSTRING_EXPLANATION = OperationDefinition.Operation.DateToString
            + "(format:<FormatterString>) where FormatterString is something like 'yyyy-MM-ddHH:mm:SS'";
    public static final String FUNCTION_STRINGTOINTEGER_EXPLANATION = OperationDefinition.Operation.StringToInteger
            + "(locale:<Locale>,error:<value>) to transform a string to an Integer, using a locale (default is Locale machine)";
    public static final String FUNCTION_STRINGTOLONG_EXPLANATION = OperationDefinition.Operation.StringToLong
            + "(locale:<Locale>,error:<value>) to transform a string to an Long, using a locale (default is Locale machine)";
    public static final String FUNCTION_STRINGTODOUBLE_EXPLANATION = OperationDefinition.Operation.StringToDouble
            + "(locale:<Locale>,error:<value>) to transform a string to an Double, using a locale (default is Locale machine)";
    public static final String FUNCTION_STRINGTOFLOAT_EXPLANATION = OperationDefinition.Operation.StringToFloat
            + "(locale:<Locale>,error:<value>) to transform a string to an Float, using a locale (default is Locale machine)";
    public static final String FUNCTION_STRINGTOEMAIL_EXPLANATION =
            OperationDefinition.Operation.StringToEmail + "(error:<value>) verify that the string is an email";
    public static final String FUNCTION_STRINGTOCURENCY_EXPLANATION = OperationDefinition.Operation.StringToCurrency
            + "(local:<Locale>, unitField:<FieldToPutCurrency>,error:<value>) to evaluate a currency and save the currency in a different field";
    public static final String FUNCTION_STRINGTOUNIT_EXPLANATION =
            OperationDefinition.Operation.StringToUnit + "(local:<Locale>, unitField:<FieldToPutCurrency>, typeData:"
                    + ALL_TYPE8DATA_NUMBER
                    + ",error:<value>) to evaluate a unit, convert the number and save the currency in a different field";
    public static final String FUNCTION_UNITTOSTRING_EXPLANATION = OperationDefinition.Operation.UnitToString //
            + "(local:<Locale>, unitFieldSuffix:<FieldToGetUnit>, unitFieldPrefix:<FieldToGetUnit>) from a value and a Field Unit, create one field";
    public static final String FUNCTION_CURRENCYTOSTRING_EXPLANATION = OperationDefinition.Operation.CurrencyToString //
            + "(local:<Locale>, unitField:<FieldToGetCurrency>) from a value and a currency Unit, create one field";

    /* The match does not work ? Keep it for information
     matchFunction = "(\\w+)\\(([^)]+)\\)";
     matchParameter = "\"([^\"]*)\"|([^,]+)";
    */
    private final Logger logger = LoggerFactory.getLogger(OperationsTransformer.class.getName());
    private Map<String, OperationDefinition> operationDefinitionMap = new HashMap<>();

    /**
     * Constructor
     */
    public OperationsTransformer(CsvInput csvInput) {
        super(csvInput.getOperationTransformers() != null);
        if (csvInput.getOperationTransformers()!=null)
            setTransformerMap(csvInput.getOperationTransformers());
    }

    public static Map<String, String> getBpmnErrors() {
        return Map.of(CsvError.BAD_TRANSFORMATION_DEFINITION, CsvError.BAD_TRANSFORMATION_DEFINITION_EXPLANATION,
                CsvError.BAD_TRANSFORMATION_EXECUTION, CsvError.BAD_TRANSFORMATION_EXECUTION_EXPLANATION);
    }

    public static String getMapperExplanation() {
        return OperationDefinition.Operation.Now + ":" + FUNCTION_NOW_EXPLANATION + ", "
                + OperationDefinition.Operation.StringToDate + ":" + FUNCTION_STRINGTODATE_EXPLANATION + ", "
                + OperationDefinition.Operation.DateToString + ":" + FUNCTION_DATETOSTRING_EXPLANATION + ", "
                + OperationDefinition.Operation.StringToInteger + ":" + FUNCTION_STRINGTOINTEGER_EXPLANATION + ", "
                + OperationDefinition.Operation.StringToLong + ":" + FUNCTION_STRINGTOLONG_EXPLANATION + ", "
                + OperationDefinition.Operation.StringToDouble + ":" + FUNCTION_STRINGTODOUBLE_EXPLANATION + ", "
                + OperationDefinition.Operation.StringToFloat + ":" + FUNCTION_STRINGTOFLOAT_EXPLANATION + ", "
                + OperationDefinition.Operation.StringToEmail + ":" + FUNCTION_STRINGTOEMAIL_EXPLANATION + ", "
                + OperationDefinition.Operation.StringToUnit + ":" + FUNCTION_STRINGTOUNIT_EXPLANATION + ", "
                + OperationDefinition.Operation.StringToCurrency + ":" + FUNCTION_STRINGTOCURENCY_EXPLANATION + ", "
                + OperationDefinition.Operation.UnitToString + ":" + FUNCTION_UNITTOSTRING_EXPLANATION + ", "
                + OperationDefinition.Operation.CurrencyToString + ":" + FUNCTION_CURRENCYTOSTRING_EXPLANATION + ", ";
    }

    /**
     * Set the transformation map
     *
     * @param transformerFunctionMap contains all transformation
     * @throws ConnectorException if decodage failed
     */
    public void setTransformerMap(Map<String, String> transformerFunctionMap) throws ConnectorException {
        this.operationDefinitionMap = new HashMap<>();
        for (Map.Entry<String, String> entry : transformerFunctionMap.entrySet()) {
            operationDefinitionMap.put(entry.getKey(), OperationDefinition.decodeFromString(entry.getValue()));
        }
    }

    /* ******************************************************************** */
    /*                                                                      */
    /*  Execute function                                                    */
    /*                                                                      */
    /* ******************************************************************** */

    @Override
    public void transform(DataRecordContainer dataRecordContainer) throws ConnectorException {
        if (operationDefinitionMap.isEmpty())
            return;

        for (Map.Entry<String, OperationDefinition> entry : operationDefinitionMap.entrySet()) {
            // not possible to use putAll: the map may contain null value
            for (Map.Entry<String, Object> entryData : transformValue(entry.getKey(), entry.getValue(), dataRecordContainer.getDataRecord()).entrySet())
                dataRecordContainer.put(entryData.getKey(), entryData.getValue());
        }
    }

    /**
     * Transform one key. Return a list of key/values
     *
     * @param key        key to transform
     * @param dataRecord dataRecord
     * @return a Map of KeyValue
     */
    private Map<String, Object> transformValue(String key,
                                               OperationDefinition operationDefinition,
                                               Map<String, Object> dataRecord) throws ConnectorException {
        Object value = dataRecord.get(key);

        logger.debug("Transform started key[{}] transformation[{}]", key, operationDefinition.getSynthesis());

        // Only the operation Now don't need a value, and just PRODUCE a value
        if (value == null && operationDefinition.getOperation() != OperationDefinition.Operation.Now) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put(key, null);
            return resultMap;
        }

        switch (operationDefinition.getOperation()) {
            case Now:
                return executeFunctionNow(key, operationDefinition);

            case StringToDate:
                return executeFunctionStringToDate(key, operationDefinition, dataRecord);

            case DateToString:
                return executeFunctionDateToString(key, operationDefinition, dataRecord);

            // ---------------- toInteger
            case StringToInteger:
                try {
                    // value can't be null here, but protect the code to avoid the warning
                    Number number = getNumberFromString(value == null ? "" : value.toString(), operationDefinition.getLocaleOrDefault());
                    return Map.of(key, number.intValue());
                } catch (ParseException e) {
                    if (operationDefinition.catchError())
                        return Map.of(key, operationDefinition.getErrorNumberValue().intValue());

                    throw throwBadExecutionNumber(value, operationDefinition.getLocaleOrDefault(), e);
                }

                // ------------- ToLong
            case StringToLong:
                try {
                    // value can't be null here, but protect the code to avoid the warning
                    Number number = getNumberFromString(value == null ? "" : value.toString(), operationDefinition.getLocaleOrDefault());
                    return Map.of(key, number.longValue());
                } catch (Exception e) {
                    if (operationDefinition.catchError())
                        return Map.of(key, operationDefinition.getErrorNumberValue().longValue());

                    throw throwBadExecutionNumber(value, operationDefinition.getLocaleOrDefault(), e);
                }

                // -------------- ToDouble
            case StringToDouble:
                try {
                    // value can't be null here, but protect the code to avoid the warning
                    Number number = getNumberFromString(value == null ? "" : value.toString(), operationDefinition.getLocaleOrDefault());
                    return Map.of(key, number.doubleValue());
                } catch (Exception e) {
                    if (operationDefinition.catchError())
                        return Map.of(key, operationDefinition.getErrorNumberValue());

                    throw throwBadExecutionNumber(value, operationDefinition.getLocaleOrDefault(), e);
                }

            case StringToUnit:
                return executeFunctionStringToUnit(key, operationDefinition, dataRecord);

            case StringToEmail:
                // value can't be null here, but protect the code to avoid the warning
                long count = (value == null ? "" : value.toString()).chars().filter(ch -> ch == '@').count();
                if (count != 1) {
                    if (operationDefinition.catchError())
                        return Map.of(key, operationDefinition.getErrorValue()); // not an email, clean it
                    throw CsvError.throwAndLog(CsvError.BAD_TRANSFORMATION_EXECUTION,
                            "Error from value[" + value + "] not an email Adresss (expect one and onlyt one @)");
                }
                return Map.of(key, value);

            case UnitToString:
                StringBuilder valueFormated = new StringBuilder();
                if (operationDefinition.getUnitFieldPrefix() != null) {
                    valueFormated.append(dataRecord.get(operationDefinition.getUnitFieldPrefix()));
                    valueFormated.append(" ");
                }
                NumberFormat numberFormat = NumberFormat.getInstance(operationDefinition.getLocaleOrDefault());
                valueFormated.append(numberFormat.format(value));

                if (operationDefinition.getUnitFieldSuffix() != null) {
                    valueFormated.append(" ");
                    valueFormated.append(dataRecord.get(operationDefinition.getUnitFieldSuffix()));
                }
                if (operationDefinition.getUnitField() != null) {
                    valueFormated.append(" ");
                    valueFormated.append(dataRecord.get(operationDefinition.getUnitField()));
                }
                return Map.of(key, valueFormated.toString());

            // -------------- ToDouble
            case StringToCurrency:
                return executeFunctionStringToUnit(key, operationDefinition, dataRecord);

            case NumberToString:
                return getStringFromNumber(key, operationDefinition, dataRecord);

            default:
                return Collections.emptyMap();
        }
    }

    /**
     * Now
     *
     * @param key                 key of data
     * @param operationDefinition get parameters of the function
     * @return Map of result
     */
    private Map<String, Object> executeFunctionNow(String key, OperationDefinition operationDefinition) {

        switch (operationDefinition.getTypeDataDate(OperationDefinition.TypeDataDate.Date)) {
            case Date:
                return Map.of(key, new Date());
            case LocalDate:
                return Map.of(key, LocalDate.now());
            case LocalDateTime:
                return Map.of(key, LocalDateTime.now());
            case ZonedDateTime:
                return Map.of(key, ZonedDateTime.now());
            default:
                return Map.of(key, new Date());
        }
    }

    /**
     * @param key                 key of data
     * @param operationDefinition get parameters of the function
     * @param dataRecord          Record of all data
     * @return Map of result
     * @throws ConnectorException if an error arrie
     */
    private Map<String, Object> executeFunctionStringToDate(String key,
                                                            OperationDefinition operationDefinition,
                                                            Map<String, Object> dataRecord) throws ConnectorException {

        Object value = dataRecord.get(key);
        logger.debug("FunctionStringToDate [{}] on value[{}]",
                 operationDefinition.getTypeDataDate(OperationDefinition.TypeDataDate.Date),
                value);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(operationDefinition.getFormat());
        SimpleDateFormat formatter = new SimpleDateFormat(operationDefinition.getFormat());

        try {
            switch (operationDefinition.getTypeDataDate(OperationDefinition.TypeDataDate.Date)) {
                case Date:
                    return Map.of(key, formatter.parse(value.toString()));

                case LocalDate:
                    return Map.of(key, LocalDate.parse(value.toString(), dateTimeFormatter));

                case LocalDateTime:
                    return Map.of(key, LocalDateTime.parse(value.toString(), dateTimeFormatter));

                case ZonedDateTime:
                    return Map.of(key, ZonedDateTime.parse(value.toString(), dateTimeFormatter));
                default:
                    if (operationDefinition.catchError())
                        return Map.of(key, null);

                    throw CsvError.throwAndLog(CsvError.BAD_TRANSFORMATION_EXECUTION,
                            "Unknown StringToDate transformation  [" + operationDefinition.getTypeDataDate(OperationDefinition.TypeDataDate.Date) + "]");
            }
        } catch (Exception e) {
            if (operationDefinition.catchError())
                return Map.of(key, null);

            throw CsvError.throwAndLog(CsvError.BAD_TRANSFORMATION_EXECUTION,
                    "Date can't be format[" + operationDefinition.getTypeDataDate(OperationDefinition.TypeDataDate.Date) + "] decoded from ["
                            + operationDefinition.getFormat() + "] : value[" + value + "] " + e.getMessage());

        }
    }

    /**
     * executionFunctionStringToUnit. Expected stringToUnit(TypeNumberResult, Locale,FieldUnit) where TypeNumberResult is "INT", "LONG", "DOUBLE", "FLOAT"
     * Example:
     * stringToUnit()   << Integer, use the local of the machine
     * stringToUnit(LONG)   << Long use the local of the machine
     * stringToUnit(DOUBLE, US)   << Double use the US local to decode the number
     * stringToUnit(DOUBLE, US, TemperatureUnit)   << Double use the US local to decode the number, save the unit to TemperatureUnit
     *
     * @param key                 key of data
     * @param operationDefinition get parameters of the function
     * @param dataRecord          Record of all data
     * @return Map of result
     * @throws ConnectorException
     */
    private Map<String, Object> executeFunctionStringToUnit(String key,
                                                            OperationDefinition operationDefinition,
                                                            Map<String, Object> dataRecord) throws ConnectorException {

        Object value = dataRecord.get(key);
        try {

            logger.debug("FunctionStringToUnit Type[{}] Locale[{}] CollectUnitOn[{}] on value[{}]",
                    operationDefinition.getTypeDataNumber(),
                    operationDefinition.getLocaleOrDefault(),
                    operationDefinition.getUnitField(),
                    value);

            Map<String, Object> resultMap = new HashMap<>();
            // The unit may be before ( $ 233) - so we use the extraction
            List<String> listAmounts = extractAmount(value.toString());
            Number number = getNumberFromString(listAmounts.get(0), operationDefinition.getLocaleOrDefault());
            switch (operationDefinition.getTypeDataNumber()) {
                case INTEGER:
                    resultMap.put(key, number.intValue());
                    break;

                case LONG:
                    resultMap.put(key, number.longValue());
                    break;

                case DOUBLE:
                    resultMap.put(key, number.doubleValue());
                    break;

                case FLOAT:
                    resultMap.put(key, number.floatValue());
                    break;
                default:
                    resultMap.put(key, number.intValue());
                    break;
            }
            // save the unit?
            if (operationDefinition.getUnitField() != null)
                resultMap.put(operationDefinition.getUnitField(), listAmounts.get(1));
            return resultMap;
        } catch (Exception e) {
            if (operationDefinition.catchError()) {
                HashMap<String,Object> resultMap = new HashMap<>();
                resultMap.put(key, null);
                return resultMap;
            }

            throw CsvError.throwAndLog(CsvError.BAD_TRANSFORMATION_EXECUTION,
                    "Extract Number Key[" + key + "] Type[" + operationDefinition.getTypeDataNumber() + "] Locale["
                            + operationDefinition.getLocaleOrDefault() + "] CollectUnitOn[" + operationDefinition.getUnitField()
                            + "] on value[" + value + "]");
        }

    }

    /* ******************************************************************** */
    /*                                                                      */
    /*  toolbox                                                             */
    /*                                                                      */
    /* ******************************************************************** */

    private Map<String, Object> getStringFromNumber(String key,
                                                    OperationDefinition operationDefinition,
                                                    Map<String, Object> dataRecord) throws ConnectorException {
        try {
            NumberFormat numberFormat = NumberFormat.getInstance(operationDefinition.getLocaleOrDefault());
            HashMap<String,Object> resultMap = new HashMap<>();
            resultMap.put(key, numberFormat.format(dataRecord.get(key)));
            return resultMap;
        } catch (Exception e) {
            if (operationDefinition.catchError()) {
                HashMap<String,Object> resultMap = new HashMap<>();
                resultMap.put(key, null);
                return resultMap;
            }
            throw CsvError.throwAndLog(CsvError.BAD_TRANSFORMATION_EXECUTION,
                    "Number can't be formated. value[" + dataRecord.get(key) + "] " + e.getMessage());
        }
    }

    /**
     * @param key                 key of data
     * @param operationDefinition get parameters of the function
     * @param dataRecord          Record of all data
     * @return Map of result
     */
    public Map<String, Object> executeFunctionDateToString(String key,
                                                           OperationDefinition operationDefinition,
                                                           Map<String, Object> dataRecord) {

        SimpleDateFormat simpleFormatter = new SimpleDateFormat(operationDefinition.getFormat());
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(operationDefinition.getFormat());

        Object value = dataRecord.get(key);

        if (value instanceof Date valueDate)
            return Map.of(key, simpleFormatter.format(valueDate));
        if (value instanceof LocalDateTime valueLocalDateTime)
            return Map.of(key, valueLocalDateTime.format(dateTimeFormatter));
        if (value instanceof LocalDate valueLocalDate)
            return Map.of(key, valueLocalDate.format(dateTimeFormatter));
        if (value instanceof LocalTime valueLocalTime)
            return Map.of(key, valueLocalTime.format(dateTimeFormatter));
        if (value instanceof ZonedDateTime zonedDateTime)
            return Map.of(key, zonedDateTime.format(dateTimeFormatter));

        return Map.of(key, value.toString());
    }

    /**
     * The pattern Pattern.compile("([A-Za-z]+)|([0-9,.]+)"); does not work, so do it manually
     *
     * @param value value to work on
     * @return list(0) is the amount. list(1) the text
     */
    private List<String> extractAmount(String value) {
        StringBuilder amount = new StringBuilder();
        StringBuilder label = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            if ((value.charAt(i) >= '0' && value.charAt(i) <= '9') || value.charAt(i) == '-' || value.charAt(i) == '.'
                    || value.charAt(i) == ',')
                amount.append(value.charAt(i));
            else
                label.append(value.charAt(i));
        }
        return List.of(amount.toString(), label.toString().trim());
    }

    private Number getNumberFromString(String value, Locale locale) throws ParseException {
        NumberFormat numberFormat = NumberFormat.getInstance(locale);
        return numberFormat.parse(value);
    }

    public ConnectorException throwBadExecutionNumber(Object value, Locale locale, Exception e) {
        return CsvError.throwAndLog(CsvError.BAD_TRANSFORMATION_EXECUTION,
                "Error from value[" + value + "] locale [" + locale.toString() + "] : parsing error " + e.getMessage());
    }

}
