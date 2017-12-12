package fr.kanopytest.maximedonnet.kanopytest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * This activity shows more details of the selected commit on the CommitsListActivity.
 * It display users avatar and open the web browsers with users url when user clicked on the images.
 */
public class CommitDetailsActivity extends Activity {
    private final String TAG = "CommitDetailsActivity";
    private TextView titleView, messageView, authorLoginView, committerLoginView, dateView, filesTitle;
    private ImageButton committerAvatarView, authorAvatarView;
    private RelativeLayout detailsLayout, committerLayout, authorLayout;
    private LinearLayout filesList;
    private String url;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commit_details);
        url = this.getIntent().getStringExtra("url");
        titleView = findViewById(R.id.commitDetailsActivityTitle);
        messageView = findViewById(R.id.commitDetailsMessage);
        authorLoginView = findViewById(R.id.commitDetailsAuthorLogin);
        committerLoginView = findViewById(R.id.commitDetailsCommitterLogin);
        dateView = findViewById(R.id.commitDetailsDate);
        committerAvatarView = findViewById(R.id.commitCommitterAvatar);
        authorAvatarView = findViewById(R.id.commitDetailsAuthorAvatar);
        detailsLayout = findViewById(R.id.commitDetailsLayout);
        committerLayout = findViewById(R.id.commitDetailsCommitterLayout);
        authorLayout = findViewById(R.id.commitDetailsAuthorLayout);
        filesTitle = findViewById(R.id.commitDetailsFilesTitle);
        filesList = findViewById(R.id.commitDetailsFilesList);
        progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        progressBar.setIndeterminate(true);
        ViewGroup root = findViewById(android.R.id.content);
        root.addView(progressBar);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MainActivity.isOnline()) {
            detailsLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            displayData(url);
        } else {
            detailsLayout.setVisibility(View.GONE);
            CharSequence log  = "No internet connexion ...";
            Toast.makeText(getApplicationContext(), log, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Make the request and Display commit details.
     * @param url String value of the url that correspond to the specified commit
     */
    private void displayData(String url) {
        Log.i(TAG, "Getting information for " + url);
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject commit) {
                Log.d(TAG, "JSONObject : " + commit.toString());
                try {
                    String title = null, message = null, committer_login = null, date = null, author_login, html_url, committer_avatar_url,
                            committer_html_url, committer_id = null, author_avatar_url, author_html_url, author_id;
                    //Parse information
                    if (!commit.isNull("commit")) {
                        message = commit.getJSONObject("commit").getString("message");
                        title = message.split("\n")[0];
                    }
                    if (!commit.isNull("committer")) {
                        committer_id = commit.getJSONObject("committer").getString("id");
                        committer_login = commit.getJSONObject("committer").getString("login");
                        committer_avatar_url = commit.getJSONObject("committer").getString("avatar_url");
                        committer_html_url = commit.getJSONObject("committer").getString("html_url");
                        displayUserAvatar(committer_avatar_url, committer_html_url, committerAvatarView);
                    }
                    else if (!commit.isNull("commit")&& !commit.getJSONObject("commit").isNull("committer")) {
                        committer_login = commit.getJSONObject("commit").getJSONObject("committer").getString("name");
                        committerAvatarView.setVisibility(View.GONE);
                    }
                    if (!commit.isNull("commit") && !commit.getJSONObject("commit").isNull("committer"))
                        date = commit.getJSONObject("commit").getJSONObject("committer").getString("date");
                    if (!commit.isNull("author")) {
                        author_id = commit.getJSONObject("author").getString("id");
                        if (author_id.equals(committer_id))
                            authorLayout.setVisibility(View.GONE);
                        else {
                            author_login = commit.getJSONObject("author").getString("login");
                            author_avatar_url = commit.getJSONObject("author").getString("avatar_url");
                            author_html_url = commit.getJSONObject("author").getString("html_url");
                            displayUserAvatar(author_avatar_url, author_html_url, authorAvatarView);
                            authorLoginView.setText(author_login);
                        }
                    }
                    else if (!commit.isNull("commit")) {
                        author_login = commit.getJSONObject("commit").getJSONObject("author").getString("name");
                        if (author_login.equals(committer_login))
                            authorLayout.setVisibility(View.GONE);
                        else {
                            authorLoginView.setText(author_login);
                            authorAvatarView.setVisibility(View.GONE);
                        }
                    }
                    html_url = commit.getString("html_url");
                    titleView.setText(title);
                    messageView.setText(message);
                    committerLoginView.setText(committer_login);
                    dateView.setText(date);

                    if (!commit.isNull("files")) {
                        JSONArray files = commit.getJSONArray("files");
                        filesTitle.setText(files.length() + " files changed");
                        for (int i = 0; i < files.length(); i++) {
                            if (!files.isNull(i)) {
                                String filename, status;
                                filename = files.getJSONObject(i).getString("filename");
                                status = files.getJSONObject(i).getString("status");
                                TextView fileView = new TextView(getApplicationContext());
                                fileView.setText("[" + status + "] " + filename);
                                fileView.setTextAppearance(getApplicationContext(), android.R.style.TextAppearance_Material_Body1);
                                fileView.setTextColor(Color.BLACK);
                                filesList.addView(fileView, i);
                            }
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                } catch (JSONException error) {
                    CharSequence log  = "Impossible to retrieve data (" + error.getMessage() + ")";
                    Toast.makeText(getApplicationContext(), log, Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                    detailsLayout.setVisibility(View.GONE);
                    error.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                CharSequence log;
                if (error.getMessage() != null)
                    log  = "Impossible to retrieve data (" + error.getMessage() + ")";
                else
                    log  = "Impossible to retrieve data";
                Toast.makeText(getApplicationContext(), log, Toast.LENGTH_LONG).show();
                detailsLayout.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                error.printStackTrace();
            }
        });
        MainActivity.mRequestQueue.add(jsObjRequest);
    }

    /**
     * Make request and display avatar image of a particular GitLab login. It also define the onClickListener of the image click to open a web browser with the url view of the login.
     * @param imageUrl String value of the image url to retrieve
     * @param userUrl String value of the login html view to shows when user click on the image.
     * @param userAvatarView UI Object view on which display the image.
     */
    private void displayUserAvatar(String imageUrl, final String userUrl, final ImageButton userAvatarView) {
        ImageRequest imageRequest=new ImageRequest (imageUrl, new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap image) {
                userAvatarView.setVisibility(View.VISIBLE);
                Bitmap scaledImage = Bitmap.createScaledBitmap(image, userAvatarView.getWidth(), userAvatarView.getHeight(), false);
                userAvatarView.setImageBitmap(scaledImage);
            }
        },0,0, ImageView.ScaleType.CENTER_INSIDE,null, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Bitmap scaledImage = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.image_not_found), userAvatarView.getWidth(), userAvatarView.getHeight(), false);
                userAvatarView.setImageBitmap(scaledImage);
                CharSequence log;
                if (error.getMessage() != null)
                    log  = "Impossible to retrieve data (" + error.getMessage() + ")";
                else
                    log  = "Impossible to retrieve data";
                Toast.makeText(getApplicationContext(), log, Toast.LENGTH_LONG).show();
                error.printStackTrace();
            }
        });
        MainActivity.mRequestQueue.add(imageRequest);

        View.OnClickListener onImageClick = new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(userUrl));
                startActivity(i);
            }
        };
        userAvatarView.setOnClickListener(onImageClick);
    }
}
