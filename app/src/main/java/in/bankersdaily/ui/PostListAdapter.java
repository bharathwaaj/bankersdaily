package in.bankersdaily.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import in.bankersdaily.R;
import in.bankersdaily.model.Post;
import in.bankersdaily.util.FormatDate;
import in.bankersdaily.util.SingleTypeAdapter;

public class PostListAdapter extends SingleTypeAdapter<Post> {

    private Context context;

    /**
     * @param context
     * @param items
     */
    public PostListAdapter(final Context context, final List<Post> items) {
        super(context, R.layout.post_list_item);
        this.context = context;
        setItems(items);
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.title, R.id.date };
    }

    @Override
    protected void update(final int position, final Post Post) {
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Post post = getItem(position);
        if(convertView == null) {
            convertView = LayoutInflater.from(context).inflate( R.layout.post_list_item, null);
        }

        TextView title = (TextView)convertView.findViewById(R.id.title);
        title.setText(Html.fromHtml(post.getTitle()));
        TextView date = (TextView)convertView.findViewById(R.id.date);
        date.setText(FormatDate.getAbbreviatedTimeSpan(post.getDate().getTime()));
        return convertView;
    }

}
