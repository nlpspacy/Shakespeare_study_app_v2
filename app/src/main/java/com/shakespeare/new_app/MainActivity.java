package com.shakespeare.new_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

//import com.example.new_app.R;
import com.shakespeare.new_app.AMSND;
import com.shakespeare.new_app.R;


public class MainActivity extends AppCompatActivity {

    private String[] array_plays_all;
    private Spinner playslist_spinner;
    private Button close_Button;

    public  void adjustFontScale( Configuration configuration,float scale) {

        configuration.fontScale = scale;
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        metrics.scaledDensity = configuration.fontScale * metrics.density;
        getBaseContext().getResources().updateConfiguration(configuration, metrics);

    }

    //LocaleConfigurationUtil.class
    public static Context adjustFontSize(Context context){
        Configuration configuration = context.getResources().getConfiguration();
        // This will apply to all text like -> Your given text size * fontScale
        configuration.fontScale = 1.0f;

        return context.createConfigurationContext(configuration);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        this.setTitle(getResources().getString(R.string.home_screen_title));
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        array_plays_all = getResources().getStringArray(R.array.plays_all);

        playslist_spinner = (Spinner) findViewById(R.id.spinnerPlaysList);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, array_plays_all);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        playslist_spinner.setAdapter(dataAdapter);

        playslist_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                // note that this is the play selected
                Log.d("spinner interaction", "selected " + playslist_spinner.getSelectedItem().toString());

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }


        });

        // check for prior set value of system prompt and if it exists then assign this to the system prompt global variable
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String stringSystemPrompt = sharedPreferences.getString("system prompt", "xx");
        if (!stringSystemPrompt.equals("xx")){
            // set the value of the spinner
            GlobalClass.system_prompt = stringSystemPrompt;
        Log.d("retrieve from SharedPreferences", "system prompt from SharedPreferences: " + stringSystemPrompt);
        }
        else{
            Log.d("retrieve from SharedPreferences","system prompt is not in SharedPreferences");

        }


    }


    public void filterPlaysList (View v) {

        RadioGroup radiobuttongroupGenreSelector;
        Button genreSelected;

        radiobuttongroupGenreSelector = (RadioGroup) findViewById(R.id.rabtngGenreSelector);
        int genreSelectedId = radiobuttongroupGenreSelector.getCheckedRadioButtonId();
        genreSelected = (RadioButton) findViewById(genreSelectedId);
        String genre = genreSelected.getText().toString();

        Log.d("spinner interaction","filter list to: " + genre);

        String[] array_plays;

        playslist_spinner = (Spinner) findViewById(R.id.spinnerPlaysList);

        if(genre.equals("All")){
            array_plays = getResources().getStringArray(R.array.plays_all);
        }
        else if(genre.equals("Histories")){
            array_plays = getResources().getStringArray(R.array.plays_histories);
        }
        else if(genre.equals("Tragedies")){
            array_plays = getResources().getStringArray(R.array.plays_tragedies);
        }
        else if(genre.equals("Comedies")){
            array_plays = getResources().getStringArray(R.array.plays_comedies);
        }
        else {
            array_plays = getResources().getStringArray(R.array.plays_all);
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, array_plays);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        playslist_spinner.setAdapter(dataAdapter);

    }

    //    public void openSelectedPlay (View v) {
