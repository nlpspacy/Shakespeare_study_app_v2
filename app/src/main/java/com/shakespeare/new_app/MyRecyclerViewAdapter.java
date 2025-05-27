package com.shakespeare.new_app;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

//import com.example.new_app.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    private List<CharSequence> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private OnClickListener onClickListener;

    private boolean contentIsPreStyled;


    // data is passed into the constructor
    MyRecyclerViewAdapter(Context context, ArrayList<CharSequence> data, boolean contentIsPreStyled) {
        this.mInflater = LayoutInflater.from((Context) context);
        this.mData = data;
        this.contentIsPreStyled = contentIsPreStyled;
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

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Boolean boolSpeakThisLine;
        boolSpeakThisLine = Boolean.TRUE;

        holder.itemView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        // using https://www.geeksforgeeks.org/how-to-apply-onclicklistener-to-recyclerview-items-in-android/
        holder.itemView.setOnClickListener(view -> {
            if (onClickListener != null) {
                onClickListener.onClick(position);
                Log.d("click check", "script line clicked");
            }
        });

        CharSequence strContent = mData.get(position);
        String strContentAsString = strContent.toString();
        String strSpokenText;
        Integer intContentLength = strContent.length();
//        Log.d("show position","ViewHolder position: " + String.valueOf(position) + ". Content length: " + String.valueOf(strContent.length()));
        //        String strContentNew;
//        Log.d("character text", String.valueOf(intContentLength) + ": " + strContent.substring(intContentLength-1, intContentLength));

        // insert here an alternating hidden text RecyclerView item giving
        // the location of the script line in the play.
//        holder.myTextView.setText("play, act, scene and play_line_nr reference");
//        holder.myTextView.setVisibility(View.GONE);

        // We do not want to show a character name for stage instructions or scene information.
        if (strContent.equals("N.A.+")) {
            strContent = "Stage direction+";
        }
//        holder.myTextView.setText(strContent);
//        Log.d("strContent", "strContent: "+ strContent);

        // This produces the following error:
        // E  FATAL EXCEPTION: main
        // Process: com.shakespeare.new_app, PID: 8986
        // java.lang.ClassCastException: android.text.SpannableStringBuilder cannot be cast to java.lang.String
        // at com.shakespeare.new_app.MyRecyclerViewAdapter.onBindViewHolder(MyRecyclerViewAdapter.java:88)
        //        CharSequence styled = Html.fromHtml((String) strContent, Html.FROM_HTML_MODE_LEGACY);

//        // This styles the play script in HTML but ignores the HTML tags for the bookmarks.
//        CharSequence styled = Html.fromHtml(strContent.toString(), Html.FROM_HTML_MODE_LEGACY);
//
//        // This styles the bookmarks in HTML but not the play script and shows the <font> tags in HTML
//        // but doesn't use the for styling.
//        CharSequence styled = strContent; // already styled, no need to re-parse
//
//        holder.myTextView.setText(styled);
        if (contentIsPreStyled) {
            // Bookmarks: already styled
            holder.myTextView.setText(strContent);
        } else {
            // Play script: still needs HTML parsed
            holder.myTextView.setText(Html.fromHtml(strContent.toString(), Html.FROM_HTML_MODE_LEGACY));
        }

        if (strContentAsString.substring(intContentLength - 1, intContentLength).equals("+")) {
            // If it is a character marker as indicated by the plus sign (+) at the end of the character name,
            // then re-assign the holder without the plus (+) sign at the end of the character name.
            strContent = strContentAsString.substring(0, intContentLength - 1);

//            // 27 May 2025: convert back to html after applying string processing function
//            styled = Html.fromHtml((String) strContent, Html.FROM_HTML_MODE_LEGACY);
//
//            holder.myTextView.setText(strContent);
//            holder.myTextView.setText(styled);

            if (contentIsPreStyled) {
                // Bookmarks: already styled
                holder.myTextView.setText(strContent);
            } else {
                // Play script: still needs HTML parsed
                holder.myTextView.setText(Html.fromHtml(strContent.toString(), Html.FROM_HTML_MODE_LEGACY));
            }

            holder.myTextView.setTypeface(null, Typeface.BOLD);
        } else if (strContent.equals("Characters in the Play")) {
            holder.myTextView.setTypeface(null, Typeface.BOLD);

        } else {
            holder.myTextView.setTypeface(null, Typeface.NORMAL);

        }
//            Log.d("update onBindViewHolder", position + " " + strContent);
        holder.myTextView.setTextIsSelectable(true);

        // ensure font size is set to the global variable
        holder.myTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, com.shakespeare.new_app.GlobalClass.fontsizesp);
        holder.myTextView.setVisibility(View.VISIBLE);
