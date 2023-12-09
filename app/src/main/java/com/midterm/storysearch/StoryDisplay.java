package com.midterm.storysearch;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

public class StoryDisplay extends AppCompatActivity {

    private TextView storyContentView; // Declare the TextView for story content

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_display);

        Intent intent = getIntent();
        int selectedDocId = intent.getIntExtra("selectedDocId", -1);

        if (selectedDocId != -1) {
            View rootView = findViewById(android.R.id.content);
            initializeViews(rootView, selectedDocId);
        } else {
            // Handle the case where selectedDocId is -1
        }
    }

    private SQLiteDatabase connectToDatabase(Context context) {
        // Open the database
        String dbPath = context.getFilesDir() + "/corpus.db";
        return SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
    }

    private void initializeViews(View rootView, int selectedDocId) {
        TextView nameView = rootView.findViewById(R.id.StoryName);
        storyContentView = rootView.findViewById(R.id.StoryContent);

        // Initialize increase and decrease font size buttons
        ImageButton increaseFontSizeButton = rootView.findViewById(R.id.increaseFontSizeButton);
        ImageButton decreaseFontSizeButton = rootView.findViewById(R.id.decreaseFontSizeButton);

        // Set click listeners for font size buttons
        increaseFontSizeButton.setOnClickListener(v -> increaseFontSize());
        decreaseFontSizeButton.setOnClickListener(v -> decreaseFontSize());

        setStory(this, rootView, selectedDocId);
    }

    private static final float MIN_FONT_SIZE = 35; // Adjust the minimum font size as needed
    private static final float MAX_FONT_SIZE = 100; // Adjust the maximum font size as needed

    private static final float FONT_SCALE_FACTOR = 1.1f; // Adjust the scale factor as needed

    private void increaseFontSize() {
        // Increase the font size gradually
        float currentSize = storyContentView.getTextSize();
        float newSize = currentSize * FONT_SCALE_FACTOR;

        if (newSize <= MAX_FONT_SIZE) {
            storyContentView.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
        } else {
            // Handle the case where the new size is greater than the maximum size
            Toast.makeText(this, "Max font size reached", Toast.LENGTH_SHORT).show();
        }
    }




    private void decreaseFontSize() {
        // Decrease the font size gradually
        float currentSize = storyContentView.getTextSize();
        float newSize = currentSize * 0.9f; // Decrease by 10%

        if (newSize >= MIN_FONT_SIZE) {
            storyContentView.setTextAppearance(android.R.style.TextAppearance_Medium); // Reset to medium size
            storyContentView.setTextSize(newSize / getResources().getDisplayMetrics().density);
        } else {
            // Handle the case where the new size is less than the minimum size
        }
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
                    TextView storyContentView = view.findViewById(R.id.StoryContent);

                    nameView.setText(storyName);

                    // Replace newline characters with HTML line break tags
                    content = content.replace("\n", "<br>");

                    // Use HtmlCompat to handle HTML tags
                    Spanned formattedContent = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY);

                    // Convert the SpannedString to a String
                    String plainTextContent = formattedContent.toString();

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
