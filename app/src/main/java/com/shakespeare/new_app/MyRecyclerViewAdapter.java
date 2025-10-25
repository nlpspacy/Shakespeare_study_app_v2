package com.shakespeare.new_app;

import com.shakespeare.new_app.models.Bookmark;
import android.database.sqlite.SQLiteDatabase;
import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shakespeare.new_app.R;
//import com.example.new_app.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {
    private BookmarkLinkMovementMethod movementMethod;
    private boolean doubleTapDetected = false;

    private Context context;
    private RecyclerView recyclerView;
    private GestureDetector gestureDetector;
    private long lastClickTime = 0;

    private List<CharSequence> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private OnClickListener onClickListener;

    private boolean contentIsPreStyled;


    // 29 June 2025 6.37am
    // Replaced with the below version to fix the problem with gesture detector not detecting double taps.
    // data is passed into the constructor
//    MyRecyclerViewAdapter(Context context, ArrayList<CharSequence> data, boolean contentIsPreStyled) {
//        this.mInflater = LayoutInflater.from((Context) context);
//        this.mData = data;
//        this.contentIsPreStyled = contentIsPreStyled;
//    }

    // 29 June 2025 6.37am
    // Added this new version to fix the problem with gesture detector not detecting double taps.
    MyRecyclerViewAdapter(Context context, ArrayList<CharSequence> data, boolean contentIsPreStyled, RecyclerView recyclerView) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.contentIsPreStyled = contentIsPreStyled;
        this.recyclerView = recyclerView;

        this.gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
//            @Override
//            public boolean onDoubleTap(MotionEvent e) {
//
//                Log.d("double tap check", "double tap check: " + toString().valueOf(doubleTapDetected));
//
//                doubleTapDetected = true;
//
//                Log.d("DoubleTap", "Double-tap detected");
//
//                View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
//                if (child != null) {
//                    int position = recyclerView.getChildAdapterPosition(child);
//                    if (position != RecyclerView.NO_POSITION) {
//                        CharSequence fullText = getItem(position);
//                        String scriptLine = fullText.toString();
//
//                        List<String> refs = extractBookmarkRefs(scriptLine);
//                        for (String ref : refs) {
//                            movementMethod.handleBookmarkClick(ref, context);
//                        }
//                    }
//                }
//                return true;
//            }

//            @Override
//            public boolean onSingleTapConfirmed(MotionEvent e) {
//                Log.d("click response", "single tap (ignored)");
//                return true;
//            }


        });

        // ✅ Initialize movement method once here
        this.movementMethod = new BookmarkLinkMovementMethod(gestureDetector);
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    // using https://www.geeksforgeeks.org/how-to-apply-onclicklistener-to-recyclerview-items-in-android/
    // Setter for the click listener
    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    // using https://www.geeksforgeeks.org/how-to-apply-onclicklistener-to-recyclerview-items-in-android/
    // Interface for the click listener
    public interface OnClickListener {
        void onClick(int position);

    }
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        com.shakespeare.new_app.GlobalClass.boolBookmarkRefClicked = false;

//        ScriptLine scriptLine = scriptLines.get(position);


        // Commented out 29 June 2025 at 7.59pm because this is likely conflicting with the
        // gestureDetector defined at the class level.
//        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
//            @Override
//            public boolean onDoubleTap(MotionEvent e) {
//                Log.d("DoubleTap", "Double-tap detected");
//
//                // Find the TextView clicked
//                View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
//                if (child != null) {
//                    int position = recyclerView.getChildAdapterPosition(child);
//                    if (position != RecyclerView.NO_POSITION) {
//                        CharSequence fullText = getItem(position);
//                        String scriptLine = fullText.toString();
//
//                        // Extract ref like <2>, <3> etc.
//                        List<String> refs = extractBookmarkRefs(scriptLine);
//                        for (String ref : refs) {
//                            showBookmarkDialog(position, context, ref); // your existing dialog method
//                        }
//                    }
//                }
//
//                return true;
//            }
//        });

        Boolean boolSpeakThisLine = Boolean.TRUE;

        holder.itemView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
