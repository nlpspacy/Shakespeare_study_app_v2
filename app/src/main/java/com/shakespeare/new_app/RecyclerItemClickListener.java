// using https://stackoverflow.com/questions/24471109/recyclerview-onclick

package com.shakespeare.new_app;

import static androidx.core.content.ContextCompat.startActivity;

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
import android.widget.Toast;


public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
    private static final String TAG = "RecyclerItemClickListen";
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

                try{

                View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                Integer position = recyclerView.getChildAdapterPosition(child);
                MyRecyclerViewAdapter myAdapter = (MyRecyclerViewAdapter) recyclerView.getAdapter();
                String strScriptText = myAdapter.getItem(position);

                // find the closest previous line with script reference information
                    // identified by the first 10 characters being "play_code:"
                    String strScriptRef = myAdapter.getItem(position - 1);
                    Log.d("script ref","search for closest previous strScriptRef, position - 1: " + strScriptRef);
                    if (!strScriptRef.substring(0, 10).equals("play_code:")) {
                        strScriptRef = myAdapter.getItem(position - 2);
                        Log.d("script ref","search for closest previous strScriptRef, position - 2: " + strScriptRef);
                        if (!strScriptRef.substring(0, 10).equals("play_code:")) {
                            strScriptRef = myAdapter.getItem(position - 3);
                            Log.d("script ref","search for closest previous strScriptRef, position - 2: " + strScriptRef);
                            if (!strScriptRef.substring(0, 10).equals("play_code:")) {
                                strScriptRef = myAdapter.getItem(position - 4);
                                Log.d("script ref","search for closest previous strScriptRef, position - 2: " + strScriptRef);
                                if (!strScriptRef.substring(0, 10).equals("play_code:")) {
                                    strScriptRef = "play_code: " + com.shakespeare.new_app.GlobalClass.selectedPlay + " Act " + com.shakespeare.new_app.GlobalClass.selectedActNumber.toString() + " Scene " + com.shakespeare.new_app.GlobalClass.selectedSceneNumber.toString() + " scene_line_nr 0 play_line_nr 0";
                                    Log.d("script ref","search for closest previous strScriptRef, position - 2: " + strScriptRef);
                                }}}}

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

                    // Previous version in which the screen did not refresh after adding a new bookmark:
//                    Intent i;
//                    i = new Intent(context.getApplicationContext(), NewBookmarkPop.class);
//                    i.putExtra("scriptRef", strScriptRef);
//                    child.getContext().startActivity(i);

                    // New version to refresh screen after adding a new bookmark:
                    Context context = child.getContext();
                    if (context instanceof AMSND) {
                        ((AMSND) context).launchNewBookmarkActivity(strScriptRef);
                    }

//                    db.addBookmark(recyclerView.getChildAdapterPosition(child), strScriptText);
//                    Log.d("new bookmark pop", "RecyclerItemClickListener: bookmark added and bookmark pop closed");

                }
                }catch(Exception exception){
                    Log.e(TAG, "onLongPress: ", exception);
                    // do something
                    Log.d("exception report"," "+exception.getMessage().toString());
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