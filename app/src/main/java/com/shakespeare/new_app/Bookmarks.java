package com.shakespeare.new_app;

import static android.app.PendingIntent.getActivity;
import static android.text.Html.fromHtml;

import static com.shakespeare.new_app.RecyclerBookmarksClickListener.*;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.util.Log;
import android.view.View;

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

    private Context context;
    ArrayList<CharSequence> bookmarksList = new ArrayList<>();
    ArrayList<List<String>> bookmarkEntriesList = new ArrayList<List<String>>();
    // this is the 1-D array which is the list of items within each bookmark in the "outer" list
    ArrayList<String> bookmarkEntries = new ArrayList<>();

//    MyRecyclerViewAdapter adapter;
    BookmarkEntryAdapter adapter;

    protected void onCreate(Bundle savedInstanceState) {
        Log.d("settings", "open settings home activity");
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bookmarks);

        this.context = this; // ✅ Save the context for later use

        RecyclerView rvBookmarks = findViewById(R.id.rvBookmarks);
        rvBookmarks.setLayoutManager(new LinearLayoutManager(rvBookmarks.getContext()));

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

//        // new version which is a 2-D list
//        bookmarkEntriesList.clear(); // This was for the local sqlite db.

        // but, instead, we want to add as an array or list
    //        bookmarkEntriesList = db.getBookmarks(); // This is based on local sqlite db.
        // Now we are replacing with a call to the cloud sqlite db:
        db.getBookmarksFromCloud(new BookmarkCallback() {
            @Override
            public void onBookmarksFetched(ArrayList<List<String>> bookmarks) {
                bookmarkEntriesList.clear();
                bookmarkEntriesList.addAll(bookmarks);

                bookmarksList.clear(); // ✅ clear any old data
                String strBmk = "", strPlayFullName = "", strFirstPlayFullName = "";
                int bookmarkID, actNr, scNr, intShareWithOthers;
                String bookmarkUsername;
                String stringHeading;

                for (List<String> bookmarkEntry : bookmarkEntriesList) {
                    bookmarkID = Integer.parseInt(bookmarkEntry.get(0));
                    bookmarkUsername = bookmarkEntry.get(7);
                    actNr = Integer.parseInt(bookmarkEntry.get(3));
                    scNr = Integer.parseInt(bookmarkEntry.get(4));
                    intShareWithOthers = Integer.parseInt(bookmarkEntry.get(8));

                    // If the play name is new, i.e. has changed, then add another play name heading.
                    if (!strPlayFullName.equalsIgnoreCase(bookmarkEntry.get(2))) {
                        if (!strBmk.equals("")) {
                            bookmarksList.add(String.valueOf(fromHtml(strBmk)));
                        }
                        strPlayFullName = bookmarkEntry.get(2);
                        stringHeading = "<br><i><big>" + strPlayFullName + "</big></i>";
                        bookmarksList.add(Html.fromHtml(stringHeading, Html.FROM_HTML_MODE_LEGACY));

                        if (strFirstPlayFullName.equals("")) {
                            strFirstPlayFullName = strPlayFullName;
                        }

                        strBmk = ""; // start a fresh bookmark text string.
                    }

                    strBmk += "<br> Note {" + bookmarkID + "} by " + bookmarkUsername + ": " + bookmarkEntry.get(6);
                    if (actNr == 0 && scNr == 0) {
                        strBmk += "<br>Characters in play";
                    } else {
                        strBmk += "<br>Act " + actNr + " Scene " + scNr;
                    }
                    strBmk += "<br>" + bookmarkEntry.get(5);

//                    String currentUser = UserManager.getUsername(context);
                    String currentUser = UserManager.getUsername(Bookmarks.this);

                    if (!bookmarkUsername.equals(currentUser)) {
                        strBmk = "<font color='#0000FF'>" + strBmk + "</font>"; // shared
                    } else {
                        strBmk = "<font color='#FFFFFF'>" + strBmk + "</font>"; // own
                    }
                    bookmarksList.add(Html.fromHtml(strBmk, Html.FROM_HTML_MODE_LEGACY));
//                    bookmarksList.add(String.valueOf(fromHtml(strBmk)));
                    strBmk = "";

                    if (bookmarkUsername.equals(currentUser)) {
                        // Create a checkbox dynamically or use a layout with a checkbox per item
                        // Then set checked state from bookmarkEntry.get(12) (share_with_others)

//                        int shareFlag = Integer.parseInt(bookmarkEntry.get(12));
                        int shareFlag = intShareWithOthers;
                        boolean isShared = shareFlag == 1;

                        // For each such item, show a checkbox and on change, call:
                        db.updateBookmarkShareStatus(bookmarkID, isShared);
                    }
                }

                // 27 May 2025: This is the new adapter for bookmarks in which
                // the user's own bookmarks are accompanied by a checkbox for the user
                // to toggle sharing for their own bookmarks.
                adapter = new BookmarkEntryAdapter(Bookmarks.this, bookmarksList, bookmarkEntriesList);
                rvBookmarks.setAdapter(adapter);

                // 27 May 2025: This is the old adapter for bookmarks in which bookmarks
                // use the same adapter type as play scripts, and neither has a checkbox
                // for the user to toggle sharing for their own bookmarks.
//                adapter = new MyRecyclerViewAdapter(Bookmarks.this, bookmarksList, true);
//                rvBookmarks.setAdapter(adapter);
                rvBookmarks.smoothScrollToPosition(0);
            }
            @Override
            public void onError(Throwable e) {
                Log.e("BookmarkFetch", "Error loading bookmarks", e);
            }
        });