//        holder.itemView.setOnClickListener(view -> {
//            if (onClickListener != null) {
//                onClickListener.onClick(position);
//                Log.d("click check", "script line clicked");
//            }
//        });

        CharSequence strContent = mData.get(position);
        String strContentAsString = strContent.toString();
        String strSpokenText;
        int intContentLength = strContent.length();

        if (strContent.equals("N.A.+")) {
            strContent = "Stage direction+";
        }

        if (contentIsPreStyled) {
            holder.myTextView.setText(strContent);
            // Replaced 29 June 2025 6.47am with below version to fix problem with gesture detector double tap.
//            holder.myTextView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
            // New version 29 June 2025 6.47am to fix problem with gesture detector double tap.
            holder.myTextView.setOnTouchListener((v, event) -> {
                return gestureDetector.onTouchEvent(event);
            });
        } else {
            SpannableStringBuilder spannable;

            if (strContentAsString.endsWith("+")) {  // This is the marker for a character's name.
                strContentAsString = "<i>"+strContentAsString.substring(0, intContentLength - 1)+"</i>";
                spannable = new SpannableStringBuilder(Html.fromHtml(strContentAsString, Html.FROM_HTML_MODE_LEGACY));
                holder.myTextView.setTypeface(null, Typeface.BOLD);
            } else {
                spannable = new SpannableStringBuilder(Html.fromHtml(strContentAsString, Html.FROM_HTML_MODE_LEGACY));
                if (strContent.equals("Characters in the Play")) {
                    holder.myTextView.setTypeface(null, Typeface.BOLD);
                } else {
                    holder.myTextView.setTypeface(null, Typeface.NORMAL);
                }
            }

            // Apply clickable spans to bookmark references like <1>, <2>, etc.
            Pattern pattern = Pattern.compile("<(\\d+)>");
            Matcher matcher = pattern.matcher(spannable);
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                String ref = matcher.group(1); // the number inside <>
//                ClickableSpan clickableSpan = new ClickableSpan() {
//                    @Override
//                    public void onClick(@NonNull View widget) {
//                        Log.d("click response","bookmark reference click response");
//                        showBookmarkDialog(widget.getContext(), ref);
//                    }
//                };

                // Inside onBindViewHolder()
                final long[] lastClickTime = {1}; // mutable container for closure
                com.shakespeare.new_app.GlobalClass.boolBookmarkRefClicked = false;

                // 5 July 2025, 7.37am
                // Updated to this because using single tap not double tap anymore.
                ClickableSpan clickableSpan = new ClickableSpan() {

                    @Override
                    public void onClick(@NonNull View widget) {
                        Log.d("click response", "bookmark reference clicked");
                        com.shakespeare.new_app.GlobalClass.boolBookmarkRefClicked = true;
//                        movementMethod.handleBookmarkClick(ref, context);
                    }
                };


//                // 29 June 2025, 6.21am commenting out because this function is now performed
//                // by gesture detector which checks for double tap.
//                ClickableSpan clickableSpan = new ClickableSpan() {
//                    @Override
//                    public void onClick(@NonNull View widget) {
//                        Log.d("click response", "bookmark reference clicked");
//                        // Just call a single handler — double/single logic is handled inside the movement method
////                        BookmarkLinkMovementMethod.getInstance().handleBookmarkClick(ref, context, scriptLine);
////                        ((BookmarkLinkMovementMethod) BookmarkLinkMovementMethod.getInstance()).handleBookmarkClick(ref, context, scriptLine);
////                        ((BookmarkLinkMovementMethod) BookmarkLinkMovementMethod.getInstance()).handleBookmarkClick(ref, context);
//
//                        // updated 4 July 2025
////                        BookmarkLinkMovementMethod.getTypedInstance(gestureDetector).handleBookmarkClick(ref, context);
//                        movementMethod.handleBookmarkClick(ref, context);
//                    }
//                };


                spannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            holder.myTextView.setText(spannable);
            // 29 June 2025: Replaced to use the new Java class BookmarkLinkMovementMethod
//            holder.myTextView.setMovementMethod(LinkMovementMethod.getInstance());
//            holder.myTextView.setMovementMethod(new BookmarkLinkMovementMethod(gestureDetector));
            holder.myTextView.setMovementMethod(movementMethod);


            // 29 June 2025: Added to ensure the on touch listener is occurring here.
            holder.myTextView.setOnTouchListener((v, event) -> {
                // Must call performClick() for accessibility
                v.performClick();
                return false; // Let movement method handle it
            });

            // Replaced 29 June 2025 6.47am with below version to fix problem with gesture detector double tap.
//            holder.myTextView.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
            // New version 29 June 2025 6.47am to fix problem with gesture detector double tap.
            holder.myTextView.setOnTouchListener((v, event) -> {
                return gestureDetector.onTouchEvent(event);
            });

        }

