package com.shakespeare.new_app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

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

        getWindow().setLayout((int) (width*0.8), (int) (height*0.6));

    }

    public void closePopupWithoutSaving(View v) {
        // cancel the bookmark, i.e. do not create a new bookmark record

        getOnBackPressedDispatcher().onBackPressed();

    }

    public void saveBookmarkClosePopup(View v) {
        // save the bookmark record and close the popup window

    }
    public void saveBookmarkKeepTyping(View v) {
        // save record but keep typing to update the record

    }

}
