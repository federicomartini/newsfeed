package app.com.ttins.newsfeed.json;

import android.os.Parcel;
import android.os.Parcelable;

public class Feed implements Parcelable {

    private String url;
    private String title;
    private String contentSnippet;
    private String link;

    public Feed() {}

    public Feed(Parcel in) {
        this.url = in.readString();
        this.title = in.readString();
        this.contentSnippet = in.readString();
        this.link = in.readString();
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return this.url;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.contentSnippet;
    }

    public void setContentSnippet(String contentSnippet) {
        this.contentSnippet = contentSnippet;
    }

    public String getContentSnippet() {
        return this.contentSnippet;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLink() {
        return this.link;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.url);
        dest.writeString(this.title);
        dest.writeString(this.contentSnippet);
        dest.writeString(this.link);
    }

    public static final Parcelable.Creator<Feed> CREATOR = new Parcelable.Creator<Feed>() {
        @Override
        public Feed[] newArray(int size) {
            return new Feed[size];
        }

        @Override
        public Feed createFromParcel(Parcel source) {
            return new Feed(source);
        }
    };
}
