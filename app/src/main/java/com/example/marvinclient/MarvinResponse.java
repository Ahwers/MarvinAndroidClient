package com.example.marvinclient;

import org.json.JSONException;
import org.json.JSONObject;

public class MarvinResponse {

//    {
//        "currentStateId": 1,
//        "previousStateId": 0,
//    },
//        "failException": null,
//            "responseMessage": null,
//            "commandStatus": "SUCCESS"
//    }

    private String responseString;

    private String commandStatus;
    private String responseMessage;
    private String failException;
    private Resource resource;

    public MarvinResponse(JSONObject jsonResponse) throws JSONException {
        responseString = jsonResponse.toString();

        this.commandStatus = jsonResponse.getString("commandStatus");
        this.responseMessage = jsonResponse.getString("responseMessage");
        this.failException = jsonResponse.getString("failException");
        this.resource = new Resource(jsonResponse.getJSONObject("resource"));
    }

    public String toString() {
        return this.responseString;
    }

    public String getCommandStatus() {
        return this.commandStatus;
    }

    public String getResponseMessage() {
        return this.responseMessage;
    }

    public String getFailException() {
        return this.failException;
    }

    public Resource getResource() {
        return this.resource;
    }
}
