package com.shakespeare.new_app;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Bookmarks extends AppCompatActivity {

    ArrayList<String> bookmarksList = new ArrayList<>();
    ArrayList<List<String>> bookmarkEntriesList = new ArrayList<List<String>>();
    // this is the 1-D array which is the list of items within each bookmark in the "outer" list
    ArrayList<String> bookmarkEntries = new ArrayList<>();

    MyRecyclerViewAdapter adapter;


    protected void onCreate(Bundle savedInstanceState) {
        Log.d("settings", "open settings home activity");
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bookmarks);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bookmarkspage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        try {
            DatabaseHelper mDBHelper = new DatabaseHelper(this); // instantiates the database helper
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // read from database current act number and scene number with current play code
        // and assign to the global variables
        // because this is the starting point for the play navigation
        DatabaseHandler db = new DatabaseHandler(this) {
            @Override
            public void onCreate(SQLiteDatabase db) {
                Log.d("sqllite","on create");
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                Log.d("sqllite","on ugprade");
            }
        };

        // old version which is a 1-D list
//        bookmarksList.clear();
//
//        // but, instead, we want to add as an array or list
//        bookmarksList = db.getBookmarks();
//        Log.d("script", "scriptLinesList: " + bookmarksList.size());
//
//        RecyclerView rvBookmarks = findViewById(R.id.rvBookmarks);
//
//        rvBookmarks.setLayoutManager(new LinearLayoutManager(rvBookmarks.getContext()));
//
//        // *** start: loop through ArrayList scriptLinesList
//        Integer i = 0;
//        for(String string1: bookmarksList)
//        {
//            i++;
//            System.out.println(i);
//            System.out.println("list item is " + string1);
//        }
//        // *** end: loop through ArrayList scriptLinesList
//
//        adapter = new MyRecyclerViewAdapter(rvBookmarks.getContext(), bookmarksList);
//        rvBookmarks.setAdapter(adapter);
//        int listLength = bookmarksList.size();
////        rvScript.smoothScrollToPosition(listLength);
//        rvBookmarks.smoothScrollToPosition(0);
//        // *** end recycler view logic ***

        // new version which is a 2-D list
        bookmarkEntriesList.clear();

        // but, instead, we want to add as an array or list
        bookmarkEntriesList = db.getBookmarks();
//        Log.d("bookmarkEntriesList", "bookmarkEntriesList: " + bookmarkEntriesList.toString());
//        Log.d("script", "bookmarkEntriesList: " + bookmarkEntriesList.size());

        RecyclerView rvBookmarks = findViewById(R.id.rvBookmarks);

        rvBookmarks.setLayoutManager(new LinearLayoutManager(rvBookmarks.getContext()));

        // *** start: loop through ArrayList bookmarks
        String strBmk = "";
        Integer actNr = 0;
        Integer scNr = 0;

        for(List<String> bookmarkEntry : bookmarkEntriesList) {

            actNr = Integer.valueOf(bookmarkEntry.get(2));
            scNr = Integer.valueOf(bookmarkEntry.get(3));

            strBmk = bookmarkEntry.get(1).toString();
            strBmk += "\n" + bookmarkEntry.get(5).toString();
            if (actNr == 0 && scNr == 0) {
                strBmk += "\nCharacters in play";
            } else {
                strBmk += "\nAct " + actNr + " Scene " + scNr;
            }
            strBmk += "\n" + bookmarkEntry.get(4).toString();

//            for(String bkmkItem : bookmarkEntry) {
//
//                // access "row" for inside arraylist or "intValue" for integer value.
//                strBmk += bkmkItem + " ";
//                System.out.println("list item is " + bkmkItem);
//                System.out.println(i);
//                i++;
//
//            }

            bookmarksList.add(strBmk);

        }

        // *** end: loop through ArrayList bookmarks

//        Log.d("bookmarksList","final bookmarksList: " + bookmarksList.toString());

        adapter = new MyRecyclerViewAdapter(rvBookmarks.getContext(), bookmarksList);
        rvBookmarks.setAdapter(adapter);
        int listLength = bookmarksList.size();
//        rvScript.smoothScrollToPosition(listLength);
        rvBookmarks.smoothScrollToPosition(0);
        // *** end recycler view logic ***
    }

    public void goBack(View v) {
        // launch a new activity

        getOnBackPressedDispatcher().onBackPressed();

//        Intent i = new Intent(this, com.shakespeare.new_app.MainActivity.class);
//        startActivity(i);
    }

}