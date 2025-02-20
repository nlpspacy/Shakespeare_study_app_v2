package com.shakespeare.new_app;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

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

        getWindow().setLayout((int) (width*0.7), (int) (height*0.6));

    }

    public void closePopupWithoutChange(View v) {
        // cancel the bookmark, i.e. do not create a new bookmark record

        getOnBackPressedDispatcher().onBackPressed();

    }


}
