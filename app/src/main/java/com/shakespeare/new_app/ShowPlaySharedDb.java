package com.shakespeare.new_app;

//import static android.os.Build.VERSION_CODES.R;

import com.example.database.RemoteDatabaseHelper;

import android.os.Bundle;
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

    }

    public void runTestQuery(View v) {

        AtomicReference<TextView> status = new AtomicReference<>((TextView) findViewById(R.id.result_text));
        String url = "sqlitecloud://cgdjyovjhk.g2.sqlite.cloud:8860/play_navigation.db?apikey=SFR0f2mYTxb3bbOiaALxEyatvEt2WDn5hYygAXiuE2o";
        RemoteDatabaseHelper helper = new RemoteDatabaseHelper(this, url);
//        RemoteDatabaseHelper helper = new RemoteDatabaseHelper(this);

        helper.runQueryFromJava("SELECT * FROM play_character", result -> {
            RecyclerView recyclerView = findViewById(R.id.query_result_list);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            if (result.getSuccess()) {
                List<Map<String, String>> data = result.getData();
                QueryResultAdapter adapter = new QueryResultAdapter(data);
                recyclerView.setAdapter(adapter);
                status.set(findViewById(R.id.result_text));
                status.get().setText("Query succeeded: " + result.getData().size() + " rows");
            } else {
                Toast.makeText(this, "Query failed: " + result.getError().getMessage(), Toast.LENGTH_LONG).show();
                status.get().setText("Query failed: " + result.getError().getMessage());
            }
            return null;
        });


    }

}

