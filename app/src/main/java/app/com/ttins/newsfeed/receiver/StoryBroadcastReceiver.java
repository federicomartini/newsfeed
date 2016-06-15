package app.com.ttins.newsfeed.receiver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StoryBroadcastReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = StoryBroadcastReceiver.class.getSimpleName();
    private static Listener listener;

    public void setListener(Listener listener) {
        if (listener != null) {
            StoryBroadcastReceiver.listener = listener;
        }
        else {
            Log.d(LOG_TAG, "setListener: listener is null");
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(listener != null) {
            listener.onFetchStory();
        } else {
            Log.d(LOG_TAG, "Listener is null");
        }
    }

    public interface Listener {
        void onFetchStory();
    }
}
