package app.com.ttins.newsfeed.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import app.com.ttins.newsfeed.R;
import app.com.ttins.newsfeed.json.Feed;
import app.com.ttins.newsfeed.utils.Utility;

public class FeedAdapter extends ArrayAdapter<Feed> {

    private Context context;
    private ArrayList<Feed> feeds;
    private Listener listener;


    public FeedAdapter(Context context, ArrayList<Feed> feeds, Listener listener) {
        super(context, -1, feeds);
        this.context = context;
        this.feeds = feeds;
        this.listener = listener;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public int getCount() {
        return feeds.size();
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.feed_list_item_layout, null);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.url = (TextView) view.findViewById(R.id.feed_list_item_text_view_url);
            view.setTag(viewHolder);
        }

        final ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.url.setText(Utility.stripHtml(feeds.get(position).getUrl()));

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onFeedClickListener(viewHolder.url.getText().toString());
            }
        });

        return view;
    }

    static class ViewHolder {
        public TextView url;
    }

    public interface Listener {
        void onFeedClickListener(String url);
    }
}
