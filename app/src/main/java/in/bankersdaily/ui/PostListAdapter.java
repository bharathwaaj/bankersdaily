package in.bankersdaily.ui;

import android.content.Context;
import android.text.Html;

import in.bankersdaily.R;
import in.bankersdaily.model.Post;
import in.bankersdaily.model.PostDao;
import in.bankersdaily.util.FormatDate;
import in.bankersdaily.util.SingleTypeAdapter;

public class PostListAdapter extends SingleTypeAdapter<Post> {

    private Context context;
    private PostDao postDao;

    PostListAdapter(final Context context, final PostDao postDao) {
        super(context, R.layout.post_list_item);
        this.context = context;
        this.postDao = postDao;
    }

    @Override
    public int getCount() {
        return (int) postDao.queryBuilder().count();
    }

    @Override
    public Post getItem(int position) {
        return postDao.queryBuilder().orderDesc(PostDao.Properties.Date).listLazy().get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.title, R.id.date };
    }

    @Override
    protected void update(final int position, final Post post) {
        setText(0, Html.fromHtml(post.getTitle()));
        setText(1, FormatDate.getAbbreviatedTimeSpan(post.getDate().getTime()));
    }

}
