package com.shakespeare.new_app;

import static androidx.test.core.app.ApplicationProvider.*;

import android.content.Context;
import android.graphics.Typeface;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.InstrumentationRegistry;

//import com.example.new_app.R;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    private List<String> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private OnClickListener onClickListener;

    // data is passed into the constructor
    MyRecyclerViewAdapter(Context context, List<String> data) {
        this.mInflater = LayoutInflater.from((Context) context);
        this.mData = data;
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

        String strContent = mData.get(position);
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
        holder.myTextView.setText(strContent);

        if (strContent.substring(intContentLength - 1, intContentLength).equals("+")) {
            // If it is a character marker as indicated by the plus sign (+) at the end of the character name,
            // then re-assign the holder without the plus (+) sign at the end of the character name.
            strContent = strContent.substring(0, intContentLength - 1);
            holder.myTextView.setText(strContent);
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
            if (strContent.substring(0, 10).equals("play_code:")) {

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

            strSpokenText = strContent;

            // If the user has opted for text to speech, then speak the line out loud.
            if (com.shakespeare.new_app.GlobalClass.boolSoundOn.equals(Boolean.TRUE)) {

                // If there are line numbers, then remove the line number reference
                // from the spoken text.
                if (com.shakespeare.new_app.GlobalClass.scriptSceneLineNr!=0) {
                    if (com.shakespeare.new_app.GlobalClass.selectedSceneNumber != 0) {
                        if (!strContent.contains("[") && strContent.contains(" ")) {
                            strSpokenText = strContent.substring(strContent.indexOf(" "), strContent.length());
                        } else {
                            strSpokenText = strContent;
                        }
                    } else {
                        strSpokenText = strContent;
                    }
                }

                MyApplication.setLanguage(Locale.ENGLISH);
//                MyApplication.textToSpeech.speak(strSpokenText, TextToSpeech.QUEUE_ADD, null,
//                        UUID.randomUUID().toString());
//                VoiceSynthesizer.synthesizeAndPlay(this, strSpokenText, "nova");
//                VoiceSynthesizer.synthesizeAndPlay(mInflater.getContext(), strSpokenText, "nova");
                int currentGen = VoiceSynthesizer.getCurrentGeneration();
                VoiceSynthesizer.synthesizeAndPlay(mInflater.getContext(), strSpokenText, "nova", currentGen);
                Log.d("strSpokenText", strSpokenText);

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
            myTextView = itemView.findViewById(R.id.tvAnimalName);

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
    String getItem(int id) {
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
