package io.camunda.connector.csv.toolbox;

import io.camunda.connector.cherrytemplate.RunnerParameter;
import io.camunda.connector.csv.CsvFunction;
import io.camunda.connector.csv.CsvInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.*;

public class ParameterToolbox {
    private static final Logger logger = LoggerFactory.getLogger(ParameterToolbox.class.getName());

    /**
     * This is a toolbox, only static method
     */
    private ParameterToolbox() {
    }

    public static List<Map<String, Object>> getInputParameters() {
        return getParameters(true);
    }

    public static List<Map<String, Object>> getOutputParameters() {
        return getParameters(false);
    }

    /**
     * Return the list of parameters
     *
     * @param inputParameters type of parameters (INPUT, OUTPUT)
     * @return list of parameters on a list of MAP
     */
    private static List<Map<String, Object>> getParameters(boolean inputParameters) {

        List<RunnerParameter> parametersCollectList = new ArrayList<>();
        logger.debug("getParameters input? {}", inputParameters);

        // add the "choose the function" parameters
        RunnerParameter chooseFunction = new RunnerParameter(CsvInput.CSV_FUNCTION, "Csv Function", String.class,
                RunnerParameter.Level.REQUIRED, "Choose the function to execute");
        chooseFunction.setAttribute("priority", -2);

        // Csv common parameters

        // add the input only at the INPUT parameters
        boolean isCommonParameterInAGroup = false;
        if (inputParameters) {
            parametersCollectList.add(chooseFunction);
            for (RunnerParameter runnerParameter : CsvFunction.getInputCommonParameters()) {
                runnerParameter.setAttribute("priority", -1);
                if (runnerParameter.group != null)
                    isCommonParameterInAGroup = true;
                parametersCollectList.add(runnerParameter);
            }

        }


        //  now, we collect all functions, and for each function, we collect parameters
        for (Class<?> classFunction : CsvFunction.getAllFunctions()) {
            try {
                Constructor<?> constructor = classFunction.getConstructor();
                CsvSubFunction inputSubFunction = (CsvSubFunction) constructor.newInstance();

                List<RunnerParameter> subFunctionsParametersList = inputParameters ?
                        inputSubFunction.getInputsParameter() :
                        inputSubFunction.getOutputsParameter();

                chooseFunction.addChoice(inputSubFunction.getSubFunctionType(), inputSubFunction.getSubFunctionName());
                logger.debug("CsvFunction SubFunctionName[{}] TypeChoice [{}] parameterList.size={}",
                        inputSubFunction.getSubFunctionName(), inputSubFunction.getSubFunctionType(),
                        subFunctionsParametersList.size());

                for (RunnerParameter parameter : subFunctionsParametersList) {
                    parameter.setAttribute("sub-function", inputSubFunction.getSubFunctionType());
                    // add the parameter in a group, else the common group will be at the end in the modeler
                    if (isCommonParameterInAGroup && parameter.group == null)
                        parameter.setGroup("Parameters");
                    // one parameter may be used by multiple functions, and we want to create only one, but play on condition to show it
                    Optional<RunnerParameter> parameterInList = parametersCollectList.stream()
                            .filter(t -> t.getName().equals(parameter.getName()))
                            .findFirst();
                    if (parameterInList.isEmpty()) {
                        registerSubFunctionInParameter(parameter, inputSubFunction.getSubFunctionType());

                        // We search where to add this parameter. It is at the end of the group with the same priority
                        int positionToAdd = 0;
                        for (RunnerParameter indexParameter : parametersCollectList) {
                            if (indexParameter.getAttributeInteger("priority", 0) <= parameter.getAttributeInteger("priority", 0))
                                positionToAdd++;
                        }
                        parametersCollectList.add(positionToAdd, parameter);
                        logger.debug("  check parameter[{}.{}] : New Add at [{}] newSize[{}] - registered in[{}]",
                                inputSubFunction.getSubFunctionName(), parameter.getName(), positionToAdd, parametersCollectList.size(),
                                parameter.getAttribute("ret"));
                        // Already exist
                    } else {
                        if (!checkEquals(parameter.group, parameterInList.get().group)) {
                            logger.error("  parameter Not in same groupe between[{}.{}] group[{}] and [{}] group[{}]",
                                    inputSubFunction.getSubFunctionName(), parameter.getName(), parameter.group,
                                    parameterInList.get().getName(), parameterInList.get().group);

                        }
                        // Register this function in that parameter
                        registerSubFunctionInParameter(parameterInList.get(), inputSubFunction.getSubFunctionType());
                        logger.debug("  check parameter[{}.{}] : Already exist - registered in[{}]",
                                inputSubFunction.getSubFunctionName(), parameter.getName(), inputSubFunction.getSubFunctionType());
                    }

                }

            } catch (Exception e) {
                logger.error("Exception during the getInputParameters functions {}", e.toString());
            }
        }

        // Now, build the list from all parameters collected
        // We add a condition for each parameter coming from a sub-function
        for (RunnerParameter parameter : parametersCollectList) {

            // There is an explicit condition: do not override it
            if (parameter.getCondition() != null)
                continue;

            if (parameter.getAttribute("sub-function") == null)
                continue;

            List<String> listFunctionForThisParameter = getSubFunctionFromParameter(parameter);
            if (listFunctionForThisParameter.isEmpty() || listFunctionForThisParameter.size() == CsvFunction.getAllFunctions()
                    .size()) {
                logger.debug("parameter [{}] Register in none or ALL functions", parameter.getName());
            } else {
                logger.debug("parameter [{}] Register in some functions [{}]", parameter.getName(),
                        listFunctionForThisParameter);
                parameter.addCondition(chooseFunction.getName(), listFunctionForThisParameter);
            }
        }
        // first, the function selection
        logger.debug(" Parameter => Map Size={}", parametersCollectList.size());

        return parametersCollectList.stream().map(t -> t.toMap(CsvInput.CSV_FUNCTION)).toList();

    }

    private static void registerSubFunctionInParameter(RunnerParameter parameter, String type) {
        List<String> registerInType = (List<String>) parameter.getAttribute("register-in-type");
        if (registerInType == null)
            registerInType = new ArrayList<>();

        if (!registerInType.contains(type))
            registerInType.add(type);

        parameter.setAttribute("register-in-type", registerInType);
    }

    private static List<String> getSubFunctionFromParameter(RunnerParameter parameter) {
        List<String> registerInType = (List<String>) parameter.getAttribute("register-in-type");
        if (registerInType == null)
            registerInType = new ArrayList<>();
        return registerInType;
    }

    private static boolean checkEquals(Object o1, Object o2) {
        if (o1 == null && o2 == null)
            return true;
        if (o1 == null || o2 == null)
            return false;
        return o1.equals(o2);

    }
}
