package app.com.ttins.newsfeed.utils;

import android.text.Html;

public class Utility {

    static public String stripHtml(String html) {
        return Html.fromHtml(html).toString();
    }
}
