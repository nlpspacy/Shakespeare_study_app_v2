package com.shakespeare.new_app;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

public class NewBookmarkPop extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newbookmarkpop);
        Log.d("new bookmark pop", "NewBookmarkPop: new bookmark pop");

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width*0.9), (int) (height*0.8));

        TextView vwScriptText = findViewById(R.id.scripttext);

        // obtain detailed reference information
        Intent intent = getIntent();
        String strScriptRef = intent.getStringExtra("scriptRef");
        Log.d("strScriptRef",strScriptRef);
        Integer intSceneLineNr;
        Integer intPlayLineNr;

        Integer intScriptLineNrStart = 15+strScriptRef.indexOf(" scene_line_nr ");
        Integer intScriptLineNrEnd = intScriptLineNrStart+strScriptRef.substring(intScriptLineNrStart,strScriptRef.length()).indexOf(" ");
        Log.d("strScriptRef",intScriptLineNrStart+" "+intScriptLineNrEnd);
        String strSceneLineNr = strScriptRef.substring(intScriptLineNrStart,intScriptLineNrEnd);
        Log.d("strSceneLineNr",strSceneLineNr);
        intSceneLineNr = Integer.valueOf(strSceneLineNr);
        com.shakespeare.new_app.GlobalClass.scriptSceneLineNr = intSceneLineNr;

        Integer intScriptPlayNrStart = 14+strScriptRef.indexOf(" play_line_nr ");
        Integer intScriptPlayNrEnd = strScriptRef.length();
        String strPlayLineNr = strScriptRef.substring(intScriptPlayNrStart,intScriptPlayNrEnd);
        intPlayLineNr = Integer.valueOf(strPlayLineNr);
        com.shakespeare.new_app.GlobalClass.scriptPlayLineNr = intPlayLineNr;

        String strLocationHeading = "'" + com.shakespeare.new_app.GlobalClass.selectedPlay + "' Act " + GlobalClass.selectedActNumber + ", Sc " + GlobalClass.selectedSceneNumber + ", Line " + intSceneLineNr;

        vwScriptText.setText(strLocationHeading + "\n" + com.shakespeare.new_app.GlobalClass.scriptText);
        vwScriptText.setTypeface(null, Typeface.ITALIC);


    }

    public void closePopupWithoutSaving(View v) {
        // cancel the bookmark, i.e. do not create a new bookmark record

        getOnBackPressedDispatcher().onBackPressed();

    }

    public void saveBookmarkClosePopup(View v) {
        // save the bookmark record and close the popup window

        EditText bookmarkUserNote;
        bookmarkUserNote = findViewById(R.id.bookmarkusernote);

        String strUserNote = "";
        strUserNote = bookmarkUserNote.getText().toString().trim();

        DatabaseHandler db = new DatabaseHandler(this.getApplicationContext()) {
            @Override
            public void onCreate(SQLiteDatabase db) {
                Log.d("sqllite","onCreate");
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                Log.d("sqllite","onUpgrade");
            }
        };

        db.addBookmark(com.shakespeare.new_app.GlobalClass.scriptPosition, com.shakespeare.new_app.GlobalClass.scriptText, strUserNote, GlobalClass.scriptSceneLineNr, GlobalClass.scriptPlayLineNr);
        Log.d("new bookmark pop", "RecyclerItemClickListener: bookmark added and bookmark pop closed");

        Intent result = new Intent();
        result.putExtra("bookmarkSaved", true);
        setResult(RESULT_OK, result);
        finish(); // closes NewBookmarkPop and triggers onActivityResult

        getOnBackPressedDispatcher().onBackPressed();

    }
    public void saveBookmarkKeepTyping(View v) {
        // save record but keep typing to update the record

        EditText bookmarkUserNote;
        bookmarkUserNote = findViewById(R.id.bookmarkusernote);

        String strUserNote = "";
        strUserNote = bookmarkUserNote.getText().toString().trim();

        DatabaseHandler db = new DatabaseHandler(this.getApplicationContext()) {
            @Override
            public void onCreate(SQLiteDatabase db) {
                Log.d("sqllite","onCreate");
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                Log.d("sqllite","onUpgrade");
            }
        };

        db.addBookmark(com.shakespeare.new_app.GlobalClass.scriptPosition, com.shakespeare.new_app.GlobalClass.scriptText, strUserNote, GlobalClass.scriptSceneLineNr, GlobalClass.scriptPlayLineNr);
        Log.d("new bookmark pop", "RecyclerItemClickListener: bookmark added and bookmark pop closed");


    }

}
