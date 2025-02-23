package com.shakespeare.new_app;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RemoveBookmarkPop extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_removebookmarkpop);
        Log.d("new bookmark pop", "NewBookmarkPop: new bookmark pop");

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width*0.8), (int) (height*0.6));

        TextView vwBookmarkText = findViewById(R.id.bookmarktext);

//        String strLocationHeading = "'" + com.shakespeare.new_app.GlobalClass.selectedPlay + "' Act " + GlobalClass.selectedActNumber + ", Sc " + GlobalClass.selectedSceneNumber;
        Bundle extras = getIntent().getExtras();
        String strBookmarkText = extras.getString("strBookmarkText");
        vwBookmarkText.setText(strBookmarkText);
        vwBookmarkText.setTypeface(null, Typeface.ITALIC);
    }

    public void closePopupWithoutChange(View v) {
        // cancel the remove request, i.e. do not remove the bookmark

        getOnBackPressedDispatcher().onBackPressed();
    }

    public void removeBookmarkClosePopup(View v) {
        // remove the bookmark

        // obtain the text of the bookmark which the user long-clicked
        Bundle extras = getIntent().getExtras();
        String strBookmarkText = extras.getString("strBookmarkText");

        // extract the bookmark reference from the bookmark text
        Integer intStartIndex = strBookmarkText.indexOf("Note {")+6;
        Integer intEndIndex = strBookmarkText.indexOf("}: ");
        String strBookmarkReference = strBookmarkText.substring(intStartIndex,intEndIndex);
        Integer intBookmarkReference = Integer.valueOf(strBookmarkReference);
//        Toast.makeText(this, String.valueOf(intBookmarkReference), Toast.LENGTH_SHORT).show();

        // pass the bookmark reference to the delete SQL statement to remove the bookmark
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

        db.removeBookmarkLongClicked(intBookmarkReference);
//        Toast.makeText(this, "Bookmark {"+String.valueOf(intBookmarkReference)+"} removed.", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "Bookmark removed", Toast.LENGTH_SHORT).show();

        Intent i = new Intent(this, Bookmarks.class);
        startActivity(i);

//        getOnBackPressedDispatcher().onBackPressed();
    }
}
