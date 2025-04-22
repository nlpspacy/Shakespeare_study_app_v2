// This will be your RecyclerView adapter for displaying characters and groups
package com.shakespeare.new_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shakespeare.new_app.R;

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

        if (holder instanceof GroupViewHolder) {
            ((GroupViewHolder) holder).groupText.setText(item.get("is_a_group") + ":");
        } else if (holder instanceof CharacterViewHolder) {
            String name = item.get("character_full_name");
            String desc = item.get("character_description");
            String displayText = name;
            if (desc != null && !desc.equals("null") && !desc.isEmpty()) {
                displayText += ", " + desc;
            }
            ((CharacterViewHolder) holder).nameText.setText(displayText);
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
}
