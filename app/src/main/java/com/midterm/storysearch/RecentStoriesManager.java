package com.midterm.storysearch;

import java.util.ArrayList;
import java.util.List;

public class RecentStoriesManager {

    private static RecentStoriesManager instance;
    private final List<RecentStory> readHistory;

    private RecentStoriesManager() {
        readHistory = new ArrayList<>();
    }

    public static synchronized RecentStoriesManager getInstance() {
        if (instance == null) {
            instance = new RecentStoriesManager();
        }
        return instance;
    }

    public void addRecentStory(RecentStory recentStory) {
        readHistory.add(recentStory);

        if (readHistory.size() > 5) {
            readHistory.remove(0);
        }
    }

    public List<RecentStory> getReadHistory() {
        return readHistory;
    }

    public static class RecentStory {
        private final int selectedDocId;
        private final String storyName;
        private final String startingLine;

        public RecentStory(int selectedDocId, String storyName, String startingLine) {
            this.selectedDocId = selectedDocId;
            this.storyName = storyName;
            this.startingLine = startingLine;
        }

        public int getSelectedDocId() {
            return selectedDocId;
        }

        public String getStoryName() {
            return storyName;
        }

        public String getStartingLine() {
            return startingLine;
        }
    }
}
