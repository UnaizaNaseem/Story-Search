package com.midterm.storysearch;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ReadHistory extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_history);

        ListView listViewReadHistory = findViewById(R.id.listViewRandomStories);

        RecentStoriesManager recentStoriesManager = RecentStoriesManager.getInstance();
        List<RecentStoriesManager.RecentStory> readHistory = recentStoriesManager.getReadHistory();

        StoryAdapter adapter = new StoryAdapter(this, getStoryDetails(readHistory));
        listViewReadHistory.setAdapter(adapter);

        listViewReadHistory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int selectedDocId = readHistory.get(position).getSelectedDocId();
                openStoryScreen(selectedDocId);
            }
        });
    }

    private List<String> getStoryDetails(List<RecentStoriesManager.RecentStory> stories) {
        List<String> storyDetails = new ArrayList<>();
        for (RecentStoriesManager.RecentStory story : stories) {
            String storyDetail = story.getStoryName() + "\n" + story.getStartingLine();
            storyDetails.add(storyDetail);
        }
        return storyDetails;
    }

    private void openStoryScreen(int selectedDocId) {
        Intent intent = new Intent(this, StoryDisplay.class);
        intent.putExtra("selectedDocId", selectedDocId);
        startActivity(intent);
    }
}
