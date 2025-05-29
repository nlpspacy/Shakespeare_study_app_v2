package com.shakespeare.new_app;

import android.content.Context;
import android.graphics.Rect;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerBookmarksClickListener implements RecyclerView.OnItemTouchListener {

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    private final OnItemClickListener listener;
    private final GestureDetector gestureDetector;

    public RecyclerBookmarksClickListener(Context context, final RecyclerView recyclerView, OnItemClickListener listener) {
        this.listener = listener;

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        View child = rv.findChildViewUnder(e.getX(), e.getY());

        if (child != null && gestureDetector.onTouchEvent(e)) {
            View checkbox = child.findViewById(R.id.shareCheckbox);
            if (checkbox != null) {
                Rect rect = new Rect();
                checkbox.getHitRect(rect);
                rect.offset(child.getLeft(), child.getTop());

                if (rect.contains((int) e.getX(), (int) e.getY())) {
                    // Click was on checkbox — don’t trigger item click
                    return false;
                }
            }

            listener.onItemClick(child, rv.getChildAdapterPosition(child));
            return true;
        }
        return false;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        // Not used
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        // Not used
    }
}
