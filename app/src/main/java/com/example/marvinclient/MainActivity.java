package com.example.marvinclient;

import androidx.appcompat.app.AppCompatActivity;

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

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private Resource activeResource; // TODO: Load a default resource to start with, maybe like a general information one or something. Like what's in my meal plan for today and what evens i have on and that.

    private WebView resourceViewer;
    private EditText writtenCommandEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resourceViewer = (WebView) findViewById(R.id.resourceViewer);
        resourceViewer.getSettings().setJavaScriptEnabled(true);
        resourceViewer.setWebViewClient(new WebViewClient());

        writtenCommandEditText = (EditText) findViewById(R.id.writtenCommandEditText);
        writtenCommandEditText.setVisibility(View.GONE);
        writtenCommandEditText.setOnKeyListener((view, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                hideSoftKeyboard();
                writtenCommandEditText.setVisibility(View.GONE);
                sendWrittenCommand();
                writtenCommandEditText.setText("");
            }
            return true;
        });

        Button quickCommandButton = (Button) findViewById(R.id.quickCommandButton);
        quickCommandButton.setOnClickListener(view -> sendQuickVoiceCommand());

        Button writtenCommandButton = (Button) findViewById(R.id.writtenCommandButton);
        writtenCommandButton.setOnClickListener(view -> writtenCommandEditText.setVisibility(View.VISIBLE));

        Button sureCommandButton = (Button) findViewById(R.id.sureCommandButton);
        sureCommandButton.setOnClickListener(view -> sendSureVoiceCommand());
    }

    private void hideSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }

        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void sendQuickVoiceCommand() {
        String command = "plot y = x ^ 2 + (3x + 5)/x";
        commandMarvin(command);
    }

    private void sendWrittenCommand() {
        commandMarvin(writtenCommandEditText.getText().toString());
    }

    private void sendSureVoiceCommand() {
        Toast.makeText(getApplicationContext(), "TODO: Implement", Toast.LENGTH_SHORT).show();
    }

    private void commandMarvin(String command) {
        final String HOST = "https://a45547853824.ngrok.io";

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = (HOST + "/RestfulMarvin/services/command");
        JSONObject requestBody = null;
        try {
            requestBody = new JSONObject().put("command", command);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonResponse) {
                        try {
                            MarvinResponse response = new MarvinResponse(jsonResponse);
                            handleResponse(response); // TODO: Should put the json->response logic in a builder class.
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                error -> Toast.makeText(getApplicationContext(), "HTTP Request Error", Toast.LENGTH_SHORT).show()
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