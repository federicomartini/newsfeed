package app.com.ttins.newsfeed;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.SystemClock;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import java.util.Date;

import app.com.ttins.newsfeed.adapter.FeedAdapter;
import app.com.ttins.newsfeed.json.Feed;
import app.com.ttins.newsfeed.receiver.FeedBroadcastReceiver;


public class MainActivity extends AppCompatActivity implements FeedAdapter.Listener,
                                                                FeedBroadcastReceiver.Listener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    ListView feedListView;
    TextView emptyListTextView;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton fab;
    Toolbar toolbar;
    ArrayList<Feed> feedList = new ArrayList<>();
    FeedAdapter feedAdapter;
    AlarmManager alarmManager;
    FeedBroadcastReceiver mReceiver;
    PendingIntent pendingIntent;
    Intent intentFeed;
    IntentFilter intentFilter;


    public void onSetAlarm()
    {
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                1000,
                pendingIntent);
    }

    private void registerAlarmBroadcast()
    {
        intentFeed = new Intent(this, FeedBroadcastReceiver.class);
        intentFeed.setAction(getString(R.string.receiver_action_update_feed));
        pendingIntent = PendingIntent
                .getBroadcast(this, 0, intentFeed, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager = (AlarmManager)(this.getSystemService(Context.ALARM_SERVICE));
    }

    private void unregisterAlarmBroadcast()
    {
        alarmManager.cancel(pendingIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intentFilter = new IntentFilter(getString(R.string.receiver_action_update_feed));
        mReceiver = new FeedBroadcastReceiver();
        mReceiver.setListener(this);
        feedListView = (ListView) findViewById(R.id.news_list_view_main_activity);
        emptyListTextView = (TextView) findViewById(R.id.news_empty_list_text_view_main_activity);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_main_activity);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        collapsingToolbarLayout.setTitle(getString(R.string.app_name));
        feedListView.setEmptyView(emptyListTextView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            feedListView.setNestedScrollingEnabled(true);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] params = {getResources().getString(R.string.http_find_feed_query_address),
                        getString(R.string.news_topic_query)};
                HttpAsyncTask loadNewsAsyncTask = new HttpAsyncTask();
                loadNewsAsyncTask.execute(params);
            }
        });

        registerAlarmBroadcast();
        onSetAlarm();
    }

    private void registerFeedReceiver() {
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerFeedReceiver();
        enableReceiver();
        //sendBroadcast(intentFeed);
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
            updateFeedList();
        }
    }

    private void updateFeedList() {
        feedAdapter = new FeedAdapter(getBaseContext(), feedList, this);
        feedListView.setAdapter(feedAdapter);
        feedAdapter.notifyDataSetChanged();
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
                    parseJsonFeed(stringResponse);
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

    private void showShortToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    public void parseJsonFeed(String stringFromInputStream) {
        try {
            feedList.clear();
            JSONObject jsonObject = new JSONObject(stringFromInputStream);
            JSONObject jResponseData = jsonObject.getJSONObject(getString(R.string.json_feed_response_data_key));
            JSONArray jEntryArray = jResponseData.getJSONArray(getString(R.string.json_feed_entries_key));

            for(int i = 0; i < jEntryArray.length(); i++) {
                Feed feed = new Feed();
                feed.setUrl(jEntryArray.getJSONObject(i)
                        .getString(getString(R.string.json_feed_url_key)));
                feed.setTitle(jEntryArray.getJSONObject(i)
                        .getString(getString(R.string.json_feed_title_key)));
                feed.setContentSnippet(jEntryArray.getJSONObject(i)
                        .getString(getString(R.string.json_feed_content_snippet_key)));
                feed.setLink(jEntryArray.getJSONObject(i)
                        .getString(getString(R.string.json_feed_link_key)));

                feedList.add(feed);
            }

        } catch (JSONException e) {
            Log.d(LOG_TAG, "JSONException");
            e.printStackTrace();
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterAlarmBroadcast();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
        disableReceiver();
    }

    @Override
    public void onFeedClickListener(String url) {
        Intent intent = new Intent(this, StoryListActivity.class);
        intent.putExtra(getString(R.string.INTENT_ACTION_STORY_LIST), url);
        startActivity(intent);
    }

    public void onFetchFeed() {
        Log.d(LOG_TAG, "onFetchFeed");
        String[] params = {getResources().getString(R.string.http_find_feed_query_address),
                getString(R.string.news_topic_query)};
        HttpAsyncTask loadNewsAsyncTask = new HttpAsyncTask();
        loadNewsAsyncTask.execute(params);
    }

    private void enableReceiver() {
        PackageManager pm  = MainActivity.this.getPackageManager();
        ComponentName componentName = new ComponentName(this, FeedBroadcastReceiver.class);
        pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    private void disableReceiver() {
        PackageManager pm  = MainActivity.this.getPackageManager();
        ComponentName componentName = new ComponentName(this, FeedBroadcastReceiver.class);
        pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

}
