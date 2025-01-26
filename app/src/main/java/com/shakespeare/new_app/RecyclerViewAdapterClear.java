package com.shakespeare.new_app;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RecyclerViewAdapterClear extends RecyclerView.Adapter {


    private List<String> mData;
    private LayoutInflater mInflater;
    private RecyclerViewAdapterClear.ItemClickListener mClickListener;

    // data is passed into the constructor
    RecyclerViewAdapterClear(Context context, List<String> data) {
        this.mInflater = LayoutInflater.from((Context) context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public RecyclerViewAdapterClear.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_row, parent, false);
        return new RecyclerViewAdapterClear.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

    }

    // binds the data to the TextView in each row
//    @Override
    public void onBindViewHolder(RecyclerViewAdapterClear.ViewHolder holder, int position) {
        String animal = mData.get(position);
        holder.myTextView.setText(animal);
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
/*
            itemView.setOnClickListener(this);

 */
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(RecyclerViewAdapterClear.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}
