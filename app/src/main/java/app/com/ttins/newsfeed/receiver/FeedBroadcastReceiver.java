package app.com.ttins.newsfeed.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class FeedBroadcastReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = FeedBroadcastReceiver.class.getSimpleName();
    private static Listener listener;

    public void setListener(Listener listener) {
        if (listener != null) {
            FeedBroadcastReceiver.listener = listener;
        }
        else {
            Log.d(LOG_TAG, "setListener: listener is null");
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(listener != null) {
            listener.onFetchFeed();
        } else {
            Log.d(LOG_TAG, "Listener is null");
        }
    }

    public interface Listener {
        void onFetchFeed();
    }

}
