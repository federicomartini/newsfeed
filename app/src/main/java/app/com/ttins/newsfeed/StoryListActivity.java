package app.com.ttins.newsfeed;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import app.com.ttins.newsfeed.adapter.StoryAdapter;
import app.com.ttins.newsfeed.json.Story;

public class StoryListActivity extends AppCompatActivity {

    private static final String LOG_TAG = StoryListActivity.class.getSimpleName();

    ListView storyListView;
    TextView emptyListTextView;
    CollapsingToolbarLayout collapsingToolbarLayout;
    Toolbar toolbar;
    String storyIntentArgument;
    ArrayList<Story> storyList = new ArrayList<>();
    StoryAdapter storyAdapter;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.story_list_activity_layout);

        storyListView = (ListView) findViewById(R.id.news_list_view_story_list_activity);
        emptyListTextView = (TextView) findViewById(R.id.news_empty_list_text_view_story_list_activity);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_story_list_activity);
        toolbar = (Toolbar) findViewById(R.id.toolbar_story_list_activity);

        if (getIntent().hasExtra(getString(R.string.INTENT_ACTION_STORY_LIST))) {
            storyIntentArgument = getIntent()
                    .getStringExtra(getString(R.string.INTENT_ACTION_STORY_LIST));
            Log.d(LOG_TAG, "Story List: " + storyIntentArgument);
            HttpAsyncTask loadFeedAsyncTask = new HttpAsyncTask();
            loadFeedAsyncTask.execute(getString(R.string.http_load_feed_query_address), storyIntentArgument);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            storyListView.setNestedScrollingEnabled(true);
        }

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        collapsingToolbarLayout.setTitle(getString(R.string.app_name));
        storyListView.setEmptyView(emptyListTextView);
    }

    public class HttpAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            try {
                httpRequest(params[0], params[1]);
            } catch (IOException e) {
                Log.d(LOG_TAG, "Error on Http Request");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            updateStoriesList();
        }
    }

    private void updateStoriesList() {
        Log.d(LOG_TAG, "updateStoriesList");
        storyAdapter = new StoryAdapter(this, storyList);
        storyListView.setAdapter(storyAdapter);
        storyAdapter.notifyDataSetChanged();
    }

    public String readIt(InputStream stream) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader responseReader = new BufferedReader(new InputStreamReader(stream));
        String line = responseReader.readLine();

        while (line != null){
            builder.append(line);
            line = responseReader.readLine();
        }

        return builder.toString();
    }

    private void httpRequest(String address, String query) throws IOException {
        InputStream inputStream = null;

        try {
            URL url;
            String encodedQuery = URLEncoder.encode(query, "utf-8");
            url = new URL(address + encodedQuery);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setReadTimeout(getResources().getInteger(R.integer.http_read_timeout));
            httpURLConnection.setConnectTimeout(getResources().getInteger(R.integer.http_connect_timeout));
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setDoInput(true);
            httpURLConnection.connect();

            int response = httpURLConnection.getResponseCode();

            switch (response) {
                case HttpURLConnection.HTTP_OK:
                    inputStream = httpURLConnection.getInputStream();
                    String stringResponse = readIt(inputStream);
                    parseJsonStories(stringResponse);
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    showShortToast(getString(R.string.http_url_not_found_message));
                    break;
                default:
                    showShortToast(getString(R.string.http_generic_error_message));
                    break;
            }

        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private void parseJsonStories(String stringFromInputStream) {
        storyList.clear();

        try {
            JSONObject jsonObject = new JSONObject(stringFromInputStream);
            JSONObject jResponseData = jsonObject.getJSONObject(getString(R.string.json_stories_response_data_key));
            JSONObject jFeed = jResponseData.getJSONObject(getString(R.string.json_stories_feed_key));
            JSONArray jEntryArray = jFeed.getJSONArray(getString(R.string.json_stories_feed_entries_key));

            for(int i = 0; i < jEntryArray.length(); i++) {
                Story story = new Story();
                story.setTitle(jEntryArray.getJSONObject(i).getString(getString(R.string.json_stories_feed_entry_title_key)));
                story.setLink(jEntryArray.getJSONObject(i).getString(getString(R.string.json_stories_feed_entry_link_key)));
                storyList.add(story);
            }

        } catch (JSONException e) {
            Log.d(LOG_TAG, "JSONException");
            e.printStackTrace();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void showShortToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
