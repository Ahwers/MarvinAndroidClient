package com.example.marvinclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speechrecognition.DataRecognitionClient;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionMode;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionServiceFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.RECORD_AUDIO;

public class MainActivity extends AppCompatActivity implements CommandBuilderDialogFragment.CommandBuilderDialogListener {

    private Resource activeResource; // TODO: Load a default resource to start with, maybe like a general information one or something. Like what's in my meal plan for today and what evens i have on and that.

    private WebView resourceViewer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int requestCode = 5; // unique code for the permission request
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{RECORD_AUDIO, INTERNET}, requestCode);

        resourceViewer = (WebView) findViewById(R.id.resourceViewer);
        resourceViewer.getSettings().setJavaScriptEnabled(true);
        resourceViewer.setWebViewClient(new WebViewClient());

        Button commandButton = (Button) findViewById(R.id.commandButton);
        commandButton.setOnClickListener(view -> sendVoiceCommand());
    }

    private void sendVoiceCommand() {
        CommandBuilderDialogFragment commandBuilderDialogFragment = new CommandBuilderDialogFragment();
        commandBuilderDialogFragment.show(getSupportFragmentManager(), "commandBuilder");
    }

    @Override
    public void onDialogPositiveClick(String builtCommand) {
//        commandMarvin(builtCommand);
        Toast.makeText(getApplicationContext(), "RUNNING: " + builtCommand, Toast.LENGTH_SHORT).show();
    }

    private void commandMarvin(String command) {
        final String HOST = "https://1e7a63cb565b.ngrok.io";

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = (HOST + "/RestfulMarvin/services/command");
        JSONObject requestBody = null;
        try {
            requestBody = new JSONObject().put("command", command);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                successfulJsonResponse -> {
                    try {
                        MarvinResponse response = new MarvinResponse(successfulJsonResponse);
                        handleResponse(response); // TODO: Should put the json->response logic in a builder class.
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
                    }
                },
                volleyError -> Toast.makeText(getApplicationContext(), ("HTTP Request Error: " + volleyError.getMessage()), Toast.LENGTH_SHORT).show()
        );
        queue.add(request);
    }

    // TODO: Handle INVALID
    private void handleResponse(MarvinResponse marvinResponse) {
        String commandStatus = marvinResponse.getCommandStatus();
        switch (commandStatus) {
            case "SUCCESS":
                loadResource(marvinResponse.getResource());
                break;
            case "FAILED":
                startExceptionInvestigationActivityWithException(marvinResponse.getFailException());
                break;
            case "UNMATCHED":
                startCommandSelectorDialogWithUnmatchedResource(marvinResponse.getResource());
                break;
            default:
                Toast.makeText(getApplicationContext(), "Error in 'handleResponse(MarvinResponse)", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadResource(Resource resource) {
        String resourceApplicationName = resource.getApplicationName();
        if (activeApplicationIs(resourceApplicationName)) {
            if (activeApplicationStateIdIs(resource.getPreviousStateId())) {
                updateResourceApplicationState(resource); // TODO: Better names for all of these
            } else {
                resetResourceApplicationState(resource);
            }
        } else {
            loadResourceApplicationHtml(resource);
        }

        activeResource = resource;
    }

    private boolean activeApplicationIs(String expectedApplicationName) {
        return ((activeResource != null) && activeResource.getApplicationName().equals(expectedApplicationName));
    }

    private boolean activeApplicationStateIdIs(int expectedStateId) {
        return ((activeResource != null) && activeResource.getCurrentStateId() == expectedStateId);
    }

    private void updateResourceApplicationState(Resource resource) {
        resourceViewer.evaluateJavascript(resource.getRepresentation("HTML_STATE_UPDATE_SCRIPT"), null);
    }

    private void resetResourceApplicationState(Resource resource) {
        resourceViewer.evaluateJavascript(resource.getRepresentation("HTML_STATE_INSTANTIATION_SCRIPT"), null);
    }

    private void loadResourceApplicationHtml(Resource resource) {
        String html = resource.getRepresentation("HTML");
        resourceViewer.loadData(html, "text/html", null);
    }

    private void startExceptionInvestigationActivityWithException(String exceptionMessage) {

    }

    private void startCommandSelectorDialogWithUnmatchedResource(Resource resource) {

    }

}