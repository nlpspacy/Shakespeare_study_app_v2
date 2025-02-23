package com.shakespeare.new_app;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

//import com.example.new_app.R;
import com.shakespeare.new_app.R;

import java.util.List;

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

        // using https://www.geeksforgeeks.org/how-to-apply-onclicklistener-to-recyclerview-items-in-android/
        holder.itemView.setOnClickListener(view -> {
                    if (onClickListener != null) {
                        onClickListener.onClick(position);
                        Log.d("click check","script line clicked");
                    }
                });

        String strContent = mData.get(position);
        Integer intContentLength = strContent.length();
        Log.d("show position","ViewHolder position: " + String.valueOf(position) + ". Content length: " + String.valueOf(strContent.length()));
        //        String strContentNew;
//        Log.d("character text", String.valueOf(intContentLength) + ": " + strContent.substring(intContentLength-1, intContentLength));

        // insert here an alternating hidden text RecyclerView item giving
        // the location of the script line in the play.

        // We do not want to show a character name for stage instructions or scene information.
        if(strContent.equals("N.A.+")) {
            strContent = "Stage direction+";
        }
            holder.myTextView.setText(strContent);
//        holder.myTextView.setVisibility(View.GONE);

            if(strContent.substring(intContentLength-1, intContentLength).equals("+")){
                // If it is a character marker as indicated by the plus sign (+) at the end of the character name,
                // then re-assign the holder without the plus (+) sign at the end of the character name.
                strContent = strContent.substring(0, intContentLength-1);
                holder.myTextView.setText(strContent);
                holder.myTextView.setTypeface(null, Typeface.BOLD);
            } else {
                holder.myTextView.setTypeface(null, Typeface.NORMAL);

            }
//            Log.d("update onBindViewHolder", position + " " + strContent);
            holder.myTextView.setTextIsSelectable(true);

            // ensure font size is set to the global variable
            holder.myTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, com.shakespeare.new_app.GlobalClass.fontsizesp);



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
                Log.d("recycler view adapter click listener","user clicked a script line");
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
