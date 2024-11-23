package io.camunda.connector.csv;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.camunda.connector.cherrytemplate.CherryOutput;
import io.camunda.connector.csv.toolbox.ParameterToolbox;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)

public class CsvOutput implements CherryOutput {

    public static final String RECORDS = "records";
    public static final String RECORDS_LABEL = "records";
    public static final String RECORDS_EXPLANATION = "Records readed by the connector. List of Map";

    public static final String CSVHEADER = "csvHeader";
    public static final String CSVHEADER_LABEL = "csvHeader";
    public static final String CSVHEADER_EXPLANATION = "Header in the CSV";

    public static final String NUMBEROFRECORDS = "numberOfRecords";
    public static final String NUMBEROFRECORDS_LABEL = "Number Of Records";
    public static final String NUMBEROFRECORDS_EXPLANATION = "Number of records in the result. Depends on filter, pagination...";

    public static final String TOTALNUMBEROFRECORDS = "totalNumberOfRecords";
    public static final String TOTALNUMBEROFRECORDS_LABEL = "Total Number Of Records";
    public static final String TOTALNUMBEROFRECORDS_EXPLANATION = "Total number or records in the CSV file";

    public static final String FILEVARIABLEREFERENCE = "fileVariableReference";
    public static final String FILEVARIABLEREFERENCE_LABEL = "File created";
    public static final String FILEVARIABLEREFERENCE_EXPLANATION =
            "A file is created by the connector, and " + "saved under this reference";

    public List<Map<String, Object>> records;

    public List<String> csvHeader;
    /* number of records which pass the filter */
    public int numberOfRecords;
    /* number of lines in the file */
    public int totalNumberOfRecords;

    public String fileVariableReference;

    @JsonIgnore
    @Override
    public List<Map<String, Object>> getOutputParameters() {
        return ParameterToolbox.getOutputParameters();
    }

}
