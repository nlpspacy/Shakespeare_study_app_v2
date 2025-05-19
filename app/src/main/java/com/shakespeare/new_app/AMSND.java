package com.shakespeare.new_app;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModel;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

//import com.example.new_app.R;
import com.shakespeare.new_app.ChatGPTApiHelper;
import com.shakespeare.new_app.DatabaseHandler;
import com.shakespeare.new_app.DatabaseHelper;
import com.shakespeare.new_app.GlobalClass;
import com.shakespeare.new_app.MainActivity;
import com.shakespeare.new_app.MyRecyclerViewAdapter;
import com.shakespeare.new_app.R;
import com.shakespeare.new_app.SettingsHomeActivity;
import com.shakespeare.new_app.SystemPromptActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;


public class AMSND extends AppCompatActivity {

    private ActivityResultLauncher<Intent> bookmarkLauncher;

    private String[] standard_prompts;
    private Spinner standardpromptsspinner;

    MyRecyclerViewAdapter adapter;
    ArrayList<String> messageList = new ArrayList<>();

    MyRecyclerViewAdapter adapterScript;
    ArrayList<String> scriptLinesList = new ArrayList<>();
    ArrayList<ArrayList<String>> scriptLinesList_2d = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_amsnd);
        this.setTitle(GlobalClass.selectedPlay);

        // get screen dimensions
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        int screenWidth = displayMetrics.widthPixels;
        Log.d("screen", "height: " + screenHeight + " width: " + screenWidth);

        try {
            DatabaseHelper mDBHelper = new DatabaseHelper(this); // instantiates the database helper
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//        View v = findViewById(R.id.textViewScript);
        RecyclerView v = findViewById(R.id.rvScript);

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

            }
        };

        GlobalClass.selectedActNumber = db.getCurrentActNumber();
        GlobalClass.selectedSceneNumber = db.getCurrentSceneNumber();

        Log.d("progress update","before updateScriptDisplay(v)");
        updateScriptDisplay(v, false, false);
        Log.d("progress update","after updateScriptDisplay(v)");

        // listen on button and when pressed then send prompt to ChatGPT API
        Button fetchDataButton = findViewById(R.id.fetchDataButton);
        fetchDataButton.setOnClickListener(vbtn -> {

            Log.d("check","Chat GPT button pressed");
            callChatGPT(v);

        });

        // default standardPromptsSpinner to not visible
        Spinner standardPromptsSpinner = (Spinner) findViewById(R.id.standardprompts_spinner);
        standardPromptsSpinner.setVisibility(View.GONE);

        // listen on text box and when enter key is pressed then send prompt to ChatGPT API
        EditText etMessageToChatGPT = findViewById(R.id.message_edit_text);
        etMessageToChatGPT.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {

                Log.d("hit enter", "---check onEditorAction---");
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    //perform your action
                    Log.d("hit enter", "onEditorAction hit enter");
                    callChatGPT(v);
                }
                Log.d("hit enter", "onEditorAction did not hit enter");
                return false;
            }
        });

        // standard prompts spinner - if we are in the "Characters in the play" section
        // then we exclude act-specific and scene-specific standard questions
        if(GlobalClass.selectedActNumber == 0 && GlobalClass.selectedSceneNumber == 0) {
            standard_prompts = getResources().getStringArray(R.array.standard_prompts_no_act_or_scene);
        } else {
            // otherwise show act-specific and scene-specific standard questions
            standard_prompts = getResources().getStringArray(R.array.standard_prompts_list);
        }

        standardpromptsspinner = (Spinner) findViewById(R.id.standardprompts_spinner);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, standard_prompts);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        standardpromptsspinner.setAdapter(dataAdapter);

        standardpromptsspinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                // resize according to the selection
                String StandardPromptSelectedItem;
                StandardPromptSelectedItem = standardPromptsSpinner.getSelectedItem().toString();
                if(!StandardPromptSelectedItem.equals("Please select...")){
                    Toast.makeText(getApplicationContext(), StandardPromptSelectedItem, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        // Default to ChatGPT window hidden with play script in full screen mode.
        // Otherwise the initial appearance of the play is too poky for the user
        // and slightly intimidating.

        Log.d("check","progress 1000");
        LinearLayout llPlayScript = (LinearLayout) findViewById(R.id.playScriptScrollLL);
        // update vertical weight of llPlayScript to 10
        LinearLayout.LayoutParams lParams = (LinearLayout.LayoutParams) llPlayScript.getLayoutParams();
        lParams.weight = 10f;
        llPlayScript.setLayoutParams(lParams);

//        llPlayScript.setLayoutParams(new LinearLayout.LayoutParams(LinearLayoutCompat.LayoutParams.MATCH_PARENT, LinearLayoutCompat.LayoutParams.WRAP_CONTENT,5f));
        LinearLayout llMessaging = (LinearLayout) findViewById(R.id.mainLinearLayout);
        LinearLayout llSendMessageLayout = (LinearLayout) findViewById(R.id.sendMessageLayout);
        llMessaging.setVisibility(View.GONE);
        llSendMessageLayout.setVisibility(View.GONE);
        Button btnShowHide = (Button) findViewById(R.id.btnShowHide);
        btnShowHide.setText("Help");

        Log.d("check","progress 1001");

        // using https://stackoverflow.com/questions/24471109/recyclerview-onclick
        RecyclerView recyclerView = findViewById(R.id.rvScript);
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, recyclerView ,new RecyclerItemClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        // do whatever
//                        Log.d("script line of text",recyclerView.position);
                        MyRecyclerViewAdapter myAdapter = (MyRecyclerViewAdapter) recyclerView.getAdapter();
                        String strScriptText = myAdapter.getItem(position);
                        // We would like to get the text of the string which is long-clicked to save in the bookmark.
                        Log.d("check","onItemClick item clicked in AMNSD.java class: position " + String.valueOf(position) + ", text " + strScriptText);
                    }

                    @Override public void onLongItemClick(View view, int position) {
                        MyRecyclerViewAdapter myAdapter = (MyRecyclerViewAdapter) recyclerView.getAdapter();
                        String strScriptText = myAdapter.getItem(position);
                        // do whatever
                        Log.d("check","onLongItemClick item clicked in AMNSD.java class: position " + String.valueOf(position) + ", text " + strScriptText);
                    }
                })
        );

        Log.d("check","progress 1002");

        bookmarkLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getBooleanExtra("bookmarkSaved", false)) {
                            updateScriptDisplay(findViewById(R.id.rvScript), false, false);
                            Log.d("refresh", "🔄 Script refreshed after bookmark");
                        }
                    }
                }
        );
    }

    public void launchNewBookmarkActivity(String scriptRef) {
        Intent intent = new Intent(this, NewBookmarkPop.class);
        intent.putExtra("scriptRef", scriptRef);
        bookmarkLauncher.launch(intent);
    }

    @Override
    protected void onResume() {

        Log.d("resuming after adding bookmark","about to run super.onResume()");
        super.onResume();

        Log.d("resuming after adding bookmark","resuming after adding bookmark");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Log.d("resuming after adding bookmark","set SharedPreferences prefs variable");
        boolean shouldRefresh = prefs.getBoolean("refresh_script_on_return", false);

        if (shouldRefresh) {
            Log.d("refresh", "🔄 Refreshing script after new bookmark");
            updateScriptDisplay(findViewById(R.id.rvScript), false, false);
            prefs.edit().putBoolean("refresh_script_on_return", false).apply(); // reset flag
        }
    }

    // read the script from the sqlite database
    public void updateScriptDisplay(View v, Boolean boolAtPrologue, Boolean boolAtEpilogue){

        // Clear the list so that the acts and scenes don't accumulate in an ever
        // increasingly long amount of scrollable text.
        scriptLinesList.clear();
        scriptLinesList_2d.clear(); // as at Sunday 13 Apr 2025, the 2d version is in dev.

        DatabaseHandler db = new DatabaseHandler(this) {
            @Override
            public void onCreate(SQLiteDatabase db) {

                Log.d("sqllite","onCreate");

            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

                Log.d("sqllite","onUpgrade");

            }
        };

//        int intActNumberReturned = db.getActNumber();
//        int intSceneNumberReturned = db.getSceneNumber();
//        Log.d("act number and scene number returned","Act and scene number returned:" + String.valueOf(intActNumberReturned) + " " + String.valueOf(intSceneNumberReturned));
        GlobalClass.numberOfScenesInAct = db.getNumberOfScenesInAct();
        GlobalClass.numberOfActsInPlay = db.getNumberOfActsInPlay();

        int intActNumberSelected = GlobalClass.selectedActNumber;
        int intSceneNumberSelected = GlobalClass.selectedSceneNumber;
        // if we are at the prologue then act number selected is -1 and we need scene number to be -1 as well.
        // if we are at the epilogue then act number selected is -2 and we need scene number to be -2 as well.
        if (intActNumberSelected<0) {
            intSceneNumberSelected = intActNumberSelected;
            GlobalClass.selectedSceneNumber = intSceneNumberSelected;
        }
        Log.d("act number and scene number returned","Act and scene number selected:" + String.valueOf(intActNumberSelected) + " " + String.valueOf(intSceneNumberSelected));

        // If the content is the preamble including Dramatis Personae
        // then don't display act number or scene number.
        if(intActNumberSelected==0){
            TextView tvActNumber = findViewById(R.id.textViewActNumber);
            tvActNumber.setVisibility(View.GONE);

            TextView tvSceneNumber = findViewById(R.id.textViewSceneNumber);
            tvSceneNumber.setVisibility(View.GONE);

        } else if(intSceneNumberSelected==0){
            // If the content is the preamble of an Act, then don't display scene number.
            TextView tvSceneNumber = findViewById(R.id.textViewSceneNumber);
            tvSceneNumber.setVisibility(View.GONE);

            // Display act number only.
            TextView tvActNumber = findViewById(R.id.textViewActNumber);
            tvActNumber.setVisibility(View.VISIBLE);
            tvActNumber.setText("Act " + String.valueOf(intActNumberSelected) + "/" + GlobalClass.numberOfActsInPlay);

        } else if(intActNumberSelected==-2){
            // If the content is the preamble of an Act, then don't display scene number.
            TextView tvSceneNumber = findViewById(R.id.textViewSceneNumber);
            tvSceneNumber.setVisibility(View.GONE);

            // Display act number only.
            TextView tvActNumber = findViewById(R.id.textViewActNumber);
            tvActNumber.setVisibility(View.VISIBLE);
            tvActNumber.setText("Prol.");

        } else if(intActNumberSelected==-1){
            // If the content is the preamble of an Act, then don't display scene number.
            TextView tvSceneNumber = findViewById(R.id.textViewSceneNumber);
            tvSceneNumber.setVisibility(View.GONE);

            // Display act number only.
            TextView tvActNumber = findViewById(R.id.textViewActNumber);
            tvActNumber.setVisibility(View.VISIBLE);
            tvActNumber.setText("Epil.");

        } else {
            // Otherwise display act number and scene number.
            TextView tvActNumber = findViewById(R.id.textViewActNumber);
            tvActNumber.setVisibility(View.VISIBLE);
            tvActNumber.setText("Act " + String.valueOf(intActNumberSelected) + "/" + GlobalClass.numberOfActsInPlay);

            TextView tvSceneNumber = findViewById(R.id.textViewSceneNumber);
            tvSceneNumber.setVisibility(View.VISIBLE);
            tvSceneNumber.setText("Sc " + String.valueOf(intSceneNumberSelected) + "/" + GlobalClass.numberOfScenesInAct);

        }

        // show the script using text view and single database row returned
//        TextView tvScript = findViewById(R.id.textViewScript);
//        tvScript.setText(String.valueOf(db.getScript()));

        // show the script using recycler view with multiple lines returned from database rows returned
        // *** start recycler view logic ***

        // this adds the script as a single string
//        scriptLinesList.add(String.valueOf(db.getScript()));

        // but, instead, we want to add as an array or list
        //scriptLinesList = db.getScript(boolAtPrologue, boolAtEpilogue);
        db.getScriptFromCloud(boolAtPrologue, boolAtEpilogue, new ScriptCallback() {
            @Override
            public void onScriptFetched(ArrayList<String> scriptLines) {
                scriptLinesList.clear();
                scriptLinesList.addAll(scriptLines);

                RecyclerView rvScript = findViewById(R.id.rvScript);
                rvScript.setLayoutManager(new LinearLayoutManager(rvScript.getContext()));

                adapter = new MyRecyclerViewAdapter(rvScript.getContext(), scriptLinesList);
                rvScript.setAdapter(adapter);
                rvScript.smoothScrollToPosition(0);

                Log.d("script", "✅ scriptLinesList loaded: " + scriptLinesList.size());

                // Set the font size for act and scene number
                TextView tvActNumber = findViewById(R.id.textViewActNumber);
                TextView tvSceneNumber = findViewById(R.id.textViewSceneNumber);
                tvActNumber.setTextSize(TypedValue.COMPLEX_UNIT_SP, GlobalClass.fontsizesp);
                tvSceneNumber.setTextSize(TypedValue.COMPLEX_UNIT_SP, GlobalClass.fontsizesp);
            }

            @Override
            public void onError(Throwable e) {
                Log.e("script", "❌ Error loading script from cloud", e);
                Toast.makeText(AMSND.this, "Failed to load script from cloud", Toast.LENGTH_SHORT).show();
            }
        });

        Log.d("script", "scriptLinesList size: " + scriptLinesList.size());

        // The 2d version is not used yet, but is in testing.
        scriptLinesList_2d = db.getScript_2d(boolAtPrologue, boolAtEpilogue);
        Log.d("script_2d", "2d scriptLinesList size: " + scriptLinesList_2d.size());

        RecyclerView rvScript = findViewById(R.id.rvScript);
        rvScript.setLayoutManager(new LinearLayoutManager(rvScript.getContext()));

        // *** start: loop through ArrayList scriptLinesList
        Integer i = 0;
        for(String string1: scriptLinesList)
        {
            i++;
//            System.out.println(i);
//            System.out.println("list item is " + string1);
        }
        // *** end: loop through ArrayList scriptLinesList

        // script lines list needs alternating line with the playcode, act, scene, play_line_nr reference
        // to include in the recycler view as a hidden row for bookmark referencing
        adapter = new MyRecyclerViewAdapter(rvScript.getContext(), scriptLinesList);
        rvScript.setAdapter(adapter);
        int listLength = scriptLinesList.size();
//        rvScript.smoothScrollToPosition(listLength);
        rvScript.smoothScrollToPosition(0);
        // *** end recycler view logic ***

        // set the act number font size which the user has specified
        TextView tvActNumber = findViewById(R.id.textViewActNumber);
        tvActNumber.setTextSize(TypedValue.COMPLEX_UNIT_SP, GlobalClass.fontsizesp);

        // set the scene number font size which the user has specified
        TextView tvSceneNumber = findViewById(R.id.textViewSceneNumber);
        tvSceneNumber.setTextSize(TypedValue.COMPLEX_UNIT_SP, GlobalClass.fontsizesp);

        // ensure scroller is at the top
        // temporarily commented this out because we have removed the scroller view temporarily
        // to simplify while investigating how to solve the vertical spacing issue.
//        ScrollView svScript = findViewById(R.id.SCROLLER_ID);
//        svScript.scrollTo(0, 0);

        // these may also work
        // from
        // https://stackoverflow.com/questions/46156882/nestedscrollviews-fullscrollview-focus-up-not-working-properly
//        svScript.fling(0);
//        svScript.smoothScrollTo(0, 0);

        //        svScript.pageScroll(View.FOCUS_UP);
//        svScript.getViewTreeObserver().setViewPosition(new Point(0,0));

        // update database play_position table with current act number and scene number for current play code
        Integer intUpdateRow = db.updateNavDbWithCurrentActSceneInPlay();
        Log.d("check", String.valueOf(intUpdateRow));

        updateStandardPromptsList(v);

    }

    public void updateStandardPromptsList(View v){
        // standard prompts spinner - if we are in the dramatis personae section
        // then we exclude act-specific and scene-specific standard questions
        if(GlobalClass.selectedActNumber == 0 && GlobalClass.selectedSceneNumber == 0) {
            standard_prompts = getResources().getStringArray(R.array.standard_prompts_no_act_or_scene);
        } else {
            // otherwise show act-specific and scene-specific standard questions
            standard_prompts = getResources().getStringArray(R.array.standard_prompts_list);
        }

        standardpromptsspinner = (Spinner) findViewById(R.id.standardprompts_spinner);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, standard_prompts);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        standardpromptsspinner.setAdapter(dataAdapter);

    }

    public void goToStartOfPlay(View v) {
        // pause text to speech as user requested to navigate to another part of the play
//        MyApplication.textToSpeech.speak("", TextToSpeech.QUEUE_FLUSH, null);
        VoiceSynthesizer.stopPlayback();

        // decrement act number
        Log.d("script navigation button", "Act before: " + String.valueOf(GlobalClass.selectedActNumber));
        GlobalClass.selectedActNumber = 0;
        GlobalClass.selectedSceneNumber = 0;
        updateScriptDisplay(v, false, false);
        Log.d("go to start of play", "Now at start of play: act " + String.valueOf(GlobalClass.selectedActNumber) + ", scene " + String.valueOf(GlobalClass.selectedSceneNumber));
        Log.d("script navigation button", "Act after: " + String.valueOf(GlobalClass.selectedActNumber));

    }
    public void decrementAct(View v) {
        // pause text to speech as user requested to navigate to another part of the play
//        MyApplication.textToSpeech.speak("", TextToSpeech.QUEUE_FLUSH, null);
        VoiceSynthesizer.stopPlayback();

        // decrement act number
        Log.d("script navigation button", "Act before: " + String.valueOf(GlobalClass.selectedActNumber));

        DatabaseHandler db = new DatabaseHandler(this) {
            @Override
            public void onCreate(SQLiteDatabase db) {

                Log.d("sqllite","onCreate");

            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

                Log.d("sqllite","onUpgrade");

            }
        };

        RecyclerView rvScript = findViewById(R.id.rvScript);

        if(GlobalClass.selectedActNumber > 1){
            GlobalClass.selectedActNumber -= 1;
            GlobalClass.selectedSceneNumber = 1;
            updateScriptDisplay(v, false, false);
        } else if(GlobalClass.selectedActNumber == 1){
            // check for prologue and if a prologue exists, then go to the prologue
            if (db.checkForPrologue() == true) {
                GlobalClass.selectedActNumber = -2;
                GlobalClass.selectedSceneNumber = -2;
                updateScriptDisplay(v, true, false);
            } else {
                // if there is no prologue then go to act 0 scene 0
                // which is the 'Characters in the Play' section
                GlobalClass.selectedActNumber = 0;
                GlobalClass.selectedSceneNumber = 0;
                updateScriptDisplay(v, false, false);

            }
        } else if(GlobalClass.selectedActNumber == -2){ // currently at prologue
            GlobalClass.selectedActNumber = 0;
            GlobalClass.selectedSceneNumber = 0;
            updateScriptDisplay(v, false, false);
        } else if(GlobalClass.selectedActNumber == -1){ // currently at epilogue
            GlobalClass.selectedActNumber = GlobalClass.numberOfActsInPlay;
            GlobalClass.selectedSceneNumber = 1;
            updateScriptDisplay(v, false, false);
        }
        Log.d("decrement scene", "Decremented scene: act " + String.valueOf(GlobalClass.selectedActNumber) + ", scene " + String.valueOf(GlobalClass.selectedSceneNumber));
        Log.d("script navigation button", "Act after: " + String.valueOf(GlobalClass.selectedActNumber));

    }

    public void incrementAct(View v) {

        // pause text to speech as user requested to navigate to another part of the play
//        MyApplication.textToSpeech.speak("", TextToSpeech.QUEUE_FLUSH, null);
        VoiceSynthesizer.stopPlayback();

        DatabaseHandler db = new DatabaseHandler(this) {
            @Override
            public void onCreate(SQLiteDatabase db) {

                Log.d("sqllite","onCreate");

            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

                Log.d("sqllite","onUpgrade");

            }
        };

        // increment act number
        if(GlobalClass.selectedActNumber < GlobalClass.numberOfActsInPlay){

            RecyclerView rvScript = findViewById(R.id.rvScript);

            Log.d("script navigation button", "Act before: " + String.valueOf(GlobalClass.selectedActNumber));

            // if we are at the 'Characters in the Play' section and the play has a prologue
            // then increment to the prologue, otherwise follow the usual act increment process.
            if (GlobalClass.selectedActNumber == 0 && db.checkForPrologue() == true) {
                    GlobalClass.selectedActNumber = -2;
                    GlobalClass.selectedSceneNumber = -2;
                    updateScriptDisplay(v, true, false);

            } else if (GlobalClass.selectedActNumber == -2) { // if at the prologue then move to act 1
                GlobalClass.selectedActNumber = 1;
                if (db.getMinimumSceneNumber() == 0) {
                    GlobalClass.selectedSceneNumber = 0;

                } else {
                    GlobalClass.selectedSceneNumber = 1;

                }
                Log.d("check position in play","Act " + String.valueOf(GlobalClass.selectedActNumber) + ", scene " + String.valueOf(GlobalClass.selectedSceneNumber));
                updateScriptDisplay(v, false, false);
                Log.d("script navigation button", "Act after: " + String.valueOf(GlobalClass.selectedActNumber));

            } else if (GlobalClass.selectedActNumber == -1) { // if at the epilogue then do nothing
                // do nothing
                Log.d("epilogue check","at epilogue");

            } else { // standard case of incrementing the act number
                GlobalClass.selectedActNumber += 1;
                // need to include logic here
                // if minimum scene number for the new act number is 0 then GlobalClass.selectedSceneNumber = 0 else GlobalClass.selectedSceneNumber = 1;
                if (db.getMinimumSceneNumber() == 0) {
                    GlobalClass.selectedSceneNumber = 0;

                } else {
                    GlobalClass.selectedSceneNumber = 1;

                }

                // If the user has opted for text to speech, then say the act and scene number.
                if (com.shakespeare.new_app.GlobalClass.boolSoundOn.equals(Boolean.TRUE)) {

                    MyApplication.setLanguage(Locale.ENGLISH);
                    MyApplication.textToSpeech.speak("Act " + GlobalClass.selectedActNumber.toString() + " Scene " + GlobalClass.selectedSceneNumber.toString(), TextToSpeech.QUEUE_ADD, null,
                            UUID.randomUUID().toString());

                }


                Log.d("check position in play","Act " + String.valueOf(GlobalClass.selectedActNumber) + ", scene " + String.valueOf(GlobalClass.selectedSceneNumber));
                updateScriptDisplay(v, false, false);
                Log.d("script navigation button", "Act after: " + String.valueOf(GlobalClass.selectedActNumber));

            }

        } else {
            // if we are at the maximum act number and there is an epilogue
            // then move to the epilogue, otherwise do nothing i.e. stay at max act number
            // check whether there is an epilogue in this play
            if (db.checkForEpilogue() == true) {
                GlobalClass.selectedActNumber = -1;
                Log.d("epilogue check","at epilogue");
                updateScriptDisplay(v, false, true);

            } else {
                Log.d("epilogue check","there is no epilogue so do not navigate forward");
                // do nothing
            }
        }

    }
    public void decrementScene(View v) {

        // pause text to speech as user requested to navigate to another part of the play
//        MyApplication.textToSpeech.speak("", TextToSpeech.QUEUE_FLUSH, null);
        VoiceSynthesizer.stopPlayback();

        DatabaseHandler db = new DatabaseHandler(this) {
            @Override
            public void onCreate(SQLiteDatabase db) {

                Log.d("sqllite","onCreate");

            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

                Log.d("sqllite","onUpgrade");

            }
        };

        Integer intMinScNr = db.getMinimumSceneNumber();

        RecyclerView rvScript = findViewById(R.id.rvScript);
        // attempt to clear the recycler view
        adapter = new MyRecyclerViewAdapter(rvScript.getContext(), null);
        rvScript.setAdapter(adapter);
        rvScript.smoothScrollToPosition(0);

        // decrement scene number
        Log.d("script navigation button", "Scene before: " + String.valueOf(GlobalClass.selectedSceneNumber));
        if(GlobalClass.selectedSceneNumber > intMinScNr){
            GlobalClass.selectedSceneNumber -= 1;

            // If the user has opted for text to speech, then say the act and scene number.
            if (com.shakespeare.new_app.GlobalClass.boolSoundOn.equals(Boolean.TRUE)) {

                MyApplication.setLanguage(Locale.ENGLISH);
//                MyApplication.textToSpeech.speak("Act " + GlobalClass.selectedActNumber.toString() + " Scene " + GlobalClass.selectedSceneNumber.toString(), TextToSpeech.QUEUE_ADD, null,
//                        UUID.randomUUID().toString());
//                VoiceSynthesizer.synthesizeAndPlay(this, "Act " + GlobalClass.selectedActNumber.toString() + " Scene " + GlobalClass.selectedSceneNumber.toString(), "nova");

//                int currentGen = VoiceSynthesizer.nextGeneration();
                String sceneKey = com.shakespeare.new_app.GlobalClass.selectedPlayCode + "_" +
                        com.shakespeare.new_app.GlobalClass.selectedActNumber + "_" +
                        com.shakespeare.new_app.GlobalClass.selectedSceneNumber;

                Log.d("generation tracking", "sceneKey: " + sceneKey);
                VoiceSynthesizer.synthesizeAndPlay(this, "Act " + GlobalClass.selectedActNumber.toString() + " Scene " + GlobalClass.selectedSceneNumber.toString(), "nova", sceneKey);

            }

            updateScriptDisplay(v, false, false);
        } else if(GlobalClass.selectedActNumber == 0){ // currently at Characters in the Play section
            // do nothing
        } else if(GlobalClass.selectedSceneNumber == intMinScNr){
            Log.d("decrement scene", "Decremented scene: act " + String.valueOf(GlobalClass.selectedActNumber) + ", scene " + String.valueOf(GlobalClass.selectedSceneNumber));
            decrementAct(v);
        }
        Log.d("script navigation button", "Scene after: " + String.valueOf(GlobalClass.selectedSceneNumber));

    }

    public void incrementScene(View v) {

        // pause text to speech as user requested to navigate to another part of the play
//        MyApplication.textToSpeech.speak("", TextToSpeech.QUEUE_FLUSH, null);
        VoiceSynthesizer.stopPlayback();

        RecyclerView rvScript = findViewById(R.id.rvScript);
        // attempt to clear the recycler view
        adapter = new MyRecyclerViewAdapter(rvScript.getContext(), null);
//        rvScript.setAdapter(adapter);
        //rvScript.setAdapter(null);
        rvScript.smoothScrollToPosition(0);

        // increment scene number
        if(GlobalClass.selectedActNumber == -1){
            // if already at the epilogue, do nothing
            Log.d("epilogue","we are at the epilogue, stop here");
        } else if(GlobalClass.selectedSceneNumber < GlobalClass.numberOfScenesInAct){
            Log.d("script navigation button", "Scene before: " + String.valueOf(GlobalClass.selectedSceneNumber));
            GlobalClass.selectedSceneNumber += 1;

            // If the user has opted for text to speech, then say the act and scene number.
            if (com.shakespeare.new_app.GlobalClass.boolSoundOn.equals(Boolean.TRUE)) {

                MyApplication.setLanguage(Locale.ENGLISH);
                MyApplication.textToSpeech.speak("Act " + GlobalClass.selectedActNumber.toString() + " Scene " + GlobalClass.selectedSceneNumber.toString(), TextToSpeech.QUEUE_ADD, null,
                        UUID.randomUUID().toString());

            }

            updateScriptDisplay(v, false, false);
            Log.d("script navigation button", "Scene after: " + String.valueOf(GlobalClass.selectedSceneNumber));
        } else if(Objects.equals(GlobalClass.selectedSceneNumber, GlobalClass.numberOfScenesInAct)){
            incrementAct(v);
        }

    }


    public void returnToMain(View v) {
        // launch a new activity

        // pause text to speech as user requested to navigate to another part of the play
//        MyApplication.textToSpeech.speak("", TextToSpeech.QUEUE_FLUSH, null);
        VoiceSynthesizer.stopPlayback();

        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    public void openBookmarks(View v) {
        // launch a new activity

        // pause text to speech as user requested to navigate to another part of the play
//        MyApplication.textToSpeech.speak("", TextToSpeech.QUEUE_FLUSH, null);
        VoiceSynthesizer.stopPlayback();

        Intent i = new Intent(this, Bookmarks.class);
//        i.putExtra("sourceScreen","PlayScreen");
        GlobalClass.bookmarkSourceScreen = "PlayScreen";
        Log.d("message","put extra");
        startActivity(i);
    }
    public void launchSettings(View v) {
        // launch a new activity

        Intent i = new Intent(this, SettingsHomeActivity.class);
        startActivity(i);
    }

    public void showHideMessaging(View v) {
        // mainLinearLayout shown or hidden
        Log.d("full screen or split screen layout","show / hide messaging pane");

        Button btnShowHide = (Button) findViewById(R.id.btnShowHide);

        // play script box show/hide
        LinearLayout llPlayScript = (LinearLayout) findViewById(R.id.playScriptScrollLL);

        // send message and transcript box show/hide
        LinearLayout llMessaging = (LinearLayout) findViewById(R.id.mainLinearLayout);
        LinearLayout llSendMessageLayout = (LinearLayout) findViewById(R.id.sendMessageLayout);

        if(llPlayScript.getVisibility() == View.VISIBLE && llMessaging.getVisibility() == View.GONE) {
            llPlayScript.setVisibility(View.VISIBLE);
            llMessaging.setVisibility(View.VISIBLE);
            llSendMessageLayout.setVisibility(View.VISIBLE);

            // update vertical weight of llPlayScript to 5
            LinearLayout.LayoutParams lParams1 = (LinearLayout.LayoutParams) llPlayScript.getLayoutParams();
            lParams1.weight = 5f;
            llPlayScript.setLayoutParams(lParams1);

            // update vertical weight of llMessaging to 5
            LinearLayout.LayoutParams lParams2 = (LinearLayout.LayoutParams) llMessaging.getLayoutParams();
            lParams2.weight = 5f;
            llMessaging.setLayoutParams(lParams2);

            // update button label
            btnShowHide.setText("Full");
        }
        else if(llPlayScript.getVisibility() == View.VISIBLE && llMessaging.getVisibility() == View.VISIBLE) {
            llPlayScript.setVisibility(View.GONE);
            llMessaging.setVisibility(View.VISIBLE);
            llSendMessageLayout.setVisibility(View.VISIBLE);

            // update vertical weight of llMessaging to 10
            LinearLayout.LayoutParams lParams2 = (LinearLayout.LayoutParams) llMessaging.getLayoutParams();
            lParams2.weight = 10f;
            llMessaging.setLayoutParams(lParams2);

            // update button label
            btnShowHide.setText("Script");
        } else  {
            llPlayScript.setVisibility(View.VISIBLE);
            llMessaging.setVisibility(View.GONE);
            llSendMessageLayout.setVisibility(View.GONE);

            // update vertical weight of llPlayScript to 10
            LinearLayout.LayoutParams lParams = (LinearLayout.LayoutParams) llPlayScript.getLayoutParams();
            lParams.weight = 10f;
            llPlayScript.setLayoutParams(lParams);

            // update button label
            btnShowHide.setText("Help");
        }

    }
    public void showStandardPromptsOrFreeTextEntry(View v) {
        // Standard pre-canned prompts show and free text hide, or vice-versa.
        Log.d("prompts show standard or allow free text","show standard prompts or allow free text");

        Button btnShowHide = (Button) findViewById(R.id.btnStandardFree);

        // Standard prompts show/hide and free text hide/show
        Spinner standardPromptsSpinner = (Spinner) findViewById(R.id.standardprompts_spinner);
        EditText freeTextPrompt = (EditText) findViewById(R.id.message_edit_text);
        if(standardPromptsSpinner.getVisibility() == View.GONE) {
            standardPromptsSpinner.setVisibility(View.VISIBLE);
            freeTextPrompt.setVisibility(View.GONE);
            Log.d("prompts show standard or allow free text","show standard prompts spinner");
            btnShowHide.setText("Free");
        }
        else {
            standardPromptsSpinner.setVisibility(View.GONE);
            freeTextPrompt.setVisibility(View.VISIBLE);
            Log.d("prompts show standard or allow free text","show free text entry box");
            btnShowHide.setText("Qns");
        }

    }
    public void showSystemPrompt(View v) {
        // pause text to speech as user requested to navigate to another part of the play
//        MyApplication.textToSpeech.speak("", TextToSpeech.QUEUE_FLUSH, null);
        VoiceSynthesizer.stopPlayback();

        // launch a new activity
        Intent i = new Intent(this, SystemPromptActivity.class);
        Log.d("settings","open system prompt");

        try {
            startActivity(i);
        } catch (IllegalStateException ex) {
            Log.e("oops!", String.valueOf(ex.getCause()));
        }

    }
    public void clearEditTextBox() {
        // set edit text view to blank so user doesn't have to manually clear it
        EditText editTV = findViewById(R.id.message_edit_text);
        editTV.getText().clear();
    }


    public void setFontSmall12sp(View v){
        // set font size to small 12sp
        GlobalClass.fontsizesp = 12;
        Log.d("fontLog","font size set to 12sp");
        updateScriptDisplay(v, false, false);
    }
    public void setFontMedium16sp(View v){
        // set font size to medium 16sp
        GlobalClass.fontsizesp = 16;
        Log.d("fontLog","font size set to 16sp");
        updateScriptDisplay(v, false, false);
    }
    public void setFontLarge20sp(View v){
        // set font size to large 20sp
        GlobalClass.fontsizesp = 20;
        Log.d("fontLog","font size set to 20sp");
        updateScriptDisplay(v, false, false);
    }

    public void openAMSND(View v) {
        // launch a new activity

        Intent i = new Intent(this, AMSND.class);
        startActivity(i);
    }

    public void callChatGPT(View v) {

        // pause text to speech as user requested to navigate to another part of the play
//        MyApplication.textToSpeech.speak("", TextToSpeech.QUEUE_FLUSH, null);
        VoiceSynthesizer.stopPlayback();

        Log.d("check","Chat GPT button pressed");

        LinearLayout llChat = (LinearLayout) findViewById(R.id.mainLinearLayout);

        // webview version
        //WebView tvScript = (WebView) findViewById(R.id.textviewAMSND);

        // textview version
//        TextView tvScript = (TextView) findViewById(R.id.textViewScript);
        // RecyclerView version
        RecyclerView rvScript = (RecyclerView) findViewById(R.id.rvScript);

//        int llChatHeight = llChat.getHeight();
//        Log.d("info","chat linear layout height: " + Integer.toString(llChatHeight));
//
////            RelativeLayout.LayoutParams layoutparams = new RelativeLayout.LayoutParams(100,250);
////            layoutparams.setMargins(10,20,10,20);
//
//        ViewGroup.LayoutParams llChatLayoutParams = llChat.getLayoutParams();
//
//        // Changes the height and width to the specified *pixels*
////            llChatLayoutParams.height = screenHeight/2;
//        int screenWidth = 0;
//        llChatLayoutParams.width = screenWidth;
//        llChat.setLayoutParams(llChatLayoutParams);
//
//        RecyclerView rvMessages = (RecyclerView) findViewById(R.id.rvMessages);
//        int rvMessagesHeight = rvMessages.getHeight();
//        Log.d("info","chat text height: " + Integer.toString(rvMessagesHeight));
////            rvMessagesHeight.setHeight() = 100;

        String userPrompt;

        // Decide whether the userPrompt is from the free text entry box or from the
        // spinner of standard prompts.
        // User free-text entry prompt
        EditText messageEditText;
        messageEditText = findViewById(R.id.message_edit_text);
        // User selection from standard prompts spinner
        Spinner standardPromptsSpinner;
        standardPromptsSpinner = findViewById(R.id.standardprompts_spinner);

        if(standardPromptsSpinner.getVisibility() == View.GONE) {
            userPrompt = messageEditText.getText().toString().trim();
        }
        else {
            userPrompt = standardPromptsSpinner.getSelectedItem().toString();
        }

        // If user selects a separator row then go no further.
        if (userPrompt.lastIndexOf("---")!=-1){
            return;
        }

        String userPromptPlay = " My query relates to the Shakespeare play <i>" + GlobalClass.selectedPlay + "</i>.";

        // If the selected standard prompt / prepared question relates to the selected Act or Scene
        // then the prompt needs to incorporate reference to the current act or scene to send this
        // information to ChatGPT in the prompt.
        if (userPrompt.lastIndexOf("this Act")!=-1){
            userPromptPlay = " My query relates specifically to act " + String.valueOf(GlobalClass.selectedActNumber) + " of the Shakespeare play <i>" + GlobalClass.selectedPlay + "</i>.";
        } else if (userPrompt.lastIndexOf("this scene")!=-1){
            userPromptPlay = " My query relates specifically to act " + String.valueOf(GlobalClass.selectedActNumber) + " scene " + String.valueOf(GlobalClass.selectedSceneNumber) + " of the Shakespeare play <i>" + GlobalClass.selectedPlay + "</i>.";
        }

        Log.d("message", "userPrompt: " + userPrompt);
        Toast.makeText(getApplicationContext(), "Thinking...", Toast.LENGTH_SHORT).show();
        ChatGPTApiHelper.callChatGPTApi("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", userPrompt, userPromptPlay, new ChatGPTApiHelper.ChatGPTResponseCallback() {

            @Override
            public void onSuccess(String response, int responseCode) {
                runOnUiThread(() -> {
                    // Log the response code and response
                    System.out.println("Response Code: " + responseCode);
                    System.out.println("Response: " + response);
                    Log.d("response", response);
                    try {
                        JSONObject gptResponse = new JSONObject(response);

                        String gptMsg_choices =  gptResponse.getString("choices");
                        JSONArray jsonArrayChoices = new JSONArray(gptMsg_choices);
                        JSONObject gptChoices = new JSONObject(String.valueOf(jsonArrayChoices.getJSONObject(0)));
                        String gptMsg_message =  gptChoices.getString("message");
                        JSONObject gptMessage = new JSONObject(gptMsg_message);
                        String gptMsg_role =  gptMessage.getString("role");
                        String gptMsg_content =  gptMessage.getString("content");
                        System.out.println(gptMsg_role + " says: " + gptMsg_content);

                        messageList.add("You: " + userPrompt);
                        String gptMsg_role_cap = gptMsg_role.substring(0, 1).toUpperCase();
                        String gptMsg_role_rest = gptMsg_role.substring(1, gptMsg_role.length());
                        messageList.add(gptMsg_role_cap + gptMsg_role_rest + ": " + gptMsg_content);
                        Log.d("message", "messageList: " + String.valueOf(messageList.size()));

                        RecyclerView recyclerView = findViewById(R.id.rvMessages);
                        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));

                        adapter = new MyRecyclerViewAdapter(recyclerView.getContext(), messageList);
                        recyclerView.setAdapter(adapter);
                        int listLength = messageList.size();
                        recyclerView.smoothScrollToPosition(listLength);

                        // set edit text view to blank so user doesn't have to manually clear it
                        clearEditTextBox();

                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                });

            }

            @Override
            public void onError(Exception e, int responseCode) {
                runOnUiThread(() -> {
                    // Log the error and response code
                    System.out.println("Error: " + e.getMessage());
                    System.out.println("Response Code: " + responseCode);
                });
            }
        });

    }


    // read the script from the sqlite database
    // test version using 2-dimensional array with character name included
    public void updateScriptDisplay_2d(View v, Boolean boolAtPrologue, Boolean boolAtEpilogue){

        // Clear the list so that the acts and scenes don't accumulate in an ever
        // increasingly long amount of scrollable text.
        scriptLinesList.clear();
        scriptLinesList_2d.clear(); // as at Sunday 13 Apr 2025, the 2d version is in dev.

        DatabaseHandler db = new DatabaseHandler(this) {
            @Override
            public void onCreate(SQLiteDatabase db) {

                Log.d("sqllite","onCreate");

            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

                Log.d("sqllite","onUpgrade");

            }
        };

        GlobalClass.numberOfScenesInAct = db.getNumberOfScenesInAct();
        GlobalClass.numberOfActsInPlay = db.getNumberOfActsInPlay();

        int intActNumberSelected = GlobalClass.selectedActNumber;
        int intSceneNumberSelected = GlobalClass.selectedSceneNumber;
        // if we are at the prologue then act number selected is -1 and we need scene number to be -1 as well.
        // if we are at the epilogue then act number selected is -2 and we need scene number to be -2 as well.
        if (intActNumberSelected<0) {
            intSceneNumberSelected = intActNumberSelected;
            GlobalClass.selectedSceneNumber = intSceneNumberSelected;
        }
        Log.d("act number and scene number returned","Act and scene number selected:" + String.valueOf(intActNumberSelected) + " " + String.valueOf(intSceneNumberSelected));

        // If the content is the preamble including Dramatis Personae
        // then don't display act number or scene number.
        if(intActNumberSelected==0){
            TextView tvActNumber = findViewById(R.id.textViewActNumber);
            tvActNumber.setVisibility(View.GONE);

            TextView tvSceneNumber = findViewById(R.id.textViewSceneNumber);
            tvSceneNumber.setVisibility(View.GONE);

        } else if(intSceneNumberSelected==0){
            // If the content is the preamble of an Act, then don't display scene number.
            TextView tvSceneNumber = findViewById(R.id.textViewSceneNumber);
            tvSceneNumber.setVisibility(View.GONE);

            // Display act number only.
            TextView tvActNumber = findViewById(R.id.textViewActNumber);
            tvActNumber.setVisibility(View.VISIBLE);
            tvActNumber.setText("Act " + String.valueOf(intActNumberSelected) + "/" + GlobalClass.numberOfActsInPlay);

        } else if(intActNumberSelected==-2){
            // If the content is the preamble of an Act, then don't display scene number.
            TextView tvSceneNumber = findViewById(R.id.textViewSceneNumber);
            tvSceneNumber.setVisibility(View.GONE);

            // Display act number only.
            TextView tvActNumber = findViewById(R.id.textViewActNumber);
            tvActNumber.setVisibility(View.VISIBLE);
            tvActNumber.setText("Prol.");

        } else if(intActNumberSelected==-1){
            // If the content is the preamble of an Act, then don't display scene number.
            TextView tvSceneNumber = findViewById(R.id.textViewSceneNumber);
            tvSceneNumber.setVisibility(View.GONE);

            // Display act number only.
            TextView tvActNumber = findViewById(R.id.textViewActNumber);
            tvActNumber.setVisibility(View.VISIBLE);
            tvActNumber.setText("Epil.");

        } else {
            // Otherwise display act number and scene number.
            TextView tvActNumber = findViewById(R.id.textViewActNumber);
            tvActNumber.setVisibility(View.VISIBLE);
            tvActNumber.setText("Act " + String.valueOf(intActNumberSelected) + "/" + GlobalClass.numberOfActsInPlay);

            TextView tvSceneNumber = findViewById(R.id.textViewSceneNumber);
            tvSceneNumber.setVisibility(View.VISIBLE);
            tvSceneNumber.setText("Sc " + String.valueOf(intSceneNumberSelected) + "/" + GlobalClass.numberOfScenesInAct);

        }

        // show the script using recycler view with multiple lines returned from database rows returned
        // *** start recycler view logic ***

        // add the script as an array or list - this is the earlier 1-d version which is no longer user
        scriptLinesList = db.getScript(boolAtPrologue, boolAtEpilogue);
        Log.d("script", "scriptLinesList size: " + scriptLinesList.size());

        // This is the 2-d version which is now being used.
        scriptLinesList_2d = db.getScript_2d(boolAtPrologue, boolAtEpilogue);
        Log.d("script_2d", "2d scriptLinesList size: " + scriptLinesList_2d.size());

        RecyclerView rvScript = findViewById(R.id.rvScript);
        rvScript.setLayoutManager(new LinearLayoutManager(rvScript.getContext()));

        // *** start: loop through ArrayList scriptLinesList

        ArrayList<String> scriptLinesList = null;
        Integer i = 0;

        for(ArrayList<String> data: scriptLinesList_2d)
        {
            // Assign just the script part of the array.
            // Check whether the character is specified as the user's character
            // and if so then include a flag to indicate to not speak that text.
            scriptLinesList.add(String.valueOf(data.get(1)));
            i++;
            Log.d("2d adapter view","2d adapter view: " + String.valueOf(data.get(1)));
        }

        // *** end: loop through ArrayList scriptLinesList

        // script lines list needs alternating line with the playcode, act, scene, play_line_nr reference
        // to include in the recycler view as a hidden row for bookmark referencing
        adapter = new MyRecyclerViewAdapter(rvScript.getContext(), scriptLinesList);
        rvScript.setAdapter(adapter);
        int listLength = scriptLinesList.size();
//        rvScript.smoothScrollToPosition(listLength);
        rvScript.smoothScrollToPosition(0);
        // *** end recycler view logic ***

        // set the act number font size which the user has specified
        TextView tvActNumber = findViewById(R.id.textViewActNumber);
        tvActNumber.setTextSize(TypedValue.COMPLEX_UNIT_SP, GlobalClass.fontsizesp);

        // set the scene number font size which the user has specified
        TextView tvSceneNumber = findViewById(R.id.textViewSceneNumber);
        tvSceneNumber.setTextSize(TypedValue.COMPLEX_UNIT_SP, GlobalClass.fontsizesp);

        // update database play_position table with current act number and scene number for current play code
        Integer intUpdateRow = db.updateNavDbWithCurrentActSceneInPlay();
        Log.d("check", String.valueOf(intUpdateRow));

        updateStandardPromptsList(v);

    }


}