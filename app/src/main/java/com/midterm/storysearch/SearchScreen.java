package com.midterm.storysearch;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.util.ArrayList;
import java.util.List;

public class SearchScreen extends AppCompatActivity {

    private Python python;
    private Context appContext;
    private LoadingScreenFragment loadingScreenFragment;
    boolean isStandardSearch = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_screen);

        appContext = getApplicationContext();

        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
        python = Python.getInstance();

        final EditText userQueryEditText = findViewById(R.id.UserQuery);
        Button searchButton = findViewById(R.id.SearchButton);
        TextView adminPanel = findViewById(R.id.moveToAdminScreen);
        TextView likedStories = findViewById(R.id.moveToLikedStories);

        adminPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchScreen.this, AdminPanel.class);
                startActivity(intent);
            }
        });
        likedStories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchScreen.this, LikedStories.class);
                startActivity(intent);
            }
        });

        ImageView historyButton = findViewById(R.id.HistoryButton);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onHistoryButtonClick(view);
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userQuery = userQueryEditText.getText().toString().trim();

                if (userQuery.isEmpty()) {
                    showToast("Please enter a query");
                } else {
                    showLoadingScreen();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            List<Integer> documentIDs = callPythonFunction(userQuery);
                            hideLoadingScreen();
                            updateSearchResultsFragment(documentIDs, userQuery,isStandardSearch);
                        }
                    }, 600);
                }
            }
        });
    }

    private List<Integer> callPythonFunction(String userQuery) {
        Python python = Python.getInstance();
        PyObject pythonCodeModule = python.getModule("PythonCode");

        PyObject contentListObj = pythonCodeModule.callAttr("read_csv_content", appContext);

        List<Integer> searchResults = new ArrayList<>();


        if (contentListObj != null) {
            PyObject conn = pythonCodeModule.callAttr("connect_to_database", appContext, contentListObj);

            String index_path = "/data/user/0/com.midterm.storysearch/files/index.csv";

            pythonCodeModule.callAttr("create_index", conn, index_path);

            List<PyObject> pythonList = pythonCodeModule.callAttr("search_documents", index_path, userQuery, appContext).asList();

            for (PyObject item : pythonList) {
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

            if (searchResults.isEmpty()) {
                isStandardSearch = false;
                List<Integer> similarOrRandomStories = getRandomStories(); // Replace with actual logic
                searchResults.addAll(similarOrRandomStories);
            }
        }

        updateSearchResultsFragment(searchResults, userQuery, isStandardSearch);

        return searchResults;
    }

    private List<Integer> getRandomStories() {
        List<Integer> randomStories = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            int randomID = (int) (Math.random() * 450) + 1;
            randomStories.add(randomID);
        }
        return randomStories;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void updateSearchResultsFragment(List<Integer> documentIDs, String userQuery, boolean isStandardSearch) {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        RandomStoriesFragment randomStoriesFragment = new RandomStoriesFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.fragmentContainerView, randomStoriesFragment);
        fragmentTransaction.addToBackStack(RandomStoriesFragment.class.getName());

        SearchResultsFragment searchResultsFragment = SearchResultsFragment.newInstance(userQuery, documentIDs, isStandardSearch);

        fragmentTransaction.replace(R.id.fragmentContainerView, searchResultsFragment);

        fragmentTransaction.commit();
    }

    private void showLoadingScreen() {
        loadingScreenFragment = new LoadingScreenFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.fragmentContainerView, loadingScreenFragment);

        fragmentTransaction.commit();
    }

    private void hideLoadingScreen() {
        if (loadingScreenFragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(loadingScreenFragment);
            fragmentTransaction.commit();
        }
    }

    private void onHistoryButtonClick(View view) {
        Intent intent = new Intent(SearchScreen.this, ReadHistory.class);
        startActivity(intent);
    }
}