//        Log.d("button interaction","open selected play: " + playslist_spinner.getSelectedItem().toString());
//    }
    public void HandleText (View v) {
        v.setEnabled(false);
        Log.d("myLog","*** message ***");
        Toast.makeText(this, "clicked", Toast.LENGTH_LONG).show();
    }

    public void launchUserInfoScreen(View v) {
        // launch a new activity

        Intent i = new Intent(this, com.shakespeare.new_app.SettingsActivity.class);
        startActivity(i);
        GlobalClass.intAboutYouScreenSource = 0;
    }

    public void launchSettings(View v) {
        // launch a new activity

        Intent i = new Intent(this, com.shakespeare.new_app.SettingsHomeActivity.class);
        startActivity(i);
    }

    public void exitApplication(View v) {
        // exit the application
        finish();
        System.exit(0);

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

    public void openAMSND(View v) {
        // launch a new activity

        Intent i = new Intent(this, AMSND.class);
        startActivity(i);
    }


    public void openSelectedPlay(View v) {
        // launch a new activity

        Log.d("action","hit open button");

        Intent i = new Intent(this, AMSND.class);
        // selected play to pass to the next activity
        com.shakespeare.new_app.GlobalClass.selectedPlay = playslist_spinner.getSelectedItem().toString();
        // specify the play filename based on the selection
        com.shakespeare.new_app.GlobalClass.selectedPlayCode = "";
//        GlobalClass.selectedPlayFilename = "";

        if(com.shakespeare.new_app.GlobalClass.selectedPlay.equals("Antony and Cleopatra") || com.shakespeare.new_app.GlobalClass.selectedPlay.equals("Antony and Cleopatra (1606)")) {
            com.shakespeare.new_app.GlobalClass.selectedPlay = "Antony and Cleopatra";
            com.shakespeare.new_app.GlobalClass.selectedPlayCode = "AAC";
//            GlobalClass.selectedPlayFilename = "AAC.txt";
        } else if(com.shakespeare.new_app.GlobalClass.selectedPlay.equals("Midsummer Night\'s Dream") || com.shakespeare.new_app.GlobalClass.selectedPlay.equals("A Midsummer Night\'s Dream (1595)")) {
            com.shakespeare.new_app.GlobalClass.selectedPlay = "A Midsummer Night\'s Dream";
            com.shakespeare.new_app.GlobalClass.selectedPlayCode = "AMSND";
//            GlobalClass.selectedPlayFilename = "AMSND.txt";
        } else if(com.shakespeare.new_app.GlobalClass.selectedPlay.equals("Romeo and Juliet") || com.shakespeare.new_app.GlobalClass.selectedPlay.equals("Romeo and Juliet (1594)")) {
            com.shakespeare.new_app.GlobalClass.selectedPlay = "Romeo and Juliet";
            com.shakespeare.new_app.GlobalClass.selectedPlayCode = "RomeoAndJuliet";
//            GlobalClass.selectedPlayFilename = "RANDJ.txt";
        } else if(com.shakespeare.new_app.GlobalClass.selectedPlay.equals("All\'s Well That Ends Well") || com.shakespeare.new_app.GlobalClass.selectedPlay.equals("All\'s Well That Ends Well (1602)")) {
            com.shakespeare.new_app.GlobalClass.selectedPlay = "All\'s Well That Ends Well";
            com.shakespeare.new_app.GlobalClass.selectedPlayCode = "AllsWellThatEndsWell";
        } else if(com.shakespeare.new_app.GlobalClass.selectedPlay.equals("As You Like It") || com.shakespeare.new_app.GlobalClass.selectedPlay.equals("As You Like It (1599)")) {
            com.shakespeare.new_app.GlobalClass.selectedPlay = "As You Like It";
            com.shakespeare.new_app.GlobalClass.selectedPlayCode = "AsYouLikeIt";
        } else if(com.shakespeare.new_app.GlobalClass.selectedPlay.equals("Comedy of Errors") || com.shakespeare.new_app.GlobalClass.selectedPlay.equals("The Comedy of Errors (1589)")) {
            com.shakespeare.new_app.GlobalClass.selectedPlay = "The Comedy of Errors";
            com.shakespeare.new_app.GlobalClass.selectedPlayCode = "ComedyOfErrors";
        } else if(com.shakespeare.new_app.GlobalClass.selectedPlay.equals("Hamlet") || com.shakespeare.new_app.GlobalClass.selectedPlay.equals("Hamlet, Prince of Denmark (1600)")) {
            com.shakespeare.new_app.GlobalClass.selectedPlay = "Hamlet, Prince of Denmark";
            com.shakespeare.new_app.GlobalClass.selectedPlayCode = "Hamlet";
        } else if(com.shakespeare.new_app.GlobalClass.selectedPlay.equals("Henry IV, Part I") || com.shakespeare.new_app.GlobalClass.selectedPlay.equals("Henry IV, Part I (1597)")) {
            com.shakespeare.new_app.GlobalClass.selectedPlay = "Henry IV, Part I";
            com.shakespeare.new_app.GlobalClass.selectedPlayCode = "HenryIVPart1";
        } else if(com.shakespeare.new_app.GlobalClass.selectedPlay.equals("Henry IV, Part II") || com.shakespeare.new_app.GlobalClass.selectedPlay.equals("Henry IV, Part II (1597)")) {
            com.shakespeare.new_app.GlobalClass.selectedPlay = "Henry IV, Part II";
            com.shakespeare.new_app.GlobalClass.selectedPlayCode = "HenryIVPart2";
        } else if(com.shakespeare.new_app.GlobalClass.selectedPlay.equals("Henry V") || com.shakespeare.new_app.GlobalClass.selectedPlay.equals("Henry V (1598)")) {
            com.shakespeare.new_app.GlobalClass.selectedPlay = "Henry V";
            com.shakespeare.new_app.GlobalClass.selectedPlayCode = "HenryV";
        } else if(com.shakespeare.new_app.GlobalClass.selectedPlay.equals("Julius Caesar") || com.shakespeare.new_app.GlobalClass.selectedPlay.equals("Julius Caesar (1599)")) {
            com.shakespeare.new_app.GlobalClass.selectedPlay = "Julius Caesar";
            com.shakespeare.new_app.GlobalClass.selectedPlayCode = "JuliusCaesar";
        } else if(com.shakespeare.new_app.GlobalClass.selectedPlay.equals("King Lear") || com.shakespeare.new_app.GlobalClass.selectedPlay.equals("The Tragedy of King Lear (1605)")) {
            com.shakespeare.new_app.GlobalClass.selectedPlay = "King Lear";
            com.shakespeare.new_app.GlobalClass.selectedPlayCode = "KingLear";
        } else if(com.shakespeare.new_app.GlobalClass.selectedPlay.equals("Macbeth") || com.shakespeare.new_app.GlobalClass.selectedPlay.equals("The Tragedy of Macbeth (1605)")) {
            com.shakespeare.new_app.GlobalClass.selectedPlay = "The Tragedy of Macbeth";
            com.shakespeare.new_app.GlobalClass.selectedPlayCode = "Macbeth";
        } else if(com.shakespeare.new_app.GlobalClass.selectedPlay.equals("Merchant of Venice") || com.shakespeare.new_app.GlobalClass.selectedPlay.equals("The Merchant of Venice (1596)")) {
            com.shakespeare.new_app.GlobalClass.selectedPlay = "The Merchant of Venice";
            com.shakespeare.new_app.GlobalClass.selectedPlayCode = "MerchantOfVenice";
        } else if(com.shakespeare.new_app.GlobalClass.selectedPlay.equals("Much Ado about Nothing") || com.shakespeare.new_app.GlobalClass.selectedPlay.equals("Much Ado about Nothing (1594)")) {
            com.shakespeare.new_app.GlobalClass.selectedPlay = "Much Ado about Nothing";
            com.shakespeare.new_app.GlobalClass.selectedPlayCode = "MuchAdo";
        } else if(com.shakespeare.new_app.GlobalClass.selectedPlay.equals("Othello") || com.shakespeare.new_app.GlobalClass.selectedPlay.equals("Othello, the Moor of Venice (1604)")) {
            com.shakespeare.new_app.GlobalClass.selectedPlay = "Othello, the Moor of Venice";
            com.shakespeare.new_app.GlobalClass.selectedPlayCode = "Othello";
        } else if(com.shakespeare.new_app.GlobalClass.selectedPlay.equals("The Tempest") || com.shakespeare.new_app.GlobalClass.selectedPlay.equals("The Tempest (1611)")) {
            com.shakespeare.new_app.GlobalClass.selectedPlay = "The Tempest";
            com.shakespeare.new_app.GlobalClass.selectedPlayCode = "Tempest";
        } else {
            return;
        }
        Log.d("open play","selected play: " + com.shakespeare.new_app.GlobalClass.selectedPlay);
        Log.d("open play","selected play filename: " + com.shakespeare.new_app.GlobalClass.selectedPlayCode);
//        Log.d("open play","selected play filename: " + com.shakespeare.new_app.GlobalClass.selectedPlayFilename);
        startActivity(i);

    }

    public void setFontSmall12sp(View v){
        // set font size to small 12sp
        com.shakespeare.new_app.GlobalClass.fontsizesp = 12;
        Log.d("fontLog","font size set to 12sp");
        refreshFontSizeGenreSelector(v);

    }
    public void setFontMedium16sp(View v){
        // set font size to medium 16sp
        com.shakespeare.new_app.GlobalClass.fontsizesp = 16;
        Log.d("fontLog","font size set to 16sp");
        refreshFontSizeGenreSelector(v);
    }
    public void setFontLarge20sp(View v){
        // set font size to large 20sp
        com.shakespeare.new_app.GlobalClass.fontsizesp = 20;
        Log.d("fontLog","font size set to 20sp");
        refreshFontSizeGenreSelector(v);
    }

    public void refreshFontSizeGenreSelector(View v){
        TextView btnfontSLabel = findViewById(R.id.btnfontsmall);
        btnfontSLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, com.shakespeare.new_app.GlobalClass.fontsizesp);

        TextView btnfontMLabel = findViewById(R.id.btnfontmedium);
        btnfontMLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, com.shakespeare.new_app.GlobalClass.fontsizesp);

        TextView btnfontLLabel = findViewById(R.id.btnfontlarge);
        btnfontLLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, com.shakespeare.new_app.GlobalClass.fontsizesp);

