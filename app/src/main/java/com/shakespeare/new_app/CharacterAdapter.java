package com.shakespeare.new_app;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.view.ViewGroup.MarginLayoutParams;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.*;
import org.json.JSONObject;

public class CharacterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Map<String, String>> characterList;
    private final Context context;
    private final Handler handler = new Handler();
    private final Map<String, Runnable> pendingUpdates = new HashMap<>();

    private static final int TYPE_GROUP_HEADER = 0;
    private static final int TYPE_CHARACTER = 1;

    public CharacterAdapter(Context context, List<Map<String, String>> characterList) {
        this.context = context;
        this.characterList = characterList;
    }

    @Override
    public int getItemViewType(int position) {
        Map<String, String> item = characterList.get(position);
        return "group".equals(item.get("row_type")) ? TYPE_GROUP_HEADER : TYPE_CHARACTER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_GROUP_HEADER) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_group_header, parent, false);
            return new GroupViewHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_character, parent, false);
            return new CharacterViewHolder(view);
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

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Map<String, String> item = characterList.get(position);
        String rowType = item.get("row_type");

        if (holder instanceof GroupViewHolder) {
            ((GroupViewHolder) holder).groupText.setText(item.get("is_a_group") + ":");
        } else if (holder instanceof CharacterViewHolder) {
            CharacterViewHolder charHolder = (CharacterViewHolder) holder;

            String name = toTitleCase(item.get("character_full_name"));
            String desc = item.get("character_description");
            String belongsToGroup = item.get("belongs_to_group");

            String displayText = name;
            if ((belongsToGroup == null || !belongsToGroup.equals("true")) && desc != null && !desc.equals("null") && !desc.isEmpty()) {
                displayText += ", " + desc;
            }

            charHolder.nameText.setText(displayText);

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) charHolder.nameText.getLayoutParams();
            if ("true".equals(belongsToGroup)) {
                params.setMarginStart(60);
            } else {
                params.setMarginStart(0);
            }
            charHolder.nameText.setLayoutParams(params);

            // Handle checkbox and other logic (already working)
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

    @Override
    public int getItemCount() {
        return characterList.size();
    }

//    public static List<Map<String, String>> structureGroupedList(List<Map<String, String>> rows) {
//        List<Map<String, String>> structuredRows = new java.util.ArrayList<>();
//
//        String currentGroup = null;
//
//        for (Map<String, String> row : rows) {
//            String groupName = row.get("is_a_group");
//
//            // If a group header is encountered
//            if (groupName != null && !groupName.equals("null") && !groupName.isEmpty()) {
//                Map<String, String> groupRow = new java.util.HashMap<>();
//                groupRow.put("row_type", "group");
//                groupRow.put("is_a_group", groupName);
//                structuredRows.add(groupRow);
//
//                currentGroup = groupName;
//            }
//
//            // Regular character row
//            Map<String, String> charRow = new java.util.HashMap<>(row);
//            charRow.put("row_type", "character");
//
//            // If inside a group, mark character as belonging to a group
//            if (currentGroup != null) {
//                charRow.put("belongs_to_group", "true");
//            }
//
//            structuredRows.add(charRow);
//
//            // If character is not part of any group, reset current group
//            if (groupName == null || groupName.equals("null") || groupName.isEmpty()) {
//                currentGroup = null;
//            }
//        }
//
//        return structuredRows;
//    }

    //    private List<Map<String, String>> structureGroupedList(List<Map<String, String>> rows) {
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

    public static class CharacterViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        CheckBox selectCheckBox;
        public CharacterViewHolder(@NonNull View itemView) {
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
