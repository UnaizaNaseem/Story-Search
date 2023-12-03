package com.midterm.storysearch;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.util.ArrayList;
import java.util.List;

public class AdminPanel extends AppCompatActivity {
    private Context appContext;
    private Python python;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);
        appContext = getApplicationContext();

        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
        python = Python.getInstance();

        final EditText adminQuery = findViewById(R.id.AdminQuery);
        Button searchButton = findViewById(R.id.SearchButton);
        final ListView docListView = findViewById(R.id.docListView);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String AdminQuery = adminQuery.getText().toString().trim();

                if (AdminQuery.isEmpty()) {
                    showToast("Please enter a query");
                } else {
                    // Call the Python function with the user query
                    List<Integer> documentIDs = callPythonFunction(AdminQuery);
                    List<String> docTitles = getDocNames(appContext, documentIDs);
                    displayNames(docTitles, docListView,AdminQuery);
                }
            }
        });
    }

    private List<Integer> callPythonFunction(String adminQuery) {
        Python python = Python.getInstance();
        PyObject pythonCodeModule = python.getModule("PythonCode");

        List<Integer> searchResults = new ArrayList<>();

        String index_path = "/data/user/0/com.midterm.storysearch/files/index.csv";

        List<PyObject> pythonList = pythonCodeModule.callAttr("search_documents", index_path, adminQuery, appContext).asList();
        searchResults = new ArrayList<>();

        for (PyObject item : pythonList) {
            // Convert each PyObject to Java and then to Integer
            try {
                Integer result = item.toJava(Integer.class);
                searchResults.add(result);
            } catch (ClassCastException e) {
                String stringValue = item.toJava(String.class);
                try {
                    Integer result = Integer.parseInt(stringValue);
                    searchResults.add(result);
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
        }

        System.out.println("Search Results: " + searchResults);
        return searchResults;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private List<String> getDocNames(Context context, List<Integer> documentIDs) {
        //get Document Names corresponding to DocID's from the database
        List<String> storyNames = new ArrayList<>();

        SQLiteDatabase db = connectToDatabase(context);
        if (db != null) {
            for (int i = 0; i < documentIDs.size(); i++) {
                int docId = documentIDs.get(i);
                String query = "SELECT name, content FROM documents WHERE id = ?";
                Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(docId)});
                if (cursor.moveToFirst()) {
                    String storyName = cursor.getString(0);
                    storyNames.add(storyName);
                }
            }

        }
        return storyNames;
    }

    private SQLiteDatabase connectToDatabase(Context context) {
        // Open the database
        String dbPath = context.getFilesDir() + "/corpus.db";
        return SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY);
    }

    private void displayNames(final List<String> docTitles, final ListView listView,String adminQuery) {
        // Set up the CustomAdapter
        CustomAdapter adapter = new CustomAdapter(this, docTitles);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Button removeButton = view.findViewById(R.id.removeButton);

                if (removeButton.getVisibility() == View.VISIBLE) {
                    removeButton.setVisibility(View.GONE);
                } else {
                    removeButton.setVisibility(View.VISIBLE);

                    removeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showToast("Remove button clicked for: " + docTitles.get(position));
                            removeStory(docTitles.get(position));
                            // Refresh the ListView after removing the item
                            List<Integer> documentIDs = callPythonFunction(adminQuery);
                            List<String> docTitles = getDocNames(appContext, documentIDs);
                            displayNames(docTitles, (ListView) findViewById(R.id.docListView),adminQuery);
                        }
                    });
                }
            }
        });
    }

    // Method to remove the story from the database
    private void removeStory(String storyName) {
        String storyNameToRemove = storyName;

        // Example: Call a Python function to remove the story
        python.getModule("PythonCode").callAttr("remove_story_from_database", storyNameToRemove);

        showToast("Story removed: " + storyNameToRemove);
    }

    private class CustomAdapter extends ArrayAdapter<String> {

        public CustomAdapter(Context context, List<String> items) {
            super(context, 0, items);
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            String docTitle = getItem(position);


            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_layout, parent, false);
            }


            TextView storyNameTextView = convertView.findViewById(R.id.storyNameTextView);
            Button removeButton = convertView.findViewById(R.id.removeButton);


            storyNameTextView.setText(docTitle);
            removeButton.setVisibility(View.GONE);

            // Set onClickListener for the remove button
            removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeStory(getItem(position));
                }
            });

            return convertView;
        }
    }
}
