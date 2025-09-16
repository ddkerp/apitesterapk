package com.example.apitester;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView resultText;
    private Button callApiBtn, configBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        resultText = findViewById(R.id.resultText);
        resultText.setMovementMethod(new ScrollingMovementMethod());
        callApiBtn = findViewById(R.id.callApiBtn);
        configBtn = findViewById(R.id.configBtn);

        callApiBtn.setOnClickListener(view -> callApi());
        configBtn.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ConfigActivity.class)));
    }

    private void callApi() {
        SharedPreferences prefs = getSharedPreferences("API_CONFIG", MODE_PRIVATE);
        String endpoint = prefs.getString("endpoint", "https://postman-echo.com/get?foo=bar");
        String method = prefs.getString("method", "GET");
        String params = prefs.getString("params", "");
        String headers = prefs.getString("headers", "");
        String cookies = prefs.getString("cookies", "");

        new ApiTask(endpoint, method, params, headers, cookies).execute();
    }

    private class ApiTask extends AsyncTask<Void, Void, String> {
        private String endpoint, method, params, headers, cookies;
        private int responseCode = -1;

        ApiTask(String endpoint, String method, String params, String headers, String cookies) {
            this.endpoint = endpoint;
            this.method = method.toUpperCase(Locale.ROOT);
            this.params = params;
            this.headers = headers;
            this.cookies = cookies;
        }

        @Override protected void onPreExecute() { resultText.setText("Calling..."); }

        @Override protected String doInBackground(Void... voids) {
            HttpURLConnection conn = null;
            BufferedReader reader = null;
            try {
                String urlStr = endpoint;
                boolean hasBody = method.equals("POST") || method.equals("PUT") || method.equals("PATCH");
                if (!hasBody && params != null && params.trim().length() > 0) {
                    if (urlStr.contains("?")) urlStr += "&" + params;
                    else urlStr += "?" + params;
                }
                URL url = new URL(urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod(method);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                if (headers != null && headers.trim().length() > 0) {
                    String[] lines = headers.split("\r?\n");
                    for (String line : lines) {
                        int idx = line.indexOf(':');
                        if (idx > 0) {
                            conn.setRequestProperty(line.substring(0, idx).trim(), line.substring(idx+1).trim());
                        }
                    }
                }
                if (cookies != null && cookies.trim().length() > 0) {
                    conn.setRequestProperty("Cookie", cookies.trim());
                }
                if (hasBody) {
                    conn.setDoOutput(true);
                    byte[] out = (params != null ? params : "").getBytes("UTF-8");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestProperty("Content-Length", String.valueOf(out.length));
                    DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                    dos.write(out);
                    dos.flush();
                    dos.close();
                }
                responseCode = conn.getResponseCode();
                reader = new BufferedReader(new InputStreamReader(
                    responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream()
                ));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line).append("\n");
                return "HTTP " + responseCode + "\n\n" + sb.toString();
            } catch (Exception e) {
                return "Error: " + e.getMessage();
            } finally {
                try { if (reader != null) reader.close(); } catch (Exception ignored) {}
                if (conn != null) conn.disconnect();
            }
        }
        @Override protected void onPostExecute(String s) { resultText.setText(s); }
    }
}

