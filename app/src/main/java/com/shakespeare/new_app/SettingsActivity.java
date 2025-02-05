package com.shakespeare.new_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import com.shakespeare.new_app.R;

public class SettingsActivity extends AppCompatActivity {

    private String[] states;
    private TypedArray imgs;
    private ImageView image;
    private Spinner countryspinner;

    private String[] agedecades;
    private Spinner agedecadesspinner;

    private String[] ageyears;
    private Spinner ageyearsspinner;

    private RadioGroup purposerdogrp;
    private Button radioButtonPurposeSelected;
    private RadioGroup genderrdogrp;

    private Button radioButtonGenderSelected;
    private String[] languages;
    private Spinner languagespinner;
    private EditText anythingElse_et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("settings","open settings activity");
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // countries spinner
        states = getResources().getStringArray(R.array.countries_list);
        imgs = getResources().obtainTypedArray(R.array.countries_flag_list);

        image = (ImageView) findViewById(R.id.country_image);
        countryspinner = (Spinner) findViewById(R.id.country_spinner);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, states);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        countryspinner.setAdapter(dataAdapter);

        // check for prior set value of country and if it exists then pre-populate
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MyPrefs", MODE_PRIVATE);
        Integer spinnerCountryValue = sharedPreferences.getInt("country spinner",-1);
        Log.d("retrieve from SharedPreferences","spinnerCountryValue from SharedPreferences: " + spinnerCountryValue);
        if(spinnerCountryValue != -1)
            // set the value of the spinner
            countryspinner.setSelection(spinnerCountryValue);


        countryspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                image.setImageResource(imgs.getResourceId(
                        countryspinner.getSelectedItemPosition(), -1));
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        agedecades = getResources().getStringArray(R.array.age_decades_list);

        agedecadesspinner = (Spinner) findViewById(R.id.age_decades_spinner);

        ArrayAdapter<String> dataAdapter_agedecades = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, agedecades);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        agedecadesspinner.setAdapter(dataAdapter_agedecades);

        // check for prior set value of age decades and if it exists then pre-populate
        Integer spinnerAgeDecadesValue = sharedPreferences.getInt("age decades spinner",-1);
        Log.d("retrieve from SharedPreferences","spinnerAgeDecadesValue from SharedPreferences: " + spinnerAgeDecadesValue);
        if(spinnerAgeDecadesValue != -1)
            // set the value of the spinner
            agedecadesspinner.setSelection(spinnerAgeDecadesValue);

        ageyears = getResources().getStringArray(R.array.age_years_list);

        ageyearsspinner = (Spinner) findViewById(R.id.age_years_spinner);

        ArrayAdapter<String> dataAdapter_ageyears = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, ageyears);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ageyearsspinner.setAdapter(dataAdapter_ageyears);

        // check for prior set value of age years and if it exists then pre-populate
        Integer spinnerAgeYearsValue = sharedPreferences.getInt("age years spinner",-1);
        Log.d("retrieve from SharedPreferences","spinnerAgeYearsValue from SharedPreferences: " + spinnerAgeDecadesValue);
        if(spinnerAgeYearsValue != -1)
            // set the value of the spinner
            ageyearsspinner.setSelection(spinnerAgeYearsValue);

        agedecadesspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                agedecadesspinner = (Spinner) findViewById(R.id.age_decades_spinner);
                String decadesSelected = agedecadesspinner.getSelectedItem().toString();

                ageyearsspinner = (Spinner) findViewById(R.id.age_years_spinner);
                String yearsSelected = ageyearsspinner.getSelectedItem().toString();

                Log.d("check", "Decades selected: " + decadesSelected);
                Log.d("check", "Years selected: " + yearsSelected);

                TextView tv_ageSpecified = findViewById(R.id.ageSpecified);
                switch(decadesSelected) {
                    case "decades...":
                        tv_ageSpecified.setText("Age not specified yet");
                        break;
                    case "10+":
                        tv_ageSpecified.setText("Age specified: 100 or more");
                        break;
                    default:
                        switch (yearsSelected) {
                            case "years...":
                                tv_ageSpecified.setText("Age not specified yet");
                                break;
                            default:
                                tv_ageSpecified.setText("Age specified: " + decadesSelected + yearsSelected);
                                break;
                        };
                        break;
                };
                tv_ageSpecified.setTypeface(tv_ageSpecified.getTypeface(), Typeface.ITALIC);
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        final String[] agePrompt = new String[1];
        agePrompt[0] = "";

        ageyearsspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                agedecadesspinner = (Spinner) findViewById(R.id.age_decades_spinner);
                String decadesSelected = agedecadesspinner.getSelectedItem().toString();

                ageyearsspinner = (Spinner) findViewById(R.id.age_years_spinner);
                String yearsSelected = ageyearsspinner.getSelectedItem().toString();

                Log.d("check", "Decades selected: " + decadesSelected);
                Log.d("check", "Years selected: " + yearsSelected);

                TextView tv_ageSpecified = findViewById(R.id.ageSpecified);
                switch(decadesSelected) {
                    case "decades...":
                        tv_ageSpecified.setText("Age not specified yet");
                        break;
                    case "10+":
                        tv_ageSpecified.setText("Age specified: 100 or more");
                        break;
                    default:
                        switch (yearsSelected) {
                            case "years...":
                                tv_ageSpecified.setText("Age not specified yet");
                                break;
                            default:
                                tv_ageSpecified.setText("Age specified: " + decadesSelected + yearsSelected);
                                break;
                        };
                        break;
                };
                tv_ageSpecified.setTypeface(tv_ageSpecified.getTypeface(), Typeface.ITALIC);
            };

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        //check purpose setting in preferences in case already selected and if so then pre-populate
        String radioPurpose = sharedPreferences.getString("purpose","X");
        Log.d("retrieve from SharedPreferences","Radio Purpose value from SharedPreferences: " + radioPurpose);
        if(radioPurpose.equals("My primary school studies")){
            RadioButton rb_Purpose_PS = findViewById(R.id.rabtnPurposePrimarySchool);
            rb_Purpose_PS.setChecked(true);
        } else if(radioPurpose.equals("My middle school studies")){
            RadioButton rb_Purpose_MS = findViewById(R.id.rabtnPurposeMiddleSchool);
            rb_Purpose_MS.setChecked(true);
        } else if(radioPurpose.equals("My high school studies")){
            RadioButton rb_Purpose_HS = findViewById(R.id.rabtnPurposeHighSchool);
            rb_Purpose_HS.setChecked(true);
        } else if(radioPurpose.equals("University studies")){
            RadioButton rb_Purpose_Univ = findViewById(R.id.rabtnPurposeUniversity);
            rb_Purpose_Univ.setChecked(true);
        } else if(radioPurpose.equals("My personal interest")){
            RadioButton rb_Purpose_PI = findViewById(R.id.rabtnPurposeInterest);
            rb_Purpose_PI.setChecked(true);
        } else if(radioPurpose.equals("I perform in theatre")){
            RadioButton rb_Purpose_Theatre = findViewById(R.id.rabtnPurposeTheatre);
            rb_Purpose_Theatre.setChecked(true);
        } else if(radioPurpose.equals("Other or choose not to answer")){
            RadioButton rb_Purpose_Other = findViewById(R.id.rabtnPurposeOther);
            rb_Purpose_Other.setChecked(true);
        }

        languages = getResources().getStringArray(R.array.languages_list);

        languagespinner = (Spinner) findViewById(R.id.language_spinner);

        ArrayAdapter<String> dataAdapter_language = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, languages);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        languagespinner.setAdapter(dataAdapter_language);

        // check for prior set value of language and if it exists then pre-populate
        Integer spinnerLanguageValue = sharedPreferences.getInt("language spinner",-1);
        Log.d("retrieve from SharedPreferences","spinnerLanguageValue from SharedPreferences: " + spinnerLanguageValue);
        if(spinnerLanguageValue != -1)
            // set the value of the spinner
            languagespinner.setSelection(spinnerLanguageValue);


        languagespinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                languagespinner = (Spinner) findViewById(R.id.language_spinner);
                String languageSelected = languagespinner.getSelectedItem().toString();

                Log.d("check", "languagespinner: " + languageSelected);

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        //check gender in preferences in case already selected and if so then pre-populate
        String radioGender = sharedPreferences.getString("genderCodeSelected","X");
        Log.d("retrieve from SharedPreferences","Radio Gender value from SharedPreferences: " + radioPurpose);
        if(radioGender.equals("F")){
            RadioButton rb_Gender_F = findViewById(R.id.rabtnGenderFemale);
            rb_Gender_F.setChecked(true);
        } else if(radioGender.equals("M")){
            RadioButton rb_Gender_M = findViewById(R.id.rabtnGenderMale);
            rb_Gender_M.setChecked(true);
        } else if(radioGender.equals("O")){
            RadioButton rb_Gender_O = findViewById(R.id.rabtnGenderOther);
            rb_Gender_O.setChecked(true);
        } else if(radioGender.equals("")){
            RadioButton rb_Gender_ChooseNoAnswer = findViewById(R.id.rabtnGenderChooseNoAnswer);
            rb_Gender_ChooseNoAnswer.setChecked(true);
        }

        //check 'Anything Else?' in preferences in case already selected and if so then pre-populate
        String stringAnythingElse = sharedPreferences.getString("anything else","X");
        Log.d("retrieve from SharedPreferences","Text 'Anything Else?' from SharedPreferences: " + stringAnythingElse);
        if(!stringAnythingElse.equals("X")){
            EditText et_AnythingElse = findViewById(R.id.anythingElse);
            et_AnythingElse.setText(stringAnythingElse);
        }

