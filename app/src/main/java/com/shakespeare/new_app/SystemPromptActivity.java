package com.shakespeare.new_app;

import static com.shakespeare.new_app.GlobalClass.fontsizesp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

//import com.example.new_app.R;
import com.shakespeare.new_app.R;

public class SystemPromptActivity  extends AppCompatActivity  {

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_systemprompt);

        TextView tvSystemPrompt = findViewById(R.id.tvPersonalisePrompt);
        Log.d("prompts", "system prompt: " + com.shakespeare.new_app.GlobalClass.system_prompt);
        Log.d("prompts", "personalise prompt: " + com.shakespeare.new_app.GlobalClass.personalise_prompt);

        if(!com.shakespeare.new_app.GlobalClass.personalise_prompt.equals("")){
            tvSystemPrompt.setText(com.shakespeare.new_app.GlobalClass.personalise_prompt);
        } else {
            tvSystemPrompt.setText("No personalisation set.");
        }

        tvSystemPrompt.setTextSize(fontsizesp);

    }



    public void launchSettings(View v) {
        // launch a new activity

        Intent i = new Intent(this, SettingsHomeActivity.class);
        startActivity(i);
    }

    public void goBack(View v) {
        // go back to previous screen/activity

        Intent resultIntent = new Intent();
        getOnBackPressedDispatcher().onBackPressed();

//        Intent i = new Intent(this, com.shakespeare.new_app.MainActivity.class);
//        startActivity(i);
    }

    public void returnToMain(View v) {
        // launch a new activity

        Intent i = new Intent(this, com.shakespeare.new_app.MainActivity.class);
        startActivity(i);
    }



}
