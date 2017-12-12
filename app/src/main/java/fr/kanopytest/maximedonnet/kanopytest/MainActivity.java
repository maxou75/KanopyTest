package fr.kanopytest.maximedonnet.kanopytest;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This activity is started when application is launched. It allows to display repositories that match specified name.
 */
public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    public static RequestQueue mRequestQueue;
    private TextView repositoriesTitle;
    private EditText nameEdit;
    private Button nameValidate;
    private ListView repositoriesListView;
    private RelativeLayout repositoriesSearchLayout;
    private RepositoryAdapter  mAdapter;
    private ProgressBar progressBar;
    private static ConnectivityManager cm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRequestQueue = Volley.newRequestQueue(this);
        repositoriesTitle = findViewById(R.id.repositoriesListActivityTitle);
        nameEdit = findViewById(R.id.nameEdit);
        repositoriesListView = findViewById(R.id.repositoriesList);
        repositoriesSearchLayout = findViewById(R.id.repositoriesSearchLayout);
        nameValidate = findViewById(R.id.nameValidate);
        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        progressBar.setIndeterminate(true);
        ViewGroup root = findViewById(android.R.id.content);
        repositoriesListView.setEmptyView(progressBar);
        root.addView(progressBar);
        progressBar.setVisibility(View.GONE);

        onValidateClick();
        onListItemClick();
    }

    /**
     * Define the url with the regex repository name when a user click on the validate button
     */
    private void onValidateClick() {
        View.OnClickListener onValidateClick = new View.OnClickListener() {
            public void onClick(View v) {
                String regex = nameEdit.getText().toString();
                if (regex.length() > 0) {
                    String url = "https://api.github.com/search/repositories?q=" + regex;
                    if (isOnline()) {
                        progressBar.setVisibility(View.VISIBLE);
                        displayData(url);
                    }
                    else {
                        CharSequence log  = "No internet connexion ...";
                        Toast.makeText(getApplicationContext(), log, Toast.LENGTH_LONG).show();
                    }
                }
            }
        };
        nameValidate.setOnClickListener(onValidateClick);
    }

    /**
     * Check the device internet connexion
     * @return Boolean value of internet device connexion
     */
    public static boolean isOnline() {
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    /**
     * Make the request and display the repositories list in the ListView retrieved with the url parameters
     * @param url String value of the url on which retrieve repositories
     */
    private void displayData(String url) {
        Log.i(TAG, "Getting information for " + url);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (!response.isNull("items")) {
                        JSONArray repositories = response.getJSONArray("items");
                        List<Repository> repositoryList = new ArrayList<>();

                        for (int i = 0; i < repositories.length(); i++) {
                            if (!repositories.isNull(i)) {
                                JSONObject repository = repositories.getJSONObject(i);
                                String id = null, full_name = null, url = null;
                                if (!repository.isNull("id"))
                                    id = repository.getString("id");
                                if (!repository.isNull("full_name"))
                                    full_name = repository.getString("full_name");
                                if (!repository.isNull("url"))
                                    url = repository.getString("url");
                                repositoryList.add(new Repository(id, url, full_name));
                            }
                        }
                        mAdapter = new RepositoryAdapter(getApplicationContext(), repositoryList);
                        repositoriesListView.setAdapter(mAdapter);
                        progressBar.setVisibility(View.GONE);
                    }
                } catch (JSONException error) {
                    CharSequence log  = "Impossible to display data (" + error.getMessage() + ")";
                    //Show log to user
                    Toast.makeText(getApplicationContext(), log, Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                    error.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CharSequence log;
                if (error.getMessage() != null) {
                    log = "Impossible to retrieve data (" + error.getMessage() + ")";
                }
                else
                    log = "Impossible to retrieve data";
                //Show log to user
                Toast.makeText(getApplicationContext(), log, Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                error.printStackTrace();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Accept", "application/vnd.github.mercy-preview+json");
                return params;
            }
        };
        MainActivity.mRequestQueue.add(jsObjRequest);
    }

    /**
     * Start a new activity to display commits list of the repository clicked
     */
    private void onListItemClick() {
        repositoriesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!isOnline()) {
                    CharSequence log  = "No internet connexion ...";
                    Toast.makeText(getApplicationContext(), log, Toast.LENGTH_LONG).show();
                } else {
                    Repository repository = (Repository) parent.getItemAtPosition(position);
                    Log.i(TAG, "Repository clicked : " + repository.getId());
                    Intent commitsList = new Intent(getApplicationContext(), CommitsListActivity.class);
                    commitsList.putExtra("url", repository.getCommitsUrl());
                    commitsList.putExtra("full_name", repository.getFullName());
                    startActivity(commitsList);
                }
            }
        });
    }
}
