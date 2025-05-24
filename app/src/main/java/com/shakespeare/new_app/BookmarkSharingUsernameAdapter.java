package com.shakespeare.new_app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BookmarkSharingUsernameAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private List<Map<String, String>> usernameList;
    private Context context;
    private final Handler handler = new Handler();
    private final Map<String, Runnable> pendingUpdates = new HashMap<>();

    private static final int TYPE_GROUP_HEADER = 0;
    private static final int TYPE_CHARACTER = 1;

    public BookmarkSharingUsernameAdapter(Context context, List<Map<String, String>> usernameList) {
        this.context = context;
        this.usernameList = usernameList;
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_CHARACTER;
    }

    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_GROUP_HEADER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_group_header, parent, false);
            return new BookmarkSharingUsernameAdapter.GroupViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_character, parent, false);
            return new BookmarkSharingUsernameAdapter.BookmarkSharingUsernameViewHolder(view);
        }
    }

    private String toTitleCase(String input) {
        if (input == null || input.isEmpty()) return input;
        StringBuilder result = new StringBuilder();
        for (String word : input.split(" ")) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)));
                result.append(word.length() > 1 ? word.substring(1).toLowerCase() : "");
                result.append(" ");
            }
        }
        return result.toString().trim();
    }

    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Map<String, String> item = usernameList.get(position);
        String rowType = item.get("row_type");

        if (holder instanceof BookmarkSharingUsernameAdapter.GroupViewHolder) {
            ((BookmarkSharingUsernameAdapter.GroupViewHolder) holder).groupText.setText(item.get("is_a_group") + ":");
        } else if (holder instanceof BookmarkSharingUsernameAdapter.BookmarkSharingUsernameViewHolder) {
            BookmarkSharingUsernameAdapter.BookmarkSharingUsernameViewHolder bookmarkSharingUsernameViewHolder = (BookmarkSharingUsernameAdapter.BookmarkSharingUsernameViewHolder) holder;

            String username = item.get("username");
            String count = item.get("number_of_bookmarks");

            String displayText = username + " (" + count + " bookmarks)";
            bookmarkSharingUsernameViewHolder.nameText.setText(displayText);

//            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) bookmarkSharingUsernameViewHolder.nameText.getLayoutParams();
//            if ("true".equals(belongsToGroup)) {
//                params.setMarginStart(60);
//            } else {
//                params.setMarginStart(0);
//            }
//            bookmarkSharingUsernameViewHolder.nameText.setLayoutParams(params);

            final String finalDisplayText =displayText;

            // Handle checkbox and other logic (already working)

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            Set<String> selectedUsers = prefs.getStringSet("visible_bookmark_users", new HashSet<>());
            String thisUsername = item.get("username");

            bookmarkSharingUsernameViewHolder.selectCheckBox.setOnCheckedChangeListener(null); // Prevent infinite loop
            bookmarkSharingUsernameViewHolder.selectCheckBox.setChecked(selectedUsers.contains(thisUsername));

            // we have now added more logic relating to the checkbox.
            bookmarkSharingUsernameViewHolder.selectCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String selectedUsername = item.get("username");

                // ✅ Safe to declare locally here
                SharedPreferences localprefs = PreferenceManager.getDefaultSharedPreferences(context);
                Set<String> currentSet = localprefs.getStringSet("visible_bookmark_users", new HashSet<>());
                Set<String> newSet = new HashSet<>(currentSet);

                if (isChecked) {
                    newSet.add(selectedUsername);
                } else {
                    newSet.remove(selectedUsername);
                }

                localprefs.edit().putStringSet("visible_bookmark_users", newSet).apply();
            });

        }
    }


    private void sendDebouncedUpdate(String username, String characterName, boolean isUser) {
        String key = username + ":" + characterName;

        Runnable pending = pendingUpdates.get(key);
        if (pending != null) {
            handler.removeCallbacks(pending);
        }

        Runnable updateTask = () -> sendUpdateToServer(username, characterName, isUser);
        pendingUpdates.put(key, updateTask);
        handler.postDelayed(updateTask, 500); // 500ms debounce
    }

    private void sendUpdateToServer(String username, String characterName, boolean isUser) {
        try {
            OkHttpClient client = new OkHttpClient();
            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("character_full_name", characterName);
            json.put("is_user", isUser);

            RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url("https://android-sqlitecloud-api-production.up.railway.app/update_selection")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e("UpdateServer", "Failed to update server", e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        Log.e("UpdateServer", "Server error: " + response.code());
                    } else {
                        Log.d("UpdateServer", "Update successful");
                    }
                }
            });
        } catch (Exception e) {
            Log.e("UpdateServer", "Exception", e);
        }
    }

    public int getItemCount() {
        return usernameList.size();
    }

    public static List<Map<String, String>> structureGroupedList(List<Map<String, String>> rows) {
        List<Map<String, String>> structuredList = new ArrayList<>();
        Set<String> insertedGroups = new HashSet<>();

        for (Map<String, String> row : rows) {
            String groupName = row.get("is_a_group");

            if (groupName != null && !groupName.isEmpty() && !groupName.equals("null") && !insertedGroups.contains(groupName)) {
                Map<String, String> groupRow = new HashMap<>();
                groupRow.put("row_type", "group");
                groupRow.put("is_a_group", groupName);
                structuredList.add(groupRow);

                insertedGroups.add(groupName);
                Log.d("GroupStructure", "Inserted group: " + groupName);
            }

            // Always insert the character row
            Map<String, String> characterRow = new HashMap<>(row);
            characterRow.put("row_type", "character");

            // Mark whether the character belongs to a group
            if (groupName != null && !groupName.isEmpty() && !groupName.equals("null")) {
                characterRow.put("belongs_to_group", "true");
            } else {
                characterRow.put("belongs_to_group", "false");
            }

            structuredList.add(characterRow);
            Log.d("GroupStructure", "Inserted character: " + characterRow.get("character_full_name") +
                    " belongs_to_group=" + characterRow.get("belongs_to_group"));
        }

        return structuredList;
    }

    public static class BookmarkSharingUsernameViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        CheckBox selectCheckBox;
        public BookmarkSharingUsernameViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.text_character_display_text);
            selectCheckBox = itemView.findViewById(R.id.checkbox_character_select);
        }
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView groupText;
        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            groupText = itemView.findViewById(R.id.text_group_heading);
        }
    }
}
