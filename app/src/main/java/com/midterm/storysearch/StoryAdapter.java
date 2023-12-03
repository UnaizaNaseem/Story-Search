package com.midterm.storysearch;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class StoryAdapter extends ArrayAdapter<String> {

    public StoryAdapter(Context context, List<String> stories) {
        super(context, R.layout.list_item_story, stories);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_story, parent, false);
        }

        TextView textViewStoryName = convertView.findViewById(R.id.textViewStoryName);
        TextView textViewStartingLine = convertView.findViewById(R.id.textViewStartingLine);

        String story = getItem(position);
        if (story != null) {
            String[] parts = story.split("\n");
            if (parts.length == 2) {
                textViewStoryName.setText(parts[0]);
                textViewStartingLine.setText(parts[1]);
            }
        }

        return convertView;
    }
}
