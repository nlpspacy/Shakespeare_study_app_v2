package com.shakespeare.new_app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.shakespeare.new_app.R;

import java.util.List;
import java.util.Map;

public class QueryResultAdapter extends RecyclerView.Adapter<QueryResultAdapter.ViewHolder> {

    private final List<Map<String, String>> data;

    public QueryResultAdapter(List<Map<String, String>> data) {
        this.data = data;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView lineNumber, scriptText, character;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            lineNumber = itemView.findViewById(R.id.line_number);
            scriptText = itemView.findViewById(R.id.script_text);
            character = itemView.findViewById(R.id.character_name);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.query_result_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, String> row = data.get(position);
        holder.lineNumber.setText("Line: " + row.get("scene_line_number"));
        holder.scriptText.setText(row.get("script_text"));
        holder.character.setText("Character: " + row.get("character_short_name"));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
