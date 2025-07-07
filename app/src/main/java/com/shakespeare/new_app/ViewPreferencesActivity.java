package com.shakespeare.new_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

public class ViewPreferencesActivity extends AppCompatActivity {

    TextView preferencesTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_preferences);

        preferencesTextView = findViewById(R.id.preferencesTextView);

        SharedPreferences prefs = getSharedPreferences("com.shakespeare.new_app_preferences", MODE_PRIVATE);
        Map<String, ?> allPrefs = prefs.getAll();

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, ?> entry : allPrefs.entrySet()) {
            sb.append(entry.getKey()).append(" = ").append(entry.getValue().toString()).append("\n");
        }
        Log.d("check preferences","preference: <"+sb.toString()+">");
        preferencesTextView.setText(sb.toString());
    }


    public void returnToMain(View v) {
        // launch a new activity

        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
    public void goBack(View v) {
        // go back to previous screen/activity

        getOnBackPressedDispatcher().onBackPressed();

//        Intent i = new Intent(this, com.shakespeare.new_app.MainActivity.class);
//        startActivity(i);
    }


}
