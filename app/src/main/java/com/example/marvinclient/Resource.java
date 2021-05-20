package com.example.marvinclient;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Resource {

    // "resource":{
//        "applicationName": "Graphical Calculator",
//                "resourceRepresentations":{
//            "HTML_STATE_UPDATE_SCRIPT": "calculator.setExpression({ id: '1', latex: 'y=x' });",
//                    "HTML": "<HTML><HEAD>    <TITLE>Graphical Calculator Test</TITLE></HEAD><BODY>    <script src=\"https://www.desmos.com/api/v1.5/calculator.js?apiKey=dcb31709b452b1cf9dc26972add0fda6\"></script>    <div id=\"calculator\" style=\"width: 100%; height: 100%;\"></div>    <script>        var elt = document.getElementById('calculator');        var calculator = Desmos.GraphingCalculator(elt);        var stateId = 1;\nvar focusX = 0;\nvar focusY = 0;\ncalculator.setBlank();\ncalculator.setExpression({ id: '1', latex: 'y=x' });\n    </script></BODY></HTML>",
//                    "HTML_STATE_INSTANTIATION_SCRIPT": "var stateId = 1;\nvar focusX = 0;\nvar focusY = 0;\ncalculator.setBlank();\ncalculator.setExpression({ id: '1', latex: 'y=x' });\n"
//        },

    private String responseString;

    private String applicationName;
    private int currentStateId;
    private int previousStateId;
    private Map<String, String> resourceRepresentations = new HashMap<>();

    public Resource(JSONObject jsonObject) throws JSONException {
        responseString = jsonObject.toString();

        this.applicationName = jsonObject.getString("applicationName");
        this.currentStateId = jsonObject.getInt("currentStateId");
        this.previousStateId = jsonObject.getInt("previousStateId");

        JSONObject jsonResourceRepresentations = jsonObject.getJSONObject("resourceRepresentations");
        resourceRepresentations.put("HTML", jsonResourceRepresentations.getString("HTML"));
        resourceRepresentations.put("HTML_STATE_INSTANTIATION_SCRIPT", jsonResourceRepresentations.getString("HTML_STATE_INSTANTIATION_SCRIPT"));
        resourceRepresentations.put("HTML_STATE_UPDATE_SCRIPT", jsonResourceRepresentations.getString("HTML_STATE_UPDATE_SCRIPT"));
    }

    public String toString() {
        return this.responseString;
    }

    public String getApplicationName() {
        return this.applicationName;
    }

    public int getCurrentStateId() {
        return this.currentStateId;
    }

    public int getPreviousStateId() {
        return this.previousStateId;
    }

    public String getRepresentation(String representationKey) {
        return this.resourceRepresentations.get(representationKey);
    }

}
