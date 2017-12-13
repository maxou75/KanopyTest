package fr.kanopytest.maximedonnet.kanopytest;

import android.app.Activity;
import android.content.Intent;
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
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This activity shows the commits of a selected repositories and allows user to filter them by message content or committer name.
 */
public class CommitsListActivity extends Activity {
    private final String TAG = "CommitsList";
    private CommitAdapter mAdapter;
    private ListView listView;
    private TextView titleView;
    private EditText titleFilter, committerFilter;
    private RelativeLayout filtersSearchLayout;
    private ProgressBar progressBar;
    private Button validateFilters;
    private String url, full_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commits_list);
        url = this.getIntent().getStringExtra("url");
        full_name = this.getIntent().getStringExtra("full_name");
        progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        progressBar.setIndeterminate(true);
        listView = findViewById(R.id.list);
        titleView = findViewById(R.id.commitsListActivityTitle);
        titleFilter = findViewById(R.id.titleFilter);
        filtersSearchLayout = findViewById(R.id.filtersSearchLayout);
        committerFilter = findViewById(R.id.committerFilter);
        validateFilters = findViewById(R.id.validateFilters);
        listView.setEmptyView(progressBar);
        ViewGroup root = findViewById(android.R.id.content);
        root.addView(progressBar);
        progressBar.setVisibility(View.GONE);
        onListItemClick();
        onFilterClick();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MainActivity.isOnline()) {
            titleView.setText(full_name);
            displayAllCommits(url);
        }
        else {
            CharSequence log  = "No internet connexion ...";
            Toast.makeText(getApplicationContext(), log, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Create the new URL that correspond to the filters criteria specified by the user when he validates the filters.
     * The request with the url is made.
     */
    private void onFilterClick() {
        View.OnClickListener onFilterClick = new View.OnClickListener() {
            public void onClick(View v) {

                String titleRegex = titleFilter.getText().toString();
                String committerRegex = committerFilter.getText().toString();

                if (titleRegex.length() <= 0 && committerRegex.length() <= 0) {
                    if (MainActivity.isOnline()) {
                        progressBar.setVisibility(View.VISIBLE);
                        displayAllCommits(url);
                    }
                    else {
                        CharSequence log  = "No internet connexion ...";
                        Toast.makeText(getApplicationContext(), log, Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    String query = "repo:" + full_name;
                    if (titleRegex.length() > 0)
                        query = query + "+" + titleRegex;
                    if (committerRegex.length() > 0)
                        query = query + "+committer-name:" + committerRegex;
                    String filtersUrl = "https://api.github.com/search/commits?q=" + query;
                    if (MainActivity.isOnline())
                        displayFilteredCommits(filtersUrl);
                    else {
                        CharSequence log  = "No internet connexion ...";
                        Toast.makeText(getApplicationContext(), log, Toast.LENGTH_LONG).show();
                    }
                }
            }
        };
        validateFilters.setOnClickListener(onFilterClick);
    }

    /**
     * Make the request and display the commits list in the ListView retrieved with the url parameters and that match filters criteria
     * @param url String value of the url corresponding to the user filter search
     */
    private void displayFilteredCommits(String url) {
        Log.i(TAG, "Getting information for " + url);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    if (response.isNull("items"))
                        return;
                    JSONArray items = response.getJSONArray("items");
                    Log.d(TAG, "JSONArray : " + items.toString());
                    List<Commit> commitsList = parseCommitsList(items);
                    mAdapter = new CommitAdapter(getApplicationContext(), commitsList);
                    listView.setAdapter(mAdapter);
                    progressBar.setVisibility(View.GONE);
                } catch (JSONException error) {
                    CharSequence log  = "Impossible to retrieve data (" + error.getMessage() + ")";
                    Toast.makeText(getApplicationContext(), log, Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                    error.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CharSequence log;
                if (error.getMessage() != null)
                    log = "Impossible to retrieve data (" + error.getMessage() + ")";
                else
                    log = "Impossible to retrieve data (" + error.getMessage() + ")";
                Toast.makeText(getApplicationContext(), log, Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                error.printStackTrace();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Accept", "application/vnd.github.cloak-preview");
                return params;
            }
        };
        MainActivity.mRequestQueue.add(jsObjRequest);
    }

    /**
     * Make the request and display all commits of the repository that user selected on the MainActivity.
     * It show the last 30 commits of the repository.
     * @param url String value of the repository URL
     */
    private void displayAllCommits(String url) {
        Log.i(TAG, "Getting information for " + url);
        JsonArrayRequest jsArrRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                try {
                    Log.d(TAG, "JSONArray : " + response.toString());
                    List<Commit> commitsList = parseCommitsList(response);
                    mAdapter = new CommitAdapter(getApplicationContext(), commitsList);
                    listView.setAdapter(mAdapter);
                    progressBar.setVisibility(View.GONE);
                } catch (JSONException error) {
                    CharSequence log = "Impossible to retrieve data (" + error.getMessage() + ")";
                    Toast.makeText(getApplicationContext(), log, Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                    error.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                int responseCode = -1;
                if (error.networkResponse != null)
                    responseCode = error.networkResponse.statusCode;
                if (responseCode == 409){
                    TextView emptyRepository = findViewById(R.id.emptyRepository);
                    emptyRepository.setText("Repository is empty.");
                    filtersSearchLayout.setVisibility(View.GONE);
                    emptyRepository.setVisibility(View.VISIBLE);
                } else {
                    CharSequence log;
                    if (error.getMessage() != null)
                        log = "Impossible to retrieve data (" + error.getMessage() + ")";
                    else
                        log = "Impossible to retrieve data";
                    Toast.makeText(getApplicationContext(), log, Toast.LENGTH_LONG).show();
                }
                progressBar.setVisibility(View.GONE);
                error.printStackTrace();
            }
        });
        MainActivity.mRequestQueue.add(jsArrRequest);
    }

    /**
     * Parse the JSON commits list to a Java List
     * @param commitsJSONArray It contains the list of the commits in a JSON format to parse
     * @return New created list of the commits objects.
     * @throws JSONException
     */
    private List<Commit> parseCommitsList(JSONArray commitsJSONArray) throws JSONException {
        List<Commit> commitsList = new ArrayList<>();
        for (int i = 0; i < commitsJSONArray.length(); i++) {
            //HashMap<String, String> commitHM = new HashMap<>();

            JSONObject commit = commitsJSONArray.getJSONObject(i);
            //Parse main commit information
            String sha, url, title = null, committer = null, date = null;
            sha = commit.getString("sha");
            url = commit.getString("url");
            if (!commit.isNull("commit"))
                title = commit.getJSONObject("commit").getString("message").split("\n")[0];
            if (!commit.isNull("commit") && !commit.getJSONObject("commit").isNull("committer"))
                committer = commit.getJSONObject("commit").getJSONObject("committer").getString("name");
            if (!commit.isNull("commit") && !commit.getJSONObject("commit").isNull("committer"))
                date = commit.getJSONObject("commit").getJSONObject("committer").getString("date");

            commitsList.add(new Commit(sha, committer, title, date, url));
        }
        return commitsList;
    }

    /**
     * Start a new activity that show details of the commit item on which user clicked.
     */
    private void onListItemClick() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!MainActivity.isOnline()) {
                    CharSequence log  = "No internet connexion ...";
                    Toast.makeText(getApplicationContext(), log, Toast.LENGTH_LONG).show();
                } else {
                    Commit commit = (Commit) parent.getItemAtPosition(position);
                    Log.i(TAG, "Sha commit clicked : " + commit.getSha());
                    Intent commitDetails = new Intent(getApplicationContext(), CommitDetailsActivity.class);
                    commitDetails.putExtra("url", commit.getUrl());
                    startActivity(commitDetails);
                }
            }
        });
    }
}
