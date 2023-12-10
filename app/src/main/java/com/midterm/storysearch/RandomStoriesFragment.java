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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomStoriesFragment extends Fragment {

    private List<Integer> randomDocIds;

    public RandomStoriesFragment() {
         
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_random_stories, container, false);

        if (!tryOpenCorpus(view.getContext())) {
            return view;
        }

        getRandomDocIds(view.getContext());
        ImageView reloadButton = view.findViewById(R.id.ReloadButton);
        reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                onRandomizeButtonClick(view);
            }
        });

         
        view.post(new Runnable() {
            @Override
            public void run() {
                displayRandomStories(view);
            }
        });

        return view;
    }

    private boolean tryOpenCorpus(Context context) {
        String dbPath = context.getFilesDir() + "/corpus.db";

        File dbFile = new File(dbPath);
        if (!dbFile.exists()) {
            return false;
        }

        SQLiteDatabase db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
        if (db == null) {
            return false;
        }

        db.close();
        return true;
    }


    private void displayRandomStories(View view) {
        ListView listViewRandomStories = view.findViewById(R.id.listViewRandomStories);

        StoryAdapter adapter = new StoryAdapter(requireContext(), getStoryNamesWithStartingLines(requireContext()));

        listViewRandomStories.setAdapter(adapter);

        listViewRandomStories.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int selectedDocId = randomDocIds.get(position);

                 
                StoryAdapter adapter = (StoryAdapter) parent.getAdapter();

                if (adapter != null) {
                     
                    String storyDetails = adapter.getItem(position);

                     
                    String[] storyInfo = storyDetails.split("\n");

                    if (storyInfo.length >= 2) {
                         
                        String storyName = storyInfo[0];
                        String startingLine = storyInfo[1];

                        RecentStoriesManager recentStoriesManager = RecentStoriesManager.getInstance();
                        RecentStoriesManager.RecentStory recentStory = new RecentStoriesManager.RecentStory(selectedDocId, storyName, startingLine);
                        recentStoriesManager.addRecentStory(recentStory);

                        openStoryScreen(selectedDocId);
                    }
                }
            }
        });


    }


    private String getStartingLine(String content) {
        if (content != null) {
            String[] words = content.split("\\s+");
            int maxWords = 20;

            StringBuilder startingLine = new StringBuilder();

            for (int i = 0; i < Math.min(words.length, maxWords); i++) {
                startingLine.append(words[i]).append(" ");
            }

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
         
        String dbPath = context.getFilesDir() + "/corpus.db";
        return SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
    }

    private void getRandomDocIds(Context context) {
        if (randomDocIds == null) {
            randomDocIds = new ArrayList<>();
        } else {
            randomDocIds.clear();
        }

        SQLiteDatabase db = connectToDatabase(context);
        if (db == null) {
            return;
        }

        int totalStories = getTotalStories(db);

        Random random = new Random();
        while (randomDocIds.size() < 4) {
            int randomDocId = random.nextInt(totalStories) + 1;  
            if (!randomDocIds.contains(randomDocId)) {
                randomDocIds.add(randomDocId);
            }
        }

        db.close();
    }

    private int getTotalStories(SQLiteDatabase db) {

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

                     
                    if (isValidStory(storyName, content)) {
                        String startingLine = getStartingLine(content);
                        storyNamesWithStartingLines.add(storyName + "\n" + startingLine);
                    } else {
                         
                        int replacementDocId = findRandomDocId(db, randomDocIds);
                        randomDocIds.set(i, replacementDocId);
                        i--;  
                    }
                }

                cursor.close();
            }
            db.close();
        }
        return storyNamesWithStartingLines;
    }

    private boolean isValidStory(String storyName, String content) {
        return storyName != null && !storyName.isEmpty() &&
                content != null && !content.isEmpty();
    }
    private int findRandomDocId(SQLiteDatabase db, List<Integer> excludeDocIds) {
        Random random = new Random();
        int totalStories = getTotalStories(db);

        int randomDocId;
        do {
            randomDocId = random.nextInt(totalStories) + 1;
        } while (excludeDocIds.contains(randomDocId));

        return randomDocId;
    }

    public void onRandomizeButtonClick(View view) {
         
        getRandomDocIds(requireContext());
        displayRandomStories(getView());
    }
}