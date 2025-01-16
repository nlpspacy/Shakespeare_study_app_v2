package com.shakespeare.new_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

//import com.example.new_app.R;
import com.shakespeare.new_app.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AACActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_aacactivity);

//        GlobalClass.system_prompt = GlobalClass.system_prompt + " My query relates to the Shakespeare play _Antony and Cleopatra_.";
        String userPromptPlay = " My query relates to the Shakespeare play _Antony and Cleopatra_.";

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        View v = findViewById(R.id.textviewAAC);
        ShowPlayScriptOnScreen(v);
    }

    public void ShowPlayScriptOnScreen(View v){

        StringBuilder text = new StringBuilder();

        BufferedReader reader = null;

        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open("AAC.txt")));

            // do reading, usually loop until end of file reading
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                text.append(mLine);
                text.append('\n');
            }
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Error reading file!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }

            TextView output = findViewById(R.id.textviewAAC);
            // output.setText((CharSequence) text);
            output.setMovementMethod(LinkMovementMethod.getInstance());
            Linkify.addLinks(output, Linkify.ALL);
            output.setText(Html.fromHtml(String.valueOf(text), Html.FROM_HTML_MODE_LEGACY));
            output.setTextSize(TypedValue.COMPLEX_UNIT_SP, com.shakespeare.new_app.GlobalClass.fontsizesp);

        }

    }

    public void clearEditTextBox() {
        // set edit text view to blank so user doesn't have to manually clear it
        EditText editTV = findViewById(R.id.message_edit_text);
        editTV.getText().clear();
    }

    public void returnToMain(View v) {
        // launch a new activity

        Intent i = new Intent(this, com.shakespeare.new_app.MainActivity.class);
        startActivity(i);
    }

    public void launchSettings(View v) {
        // launch a new activity

        Intent i = new Intent(this, com.shakespeare.new_app.SettingsActivity.class);
        startActivity(i);
    }

    public void showSystemPrompt(View v) {
        // launch a new activity
        Intent i = new Intent(this, com.shakespeare.new_app.SystemPromptActivity.class);
        Log.d("settings","open system prompt");

        try {
            startActivity(i);
        } catch (IllegalStateException ex) {
            Log.e("oops!", String.valueOf(ex.getCause()));
        }

    }
    public void setFontSmall12sp(View v){
        // set font size to small 12sp
        com.shakespeare.new_app.GlobalClass.fontsizesp = 12;
        Log.d("fontLog","font size set to 12sp");
        ShowPlayScriptOnScreen(v);
    }
    public void setFontMedium16sp(View v){
        // set font size to medium 16sp
        com.shakespeare.new_app.GlobalClass.fontsizesp = 16;
        Log.d("fontLog","font size set to 16sp");
        ShowPlayScriptOnScreen(v);
    }
    public void setFontLarge20sp(View v){
        // set font size to large 20sp
        com.shakespeare.new_app.GlobalClass.fontsizesp = 20;
        Log.d("fontLog","font size set to 20sp");
        ShowPlayScriptOnScreen(v);
    }

    public void openAAC(View v) {
        // launch a new activity

        Intent i = new Intent(this, AACActivity.class);
        startActivity(i);
    }


}