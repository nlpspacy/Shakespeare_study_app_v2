package com.shakespeare.new_app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.database.QueryResult;
import com.example.database.RemoteDatabaseHelperHttp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowPlaySharedDb extends AppCompatActivity {

    private Spinner spinner;
    private RecyclerView recyclerView;
    private TextView headingText;
    private RemoteDatabaseHelperHttp helperHttp;

    private Map<String, String> playCodeToNameMap = new HashMap<>();
    private Map<String, String> playNameToCodeMap = new HashMap<>();
    private List<String> playNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showplayshareddb);

        spinner = findViewById(R.id.play_spinner);
        recyclerView = findViewById(R.id.recycler_view);
        headingText = findViewById(R.id.play_selected_heading);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        helperHttp = new RemoteDatabaseHelperHttp();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainlayout), (v, insets) -> insets);

        loadInitialData();
    }

    private void loadInitialData() {
        helperHttp.runQueryFromJava("SELECT DISTINCT play_code FROM play_character ORDER BY play_code", result -> {
            if (result.isSuccess()) {
                List<Map<String, String>> rows = result.getData();
                if (rows != null) {
                    for (Map<String, String> row : rows) {
                        String code = row.get("play_code");
                        String fullName = getStringResourceByName(code);
                        if (fullName != null && !fullName.isEmpty()) {
                            playCodeToNameMap.put(code, fullName);
                            playNameToCodeMap.put(fullName, code);
                            playNames.add(fullName);
                        }
                    }
                }
                setupSpinner();
            } else {
                Toast.makeText(this, "Failed to load play list.", Toast.LENGTH_SHORT).show();
            }
            return null;
        });
    }
    private void setupSpinner() {
        // Insert "Select a play..." as the first option
        List<String> spinnerOptions = new ArrayList<>();
        spinnerOptions.add("Select a play...");
        spinnerOptions.addAll(playNames);

//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinnerOptions);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, spinnerOptions) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(R.id.text_spinner_item);

                if ("Select a play...".equals(getItem(position))) {
                    textView.setTextColor(Color.GRAY); // Light gray for the hint
                } else {
                    textView.setTextColor(Color.WHITE); // Normal white for real plays
                }

                return view;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = view.findViewById(R.id.text_spinner_item);

                if ("Select a play...".equals(getItem(position))) {
                    textView.setTextColor(Color.GRAY);
                } else {
                    textView.setTextColor(Color.WHITE);
                }

                return view;
            }
        };


//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// 🛠 Set correct custom spinner item also for dropdown:
        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String playName = spinnerOptions.get(position);

                if ("Select a play...".equals(playName)) {
                    headingText.setText("Please select your play");
                    return; // 🚫 Don't load anything if it's the prompt
                }

                String playCode = playNameToCodeMap.get(playName);
                if (playCode != null) {
                    headingText.setText("Characters in " + playName);
                    runQueryForPlay(playCode);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void runQueryForPlay(String playCode) {
        String sql = "SELECT * FROM play_character_user WHERE play_code = '" + playCode + "' AND username = '" + UserManager.getUsername(this) + "' ORDER BY is_a_group, character_full_name;";
        helperHttp.runQueryFromJava(sql, result -> {
            if (result.isSuccess()) {
                List<Map<String, String>> rows = result.getData();
                List<Map<String, String>> structured = CharacterAdapter.structureGroupedList(rows);
                CharacterAdapter adapter = new CharacterAdapter(this, structured);
                recyclerView.setAdapter(adapter);
            } else {
                Toast.makeText(this, "Failed to load characters.", Toast.LENGTH_SHORT).show();
            }
            return null;
        });
    }

    private String getStringResourceByName(String aString) {
        String packageName = getPackageName();
        int resId = getResources().getIdentifier(aString, "string", packageName);
        if (resId == 0) return null;
        else return getString(resId);
    }


    public void returnToMain(View v) {
        // launch a new activity

        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
    public void goBack(View v) {
        // go back to previous screen/activity

        getOnBackPressedDispatcher().onBackPressed();

        Intent i = new Intent(this, com.shakespeare.new_app.SettingsHomeActivity.class);
        startActivity(i);
    }

    public void refreshScreen(View v) {
        // go back to previous screen/activity

        Intent i = new Intent(this, ShowPlaySharedDb.class);
        startActivity(i);

//        Intent i = new Intent(this, com.shakespeare.new_app.MainActivity.class);
//        startActivity(i);
    }

}