//        Log.d("check", String.valueOf(strContent));

        if (strContentAsString.endsWith("+")) {  // This is the marker for a character's name.
            strContent = "<i>"+strContentAsString.substring(0, intContentLength - 1)+"</i>";
            holder.myTextView.setText(Html.fromHtml(strContent.toString(), Html.FROM_HTML_MODE_LEGACY));
            holder.myTextView.setTypeface(null, Typeface.BOLD);
        } else if (strContent.equals(" Characters in the Play")) {
            Log.d("check","These are the characters in the play");
            holder.myTextView.setTypeface(null, Typeface.BOLD);
        } else if (strContent.equals("Characters in the Play")) {
            Log.d("check","These are the characters in the play");
            holder.myTextView.setTypeface(null, Typeface.BOLD);
        } else {
            holder.myTextView.setTypeface(null, Typeface.NORMAL);
        }

        holder.myTextView.setTextIsSelectable(true);
        holder.myTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, com.shakespeare.new_app.GlobalClass.fontsizesp);
        holder.myTextView.setVisibility(View.VISIBLE);
        holder.myTextView.setLineSpacing(1, 1);

        if (strContent.length() > 12 && strContentAsString.startsWith("play_code:")) {
            boolSpeakThisLine = Boolean.FALSE;
            holder.itemView.setLayoutParams(new ViewGroup.LayoutParams(0, 0));
            holder.myTextView.setTextIsSelectable(false);
            holder.myTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 0);
            holder.myTextView.setVisibility(View.GONE);
            holder.myTextView.setLineSpacing(-3, -3);
        }

        if (boolSpeakThisLine.equals(Boolean.TRUE)) {
            strSpokenText = strContentAsString;
            if (GlobalClass.boolSoundOn.equals(Boolean.TRUE)) {
                if (GlobalClass.scriptSceneLineNr != 0 &&
                        GlobalClass.selectedSceneNumber != 0 &&
                        !strContentAsString.contains("[") &&
                        strContentAsString.contains(" ")) {
                    strSpokenText = strContentAsString.substring(strContentAsString.indexOf(" "));
                }

                MyApplication.setLanguage(Locale.ENGLISH);

                String sceneKey = GlobalClass.selectedPlayCode + "_" +
                        GlobalClass.selectedActNumber + "_" +
                        GlobalClass.selectedSceneNumber;

                VoiceSynthesizer.prepareScenePlayback(sceneKey);
                VoiceSynthesizer.synthesizeAndPlay(mInflater.getContext(), strSpokenText, "nova", sceneKey);
            }
        }
    }

//    private void showBookmarkDialog(Context context, String refNumber) {
//        // You can look up the bookmark by reference number here
//        String fullText = "Bookmark text for reference <" + refNumber + "> (placeholder)";
//
//        Log.d("bookmark reference",fullText);
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle("Bookmark <" + refNumber + ">");
//        builder.setMessage(fullText);
//        builder.setPositiveButton("Edit", (dialog, which) -> {
//            // TODO: Launch edit screen for this bookmark
//        });
//        builder.setNegativeButton("Delete", (dialog, which) -> {
//            // TODO: Delete the bookmark and refresh view
//        });
//        builder.setNeutralButton("Close", null);
//        builder.show();
//    }

//

