package com.shakespeare.new_app;

import android.app.Activity;
import android.content.Context;
//import android.support.v7.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class RecyclerBookmarksClickListener implements RecyclerView.OnItemTouchListener {

    private OnItemClickListener m2Listener;

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);

        public void onLongItemClick(View view, int position);
    }

    GestureDetector mGestureDetector;

    public RecyclerBookmarksClickListener(Context context, final RecyclerView recyclerView, OnItemClickListener listener) {

        m2Listener = listener;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {

                Log.d("click check","onSingleTapUp item clicked in RecyclerBookmarksClickListener.java class " + String.valueOf(e));
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {

                DatabaseHandler db = new DatabaseHandler(context.getApplicationContext()) {
                    @Override
                    public void onCreate(SQLiteDatabase db) {
                        Log.d("sqllite","onCreate");
                    }

                    @Override
                    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                        Log.d("sqllite","onUpgrade");
                    }


                };

                View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                Integer position = recyclerView.getChildAdapterPosition(child);
                MyRecyclerViewAdapter myAdapter = (MyRecyclerViewAdapter) recyclerView.getAdapter();
                String strBookmark = myAdapter.getItem(position);
                Integer intContentLength = strBookmark.length();

                if(strBookmark.substring(intContentLength-1, intContentLength).equals("+")){
                    // If it is a character marker as indicated by the plus sign (+) at the end of the character name,
                    // then remove the plus (+) sign at the end of the character name.
                    strBookmark = strBookmark.substring(0, intContentLength-1);
                }

                // replace quote character with double version of the same kind of quote character
                // so the string can be inserted into a SQL statement without errors
                strBookmark = strBookmark.replace("\'", "\'\'");
                strBookmark = strBookmark.replace("\"","\"\"");

                if (child != null && m2Listener != null) {
                    m2Listener.onLongItemClick(child, recyclerView.getChildAdapterPosition(child));
                    String strBookmarkText = myAdapter.getItem(position);
                    Log.d("click check","onLongPress item clicked in RecyclerBookmarksClickListener.java class: position " + String.valueOf(position) + " " + strBookmarkText + ", getX " + String.valueOf(e.getX()) + " getY " + String.valueOf(e.getY()));
                    // show Yes/No message to confirm changing bookmark to inactive status
                    // if user presses Yes, then change bookmark to inactive status
                    // mark that it will not show up next time bookmarks are opened, or alternatively auto-refresh to remove bookmark
                    // use this guide https://m.youtube.com/watch?v=fn5OlqQuOCk

                    Intent i;
//                    i = new Intent(this, com.shakespeare.new_app.NewBookmarkPop.class);
                    i = new Intent(context.getApplicationContext(), RemoveBookmarkPop.class);
                    child.getContext().startActivity(i);

                }
            }
        });
    }

    @Override public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        View childView = view.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && m2Listener != null && mGestureDetector.onTouchEvent(e)) {
            m2Listener.onItemClick(childView, view.getChildAdapterPosition(childView));
            return true;
        }
        return false;
    }

    @Override public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) { }

    @Override
    public void onRequestDisallowInterceptTouchEvent (boolean disallowIntercept){}

}
