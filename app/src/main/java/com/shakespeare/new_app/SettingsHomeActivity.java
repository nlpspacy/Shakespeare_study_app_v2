package com.shakespeare.new_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

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

        if(com.shakespeare.new_app.GlobalClass.intShowLineNumbers==1){
            RadioButton rbShowLineNumbers = (RadioButton) findViewById(R.id.rbtnShowLineNumbers); // initiate a radio button
            rbShowLineNumbers.setChecked(true); // check current state of a radio button (true or false).

        } else {
            RadioButton rbShowLineNumbers = (RadioButton) findViewById(R.id.rbtnShowLineNumbers); // initiate a radio button
            rbShowLineNumbers.setChecked(false); // check current state of a radio button (true or false).

            RadioButton rbHideLineNumbers = (RadioButton) findViewById(R.id.rbtnHideLineNumbers); // initiate a radio button
            rbHideLineNumbers.setChecked(true); // check current state of a radio button (true or false).

        }


//        TextView tv_Title = (TextView) findViewById(R.id.txtSettingsInstruction);
//        tv_Title.setTextSize(TypedValue.COMPLEX_UNIT_SP, GlobalClass.fontsizesp);

    }

    public void returnToMain(View v) {
        // launch a new activity

        Intent i = new Intent(this, com.shakespeare.new_app.MainActivity.class);
        startActivity(i);
    }
    public void goBack(View v) {
        // go back to previous screen/activity

        getOnBackPressedDispatcher().onBackPressed();

//        Intent i = new Intent(this, com.shakespeare.new_app.MainActivity.class);
//        startActivity(i);
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
        com.shakespeare.new_app.GlobalClass.intShowLineNumbers = 1;
        Log.d("line numbers preference","show line numbers");
    }

    public void hideLineNumbers(View v){
        // set font size to large 20sp
        com.shakespeare.new_app.GlobalClass.intShowLineNumbers = 0;
        Log.d("line numbers preference","hide line numbers");
    }

    public void toggleSoundOnOff(View v) {
        // toggle sound on and off
        if(com.shakespeare.new_app.GlobalClass.boolSoundOn.equals(Boolean.TRUE)){

            com.shakespeare.new_app.GlobalClass.boolSoundOn = Boolean.FALSE;
            Toast.makeText(this, "Sound off", Toast.LENGTH_SHORT).show();
        } else{
            com.shakespeare.new_app.GlobalClass.boolSoundOn = Boolean.TRUE;
            Toast.makeText(this, "Sound on", Toast.LENGTH_SHORT).show();

        }
    }

}


