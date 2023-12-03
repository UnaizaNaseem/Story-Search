package com.midterm.storysearch;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class StoryDisplay extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_display);

        Intent intent = getIntent();
        int selectedDocId = intent.getIntExtra("selectedDocId", -1);

        if (selectedDocId != -1) {
            setStory(this, findViewById(android.R.id.content), selectedDocId);
        } else {
            // Handle the case when no docId is provided
        }
    }

    private SQLiteDatabase connectToDatabase(Context context) {
        // Open the database
        String dbPath = context.getFilesDir() + "/corpus.db";
        return SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
    }

    private void setStory(Context context, View view, int docId) {
        SQLiteDatabase db = connectToDatabase(context);
        if (db != null) {
            String query = "SELECT name, content FROM documents WHERE id = ?";
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(docId)});
            if (cursor.moveToFirst()) {
                String storyName = cursor.getString(0);
                String content = cursor.getString(1);

                // Check if story name or content is empty
                if (storyName == null || storyName.isEmpty() || content == null || content.isEmpty()) {
                    // Show an error toast
                    Toast.makeText(context, "Error: Empty story name or content", Toast.LENGTH_SHORT).show();

                    // Finish the current activity and return to the previous screen
                    finish();
                    return;
                } else {
                    TextView nameView = view.findViewById(R.id.StoryName);
                    TextView storyContentView = view.findViewById(R.id.StoryContent);
                    nameView.setText(storyName);
                    storyContentView.setText(content);
                }
            }
            cursor.close();
            db.close();
        }
    }

}
