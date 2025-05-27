package com.shakespeare.new_app;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.database.RemoteDatabaseHelperHttp;

import java.util.List;

public class BookmarkEntryAdapter extends RecyclerView.Adapter<BookmarkEntryAdapter.ViewHolder> {

    private static final int IDX_BOOKMARK_ROW_ID = 0;
    private static final int IDX_PLAY_CODE = 1;
    private static final int IDX_PLAY_FULL_NAME = 2;
    private static final int IDX_ACT_NR = 3;
    private static final int IDX_SCENE_NR = 4;
    private static final int IDX_SCRIPT_TEXT = 5;
    private static final int IDX_ANNOTATION = 6;
    private static final int IDX_USERNAME = 7;
    private static final int IDX_SHARE_WITH_OTHERS = 8;


    private final List<CharSequence> bookmarksList;
    private final List<List<String>> bookmarkEntriesList;
    private final Context context;

    public BookmarkEntryAdapter(Context context, List<CharSequence> bookmarksList, List<List<String>> bookmarkEntriesList) {
        this.context = context;
        this.bookmarksList = bookmarksList;
        this.bookmarkEntriesList = bookmarkEntriesList;
    }

    @NonNull
    @Override
    public BookmarkEntryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bookmark_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarkEntryAdapter.ViewHolder holder, int position) {
        CharSequence entryHtml = bookmarksList.get(position);
        holder.bookmarkTextView.setText(entryHtml);

        if (position >= bookmarkEntriesList.size()) {
            holder.shareCheckbox.setVisibility(View.GONE);
            return;
        }

        List<String> entryData = bookmarkEntriesList.get(position);
        String ownerUsername = entryData.get(IDX_USERNAME); // username
        String currentUser = UserManager.getUsername(context);

        if (ownerUsername.equals(currentUser)) {
            holder.shareCheckbox.setVisibility(View.VISIBLE);
            boolean isShared = "1".equals(entryData.get(IDX_SHARE_WITH_OTHERS)); // share_with_others column

            holder.shareCheckbox.setChecked(isShared);

            int bookmarkId = Integer.parseInt(entryData.get(IDX_BOOKMARK_ROW_ID));
            holder.shareCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updateShareStatus(bookmarkId, isChecked);
            });

        } else {
            holder.shareCheckbox.setVisibility(View.GONE);
        }
    }

    public CharSequence getItem(int position) {
        return bookmarksList.get(position);
    }

    public List<String> getBookmarkEntry(int position) {
        return bookmarkEntriesList.get(position);
    }

    private void updateShareStatus(int bookmarkId, boolean isShared) {
        int shareFlag = isShared ? 1 : 0;
        String sql = "UPDATE bookmark SET share_with_others = " + shareFlag +
                " WHERE bookmark_row_id = " + bookmarkId;

        RemoteDatabaseHelperHttp db = new RemoteDatabaseHelperHttp();
        db.runInsert(sql, new RemoteDatabaseHelperHttp.InsertCallback() {
            @Override
            public void onInsertSuccess() {
                Log.d("BookmarkEntryAdapter", "Share status updated for ID: " + bookmarkId);
            }

            @Override
            public void onInsertFailure(Throwable e) {
                Log.e("BookmarkEntryAdapter", "Failed to update share status", e);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookmarksList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView bookmarkTextView;
        CheckBox shareCheckbox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bookmarkTextView = itemView.findViewById(R.id.bookmarkTextView);
            shareCheckbox = itemView.findViewById(R.id.shareCheckbox);
        }
    }
}