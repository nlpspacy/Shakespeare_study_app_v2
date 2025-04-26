// This will be your updated activity logic with sorted play codes and grouped character display

package com.shakespeare.new_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsCompat.Type;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.database.RemoteDatabaseHelperHttp;
import com.shakespeare.new_app.R;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ShowPlaySharedDb extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showplayshareddb);

        // Set default username (only needed once, or after login)
        UserManager.saveUsername(this, "dan");

// Attempt to create user on server
        OkHttpClient client = new OkHttpClient();
        JSONObject json = new JSONObject();
        try {
            json.put("username", UserManager.getUsername(this));
            RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));

            Request request = new Request.Builder()
                    .url("https://android-sqlitecloud-api-production.up.railway.app/create_user")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e("CreateUser", "Failed to create user", e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        Log.e("CreateUser", "Server error: " + response.code());
                    } else {
                        Log.d("CreateUser", "User created successfully or already exists");
                    }
                }
            });
        } catch (Exception e) {
            Log.e("CreateUser", "Exception", e);
        }


        // ... your existing code to setup spinner, RecyclerView etc.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainlayout), (v, insets) -> {
            v.setPadding(0, insets.getInsets(Type.systemBars()).top, 0, 0);
            return insets;
        });

        String url = "sqlitecloud://cgdjyovjhk.g2.sqlite.cloud:8860/play_navigation.db?apikey=SFR0f2mYTxb3bbOiaALxEyatvEt2WDn5hYygAXiuE2o";
        RemoteDatabaseHelperHttp helper = new RemoteDatabaseHelperHttp(this, url);

        // This query is used to populate the play names in the dropdown list / spinner list.
        helper.runQueryFromJava("SELECT DISTINCT play_code FROM play_character ORDER BY play_code", result -> {

            if (result.isSuccess()) {
                List<Map<String, String>> allRows = result.getData();

                Set<String> playCodeSet = new HashSet<>();
                for (Map<String, String> row : allRows) {
                    playCodeSet.add(row.get("play_code"));
                }

                List<String> playCodes = new ArrayList<>(playCodeSet);
                playCodes.add(" Select a play..."); // placeholder
                Collections.sort(playCodes); // sort alphabetically

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
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        // Update title using the selected play name
                        String selectedPlayCodeForTitle = parent.getItemAtPosition(position).toString();

                        // Check whether a valid play has been selected
                        if (selectedPlayCodeForTitle.equals(" Select a play...")) {
                            return;
                        }

                        TextView tvTitle = findViewById(R.id.play_selected_heading);
                        tvTitle.setText("Characters in " + selectedPlayCodeForTitle);

                        String selectedPlayName = displayNames.get(position);
                        String selectedPlayCode = codeToDisplay.get(selectedPlayName);

//                        String selectedPlayShortName = parent.getItemAtPosition(position).toString();
                        String username = UserManager.getUsername(ShowPlaySharedDb.this);

                        // Escape dangerous characters
                        String safePlay = escapeSqlString(selectedPlayCode);
                        String safeUsername = escapeSqlString(username);

                        String sql = "SELECT * FROM play_character_user WHERE play_code = '"
                                + safePlay + "' AND username = '" + safeUsername + "';";
                        Log.d("sql check","safe sql: " + sql);

                        helper.runQueryFromJava(sql, filteredResult -> {
                            if (filteredResult.isSuccess()) {
                                List<Map<String, String>> filteredRows = filteredResult.getData();
                                List<Map<String, String>> structuredRows = new ArrayList<>();
                                Map<String, List<Map<String, String>>> grouped = new LinkedHashMap<>();

                                for (Map<String, String> row : filteredRows) {
                                    String group = row.get("is_a_group");
                                    if (group != null && !group.equals("null") && !group.isEmpty()) {
                                        grouped.computeIfAbsent(group, k -> new ArrayList<>()).add(row);
                                    } else {
                                        grouped.computeIfAbsent("__ungrouped__", k -> new ArrayList<>()).add(row);
                                    }
                                }

                                for (Map.Entry<String, List<Map<String, String>>> entry : grouped.entrySet()) {
                                    if (!"__ungrouped__".equals(entry.getKey())) {
                                        Map<String, String> groupHeader = new HashMap<>();
                                        groupHeader.put("row_type", "group");
                                        groupHeader.put("is_a_group", entry.getKey());
                                        structuredRows.add(groupHeader);
                                    }
                                    for (Map<String, String> charRow : entry.getValue()) {
                                        charRow.put("row_type", "character");

                                        // ✅ If character is part of a group, mark it
                                        if (charRow.containsKey("is_a_group") && charRow.get("is_a_group") != null && !charRow.get("is_a_group").equals("null") && !charRow.get("is_a_group").isEmpty()) {
                                            charRow.put("belongs_to_group", "true");
                                        }

                                        // ✅ Remove is_a_group to avoid adapter misclassifying characters as group headers
                                        charRow.remove("is_a_group");

                                        structuredRows.add(charRow);
                                    }                                }

                                CharacterAdapter charAdapter = new CharacterAdapter(ShowPlaySharedDb.this, structuredRows);
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

    public void refreshScreen(View v) {
        // go back to previous screen/activity

        Intent i = new Intent(this, ShowPlaySharedDb.class);
        startActivity(i);

//        Intent i = new Intent(this, com.shakespeare.new_app.MainActivity.class);
//        startActivity(i);
    }

    private String escapeSqlString(String input) {
        if (input == null) return "";
        return input.replace("'", "''");
    }
}
