package com.shakespeare.new_app;
// This will be your RecyclerView adapter for displaying characters

//package com.example.database;

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

public class CharacterAdapter extends RecyclerView.Adapter<CharacterAdapter.CharacterViewHolder> {

    private final List<Map<String, String>> characterList;
    private final Context context;

    public CharacterAdapter(Context context, List<Map<String, String>> characterList) {
        this.context = context;
        this.characterList = characterList;
    }

    @NonNull
    @Override
    public CharacterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_character, parent, false);
        return new CharacterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CharacterViewHolder holder, int position) {
        Map<String, String> character = characterList.get(position);

        String characterName = character.get("character_full_name");
        String characterDesc = character.get("character_description");
        String playCode = character.get("play_code");

        // Use play_code to lookup the full play name from strings.xml
        int playResId = context.getResources().getIdentifier(playCode, "string", context.getPackageName());
        String playName = playResId != 0 ? context.getString(playResId) : playCode;

        String displayText = characterName;
        if (!characterDesc.equals("null") && !characterDesc.equals(null) && !characterDesc.isEmpty()) {
            displayText += ", " + characterDesc;
        }
        holder.nameText.setText(displayText);

//        holder.nameText.setText(characterName);
//        holder.descriptionText.setText(characterDesc != null ? characterDesc : "");
//        holder.playText.setText(playName);
    }

    @Override
    public int getItemCount() {
        return characterList.size();
    }

    public static class CharacterViewHolder extends RecyclerView.ViewHolder {
//        TextView nameText, playText;
        TextView nameText;

        public CharacterViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.text_character_display_text);
//            playText = itemView.findViewById(R.id.text_play_name);
        }
    }
}