//    // Added 29 June 2025 at about 5.43am to implement double tap logic.
//    private void showBookmarkDialog(int position, Context context, String ref) {
//        CharSequence scriptLine = getItem(position);
//        String playCode = GlobalClass.selectedPlayCode;
//        int act = GlobalClass.selectedActNumber;
//        int scene = GlobalClass.selectedSceneNumber;
//
//        int lineNumber;
//        try {
//            lineNumber = Integer.parseInt(ref);
//        } catch (NumberFormatException e) {
//            lineNumber = -1;
//        }
//
//        DatabaseHandler db = new DatabaseHandler(context.getApplicationContext()) {
//            @Override
//            public void onCreate(SQLiteDatabase db) {
//
//                Log.d("sqllite","on create");
//
//            }
//
//            @Override
//            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//
//            }
//        };
//
////        DatabaseHandler db = new DatabaseHandler(context);
//        List<Bookmark> bookmarks = db.getBookmarksForLine(playCode, act, scene, lineNumber);
//
//        if (bookmarks.isEmpty()) {
//            Toast.makeText(context, "No bookmarks found.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        StringBuilder message = new StringBuilder();
//        for (Bookmark bookmark : bookmarks) {
//            message.append("- ").append(bookmark.getAnnotation()).append("\n");
//        }
//
//        new AlertDialog.Builder(context)
//                .setTitle("Bookmarks for <" + ref + ">")
//                .setMessage(message.toString())
//                .setPositiveButton("OK", null)
//                .show();
//    }

    // replaced 29 June 2025 at about 5.43am while adding the explicit double tap logic
    private void showBookmarkDialog_old(int lastClickedPosition, Context context, String ref) {
        Log.d("bookmark dialog", "Invoked with reference: " + ref);

        // 1. Extract the bookmark count from ref like "<3>"
        String countStr = ref.replaceAll("[<>]", "").trim();
        int count = 0;
        try {
            count = Integer.parseInt(countStr);
        } catch (NumberFormatException e) {
            Log.e("bookmark dialog", "Invalid bookmark reference: " + ref);
            return;
        }

        // 2. Get the script line at the clicked position
        Log.d("bookmark dialog", "Script line not found at position: " + String.valueOf(lastClickedPosition));

//        // ***********************************************************
//        // Work in progress so commented out for now. 28June2025
//        // ***********************************************************
//        ScriptLine line = getItem(lastClickedPosition);
//        if (line == null) {
//            Log.e("bookmark dialog", "Script line not found at position: " + lastClickedPosition);
//            return;
//        }
//
//        // 3. Retrieve the bookmarks for that line
//
////        DatabaseHandler dbHandler = new DatabaseHandler(context);
//        // read from database current act number and scene number with current play code
//        // and assign to the global variables
//        // because this is the starting point for the play navigation
//        DatabaseHandler dbHandler = new DatabaseHandler(context.getApplicationContext()) {
//            @Override
//            public void onCreate(SQLiteDatabase db) {
//
//                Log.d("sqllite","on create");
//
//            }
//
//            @Override
//            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//
//            }
//        };
//
//
//        List<Bookmark> bookmarks = dbHandler.getBookmarksForLine(
//                com.shakespeare.new_app.GlobalClass.selectedPlayCode,
//                com.shakespeare.new_app.GlobalClass.selectedActNumber,
//                com.shakespeare.new_app.GlobalClass.selectedSceneNumber,
//                line.getLineNumber()
//        );
//
//        if (bookmarks == null || bookmarks.isEmpty()) {
//            Toast.makeText(context, "No bookmarks found.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // 4. Build the dialog message
//        StringBuilder message = new StringBuilder();
//        for (Bookmark bookmark : bookmarks) {
//            message.append("• ")
//                    .append(bookmark.getUsername())
//                    .append(": ")
//                    .append(bookmark.getAnnotation())
//                    .append("\n\n");
//        }

        // 5. Show the AlertDialog
        new AlertDialog.Builder(context)
                .setTitle("Bookmarks for this line")
                .setMessage("Message placeholder")
//        // ***********************************************************
//        // Work in progress so commented out for now. 28June2025
//        // ***********************************************************
//                .setMessage(message.toString().trim())
                .setPositiveButton("OK", null)
                .show();
    }

    private List<String> extractBookmarkRefs(CharSequence text) {
        List<String> refs = new ArrayList<>();
        Matcher matcher = Pattern.compile("<(\\d+)>").matcher(text);
        while (matcher.find()) {
            refs.add(matcher.group(1));
        }
        return refs;
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView;
        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.tv_script_line);

            itemView.setOnClickListener(this);


        }

        @Override
        public void onClick(View view) {
            Log.d("recycler view adapter click listener", "1 user clicked a script line");
            if (mClickListener != null) {
                mClickListener.onItemClick(view, getAdapterPosition());
                Log.d("recycler view adapter click listener", "2 user clicked a script line");
            }
        }
    }

    // convenience method for getting data at click position
    CharSequence getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {

        void onItemClick(View view, int position);
    }
}
