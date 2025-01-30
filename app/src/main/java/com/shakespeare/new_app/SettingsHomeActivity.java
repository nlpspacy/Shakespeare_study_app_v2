package com.shakespeare.new_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

//import com.example.new_app.R;
import com.shakespeare.new_app.R;

public class SettingsHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("settings", "open settings home activity");
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settingshome);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//        TextView tv_Title = (TextView) findViewById(R.id.txtSettingsInstruction);
//        tv_Title.setTextSize(TypedValue.COMPLEX_UNIT_SP, GlobalClass.fontsizesp);

    }

    public void returnToMain(View v) {
        // launch a new activity

        Intent i = new Intent(this, com.shakespeare.new_app.MainActivity.class);
        startActivity(i);
    }

    public void launchUserInfoScreen(View v) {
        // launch a new activity

        Intent i = new Intent(this, com.shakespeare.new_app.SettingsActivity.class);
        startActivity(i);
        GlobalClass.intAboutYouScreenSource = 1;
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
//        TextView tv_Title = (TextView) findViewById(R.id.txtSettingsInstruction);
//        tv_Title.setTextSize(TypedValue.COMPLEX_UNIT_SP, GlobalClass.fontsizesp);
        Log.d("fontLog","font size set to 12sp");
    }
    public void setFontMedium16sp(View v){
        // set font size to medium 16sp
        com.shakespeare.new_app.GlobalClass.fontsizesp = 16;
//        TextView tv_Title = (TextView) findViewById(R.id.txtSettingsInstruction);
//        tv_Title.setTextSize(TypedValue.COMPLEX_UNIT_SP, GlobalClass.fontsizesp);
        Log.d("fontLog","font size set to 16sp");
    }
    public void setFontLarge20sp(View v){
        // set font size to large 20sp
        com.shakespeare.new_app.GlobalClass.fontsizesp = 20;
//        TextView tv_Title = (TextView) findViewById(R.id.txtSettingsInstruction);
//        tv_Title.setTextSize(TypedValue.COMPLEX_UNIT_SP, GlobalClass.fontsizesp);
        Log.d("fontLog","font size set to 20sp");
    }

    public void showLineNumbers(View v){
        // set font size to large 20sp
        com.shakespeare.new_app.GlobalClass.intLineNumbers = 1;
        Log.d("line numbers preference","show line numbers");
    }

    public void hideLineNumbers(View v){
        // set font size to large 20sp
        com.shakespeare.new_app.GlobalClass.intLineNumbers = 0;
        Log.d("line numbers preference","hide line numbers");
    }
}