//        Log.d("hide line","show line: " + strContent);
//        holder.myTextView.setMaxHeight(20);
        holder.myTextView.setLineSpacing(1, 1);

        // Check whether the line is longer than 12 characters otherwise
        // the check for the substring to indicate it's a reference line will
        // throw an error.
        if (strContent.length() > 12) {
            // if it is a reference line then hide it
            if (strContentAsString.substring(0, 10).equals("play_code:")) {

                // We do not want to speak aloud the reference information.
                boolSpeakThisLine = Boolean.FALSE;

                // Put reference information in a hidden line.
                holder.itemView.setLayoutParams(new ViewGroup.LayoutParams(0, 0));
//                Log.d("hide line","hide line: " + strContent);
                holder.myTextView.setTextIsSelectable(false);
                holder.myTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 0);
                holder.myTextView.setVisibility(View.GONE);
//                holder.myTextView.setMaxHeight(1);
                holder.myTextView.setLineSpacing(-3, -3);
//                holder.myTextView.setTextIsSelectable(true);
//                holder.myTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
            }
        }

        // If this line is a spoken line, check whether the user has turned speech on.
        if (boolSpeakThisLine.equals(Boolean.TRUE)) {
            strSpokenText = strContentAsString;

            if (com.shakespeare.new_app.GlobalClass.boolSoundOn.equals(Boolean.TRUE)) {
                if (com.shakespeare.new_app.GlobalClass.scriptSceneLineNr != 0 &&
                        com.shakespeare.new_app.GlobalClass.selectedSceneNumber != 0 &&
                        !strContentAsString.contains("[") && strContentAsString.contains(" ")) {
                    strSpokenText = strContentAsString.substring(strContentAsString.indexOf(" "));
                }

                MyApplication.setLanguage(Locale.ENGLISH);

//                String sceneKey = com.shakespeare.new_app.GlobalClass.selectedPlayCode + "_" +
//                        com.shakespeare.new_app.GlobalClass.selectedActNumber + "_" +
//                        com.shakespeare.new_app.GlobalClass.selectedSceneNumber;
//
//                VoiceSynthesizer.synthesizeAndPlay(mInflater.getContext(), strSpokenText, "nova", sceneKey);
//                Log.d("strSpokenText", strSpokenText);

                // 🧠 Only speak if the current line's scene matches the active scene
                String currentSceneKey = com.shakespeare.new_app.GlobalClass.selectedPlayCode + "_" +
                        com.shakespeare.new_app.GlobalClass.selectedActNumber + "_" +
                        com.shakespeare.new_app.GlobalClass.selectedSceneNumber;

//                Log.d("VoiceSynth", "currentSceneKey:" + currentSceneKey + "; isSceneActive(currentSceneKey): " + String.valueOf(VoiceSynthesizer.isSceneActive(currentSceneKey)));

                String sceneKey = GlobalClass.selectedPlayCode + "_" +
                        GlobalClass.selectedActNumber + "_" +
                        GlobalClass.selectedSceneNumber;

                VoiceSynthesizer.prepareScenePlayback(sceneKey);

                VoiceSynthesizer.synthesizeAndPlay(mInflater.getContext(), strSpokenText, "nova", sceneKey);

//                if (VoiceSynthesizer.isSceneActive(currentSceneKey)) {
//                    VoiceSynthesizer.synthesizeAndPlay(mInflater.getContext(), strSpokenText, "nova", currentSceneKey);
//                    Log.d("VoiceSynth", "Speaking: " + strSpokenText);
//                } else {
//                    Log.d("VoiceSynth", "Skipping line due to scene mismatch: " + strSpokenText);
//                }


            }
            }

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
            if (mClickListener != null) {
                mClickListener.onItemClick(view, getAdapterPosition());
                Log.d("recycler view adapter click listener", "user clicked a script line");
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
