package com.midterm.storysearch;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomStoriesFragment extends Fragment {

    private List<Integer> randomDocIds;

    public RandomStoriesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_random_stories, container, false);

        // Retrieve 5 random stories from the database
        getRandomDocIds(view.getContext());
        Button randomizeBtn = view.findViewById(R.id.btnRandom);

        randomizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRandomizeButtonClick(v); // Remove the argument 'view'
            }
        });

        displayRandomStories(view);

        return view;
    }


    private void displayRandomStories(View view) {
        ListView listViewRandomStories = view.findViewById(R.id.listViewRandomStories);

        // Create a custom adapter to bind data to the ListView
        StoryAdapter adapter = new StoryAdapter(requireContext(), getStoryNamesWithStartingLines(requireContext()));
        // Set the adapter to the ListView
        listViewRandomStories.setAdapter(adapter);

        // Set an OnItemClickListener to handle clicks on the items
        listViewRandomStories.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Open the story in a new screen using the selected docId
                int selectedDocId = randomDocIds.get(position);
                openStoryScreen(selectedDocId);
            }
        });
    }


    private String getStartingLine(String content) {
        if (content != null) {
            // Extract the starting line from the content
            // You can customize this based on your story content structure
            String[] words = content.split("\\s+");
            int maxWords = 20; // Set the maximum number of words for the starting line

            StringBuilder startingLine = new StringBuilder();

            for (int i = 0; i < Math.min(words.length, maxWords); i++) {
                startingLine.append(words[i]).append(" ");
            }

            // Add "..." at the end if there are more words in the story
            if (words.length > maxWords) {
                startingLine.append("...");
            }

            return startingLine.toString().trim();
        } else {
            return "";
        }
    }



    private void openStoryScreen(int selectedDocId) {

        Intent intent = new Intent(requireContext(), StoryDisplay.class);
        intent.putExtra("selectedDocId", selectedDocId);
        startActivity(intent);
    }

    private SQLiteDatabase connectToDatabase(Context context) {
        // Open the database
        String dbPath = context.getFilesDir() + "/corpus.db";
        return SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
    }

    private void getRandomDocIds(Context context) {
        // Initialize or clear the list
        if (randomDocIds == null) {
            randomDocIds = new ArrayList<>();
        } else {
            randomDocIds.clear();
        }

        // Open the database
        SQLiteDatabase db = connectToDatabase(context);
        if (db == null) {
            return;
        }

        // Get the total number of stories
        int totalStories = getTotalStories(db);

        // Generate 5 unique random numbers as docIds
        Random random = new Random();
        while (randomDocIds.size() < 5) {
            int randomDocId = random.nextInt(totalStories) + 1; // Assuming docIds start from 1
            if (!randomDocIds.contains(randomDocId)) {
                randomDocIds.add(randomDocId);
            }
        }

        // Close the database
        db.close();
    }

    private int getTotalStories(SQLiteDatabase db) {
        // Get the total number of stories from the database
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM documents", null);
        cursor.moveToFirst();
        int totalStories = cursor.getInt(0);
        cursor.close();
        return totalStories;
    }

    private List<String> getStoryNamesWithStartingLines(Context context) {
        List<String> storyNamesWithStartingLines = new ArrayList<>();
        SQLiteDatabase db = connectToDatabase(context);
        if (db != null) {
            for (int i = 0; i < randomDocIds.size(); i++) {
                int docId = randomDocIds.get(i);
                String query = "SELECT name, content FROM documents WHERE id = ?";
                Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(docId)});
                if (cursor.moveToFirst()) {
                    String storyName = cursor.getString(0);
                    String content = cursor.getString(1);

                    // Check if story name or content is empty
                    if (storyName == null || storyName.isEmpty() || content == null || content.isEmpty()) {
                        // Find another random story as a replacement
                        int replacementDocId = findRandomDocId(db, randomDocIds);
                        randomDocIds.set(i, replacementDocId); // Replace the current docId
                        cursor.close();
                        continue; // Skip the current iteration and process the replacement
                    }

                    String startingLine = getStartingLine(content);
                    storyNamesWithStartingLines.add(storyName + "\n" + startingLine);
                }
                cursor.close();
            }
            db.close();
        }
        return storyNamesWithStartingLines;
    }

    private int findRandomDocId(SQLiteDatabase db, List<Integer> excludeDocIds) {
        Random random = new Random();
        int totalStories = getTotalStories(db);

        // Generate a unique random number as docId excluding the specified docIds
        int randomDocId;
        do {
            randomDocId = random.nextInt(totalStories) + 1; // Assuming docIds start from 1
        } while (excludeDocIds.contains(randomDocId));

        return randomDocId;
    }

    public void onRandomizeButtonClick(View view) {
        // Reload and randomize the stories
        getRandomDocIds(requireContext());
        displayRandomStories(getView());
    }
}