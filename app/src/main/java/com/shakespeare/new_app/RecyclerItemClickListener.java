// using https://stackoverflow.com/questions/24471109/recyclerview-onclick

package com.shakespeare.new_app;

import android.content.Context;
//import android.support.v7.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Layout;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
    private static final String TAG = "RecyclerItemClickListen";
    private Context context;
    private OnItemClickListener mListener;
    private Object view;

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);

        public void onLongItemClick(View view, int position);
    }

    GestureDetector mGestureDetector;

    public RecyclerItemClickListener(Context context, final RecyclerView recyclerView, OnItemClickListener listener) {

        this.context = context;
        mListener = listener;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) { // onSingleTapUp(MotionEvent e) {

                Log.d("click check","onSingleTapUp item clicked in RecyclerItemClickListener.java class " + String.valueOf(e));

                View child = recyclerView.findChildViewUnder(e.getX(), e.getY());

                boolean boolClickableSpanBookmarkRefClicked = false;
                String numberOfBookmarks = "0"; // e.g., "3"


//                if (child instanceof TextView) {
//                    TextView textView = (TextView) child;

                    if (child != null) {
                        TextView textView = child.findViewById(R.id.tv_script_line); // Use your actual TextView ID
                        Log.d("Check", "TextView found: " + textView.getText());

                        if (textView != null) {

                            CharSequence text = textView.getText();

                            if (text instanceof Spanned) {
                                Spanned spanned = (Spanned) text;

                                // Get layout and tap position
                                Layout layout = textView.getLayout();
                                int x = (int) e.getX() - textView.getTotalPaddingLeft() + textView.getScrollX();
                                int y = (int) e.getY() - textView.getTotalPaddingTop() + textView.getScrollY();

                                if (layout != null) {
                                    int line = layout.getLineForVertical(y);
                                    int offset = layout.getOffsetForHorizontal(line, x);

                                    ClickableSpan[] link = spanned.getSpans(offset, offset, ClickableSpan.class);
                                    Log.d("ClickableSpan", "ClickableSpan " + Arrays.toString(link));

                                    if (link.length > 0) {

                                        // Find the content of the bookmark reference clicked
                                        // start
                                        int spanStart = spanned.getSpanStart(link[0]);
                                        int spanEnd = spanned.getSpanEnd(link[0]);
                                        CharSequence spanText = spanned.subSequence(spanStart, spanEnd);

                                        // Now apply your regex to extract the ref number inside <>
                                        Pattern pattern = Pattern.compile("<(\\d+)>");
                                        Matcher matcher = pattern.matcher(spanText);
                                        if (matcher.find()) {
                                            numberOfBookmarks = matcher.group(1); // e.g., "3"
                                            Log.d("click check", "Bookmark reference clicked: <" + numberOfBookmarks + ">");

                                        }
                                        // Find the content of the bookmark reference clicked
                                        // finish


                                        // ✅ It's a ClickableSpan!
                                        Log.d("ClickableSpan", "ClickableSpan clicked at offset " + offset);
                                        boolClickableSpanBookmarkRefClicked = true;
//                                return true; // stop further processing
                                    }
                                }
                            }
                        }
                    }

                // Added 06 July 2025 5.00am
                // If the user has clicked a ClickableSpan bookmark ref
                // then find the location and show the bookmark dialog.
                if(boolClickableSpanBookmarkRefClicked) {
                    // Commenting out this condition because we check through other means whether a clickable span was clicked.
                    // indicating the user clicks on a bookmark reference and so wants to check that bookmark reference.
//                if(com.shakespeare.new_app.GlobalClass.boolBookmarkRefClicked){

                    try {

                        //View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                        Integer position = recyclerView.getChildAdapterPosition(child);
                        MyRecyclerViewAdapter myAdapter = (MyRecyclerViewAdapter) recyclerView.getAdapter();
                        String strScriptText = myAdapter.getItem(position).toString();

                        // find the closest previous line with script reference information
                        // identified by the first 10 characters being "play_code:"
                        String strScriptRef = myAdapter.getItem(position - 1).toString();
                        Log.d("script ref", "search for closest previous strScriptRef, position - 1: " + strScriptRef);
                        if (!strScriptRef.substring(0, 10).equals("play_code:")) {
                            strScriptRef = myAdapter.getItem(position - 2).toString();
                            Log.d("script ref", "search for closest previous strScriptRef, position - 2: " + strScriptRef);
                            if (!strScriptRef.substring(0, 10).equals("play_code:")) {
                                strScriptRef = myAdapter.getItem(position - 3).toString();
                                Log.d("script ref", "search for closest previous strScriptRef, position - 2: " + strScriptRef);
                                if (!strScriptRef.substring(0, 10).equals("play_code:")) {
                                    strScriptRef = myAdapter.getItem(position - 4).toString();
                                    Log.d("script ref", "search for closest previous strScriptRef, position - 2: " + strScriptRef);
                                    if (!strScriptRef.substring(0, 10).equals("play_code:")) {
                                        strScriptRef = "play_code: " + com.shakespeare.new_app.GlobalClass.selectedPlay + " Act " + com.shakespeare.new_app.GlobalClass.selectedActNumber.toString() + " Scene " + com.shakespeare.new_app.GlobalClass.selectedSceneNumber.toString() + " scene_line_nr 0 play_line_nr 0";
                                        Log.d("script ref", "search for closest previous strScriptRef, position - 2: " + strScriptRef);
                                    }
                                }
                            }
                        }

                        Integer intContentLength = strScriptText.length();

                        if (strScriptText.substring(intContentLength - 1, intContentLength).equals("+")) {
                            // If it is a character marker as indicated by the plus sign (+) at the end of the character name,
                            // then remove the plus (+) sign at the end of the character name.
                            strScriptText = strScriptText.substring(0, intContentLength - 1);
                        }

                        // replace quote character with double version of the same kind of quote character
                        // so the string can be inserted into a SQL statement without errors
                        strScriptText = strScriptText.replace("\'", "\'\'");
                        strScriptText = strScriptText.replace("\"", "\"\"");

                        if (child != null && mListener != null) {
                            mListener.onLongItemClick(child, recyclerView.getChildAdapterPosition(child));
//                        Log.d("click check","item clicked in RecyclerItemClickListener.java class: getX " + String.valueOf(e.getX()) + " getY " + String.valueOf(e.getY()));
                            // use this guide use this guide https://m.youtube.com/watch?v=fn5OlqQuOCk
                            // to add popup for annotation
//                        Log.d("bookmark ref", "RecyclerItemClickListener: bookmark ref clicked");

                            com.shakespeare.new_app.GlobalClass.scriptPosition = recyclerView.getChildAdapterPosition(child);
                            com.shakespeare.new_app.GlobalClass.scriptText = strScriptText;

//                        Log.d("bookmark ref", "RecyclerItemClickListener: global variables assigned");

                            // Previous version in which the screen did not refresh after adding a new bookmark:
//                    Intent i;
//                    i = new Intent(context.getApplicationContext(), NewBookmarkPop.class);
//                    i.putExtra("scriptRef", strScriptRef);
//                    child.getContext().startActivity(i);

//                        // New version to refresh screen after adding a new bookmark:
//                        Context context = child.getContext();
//                        if (context instanceof AMSND) {
//                            ((AMSND) context).launchNewBookmarkActivity(strScriptRef);
//                        }

//                    db.addBookmark(recyclerView.getChildAdapterPosition(child), strScriptText);
//                    Log.d("new bookmark pop", "RecyclerItemClickListener: bookmark added and bookmark pop closed");

                            // Added 06 July 2025 5.00am
                            // If the user has clicked a ClickableSpan bookmark ref
                            // then show the bookmark dialog
                            showBookmarkDialog(strScriptRef, strScriptText, numberOfBookmarks);

                        }
                    } catch (Exception exception) {
                        Log.e(TAG, "onSingleTapUp: ", exception);
                        // do something
                        Log.d("exception report", " " + exception.getMessage().toString());
                    }

                }

                com.shakespeare.new_app.GlobalClass.boolBookmarkRefClicked = false;

                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {

                try{

                View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                Integer position = recyclerView.getChildAdapterPosition(child);
                MyRecyclerViewAdapter myAdapter = (MyRecyclerViewAdapter) recyclerView.getAdapter();
                String strScriptText = myAdapter.getItem(position).toString();

                // find the closest previous line with script reference information
                    // identified by the first 10 characters being "play_code:"
                    String strScriptRef = myAdapter.getItem(position - 1).toString();
                    Log.d("script ref","search for closest previous strScriptRef, position - 1: " + strScriptRef);
                    if (!strScriptRef.substring(0, 10).equals("play_code:")) {
                        strScriptRef = myAdapter.getItem(position - 2).toString();
                        Log.d("script ref","search for closest previous strScriptRef, position - 2: " + strScriptRef);
                        if (!strScriptRef.substring(0, 10).equals("play_code:")) {
                            strScriptRef = myAdapter.getItem(position - 3).toString();
                            Log.d("script ref","search for closest previous strScriptRef, position - 2: " + strScriptRef);
                            if (!strScriptRef.substring(0, 10).equals("play_code:")) {
                                strScriptRef = myAdapter.getItem(position - 4).toString();
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

            } // end of onLongPress block
        });
    }

//    private void startActivity(Intent i) {
//
//    }

    @Override public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {

        Log.d("check","onInterceptTouchEvent");

        View childView = view.findChildViewUnder(e.getX(), e.getY());
        if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
            Log.d("check","onInterceptTouchEvent: mListener.onItemClick(childView, view.getChildAdapterPosition(childView))");
            mListener.onItemClick(childView, view.getChildAdapterPosition(childView));
            return true;
        }
        return false;
    }

    @Override public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) { }

    @Override
    public void onRequestDisallowInterceptTouchEvent (boolean disallowIntercept){}

    public void showBookmarkDialog(String strScriptRef, String strScriptText, String numberOfBookmarks) {

        // set flag to false so that a future click is only treated as a bookmark reference
        // click if the ClickableSpan OnClick has again set this flag to true.
        com.shakespeare.new_app.GlobalClass.boolBookmarkRefClicked = false;

        this.context = context;
        String strAlertDialogTitle = "";
        String fullText = "";

        Log.d("bookmark reference", "In RecyclerItemClickListener. User tapped bookmark line " + com.shakespeare.new_app.GlobalClass.scriptPosition + ".");

        if (numberOfBookmarks.equals("1")){
            Log.d("bookmark reference", "There is " + numberOfBookmarks + " bookmark.");
            strAlertDialogTitle = numberOfBookmarks + " bookmark:";
        }
        else {
            Log.d("bookmark reference", "There are " + numberOfBookmarks + " bookmarks.");
            strAlertDialogTitle = numberOfBookmarks + " bookmarks:";
        }

        fullText = "At " + com.shakespeare.new_app.GlobalClass.scriptPosition + " " + strScriptRef;

        new AlertDialog.Builder(context)
                .setTitle(strAlertDialogTitle)
                .setMessage(fullText)
                .setPositiveButton("Edit", (dialog, which) -> {
                    // TODO: Hook up edit functionality
                })
                .setNegativeButton("Delete", (dialog, which) -> {
                    // TODO: Hook up delete functionality
                })
                .setNeutralButton("Close", null)
                .show();


    }


}