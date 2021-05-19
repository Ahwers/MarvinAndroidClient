package com.example.marvinclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private final String HOST = "https://f074eaeab6b4.ngrok.io";

    private WebView resourceViewer;
    private Button commandButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resourceViewer = (WebView) findViewById(R.id.resourceViewer);
        resourceViewer.getSettings().setJavaScriptEnabled(true);
        resourceViewer.setWebViewClient(new WebViewClient());
//        resourceViewer.loadData(encodedHtml, "text/html", "base64");

        commandButton = (Button) findViewById(R.id.commandButton);
        commandButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                command();
            }
        });
    }

    private void command() {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = (HOST + "/RestfulMarvin/services/command");
        JSONObject requestBody = null;
        try {
            requestBody = new JSONObject().put("command", "plot y = x ^ 2 + (3x + 5)/x");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonResponse) {
                        MarvinResponse marvinResponse = new MarvinResponse(jsonResponse);
                        handleResponse(marvinResponse);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "HTTP Request Error", Toast.LENGTH_SHORT).show();
            }
        });
        queue.add(request);
    }

    private void handleResponse(MarvinResponse marvinResponse) {
        resourceViewer.loadData(marvinResponse.responseString, "text/plain", null);
    }

}