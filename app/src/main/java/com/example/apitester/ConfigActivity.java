package com.example.apitester;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class ConfigActivity extends AppCompatActivity {

    private EditText endpointInput, paramsInput, headersInput, cookiesInput;
    private Spinner methodSpinner;
    private Button saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        endpointInput = findViewById(R.id.endpointInput);
        paramsInput = findViewById(R.id.paramsInput);
        headersInput = findViewById(R.id.headersInput);
        cookiesInput = findViewById(R.id.cookiesInput);
        methodSpinner = findViewById(R.id.methodSpinner);
        saveBtn = findViewById(R.id.saveBtn);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{ "GET", "POST", "PUT", "DELETE", "PATCH" });
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        methodSpinner.setAdapter(adapter);

        SharedPreferences prefs = getSharedPreferences("API_CONFIG", MODE_PRIVATE);
        // preload defaults if empty
        if (!prefs.contains("endpoint")) {
            prefs.edit()
                .putString("endpoint", "https://postman-echo.com/get")
                .putString("method", "GET")
                .putString("params", "foo=bar&hello=world")
                .putString("headers", "User-Agent: APITester\nAccept: application/json")
                .putString("cookies", "sessionid=abc123; csrftoken=xyz456")
                .apply();
        }

        endpointInput.setText(prefs.getString("endpoint", ""));
        paramsInput.setText(prefs.getString("params", ""));
        headersInput.setText(prefs.getString("headers", ""));
        cookiesInput.setText(prefs.getString("cookies", ""));

        String method = prefs.getString("method", "GET");
        int pos = 0;
        for (int i=0;i<adapter.getCount();i++) if (adapter.getItem(i).equals(method)) pos = i;
        methodSpinner.setSelection(pos);

        saveBtn.setOnClickListener(view -> {
            prefs.edit()
                .putString("endpoint", endpointInput.getText().toString())
                .putString("params", paramsInput.getText().toString())
                .putString("headers", headersInput.getText().toString())
                .putString("cookies", cookiesInput.getText().toString())
                .putString("method", methodSpinner.getSelectedItem().toString())
                .apply();
            finish();
        });
    }
}

