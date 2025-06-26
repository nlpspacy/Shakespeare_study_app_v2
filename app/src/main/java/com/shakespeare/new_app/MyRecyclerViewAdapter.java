package com.shakespeare.new_app;

import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

//import com.example.new_app.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
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
        } else {
            SpannableStringBuilder spannable;

            if (strContentAsString.endsWith("+")) {
                strContentAsString = strContentAsString.substring(0, intContentLength - 1);
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
                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        Log.d("click response","bookmark reference click response");
                        showBookmarkDialog(widget.getContext(), ref);
                    }
                };
                spannable.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            holder.myTextView.setText(spannable);
            holder.myTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }


        if (strContentAsString.endsWith("+")) {
            strContent = strContentAsString.substring(0, intContentLength - 1);
            holder.myTextView.setText(Html.fromHtml(strContent.toString(), Html.FROM_HTML_MODE_LEGACY));
            holder.myTextView.setTypeface(null, Typeface.BOLD);
        } else if (strContent.equals("Characters in the Play")) {
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

    private void showBookmarkDialog(Context context, String refNumber) {
        // You can look up the bookmark by reference number here
        String fullText = "Bookmark text for reference <" + refNumber + "> (placeholder)";

        Log.d("bookmark reference",fullText);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Bookmark <" + refNumber + ">");
        builder.setMessage(fullText);
        builder.setPositiveButton("Edit", (dialog, which) -> {
            // TODO: Launch edit screen for this bookmark
        });
        builder.setNegativeButton("Delete", (dialog, which) -> {
            // TODO: Delete the bookmark and refresh view
        });
        builder.setNeutralButton("Close", null);
        builder.show();
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
