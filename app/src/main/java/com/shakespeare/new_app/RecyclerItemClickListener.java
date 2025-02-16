// using https://stackoverflow.com/questions/24471109/recyclerview-onclick

package com.shakespeare.new_app;

import static androidx.core.content.ContextCompat.startActivity;

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


public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);

        public void onLongItemClick(View view, int position);
    }

    GestureDetector mGestureDetector;

    public RecyclerItemClickListener(Context context, final RecyclerView recyclerView, OnItemClickListener listener) {

        mListener = listener;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {

                Log.d("click check","onSingleTapUp item clicked in RecyclerItemClickListener.java class " + String.valueOf(e));
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
                String strScriptText = myAdapter.getItem(position);
                Integer intContentLength = strScriptText.length();

                if(strScriptText.substring(intContentLength-1, intContentLength).equals("+")){
                    // If it is a character marker as indicated by the plus sign (+) at the end of the character name,
                    // then remove the plus (+) sign at the end of the character name.
                    strScriptText = strScriptText.substring(0, intContentLength-1);
                }

                // replace quote character with double version of the same kind of quote character
                // so the string can be inserted into a SQL statement without errors
                strScriptText = strScriptText.replace("\'", "\'\'");
                strScriptText = strScriptText.replace("\"","\"\"");

                if (child != null && mListener != null) {
                    mListener.onLongItemClick(child, recyclerView.getChildAdapterPosition(child));
                    Log.d("click check","onLongPress item clicked in RecyclerItemClickListener.java class: getX " + String.valueOf(e.getX()) + " getY " + String.valueOf(e.getY()));
                    // use this guide use this guide https://m.youtube.com/watch?v=fn5OlqQuOCk
                    // to add popup for annotation
                    Log.d("new bookmark pop", "RecyclerItemClickListener: new bookmark pop");

                    com.shakespeare.new_app.GlobalClass.scriptPosition = recyclerView.getChildAdapterPosition(child);
                    com.shakespeare.new_app.GlobalClass.scriptText = strScriptText;

                    Log.d("new bookmark pop", "RecyclerItemClickListener: global variables assigned");

                    Intent i;
//                    i = new Intent(this, com.shakespeare.new_app.NewBookmarkPop.class);
                    i = new Intent(context.getApplicationContext(), NewBookmarkPop.class);
                    child.getContext().startActivity(i);

//                    db.addBookmark(recyclerView.getChildAdapterPosition(child), strScriptText);
//                    Log.d("new bookmark pop", "RecyclerItemClickListener: bookmark added and bookmark pop closed");

                }
            }
        });
    }

    private void startActivity(Intent i) {

    }

    @Override public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
        View childView = view.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
            mListener.onItemClick(childView, view.getChildAdapterPosition(childView));
            return true;
        }
        return false;
    }

    @Override public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) { }

    @Override
    public void onRequestDisallowInterceptTouchEvent (boolean disallowIntercept){}
}