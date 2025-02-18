package com.shakespeare.new_app;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class RemoveBookmarkPop extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newbookmarkpop);
        Log.d("new bookmark pop", "NewBookmarkPop: new bookmark pop");

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width*0.7), (int) (height*0.6));

    }

}
