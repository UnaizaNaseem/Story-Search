package com.midterm.storysearch;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import java.util.List;

import me.biubiubiu.justifytext.library.JustifyTextView;

public class StoryDisplay extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_display);

        Intent intent = getIntent();
        int selectedDocId = intent.getIntExtra("selectedDocId", -1);

        if (selectedDocId != -1) {
            View rootView = findViewById(android.R.id.content);
            setStory(this, rootView, selectedDocId);
        } else {

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

                if (storyName == null || storyName.isEmpty() || content == null || content.isEmpty()) {
                    // Handle the case where storyName or content is empty
                } else {
                    TextView nameView = view.findViewById(R.id.StoryName);
                    JustifyTextView storyContentView = view.findViewById(R.id.StoryContent);

                    // Replace newline characters with HTML line break tags
                    content = content.replace("\n", "<br>");

                    // Use HtmlCompat to handle HTML tags
                    Spanned formattedContent = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY);

                    // Convert the SpannedString to a String
                    String plainTextContent = formattedContent.toString();

                    nameView.setText(storyName);
                    storyContentView.setText(plainTextContent);
                }
            } else {
                // Handle the case where the cursor doesn't move to the first position
            }
            cursor.close();
            db.close();
        } else {
            // Handle the case where the database is null
        }
    }

}
