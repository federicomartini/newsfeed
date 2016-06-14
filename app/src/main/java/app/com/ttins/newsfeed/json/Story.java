package app.com.ttins.newsfeed.json;

import android.os.Parcel;
import android.os.Parcelable;

public class Story implements Parcelable {

    private String title;
    private String author;
    private String imgUrl;
    private String content;
    private String publishedDate;
    private String link;

    public Story() {}

    public Story(Parcel in) {
        this.title = in.readString();
        this.author = in.readString();
        this.imgUrl = in.readString();
        this.content = in.readString();
        this.publishedDate = in.readString();
        this.link = in.readString();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthor() {
        return this.author;
    }

    public void setUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getUrl() {
        return this.imgUrl;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getPublishedDate() {
        return this.publishedDate;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLink() {
        return this.link;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(author);
        dest.writeString(imgUrl);
        dest.writeString(content);
        dest.writeString(publishedDate);
        dest.writeString(link);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Story> CREATOR = new Parcelable.Creator<Story>() {
        @Override
        public Story[] newArray(int size) {
            return new Story[size];
        }

        @Override
        public Story createFromParcel(Parcel source) {
            return new Story(source);
        }
    };
}
