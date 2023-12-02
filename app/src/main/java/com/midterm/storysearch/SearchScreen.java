package com.midterm.storysearch;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.util.ArrayList;
import java.util.List;

public class SearchScreen extends AppCompatActivity {

    private Python python;
    private Context appContext;

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
        Button searchButton = findViewById(R.id.button);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userQuery = userQueryEditText.getText().toString().trim();

                if (userQuery.isEmpty()) {
                    showToast("Please enter a query");
                } else {
                    // Call the Python function with the user query
                    List<String> documentNames = callPythonFunction(userQuery);

                    // Display the retrieved document names (replace with your UI logic)
                    showToast("Documents: " + documentNames.toString());
                }
            }
        });
    }

    private List<String> callPythonFunction(String userQuery) {
        Python python = Python.getInstance();
        PyObject pythonCodeModule = python.getModule("PythonCode");

        PyObject contentListObj = pythonCodeModule.callAttr("read_csv_content", appContext);

        List<String> documentNames = new ArrayList<>();

        if (contentListObj != null) {
            // Call the connect_to_database function
            PyObject conn = pythonCodeModule.callAttr("connect_to_database", appContext, contentListObj);

            // Define the index_path variable
            String index_path = "/data/user/0/com.midterm.storysearch/files/index.csv";

            // Call the create_index function with the index_path
            pythonCodeModule.callAttr("create_index", conn, index_path);

            // Call the search_documents function (example usage)
            List<PyObject> searchResults = pythonCodeModule.callAttr("search_documents",  index_path, userQuery, appContext).asList();

            for (PyObject result : searchResults)
            {
                documentNames.add(result.toJava(String.class));
            }

            System.out.println("Search Results: " + documentNames);
        }

        return documentNames;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