//        Log.d("bookmarkEntriesList", "bookmarkEntriesList: " + bookmarkEntriesList.toString());
//        Log.d("script", "bookmarkEntriesList: " + bookmarkEntriesList.size());

//                RecyclerView rvBookmarks = findViewById(R.id.rvBookmarks);
//                rvBookmarks.setLayoutManager(new LinearLayoutManager(rvBookmarks.getContext()));

        // *** start: loop through ArrayList bookmarks
        String strBmk = "";
        String strPlayFullName = "";
        String strFirstPlayFullName = "";
        Integer bookmarkID = 0;
        Integer actNr = 0;
        Integer scNr = 0;
        String bookmarkUsername = "";
        String stringHeading = "";

        // improvements to make:
        // 1. (Done) group by play so that the name of the play appears once as a heading,
        // and the bookmarks relating to that play appear under it.

        // 2. allow the user to remove bookmarks by long-clicking which will set to inactive
        // so the bookmark no longer appears. Then open Yes/No dialog to confirm removal. See
        // https://stackoverflow.com/questions/2478517/how-to-display-a-yes-no-dialog-box-on-android

        // to achieve 2.:
        // we need each note to be a separate item to add to the bookmarksList not the entire
        // play with *all* of its bookmarks being a single item because then we cannot remove
        // individual bookmarks but only the entire play with all the bookmarks in the play.

        // 3. (Optional for first release) provide another view for inactive bookmarks and
        // allow to make them active again or delete completely.

        for(List<String> bookmarkEntry : bookmarkEntriesList) {

            Log.d("bookmarkEntry", String.valueOf(bookmarkEntry));

            bookmarkID = Integer.valueOf(bookmarkEntry.get(0));
            bookmarkUsername = bookmarkEntry.get(7);
            Log.d("bookmark ID", String.valueOf(bookmarkID));
            actNr = Integer.valueOf(bookmarkEntry.get(3));
            scNr = Integer.valueOf(bookmarkEntry.get(4));

            // If the play name is new, i.e. has changed, then add another play name heading.
            if (!strPlayFullName.equalsIgnoreCase(bookmarkEntry.get(2).toString()))
            {
                if (!strBmk.equals(("")))
                {
                    bookmarksList.add(strBmk);
                }
                strPlayFullName = bookmarkEntry.get(2).toString(); // play full name
                stringHeading = "<br><i><b>" + strPlayFullName + "</b></i>";
                bookmarksList.add(Html.fromHtml(stringHeading, Html.FROM_HTML_MODE_LEGACY));

                if (strFirstPlayFullName.equals(""))
                {
                    strFirstPlayFullName = strPlayFullName;
                }

                strBmk = ""; // start a fresh bookmark text string.
            }

            // add annotation
            strBmk +=  "<br>Note {" + bookmarkID + "} by " + bookmarkUsername + ": " + bookmarkEntry.get(6).toString();

            if (actNr == 0 && scNr == 0) {
                strBmk += "<br>Characters in play";
            } else {
                strBmk += "<br>Act " + actNr + " Scene " + scNr;
            }
            strBmk += "<br>" + bookmarkEntry.get(5).toString();

//            for(String bkmkItem : bookmarkEntry) {
//
//                // access "row" for inside arraylist or "intValue" for integer value.
//                strBmk += bkmkItem + " ";
//                System.out.println("list item is " + bkmkItem);
//                System.out.println(i);
//                i++;
//
//            }

            Log.d("bookmark",strBmk);

            if (!strBmk.equals(("")))
            {
                bookmarksList.add(String.valueOf(fromHtml(strBmk)));
            }

            strBmk = "";

        }

        // Add the bookmark for the final play in the list of bookmarks.
        // We do not want repeat a play's bookmarks if there is only one play because
        // the last will be the same as the first.