//        updateSystemPrompt(v);

        // set font size to the standard across the app
        TextView ageInstr = findViewById(R.id.ageInstruction);
        ageInstr.setTextSize(TypedValue.COMPLEX_UNIT_SP, com.shakespeare.new_app.GlobalClass.fontsizesp);
        TextView purposeInstr = findViewById(R.id.purposeInstruction);
        purposeInstr.setTextSize(TypedValue.COMPLEX_UNIT_SP, com.shakespeare.new_app.GlobalClass.fontsizesp);
        TextView countryInstr = findViewById(R.id.country_instruction);
        countryInstr.setTextSize(TypedValue.COMPLEX_UNIT_SP, com.shakespeare.new_app.GlobalClass.fontsizesp);
        TextView languageInstr = findViewById(R.id.languageInstruction);
        languageInstr.setTextSize(TypedValue.COMPLEX_UNIT_SP, com.shakespeare.new_app.GlobalClass.fontsizesp);
        TextView genderInstr = findViewById(R.id.genderInstruction);
        genderInstr.setTextSize(TypedValue.COMPLEX_UNIT_SP, com.shakespeare.new_app.GlobalClass.fontsizesp);
        TextView anythingElseInstr = findViewById(R.id.anythingElseInstruction);
        anythingElseInstr.setTextSize(TypedValue.COMPLEX_UNIT_SP, com.shakespeare.new_app.GlobalClass.fontsizesp);

    }

    public void returnToSettingsHome(View v) {
        // launch a new activity which in this case is returning to the main activity

        Intent i = new Intent(this, com.shakespeare.new_app.MainActivity.class);

        if(GlobalClass.intAboutYouScreenSource == 1) {
            i = new Intent(this, SettingsHomeActivity.class);
        }

        startActivity(i);
    }

    public void updateSystemPrompt(View v) {
        // update system prompt to use the  user profile now updated

        Log.d("system prompt", "*** Updating system prompt ***");

        agedecadesspinner = (Spinner) findViewById(R.id.age_decades_spinner);
        String decadesSelected = agedecadesspinner.getSelectedItem().toString();
        int agedecadesspinner_pos = agedecadesspinner.getSelectedItemPosition();
        Log.d("system prompt", decadesSelected);

        ageyearsspinner = (Spinner) findViewById(R.id.age_years_spinner);
        String yearsSelected = ageyearsspinner.getSelectedItem().toString();
        int ageyearsspinner_pos = ageyearsspinner.getSelectedItemPosition();
        if(ageyearsspinner_pos == 0){
            ageyearsspinner_pos = 10;
        }
        Log.d("system prompt", yearsSelected);

        String agePrompt;
        agePrompt = "";
        String ageImputed;
        ageImputed = "";
        String ageDecades;
        ageDecades = "";
        String ageYears;
        ageYears = "";

        // if decades selected are zero then the user age is under 10 and so special treatment is needed.
        if(decadesSelected.equals("0")){
            if(yearsSelected.equals("1") || yearsSelected.equals("2") || yearsSelected.equals("3") || yearsSelected.equals("4") || yearsSelected.equals("5")){
                // user specified age of under 6 years old which is too young for this application
                agePrompt = "";
                ageImputed = "none";
                ageDecades = "none";
                ageYears = "none";
            }
            else if(yearsSelected.equals("6") || yearsSelected.equals("7") || yearsSelected.equals("8") || yearsSelected.equals("9")) {
                agePrompt = "I am " + yearsSelected + " years of age. ";
                ageImputed = yearsSelected;
                ageDecades = "0";
                ageYears = yearsSelected;
            }
            else {
                agePrompt = "";
                ageImputed = "none";
                ageDecades = "none";
                ageYears = "none";
            }
        }

        // otherwise only use the age given if decades are selected
        if(!decadesSelected.equals("decades...") && !decadesSelected.equals("0")){

            if(decadesSelected.equals("10+")) {
                agePrompt = "I am over 100 years of age. ";
                ageImputed = "100+";
                ageDecades = "10";
                ageYears = "0";
            }
            else if(!yearsSelected.equals("years...")) {
                // if years are selected then use the years selected
                agePrompt = "I am " + decadesSelected + yearsSelected + " years of age. ";
                ageImputed = decadesSelected + yearsSelected;
                ageDecades = decadesSelected;
                ageYears = yearsSelected;
            }
            else {
                // if years are not selected then assume the age is on the decade
                agePrompt = "I am " + decadesSelected + "0 years of age. ";
                ageImputed = decadesSelected + "0";
                ageDecades = decadesSelected;
                ageYears = "0";
            }

        }
        Log.d("system prompt", agePrompt);
        Log.d("system prompt", ageImputed);

        purposerdogrp = (RadioGroup) findViewById(R.id.userPurpose);
        int purposeSelectedId = purposerdogrp.getCheckedRadioButtonId();
        String purposePrompt;
        purposePrompt = "";
        String purpose;
        purpose = "";
        if(purposeSelectedId != -1){

            // find the radiobutton by returned id
            radioButtonPurposeSelected = (RadioButton) findViewById(purposeSelectedId);
            purpose = radioButtonPurposeSelected.getText().toString();
            purposePrompt = "My main reason for learning Shakespeare is " + purpose + ". ";
            Log.d("system prompt", "Purpose selected: " + radioButtonPurposeSelected.getText().toString() + " (id " + purposeSelectedId + ") ");

        }
        else {
            Log.d("system prompt", "Purpose not selected");
        }

        countryspinner = (Spinner) findViewById(R.id.country_spinner);
        String countrySelected = countryspinner.getSelectedItem().toString();
        int countryspinner_pos = countryspinner.getSelectedItemPosition();
        Log.d("system prompt", countrySelected);
        String countryPrompt;
        if(!countrySelected.equals("Please select...")){

            countryPrompt = "I live in " + countrySelected + ". ";
            Log.d("system prompt", countryPrompt);

        }
        else {
            countryPrompt = "";
            Log.d("system prompt", "Country not selected");
        }

        languagespinner = (Spinner) findViewById(R.id.language_spinner);
        String languageSelected = languagespinner.getSelectedItem().toString();
        Log.d("system prompt", languageSelected);
        int languagespinner_pos = languagespinner.getSelectedItemPosition();

        String languagePrompt;
        if(!languageSelected.equals("Please select...") && !languageSelected.equals("Mine is not listed...")){

            languagePrompt = "Please give your responses in the language of " + languageSelected + ". ";
            Log.d("system prompt", languagePrompt);

        }
        else {
            languagePrompt = "";
            Log.d("system prompt", "Language not selected");
        }

        genderrdogrp = (RadioGroup) findViewById(R.id.userGenderID);
        int genderSelectedId = genderrdogrp.getCheckedRadioButtonId();
        String genderValue;
        genderValue = "";
        String genderPrompt;
        genderPrompt = "";
        String genderCodeSelected;
        genderCodeSelected = "";
        if(genderSelectedId != -1) {

            // find the radiobutton by returned id
            radioButtonGenderSelected = (RadioButton) findViewById(genderSelectedId);
            genderCodeSelected = radioButtonGenderSelected.getText().toString();
            switch(genderCodeSelected)
            {
                case "F":
                    genderPrompt = "I am female. ";
                    break;
                case "M":
                    genderPrompt = "I am male. ";
                    break;
                case "Other":
                    genderPrompt = "I am of non-binary gender, i.e. neither male nor female. ";
                    break;
                default:
                    genderPrompt = "";
            }

            Log.d("system prompt", "Gender selected: "+radioButtonGenderSelected.getText().toString() + " (id " + genderSelectedId + ") ");

        }
        else {
            Log.d("system prompt", "Gender not selected");
        }

        anythingElse_et = (EditText) findViewById(R.id.anythingElse);
        String anythingElseString = anythingElse_et.getText().toString();
        Log.d("system prompt", anythingElseString);

        String systemPrompt;

        if (anythingElseString.length() > 1){
            systemPrompt = agePrompt + genderPrompt + purposePrompt + countryPrompt + languagePrompt + " Please also bear in mind " + anythingElseString + " Assume this query relates to the works of, or a work of, William Shakespeare.";
        }
        else{
            systemPrompt = agePrompt + genderPrompt + purposePrompt + countryPrompt + languagePrompt + " Assume this query relates to the works of, or a work of, William Shakespeare.";
        }
        Log.d("system prompt", systemPrompt);
        Toast.makeText(this, "Instruction: \n" + systemPrompt, Toast.LENGTH_SHORT).show(); // in Activity

        com.shakespeare.new_app.GlobalClass.system_prompt = systemPrompt;

        Log.d("system prompt", "*** Updated system prompt ***");

        // save settings locally so that these persist next time the settings screen is opened; from:
        // https://www.quora.com/What-is-the-most-efficient-way-of-storing-data-locally-on-an-android-Android-Programming
        // https://stackoverflow.com/questions/25869956/shared-preference-returns-always-the-default-value
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("MyPrefs", MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.remove("gender").commit();

        Log.d("shared preferences","shared preferences: "+sharedPreferences.toString());

        editor.putString("age", ageImputed);
        editor.putString("age decades", ageDecades);
        editor.putString("age years", ageYears);
        editor.putInt("age decades spinner", agedecadesspinner_pos);
        editor.putInt("age years spinner", ageyearsspinner_pos);
        editor.putString("purpose", purpose);
        editor.putString("country", countrySelected);
        editor.putInt("country spinner", countryspinner_pos);
        editor.putString("language", languageSelected);
        editor.putInt("language spinner", languagespinner_pos);
        editor.putInt("genderId", genderSelectedId);
        editor.putString("genderCodeSelected", genderCodeSelected);
        editor.putString("anything else", anythingElseString);
        editor.putString("system prompt", systemPrompt);
        editor.apply(); // editor.apply() or editor.commit();

    }

    public void updateSystemPromptThenReturnToMain(View v) {
        // update system prompt to use the  user profile now updated
        // launch a new activity which in this case is returning to the main activity

        com.shakespeare.new_app.GlobalClass.system_prompt = "";
        updateSystemPrompt(v);

        Intent i = new Intent(this, com.shakespeare.new_app.MainActivity.class);

        if(GlobalClass.intAboutYouScreenSource == 1) {
            i = new Intent(this, SettingsHomeActivity.class);
        }

        startActivity(i);

    }

}

