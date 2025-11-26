package com.back.say.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class Rq {
    private final String actionName;
    private final Map<String, String> paramsMap;

    public Rq(String cmd) {
        String[] cmdBits = cmd.split("\\?", 2);
        actionName = cmdBits[0];
        String queryString = cmdBits.length > 1 ? cmdBits[1] : "";

        paramsMap = Arrays.stream(queryString.split("&"))
                .map(queryParam -> queryParam.split("=", 2))
                .filter(e -> e.length == 2 && !e[0].isBlank() && !e[1].isBlank())
                .collect(Collectors.toMap(
                        bits -> bits[0].trim(),
                        bits -> bits[1].trim()
                ));

        /*paramsMap = new HashMap<>();

        String[] cmdBits = cmd.split("\\?", 2);
        actionName = cmdBits[0];
        String queryString = cmdBits.length == 2 ? cmdBits[1].trim() : "";

        String[] queryStringBits = queryString.split("&");

        for (String queryParam : queryStringBits) {
            String[] queryParamBits = queryParam.split("=", 2);
            String key = queryParamBits[0].trim();
            String value = queryParamBits.length > 1 ? queryParamBits[1].trim() : "";

            if (value.isEmpty())
                continue;

            paramsMap.put(key, value);
        }*/
    }

    public String getActionName() {
        return actionName;
    }

    public String getParam(String paramName, String defaultValue) {
        return paramsMap.getOrDefault(paramName, defaultValue);
    }

    public int getParamAsInt(String paramName, int defaultValue) {
        if (!paramsMap.containsKey(paramName))
            return defaultValue;

        String value = paramsMap.get(paramName);

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
