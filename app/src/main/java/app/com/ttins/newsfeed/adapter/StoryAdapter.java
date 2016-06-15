package app.com.ttins.newsfeed.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import app.com.ttins.newsfeed.R;
import app.com.ttins.newsfeed.json.Story;

public class StoryAdapter extends ArrayAdapter<Story> {

    private static final String LOG_TAG = StoryAdapter.class.getSimpleName();

    private Context context;
    private ArrayList<Story> stories;

    public StoryAdapter(Context context, ArrayList<Story> stories) {
        super(context, -1, stories);
        this.context = context;
        this.stories = stories;
    }

    @Override
    public int getCount() {
        return stories.size();
    }

    @Override
    public int getPosition(Story item) {
        return super.getPosition(item);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount();
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.story_list_item_layout, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.title = (TextView) view.findViewById(R.id.story_list_item_text_view_title);
            viewHolder.author = (TextView) view.findViewById(R.id.story_list_item_text_view_author);
            viewHolder.publishedDate = (TextView) view.findViewById(R.id.story_list_item_text_view_publish_date);
            view.setTag(viewHolder);
        }

        final ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.title.setText(stories.get(position).getTitle());
        viewHolder.link = stories.get(position).getLink();
        viewHolder.author.setText(stories.get(position).getAuthor());
        viewHolder.publishedDate.setText(stories.get(position).getPublishedDate());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "Link: " + viewHolder.link);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(viewHolder.link));
                context.startActivity(intent);
            }
        });

        return view;
    }

    static class ViewHolder {
        public TextView title;
        public TextView publishedDate;
        public TextView author;
        public ImageView image;
        public String link;
    }
}