//        TextView btnSettingsLabel = findViewById(R.id.btnSettings);
//        btnSettingsLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, GlobalClass.fontsizesp);

//        TextView btnAACLabel = findViewById(R.id.btnAAC);
//        btnAACLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, com.shakespeare.new_app.GlobalClass.fontsizesp);
//
//        TextView btnAMSNDLabel = findViewById(R.id.btnAMSND);
//        btnAMSNDLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, com.shakespeare.new_app.GlobalClass.fontsizesp);

        TextView genreSelectAll = findViewById(R.id.rabtnAll);
        genreSelectAll.setTextSize(TypedValue.COMPLEX_UNIT_SP, com.shakespeare.new_app.GlobalClass.fontsizesp);

        TextView genreSelectHis = findViewById(R.id.rabtnHistories);
        genreSelectHis.setTextSize(TypedValue.COMPLEX_UNIT_SP, com.shakespeare.new_app.GlobalClass.fontsizesp);

        TextView genreSelectTra = findViewById(R.id.rabtnTragedies);
        genreSelectTra.setTextSize(TypedValue.COMPLEX_UNIT_SP, com.shakespeare.new_app.GlobalClass.fontsizesp);

        TextView genreSelectCom = findViewById(R.id.rabtnComedies);
        genreSelectCom.setTextSize(TypedValue.COMPLEX_UNIT_SP, com.shakespeare.new_app.GlobalClass.fontsizesp);

        TextView genreSelectTitle = findViewById(R.id.txtTitleSelectGenre);
        genreSelectTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, com.shakespeare.new_app.GlobalClass.fontsizesp);

//        Spinner playslist_spinner = findViewById(R.id.spinnerPlaysList);
//        playslist_spinner.setTextSize(TypedValue.COMPLEX_UNIT_SP, GlobalClass.fontsizesp);


    }
}

