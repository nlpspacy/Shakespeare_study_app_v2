package com.shakespeare.new_app;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class NewBookmarkPop extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newbookmarkpop);
        Log.d("new bookmark pop", "NewBookmarkPop: new bookmark pop");
    }
}
