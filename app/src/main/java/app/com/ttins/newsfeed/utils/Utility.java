package app.com.ttins.newsfeed.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.Html;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Utility {

    static public String stripHtml(String html) {
        return Html.fromHtml(html).toString();
    }

    static public String readIt(InputStream stream) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader responseReader = new BufferedReader(new InputStreamReader(stream));
        String line = responseReader.readLine();

        while (line != null){
            builder.append(line);
            line = responseReader.readLine();
        }

        return builder.toString();
    }

    static public void enableReceiver(Context context, Class broadcastReceiver) {
        PackageManager pm  = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, broadcastReceiver);
        pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    static public void disableReceiver(Context context, Class broadcastReceiver) {
        PackageManager pm  = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, broadcastReceiver);
        pm.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}
