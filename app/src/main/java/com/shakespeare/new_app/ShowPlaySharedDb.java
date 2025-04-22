package com.shakespeare.new_app;

//import static android.os.Build.VERSION_CODES.R;

import android.annotation.SuppressLint;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.database.RemoteDatabaseHelperHttp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ShowPlaySharedDb extends AppCompatActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_showplayshareddb);
        this.setTitle(getResources().getString(R.string.home_screen_title));
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String url = "sqlitecloud://cgdjyovjhk.g2.sqlite.cloud:8860/play_navigation.db?apikey=SFR0f2mYTxb3bbOiaALxEyatvEt2WDn5hYygAXiuE2o";
        RemoteDatabaseHelperHttp helper = new RemoteDatabaseHelperHttp(this, url);

        helper.runQueryFromJava("SELECT * FROM play_character", result -> {
            if (result.isSuccess()) {
                List<Map<String, String>> allRows = result.getData();

                // Get unique play codes
                Set<String> playCodeSet = new HashSet<>();
                for (Map<String, String> row : allRows) {
                    playCodeSet.add(row.get("play_code"));
                }

                List<String> playCodes = new ArrayList<>(playCodeSet);
                List<String> displayNames = new ArrayList<>();
                Map<String, String> codeToDisplay = new HashMap<>();

                for (String code : playCodes) {
                    int resId = getResources().getIdentifier(code, "string", getPackageName());
                    String display = resId != 0 ? getString(resId) : code;
                    displayNames.add(display);
                    codeToDisplay.put(display, code);
                }

                Spinner spinner = findViewById(R.id.spinnerPlayFilter);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, displayNames);
                spinner.setAdapter(adapter);

                RecyclerView recyclerView = findViewById(R.id.recyclerView);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));

                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (view == null) return; // user hasn't made a selection yet, just initial draw
                        String selectedPlayName = displayNames.get(position);
                        TextView tvSelectedPlayName = findViewById(R.id.play_selected_heading);
                        tvSelectedPlayName.setText("Characters in " + selectedPlayName); // show play name as a heading
                        String selectedPlayCodeForUserChr = codeToDisplay.get(selectedPlayName);
                        String sql = "SELECT * FROM play_character WHERE play_code = '" + selectedPlayCodeForUserChr + "';";
                        Log.d("query", sql);

                        helper.runQueryFromJava(sql, filteredResult -> {
                            if (filteredResult.isSuccess()) {
                                List<Map<String, String>> filteredRows = filteredResult.getData();
                                CharacterAdapter charAdapter = new CharacterAdapter(ShowPlaySharedDb.this, filteredRows);
                                recyclerView.setAdapter(charAdapter);
                            } else {
                                Log.e("SQL Query", "Failed to get characters", filteredResult.getError());
                            }
                            return null;
                        });
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
            } else {
                Log.e("HTTP Helper", "Failed to get play list", result.getError());
            }
            return null;
        });
    }


}

