package com.shakespeare.new_app;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.database.RemoteDatabaseHelperHttp.HttpCallback;
import com.example.database.QueryResult;
import com.example.database.RemoteDatabaseHelperHttp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kotlin.Unit;

public class ShowPlaySharedDb extends AppCompatActivity {

    private RemoteDatabaseHelperHttp helperHttp;
    private Spinner spinner;
    private RecyclerView recyclerView;
    private TextView tvTitle;
    private CharacterAdapter adapter;
    private List<Map<String, String>> characterList = new ArrayList<>();
    private ArrayAdapter<String> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_showplayshareddb);

        // Initialize views
        spinner = findViewById(R.id.spinner_play_list);
        recyclerView = findViewById(R.id.recycler_characters);
        tvTitle = findViewById(R.id.play_selected_heading);

        // Initialize HTTP Helper
//        helperHttp = new RemoteDatabaseHelperHttp(this, "https://android-sqlitecloud-api-production.up.railway.app");
        helperHttp = new RemoteDatabaseHelperHttp();

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CharacterAdapter(this, characterList);
        recyclerView.setAdapter(adapter);

        // Setup Spinner
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);

        // Spinner Listener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedPlayCode = parent.getItemAtPosition(position).toString();
                if (!selectedPlayCode.equals("Select a play...")) {
                    runQueryForPlay(selectedPlayCode);
                    tvTitle.setText("Characters in " + selectedPlayCode);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.topContent), (v, insets) -> insets);

        askForUsernameIfNeeded();  // Important: Only load after checking username!
    }

    private void askForUsernameIfNeeded() {
        String username = UserManager.getUsername(this);
        if (username == null || username.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Enter your username");
            final EditText input = new EditText(this);
            input.setHint("Username");
            builder.setView(input);

            builder.setPositiveButton("OK", (dialog, which) -> {
                String enteredUsername = input.getText().toString().trim();
                if (!enteredUsername.isEmpty()) {
                    UserManager.saveUsername(this, enteredUsername);
                    createUser(enteredUsername);
                } else {
                    Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
                    // 🔥 Re-show the dialog until they type something valid
                    askForUsernameIfNeeded();
                }
            });

            builder.setCancelable(false);
            builder.show();
        } else {
            Toast.makeText(this, "Welcome back, " + username, Toast.LENGTH_SHORT).show();
            loadInitialData();
        }
    }

    private void createUser(String username) {
        helperHttp.createUser(username, new HttpCallback() {
            public void exceptionOrNull(@NonNull Exception e) {

            }

            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(ShowPlaySharedDb.this, "Welcome, " + username, Toast.LENGTH_SHORT).show();
                    loadInitialData();
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(ShowPlaySharedDb.this, "Error creating user", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

//    private void loadInitialData() {
//        helperHttp.runQueryFromJava("SELECT DISTINCT play_code FROM play_character ORDER BY play_code", this::invoke2);
//    }

    private void loadInitialData() {
        helperHttp.runQueryFromJava("SELECT DISTINCT play_code FROM play_character ORDER BY play_code", result -> {
            if (result.isSuccess()) {
                List<Map<String, String>> rows = result.getData();
                List<String> playCodes = new ArrayList<>();
                for (Map<String, String> row : rows) {
                    playCodes.add(row.get("play_code"));
                }
                setupSpinner(playCodes);
            } else {
                // Handle query failure
                Log.e("LoadInitialData", "Failed to fetch play codes", result.getException());
            }
            return null; // ✅ RETURN here INSIDE the lambda (not from loadInitialData itself)
        });
    }

    private void setupSpinner(List<String> playCodes) {
        runOnUiThread(() -> {
            spinnerAdapter.clear();
            spinnerAdapter.addAll(playCodes);
            spinnerAdapter.notifyDataSetChanged();
        });
    }

    private void runQueryForPlay(String playName) {
        String username = UserManager.getUsername(this);
        String sql = "SELECT * FROM play_character_user WHERE play_code = '" + playName + "' AND username = '" + username + "' ORDER BY character_nr";

        helperHttp.runQueryFromJava(sql, this::invoke);
    }

    private Unit invoke(QueryResult<List<Map<String, String>>> filteredResult) {
        if (filteredResult.isSuccess()) {
            List<Map<String, String>> rows = filteredResult.getData();

            List<Map<String, String>> structuredList = CharacterAdapter.structureGroupedList(rows);
//            List<Map<String, String>> structuredList = QueryResultAdapter.structureGroupedList(rows);

            runOnUiThread(() -> {
                characterList.clear();
                characterList.addAll(structuredList);
                adapter.notifyDataSetChanged();
            });
        } else {
            Throwable error = filteredResult.exceptionOrNull();
            if (error != null) {
                error.printStackTrace();
            }
        }
        return null;
    }

    private Unit invoke2(QueryResult<List<Map<String, String>>> result) {
        if (result.isSuccess()) {
            List<Map<String, String>> plays = result.getData();
            List<String> playNames = new ArrayList<>();
            playNames.add("Select a play...");

            for (Map<String, String> row : plays) {
                playNames.add(row.get("play_code"));
            }

            runOnUiThread(() -> {
                spinnerAdapter.clear();
                spinnerAdapter.addAll(playNames);
                spinnerAdapter.notifyDataSetChanged();
            });
        } else {
            Throwable error = result.exceptionOrNull();
            if (error != null) {
                error.printStackTrace();
            }
        }
        return null;
    }
}
