package com.shakespeare.new_app;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class CharacterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Map<String, String>> characterList;
    private final Context context;

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

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Map<String, String> item = characterList.get(position);
        String rowType = item.get("row_type");
        Log.d("AdapterBind", "row_type: " + (rowType != null ? rowType : "null") + ", position: " + position);

        if (holder instanceof GroupViewHolder) {
            ((GroupViewHolder) holder).groupText.setText(item.get("is_a_group") + ":");
        } else if (holder instanceof CharacterViewHolder) {
            String name = toTitleCase(item.get("character_full_name"));
            String desc = toTitleCase(item.get("character_description"));
            String inGroup = item.get("is_a_group");
            String belongsToGroup = item.get("belongs_to_group");

            String displayText = name;

            Log.d("grouping",name + " belongsToGroup: <" + belongsToGroup + ">"  + " inGroup: <" + inGroup + ">" + " row type: " + rowType);

            // Only add description if not part of a group
            if ((belongsToGroup == null || !belongsToGroup.equals("true")) && desc != null && !desc.equals("null") && !desc.isEmpty()) {
                displayText += ", " + desc;
            }
//            if ((inGroup == null || inGroup.equals("null") || inGroup.isEmpty()) && desc != null && !desc.equals("null") && !desc.isEmpty()) {
//                displayText += ", " + desc;
//            }

            ((CharacterViewHolder) holder).nameText.setText(displayText);

            // Indent if character belongs to a group, and otherwise do not indent.
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) ((CharacterViewHolder) holder).nameText.getLayoutParams();
            if ("true".equals(belongsToGroup)) {
                params.setMarginStart(60); // Adjust indent value as needed
            } else {
                params.setMarginStart(0);
            }
            ((CharacterViewHolder) holder).nameText.setLayoutParams(params);
        }
    }

    @Override
    public int getItemCount() {
        return characterList.size();
    }

    public static class CharacterViewHolder extends RecyclerView.ViewHolder {
        TextView nameText;
        public CharacterViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.text_character_display_text);
        }
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView groupText;
        public GroupViewHolder(@NonNull View itemView) {
            super(itemView);
            groupText = itemView.findViewById(R.id.text_group_heading);
        }
    }

    private String toTitleCase(String input) {
        if (input == null || input.isEmpty()) return input;
        StringBuilder result = new StringBuilder();
        for (String word : input.split(" ")) {
            if (word.length() > 0) {
                result.append(Character.toUpperCase(word.charAt(0)));
                if (word.length() > 1) {
                    result.append(word.substring(1).toLowerCase());
                }
                result.append(" ");
            }
        }
        return result.toString().trim();
    }

}
