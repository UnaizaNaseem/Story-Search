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
import android.widget.ListView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.List;

public class SearchResultsFragment extends Fragment {

    private static final String ARG_SEARCH_QUERY = "searchQuery";
    private static final String ARG_DOCUMENT_IDS = "documentIds";

    private String searchQuery;
    private List<Integer> documentIDs;

    public SearchResultsFragment() {
        // Required empty public constructor
    }

    public static SearchResultsFragment newInstance(String searchQuery, List<Integer> documentIDs) {
        SearchResultsFragment fragment = new SearchResultsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SEARCH_QUERY, searchQuery);
        args.putIntegerArrayList(ARG_DOCUMENT_IDS, new ArrayList<>(documentIDs));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            searchQuery = getArguments().getString(ARG_SEARCH_QUERY);
            documentIDs = getArguments().getIntegerArrayList(ARG_DOCUMENT_IDS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_results, container, false);

        TextView titleTextView = view.findViewById(R.id.header);
        titleTextView.setText("Showing Results for: " + searchQuery);

        displaySearchedStories(view,documentIDs);

        return view;
    }

    private SQLiteDatabase connectToDatabase(Context context) {
        // Open the database
        String dbPath = context.getFilesDir() + "/corpus.db";
        return SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
    }

    private void displaySearchedStories(View view,List<Integer> documentIDs) {
        ListView listViewRandomStories = view.findViewById(R.id.listViewSearchResults);

        StoryAdapter adapter = new StoryAdapter(requireContext(), getStoryNamesWithStartingLines(requireContext()));

        listViewRandomStories.setAdapter(adapter);

        listViewRandomStories.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Open the story in a new screen using the selected docId
                int selectedDocId = documentIDs.get(position);
                openStoryScreen(selectedDocId);
            }
        });
    }
    private List<String> getStoryNamesWithStartingLines(Context context) {
        List<String> storyNamesWithStartingLines = new ArrayList<>();
        SQLiteDatabase db = connectToDatabase(context);
        if (db != null) {
            for (int i = 0; i < documentIDs.size(); i++) {
                int docId = documentIDs.get(i);
                String query = "SELECT name, content FROM documents WHERE id = ?";
                Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(docId)});
                if (cursor.moveToFirst()) {
                    String storyName = cursor.getString(0);
                    String content = cursor.getString(1);

                    if (storyName == null || storyName.isEmpty() || content == null || content.isEmpty()) {
                        continue;
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
}
