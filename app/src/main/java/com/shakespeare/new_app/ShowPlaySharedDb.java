package com.shakespeare.new_app;

import android.util.Log;
import com.example.database.RemoteDatabaseHelper;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

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

    }

    public void runTestQuery(View v) {

        RemoteDatabaseHelper helper = new RemoteDatabaseHelper();

        helper.runQueryFromJava("SELECT * FROM play_character", result -> {
//            if (result.getSuccess()) {
//                String output = result.getData();
//                Log.d("DatabaseCaller", "Query success: " + output);
//            } else {
//                Throwable error = result.getError();
//                Log.e("DatabaseCaller", "Query failed", error);
//            }

            TextView resultText = findViewById(R.id.result_text);

            if (result.getSuccess()) {
                resultText.setText("Query success:\n" + result.getData());
            } else {
                resultText.setText("Query failed:\n" + result.getError().getMessage());
            }

            return null;
        });

    }

}

