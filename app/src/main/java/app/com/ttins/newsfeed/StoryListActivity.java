package app.com.ttins.newsfeed;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
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
import app.com.ttins.newsfeed.receiver.StoryBroadcastReceiver;
import app.com.ttins.newsfeed.utils.Utility;

public class StoryListActivity extends AppCompatActivity implements StoryBroadcastReceiver.Listener {

    private static final String LOG_TAG = StoryListActivity.class.getSimpleName();

    ListView storyListView;
    TextView emptyListTextView;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton fab;
    Toolbar toolbar;
    String storyIntentArgument;
    ArrayList<Story> storyList = new ArrayList<>();
    StoryAdapter storyAdapter;
    StoryBroadcastReceiver mReceiver;
    Intent intentStory;
    PendingIntent pendingIntent;
    AlarmManager alarmManager;
    IntentFilter intentFilter;

    @Override
    protected void onResume() {
        super.onResume();
        Utility.enableReceiver(StoryListActivity.this, StoryBroadcastReceiver.class);
        updateStoriesList();
    }

    public void onSetAlarm()
    {
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                0,
                AlarmManager.INTERVAL_HALF_HOUR,
                pendingIntent);
    }

    private void registerAlarmBroadcast()
    {
        intentStory = new Intent(this, StoryBroadcastReceiver.class);
        intentStory.setAction(getString(R.string.receiver_action_update_story));
        pendingIntent = PendingIntent
                .getBroadcast(this, 0, intentStory, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager = (AlarmManager)(this.getSystemService(Context.ALARM_SERVICE));
    }

    private void unregisterAlarmBroadcast()
    {
        alarmManager.cancel(pendingIntent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.story_list_activity_layout);

        intentFilter = new IntentFilter(getString(R.string.receiver_action_update_story));
        mReceiver = new StoryBroadcastReceiver();
        mReceiver.setListener(this);
        storyListView = (ListView) findViewById(R.id.news_list_view_story_list_activity);
        emptyListTextView = (TextView) findViewById(R.id.news_empty_list_text_view_story_list_activity);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_story_list_activity);
        toolbar = (Toolbar) findViewById(R.id.toolbar_story_list_activity);
        fab = (FloatingActionButton) findViewById(R.id.story_refresh_fab);

        if (getIntent().hasExtra(getString(R.string.INTENT_ACTION_STORY_LIST))) {
            storyIntentArgument = getIntent()
                    .getStringExtra(getString(R.string.INTENT_ACTION_STORY_LIST));
            HttpAsyncTask loadFeedAsyncTask = new HttpAsyncTask();
            loadFeedAsyncTask.execute(getString(R.string.http_load_feed_query_address), storyIntentArgument);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            storyListView.setNestedScrollingEnabled(true);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (storyIntentArgument != null) {
                    String[] params = {getResources().getString(R.string.http_load_feed_query_address),
                            storyIntentArgument};
                    HttpAsyncTask loadNewsAsyncTask = new HttpAsyncTask();
                    loadNewsAsyncTask.execute(params);
                    showShortToast(getString(R.string.updating_list));
                } else {
                    showShortToast(getString(R.string.http_generic_error_message));
                }
            }
        });

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        collapsingToolbarLayout.setTitle(getString(R.string.app_name));
        storyListView.setEmptyView(emptyListTextView);

        registerAlarmBroadcast();
        onSetAlarm();

    }

    public class HttpAsyncTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            try {
                httpRequest(params[0], params[1]);
            } catch (IOException e) {
                showShortToast(getString(R.string.http_generic_error_message));
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
        storyAdapter = new StoryAdapter(this, storyList);
        storyListView.setAdapter(storyAdapter);
        storyAdapter.notifyDataSetChanged();
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
                    String stringResponse = Utility.readIt(inputStream);
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
                story.setTitle(jEntryArray.getJSONObject(i)
                        .getString(getString(R.string.json_stories_feed_entry_title_key)));
                story.setLink(jEntryArray.getJSONObject(i)
                        .getString(getString(R.string.json_stories_feed_entry_link_key)));
                story.setPublishedDate(jEntryArray.getJSONObject(i)
                        .getString(getString(R.string.json_stories_feed_entry_published_date_key))
                        .substring(0, getResources().getInteger(R.integer.num_chars_published_date)));
                story.setAuthor(jEntryArray.getJSONObject(i)
                        .getString(getString(R.string.json_stories_feed_entry_author_key)));
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
        unregisterAlarmBroadcast();
        Utility.disableReceiver(StoryListActivity.this, StoryBroadcastReceiver.class);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void showShortToast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onFetchStory() {
        String[] params = {getResources().getString(R.string.http_load_feed_query_address),
                storyIntentArgument};
        HttpAsyncTask loadStoryAsyncTask = new HttpAsyncTask();
        loadStoryAsyncTask.execute(params);
    }

}
