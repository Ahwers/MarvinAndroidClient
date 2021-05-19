package com.example.marvinclient;

import org.json.JSONObject;

public class MarvinResponse {

//    {
//        "resource":{
//        "applicationName": "Graphical Calculator",
//                "resourceRepresentations":{
//            "HTML_STATE_UPDATE_SCRIPT": "calculator.setExpression({ id: '1', latex: 'y=x' });",
//                    "HTML": "<HTML><HEAD>    <TITLE>Graphical Calculator Test</TITLE></HEAD><BODY>    <script src=\"https://www.desmos.com/api/v1.5/calculator.js?apiKey=dcb31709b452b1cf9dc26972add0fda6\"></script>    <div id=\"calculator\" style=\"width: 100%; height: 100%;\"></div>    <script>        var elt = document.getElementById('calculator');        var calculator = Desmos.GraphingCalculator(elt);        var stateId = 1;\nvar focusX = 0;\nvar focusY = 0;\ncalculator.setBlank();\ncalculator.setExpression({ id: '1', latex: 'y=x' });\n    </script></BODY></HTML>",
//                    "HTML_STATE_INSTANTIATION_SCRIPT": "var stateId = 1;\nvar focusX = 0;\nvar focusY = 0;\ncalculator.setBlank();\ncalculator.setExpression({ id: '1', latex: 'y=x' });\n"
//        },
//        "currentStateId": 1,
//        "previousStateId": 0,
//    },
//        "failException": null,
//            "responseMessage": null,
//            "commandStatus": "SUCCESS"
//    }

    public String responseString;

    private String commandStatus;
    private String responseMessage;
    private String failException;
    private Resource resource;

    public MarvinResponse(JSONObject jsonResponse) {
        responseString = jsonResponse.toString();
    }
}
