package com.midterm.storysearch;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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

        adminPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Define the intent to start the AdminPanel activity
                Intent intent = new Intent(SearchScreen.this, AdminPanel.class);
                startActivity(intent);
            }
        });

        ImageView historyButton = findViewById(R.id.HistoryButton);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                onHistoryButtonClick(view);
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userQuery = userQueryEditText.getText().toString().trim();

                if (userQuery.isEmpty()) {
                    showToast("Please enter a query");
                }
                else
                {
                    showLoadingScreen();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            // Fetch search results and update the fragment
                            List<Integer> documentIDs = callPythonFunction(userQuery);

                            // Hide loading screen after results are fetched
                            hideLoadingScreen();

                            // Update the fragment after hiding the loading screen
                            updateSearchResultsFragment(documentIDs, userQuery);
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

        List<Integer> searchResults=new ArrayList<>();
        if (contentListObj != null) {
            // Call the connect_to_database function
            PyObject conn = pythonCodeModule.callAttr("connect_to_database", appContext, contentListObj);

            // Define the index_path variable
            String index_path = "/data/user/0/com.midterm.storysearch/files/index.csv";

            // Call the create_index function with the index_path
            pythonCodeModule.callAttr("create_index", conn, index_path);

            List<PyObject> pythonList = pythonCodeModule.callAttr("search_documents", index_path, userQuery, appContext).asList();
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


        }

        return searchResults;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    private void updateSearchResultsFragment(List<Integer> documentIDs, String userQuery) {
        // Clear the entire back stack
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        // Create a new instance of RandomStoriesFragment
        RandomStoriesFragment randomStoriesFragment = new RandomStoriesFragment();

        // Replace the existing fragment with the new RandomStoriesFragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.fragmentContainerView, randomStoriesFragment);
        fragmentTransaction.addToBackStack(RandomStoriesFragment.class.getName());

        // Create a new instance of SearchResultsFragment
        SearchResultsFragment searchResultsFragment = SearchResultsFragment.newInstance(userQuery, documentIDs);

        // Replace the existing fragment with the new SearchResultsFragment
        fragmentTransaction.replace(R.id.fragmentContainerView, searchResultsFragment);

        fragmentTransaction.commit();
    }





    private void showLoadingScreen() {
        // Create and show the loading screen fragment
        loadingScreenFragment = new LoadingScreenFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Replace random stories fragment with loading screen fragment
        fragmentTransaction.replace(R.id.fragmentContainerView, loadingScreenFragment);

        fragmentTransaction.commit();
    }



    private void hideLoadingScreen() {
        // Hide the loading screen fragment
        if (loadingScreenFragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(loadingScreenFragment);
            fragmentTransaction.commit();
        }
    }

   private void  onHistoryButtonClick(View view)
   {
       Intent intent = new Intent(SearchScreen.this, ReadHistory.class);
       startActivity(intent);
   }



}
