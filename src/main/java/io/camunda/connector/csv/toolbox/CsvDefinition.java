package io.camunda.connector.csv.toolbox;

import io.camunda.connector.api.error.ConnectorException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CsvDefinition {

    public static final String DEFAULT_SEPARATOR = ";";
    List<String> header;
    String separator;

    public static CsvDefinition fromHeader(String line, String separator) {
        CsvDefinition csvDefinition = new CsvDefinition();
        csvDefinition.decodeHeader(line, separator);
        csvDefinition.separator = separator;
        return csvDefinition;
    }

    public static CsvDefinition fromFields(List<String> fields, String separator) {
        CsvDefinition csvDefinition = new CsvDefinition();
        csvDefinition.header = fields;
        csvDefinition.separator = separator;
        return csvDefinition;
    }

    public static Map<String, String> getBpmnErrors() {
        return Map.of(CsvError.TOO_MUCH_FIELDS_IN_LINE, CsvError.TOO_MUCH_FIELDS_IN_LINE_EXPLANATION,
                CsvError.NO_HEADER, CsvError.NO_HEADER_EXPLANATION);
    }

    public void decodeHeader(String line, String separator) throws ConnectorException {
        header = decodeLine(line, separator);
        if (header.isEmpty()) {
            throw new ConnectorException(CsvError.NO_HEADER, "Can't decode the header line [" + line + "]");

        }
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

    public Map<String, Object> getRecord(String line, int lineNumber) {
        List<String> fields = Arrays.asList(line.split(separator == null ? DEFAULT_SEPARATOR : separator, -1));
        if (fields.size() > header.size())
            throw new ConnectorException(CsvError.TOO_MUCH_FIELDS_IN_LINE,
                    "Line " + lineNumber + " Fields detected in the line [" + fields.size() //
                            + "] headers [" + header.size() + "]");

        Map<String, Object> dataRecord = new HashMap<>();
        for (int i = 0; i < fields.size(); i++) {
            String key = header.get(i);
            Object value = fields.get(i);
            dataRecord.put(key, value);
        }
        return dataRecord;
    }

    private List<String> decodeLine(String input, String separator) {
        return Arrays.asList(input.split(separator == null ? DEFAULT_SEPARATOR : separator, -1));
    }

    /**
     * Decode a string in multiple part. input contains a serie of comma.
     * A parameter can use " to add comma inside : example [ a, "b,c", "d"] return "a", "b,c", "d"
     *
     * @param input string to decode
     * @return the list of parameters
     */
    private List<String> extractParts(String input) {
        List<String> result = new ArrayList<>();
        // Regex to match quoted or unquoted parts
        Pattern pattern = Pattern.compile("\"([^\"]*)\"|([^,\\s]+)");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                result.add(matcher.group(1)); // Add quoted part (without quotes)
            } else {
                result.add(matcher.group(2)); // Add unquoted part
            }
        }
        return result;
    }

}