//        if (!strBmk.equals(("")) && !strPlayFullName.equals(strFirstPlayFullName))
        // *** end: loop through ArrayList bookmarks

//        Log.d("bookmarksList","final bookmarksList: " + bookmarksList.toString());

//        adapter = new MyRecyclerViewAdapter(rvBookmarks.getContext(), bookmarksList);
//        rvBookmarks.setAdapter(adapter);

        int listLength = bookmarksList.size();
//        rvScript.smoothScrollToPosition(listLength);
        rvBookmarks.smoothScrollToPosition(0);
        // *** end recycler view logic ***

        rvBookmarks.addOnItemTouchListener(
                // check for clicks

              //this, recyclerView ,new RecyclerItemClickListener.OnItemClickListener()

                // in progress to hook this up to the new click listener class RecyclerBookmarksClickListener
                new RecyclerBookmarksClickListener(this, rvBookmarks ,new RecyclerBookmarksClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        // do whatever
//                        Log.d("script line of text",recyclerView.position);

                        // old adapter for click listener
//                        MyRecyclerViewAdapter myAdapter = (MyRecyclerViewAdapter) rvBookmarks.getAdapter();
//                        String strBookmarks = myAdapter.getItem(position).toString();

                        // 28 May 2025: new adapter for click listener
                        BookmarkEntryAdapter bookmarkAdapter = (BookmarkEntryAdapter) rvBookmarks.getAdapter();
                        String strBookmarks = bookmarkAdapter.getItem(position).toString(); // line from before the new bookmark adapter

                        // We would like to get the text of the string which is long-clicked to save in the bookmark.
                        Log.d("check","onItemClick item clicked in Bookmarks.java class: position " + String.valueOf(position) + ", text ");

                        // 28 May 2025: extra functionality with the new adapter
                        CharSequence clickedText = bookmarkAdapter.getItem(position);
//                        List<String> entryData = bookmarkAdapter.bookmarkEntriesList.get(position);
                        List<String> entryData = bookmarkAdapter.getBookmarkEntry(position);
                        String bookmarkId = entryData.get(0);
                        Log.d("check", "onItemClick: position " + position + ", text: " + clickedText);

                    }

                    @Override public void onLongItemClick(View view, int position) {
                        MyRecyclerViewAdapter myAdapter = (MyRecyclerViewAdapter) rvBookmarks.getAdapter();
                        String strBookmarks = myAdapter.getItem(position).toString();
                        // *** request confirmation to remove this bookmark
                        // which will be actioned by setting the active_0_or_1 to 0.
                        Log.d("check","onLongItemClick item clicked in Bookmarks.java class: position " + String.valueOf(position) + ", text ");

                    }

                })

        );

    }

    public void goBack(View v) {

        // turn off any text to speech that is currently in progress
        MyApplication.textToSpeech.speak("", TextToSpeech.QUEUE_FLUSH, null);

        // launch a new activity

        // We cannot use this because when new activities are opened the extras are wiped.
        // For some reason currently it isn't working from when we first open the
        // bookmarks screen activity either. So unfortunately we need to use a global
        // variable to store the source screen.
//        Bundle extras = getIntent().getExtras();
//        String strSourceScreen = extras.getString("sourceScreen");

        if(GlobalClass.bookmarkSourceScreen.equals("HomeScreen")) {
            Intent i = new Intent(this, com.shakespeare.new_app.MainActivity.class);
            startActivity(i);
        }
        else if(GlobalClass.bookmarkSourceScreen.equals("PlayScreen")) {
            Intent i = new Intent(this, com.shakespeare.new_app.AMSND.class);
            startActivity(i);
        }
        else {
            getOnBackPressedDispatcher().onBackPressed();

        }

    }

}