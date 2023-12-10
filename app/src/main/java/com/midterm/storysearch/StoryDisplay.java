package com.midterm.storysearch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import java.util.ArrayList;
import java.util.List;

public class StoryDisplay extends AppCompatActivity {

    private static final float MIN_FONT_SIZE = 35;
    private static final float MAX_FONT_SIZE = 100;
    private static final float FONT_SCALE_FACTOR = 1.1f;
    private static final String PREF_FILE_NAME = "LikedStoriesPrefs";
    private static final String LIKED_STORIES_KEY = "likedStories";
    private TextView storyContentView;
    private int selectedDocId;
    private boolean isLiked;
    private ImageButton starButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_display);

        Intent intent = getIntent();
        selectedDocId = intent.getIntExtra("selectedDocId", -1);

        if (selectedDocId != -1) {
            View rootView = findViewById(android.R.id.content);
            initializeViews(rootView);
            setStory(this, rootView, selectedDocId);
        } else {

        }
    }


    private boolean isStoryLiked(Context context, int docId) {
        List<Integer> likedStories = getLikedStories(context);
        return likedStories.contains(docId);
    }

    private void addLikedStory(Context context, int docId) {
        List<Integer> likedStories = getLikedStories(context);
        likedStories.add(docId);
        saveLikedStories(context, likedStories);
    }

    private void removeLikedStory(Context context, int docId) {
        List<Integer> likedStories = getLikedStories(context);
        likedStories.remove(Integer.valueOf(docId));
        saveLikedStories(context, likedStories);
    }

    private List<Integer> getLikedStories(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        String likedStoriesString = prefs.getString(LIKED_STORIES_KEY, "");
        String[] likedStoriesArray = likedStoriesString.split(",");
        List<Integer> likedStories = new ArrayList<>();
        for (String storyId : likedStoriesArray) {
            if (!storyId.isEmpty()) {
                likedStories.add(Integer.parseInt(storyId));
            }
        }
        return likedStories;
    }

    private void saveLikedStories(Context context, List<Integer> likedStories) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        StringBuilder likedStoriesString = new StringBuilder();
        for (Integer storyId : likedStories) {
            likedStoriesString.append(storyId).append(",");
        }
        prefs.edit().putString(LIKED_STORIES_KEY, likedStoriesString.toString()).apply();
    }


    private void initializeViews(View rootView) {
        TextView nameView = rootView.findViewById(R.id.StoryName);
        storyContentView = rootView.findViewById(R.id.StoryContent);


        ImageButton increaseFontSizeButton = rootView.findViewById(R.id.increaseFontSizeButton);
        ImageButton decreaseFontSizeButton = rootView.findViewById(R.id.decreaseFontSizeButton);


        starButton = rootView.findViewById(R.id.likeButton);
        starButton.setVisibility(View.VISIBLE);
        updateStarButtonAppearance();


        starButton.setOnClickListener(v -> toggleLikeStatus());


        increaseFontSizeButton.setOnClickListener(v -> increaseFontSize());
        decreaseFontSizeButton.setOnClickListener(v -> decreaseFontSize());


        isLiked = isStoryLiked(this, selectedDocId);
        updateLikeButtonState(starButton);
    }


    private void updateStarButtonAppearance() {
        if (isLiked) {

            starButton.setImageResource(R.drawable.ic_star_filled);
        } else {

            starButton.setImageResource(R.drawable.ic_star_outline);
        }
    }

    private void toggleLikeStatus() {
        toggleLike();
        updateStarButtonAppearance();
    }

    private void toggleLike() {
        isLiked = !isLiked;


        updateLikeButtonState(starButton);


        if (isLiked) {
            addLikedStory(this, selectedDocId);
        } else {
            removeLikedStory(this, selectedDocId);
        }
    }

    private void updateLikeButtonState(ImageButton likeButton) {
        int likeIcon = isLiked ? R.drawable.ic_star_filled : R.drawable.ic_star_outline;
        likeButton.setImageResource(likeIcon);
    }

    private SQLiteDatabase connectToDatabase(Context context) {

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

                } else {
                    TextView nameView = view.findViewById(R.id.StoryName);
                    TextView storyContentView = view.findViewById(R.id.StoryContent);

                    nameView.setText(storyName);


                    content = content.replace("\n", "<br>");


                    Spanned formattedContent = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY);


                    String plainTextContent = formattedContent.toString();

                    storyContentView.setText(plainTextContent);
                }
            } else {

            }
            cursor.close();
            db.close();
        } else {

        }
    }


    private void increaseFontSize() {

        float currentSize = storyContentView.getTextSize();
        float newSize = currentSize * FONT_SCALE_FACTOR;

        if (newSize <= MAX_FONT_SIZE) {
            storyContentView.setTextSize(TypedValue.COMPLEX_UNIT_PX, newSize);
        } else {

        }
    }

    private void decreaseFontSize() {

        float currentSize = storyContentView.getTextSize();
        float newSize = currentSize * 0.9f;

        if (newSize >= MIN_FONT_SIZE) {
            storyContentView.setTextAppearance(android.R.style.TextAppearance_Medium);
            storyContentView.setTextSize(newSize / getResources().getDisplayMetrics().density);
        } else {

        }
    }
}
