package com.midterm.storysearch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class LikedStories extends AppCompatActivity {

    private ListView listViewLikedStories;
    private List<Integer> likedStories;
    private SQLiteDatabase database;

     
    private static final String LIKED_STORIES_KEY = "likedStories";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liked_stories);

         
        database = connectToDatabase();

         
        TextView titleTextView = findViewById(R.id.titleTextView);
        TextView secondTitle = findViewById(R.id.secondTitle);
        listViewLikedStories = findViewById(R.id.listViewRandomStories);

         
        titleTextView.setText("Story Search");
        secondTitle.setText("Liked Stories");

         
        likedStories = getLikedStories();

         
        populateLikedList();

         
        listViewLikedStories.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 
                int selectedStoryId = likedStories.get(position);
                openSelectedStory(selectedStoryId);
            }
        });
    }

    private SQLiteDatabase connectToDatabase() {
         
        String dbPath = getFilesDir() + "/corpus.db";
        return SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
    }

     
    private void refreshLikedStoriesScreen() {
         
        likedStories = getLikedStories();

         
        populateLikedList();
    }

    private void populateLikedList() {
         
        StoryAdapter adapter = new StoryAdapter(this, getLikedStoriesDetails());
        listViewLikedStories.setAdapter(adapter);
    }

    private List<String> getLikedStoriesDetails() {
         
        List<String> likedStoriesDetails = new ArrayList<>();

        for (int storyId : likedStories) {
            String title = getStoryTitle(storyId);
            String startingLine = getStartingLine(storyId);

            if (title != null && startingLine != null) {
                 
                String truncatedStartingLine = truncateStartingLine(startingLine, 20);
                String likedStoryDetail = title + "\n" + truncatedStartingLine;
                likedStoriesDetails.add(likedStoryDetail);
            }
        }

        return likedStoriesDetails;
    }


    private List<String> getLikedStoriesTitles() {
         
        List<String> likedStoriesTitles = new ArrayList<>();

        for (int storyId : likedStories) {
            String title = getStoryTitle(storyId);
            String startingLine = getStartingLine(storyId);

            if (title != null && startingLine != null) {
                 
                String truncatedStartingLine = truncateStartingLine(startingLine, 20);
                String likedStoryTitle = title + "\n" + truncatedStartingLine;
                likedStoriesTitles.add(likedStoryTitle);
            }
        }

        return likedStoriesTitles;
    }

    private String truncateStartingLine(String startingLine, int wordLimit) {
        String[] words = startingLine.split("\\s+");
        StringBuilder truncatedLine = new StringBuilder();

        for (int i = 0; i < Math.min(wordLimit, words.length); i++) {
            truncatedLine.append(words[i]).append(" ");
        }

        return truncatedLine.toString().trim();
    }

    private String getStartingLine(int storyId) {
         
        String startingLine = null;
        if (database != null) {
            String query = "SELECT content FROM documents WHERE id = ?";
            Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(storyId)});
            if (cursor.moveToFirst()) {
                startingLine = cursor.getString(0);
            }
            cursor.close();
        }

        return startingLine;
    }

    private List<Integer> getLikedStories() {
         
        SharedPreferences prefs = getSharedPreferences("LikedStoriesPrefs", Context.MODE_PRIVATE);
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

    private String getStoryTitle(int storyId) {
         
        String title = null;
        if (database != null) {
            String query = "SELECT name FROM documents WHERE id = ?";
            Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(storyId)});
            if (cursor.moveToFirst()) {
                title = cursor.getString(0);
            }
            cursor.close();
        }

        return title;
    }

    private void openSelectedStory(int storyId) {
        Intent intent = new Intent(this, StoryDisplay.class);
        intent.putExtra("selectedDocId", storyId);
        startActivity(intent);
    }
}
