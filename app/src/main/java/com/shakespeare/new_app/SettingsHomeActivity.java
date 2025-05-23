package com.shakespeare.new_app;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

//import com.example.new_app.R;
import com.shakespeare.new_app.R;

public class SettingsHomeActivity extends AppCompatActivity {

    private Integer font_size_when_open;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        font_size_when_open = GlobalClass.fontsizesp;

        Log.d("settings", "open settings home activity");
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settingshome);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if(GlobalClass.intShowLineNumbers==1){
            RadioButton rbShowLineNumbers = findViewById(R.id.rbtnShowLineNumbers); // initiate a radio button
            rbShowLineNumbers.setChecked(true); // check current state of a radio button (true or false).

        } else {
            RadioButton rbShowLineNumbers = findViewById(R.id.rbtnShowLineNumbers); // initiate a radio button
            rbShowLineNumbers.setChecked(false); // check current state of a radio button (true or false).

            RadioButton rbHideLineNumbers = findViewById(R.id.rbtnHideLineNumbers); // initiate a radio button
            rbHideLineNumbers.setChecked(true); // check current state of a radio button (true or false).

        }

        if(com.shakespeare.new_app.GlobalClass.fontsizesp == 12) {
            RadioButton rbtnfontsmall = findViewById(R.id.rbtnfontsmall); // initiate a radio button
            rbtnfontsmall.setChecked(true); // check current state of a radio button (true or false).
            RadioButton rbtnfontmedium = findViewById(R.id.rbtnfontmedium); // initiate a radio button
            rbtnfontmedium.setChecked(false); // check current state of a radio button (true or false).
            RadioButton rbtnfontlarge = findViewById(R.id.rbtnfontlarge); // initiate a radio button
            rbtnfontlarge.setChecked(false); // check current state of a radio button (true or false).

        } else if(com.shakespeare.new_app.GlobalClass.fontsizesp == 16) {
            RadioButton rbtnfontsmall = findViewById(R.id.rbtnfontsmall); // initiate a radio button
            rbtnfontsmall.setChecked(false); // check current state of a radio button (true or false).
            RadioButton rbtnfontmedium = findViewById(R.id.rbtnfontmedium); // initiate a radio button
            rbtnfontmedium.setChecked(true); // check current state of a radio button (true or false).
            RadioButton rbtnfontlarge = findViewById(R.id.rbtnfontlarge); // initiate a radio button
            rbtnfontlarge.setChecked(false); // check current state of a radio button (true or false).

        } else if(com.shakespeare.new_app.GlobalClass.fontsizesp == 20) {
            RadioButton rbtnfontsmall = findViewById(R.id.rbtnfontsmall); // initiate a radio button
            rbtnfontsmall.setChecked(false); // check current state of a radio button (true or false).
            RadioButton rbtnfontmedium = findViewById(R.id.rbtnfontmedium); // initiate a radio button
            rbtnfontmedium.setChecked(false); // check current state of a radio button (true or false).
            RadioButton rbtnfontlarge = findViewById(R.id.rbtnfontlarge); // initiate a radio button
            rbtnfontlarge.setChecked(true); // check current state of a radio button (true or false).

        } ;

//        TextView tv_Title = (TextView) findViewById(R.id.txtSettingsInstruction);
//        tv_Title.setTextSize(TypedValue.COMPLEX_UNIT_SP, GlobalClass.fontsizesp);

        ImageButton btnSound = findViewById(R.id.btnSound);
//        TextView tvSoundOnOffInd = findViewById(R.id.txtSoundOnOffIndicator);
        if(GlobalClass.boolSoundOn == Boolean.TRUE){
            btnSound.setImageResource(R.drawable.sound_icon_transparent_bg);
//            tvSoundOnOffInd.setText("Sound on");
        } else {
            btnSound.setImageResource(R.drawable.mute_icon_transparent_bg);
//            tvSoundOnOffInd.setText("Sound off");
        }

    }

    public void returnToMain(View v) {
        // launch a new activity

        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
    public void goBack(View v) {
        // go back to previous screen/activity

        if (GlobalClass.fontsizesp != font_size_when_open) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("fontSizeChanged", true);
            setResult(RESULT_OK, resultIntent);
        }

        getOnBackPressedDispatcher().onBackPressed();

//        Intent i = new Intent(this, com.shakespeare.new_app.MainActivity.class);
//        startActivity(i);
    }

    public void openUserSelectCharacters(View v) {
        // go back to previous screen/activity

        Intent i = new Intent(this, ShowPlaySharedDb.class);
        startActivity(i);

//        Intent i = new Intent(this, com.shakespeare.new_app.MainActivity.class);
//        startActivity(i);
    }

    public void openBookmarkSharingPreferences(View v) {
        // go back to previous screen/activity

        Intent i = new Intent(this, BookmarkSharingPreferences.class);
        startActivity(i);

//        Intent i = new Intent(this, com.shakespeare.new_app.MainActivity.class);
//        startActivity(i);
    }

//    prefs.edit().putString("sharedUsersToShow", "sophie,dan,julia").apply();

    public void launchUserInfoScreen(View v) {
        // launch a new activity

        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
        GlobalClass.intAboutYouScreenSource = 1;
    }

    public void showSystemPrompt(View v) {
        // launch a new activity
        Intent i = new Intent(this, SystemPromptActivity.class);
        Log.d("settings","open system prompt");

        try {
            startActivity(i);
        } catch (IllegalStateException ex) {
            Log.e("oops!", String.valueOf(ex.getCause()));
        }

    }

    public void setFontSmall12sp(View v){
        // set font size to small 12sp
        GlobalClass.fontsizesp = 12;
//        TextView tv_Title = (TextView) findViewById(R.id.txtSettingsInstruction);
//        tv_Title.setTextSize(TypedValue.COMPLEX_UNIT_SP, GlobalClass.fontsizesp);
        Log.d("fontLog","font size set to 12sp");
    }
    public void setFontMedium16sp(View v){
        // set font size to medium 16sp
        GlobalClass.fontsizesp = 16;
//        TextView tv_Title = (TextView) findViewById(R.id.txtSettingsInstruction);
//        tv_Title.setTextSize(TypedValue.COMPLEX_UNIT_SP, GlobalClass.fontsizesp);
        Log.d("fontLog","font size set to 16sp");
    }
    public void setFontLarge20sp(View v){
        // set font size to large 20sp
        GlobalClass.fontsizesp = 20;
//        TextView tv_Title = (TextView) findViewById(R.id.txtSettingsInstruction);
//        tv_Title.setTextSize(TypedValue.COMPLEX_UNIT_SP, GlobalClass.fontsizesp);
        Log.d("fontLog","font size set to 20sp");
    }

    public void showLineNumbers(View v){
        // set font size to large 20sp
        GlobalClass.intShowLineNumbers = 1;
        Log.d("line numbers preference","show line numbers");
    }

    public void hideLineNumbers(View v){
        // set font size to large 20sp
        GlobalClass.intShowLineNumbers = 0;
        Log.d("line numbers preference","hide line numbers");
    }

    public void toggleSoundOnOff(View v) {
        // toggle sound on and off
//        TextView tvSoundOnOffInd = findViewById(R.id.txtSoundOnOffIndicator);
        ImageButton btnSound = findViewById(R.id.btnSound);

        if(GlobalClass.boolSoundOn.equals(Boolean.TRUE)){ // if tts is on, then turn off

            // turn off any text to speech that is currently in progress
            MyApplication.textToSpeech.speak("", TextToSpeech.QUEUE_FLUSH, null);

            GlobalClass.boolSoundOn = Boolean.FALSE;
            btnSound.setImageResource(R.drawable.mute_icon_transparent_bg);
//            tvSoundOnOffInd.setText("Sound off");
            Toast.makeText(this, "Sound off", Toast.LENGTH_SHORT).show();
        } else{ // if tts is off, then turn on
            GlobalClass.boolSoundOn = Boolean.TRUE;
            btnSound.setImageResource(R.drawable.sound_icon_transparent_bg);
//            tvSoundOnOffInd.setText("Sound on");
            Toast.makeText(this, "Sound on", Toast.LENGTH_SHORT).show();

        }
    }

}


