package in.bankersdaily.ui;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.greenrobot.greendao.query.LazyList;
import org.greenrobot.greendao.query.QueryBuilder;

import in.bankersdaily.R;
import in.bankersdaily.model.Post;
import in.bankersdaily.util.FormatDate;
import in.bankersdaily.util.SingleTypeAdapter;
import in.bankersdaily.util.ViewUtils;
import in.testpress.core.TestpressSdk;

import static in.bankersdaily.ui.PostDetailFragment.POST_SLUG;

public class HomePostListAdapter extends SingleTypeAdapter<Post> {

    private QueryBuilder<Post> queryBuilder;
    private LazyList<Post> posts;

    HomePostListAdapter(Context context, int categoryId) {
        super(context, R.layout.post_search_list_item);
        queryBuilder = Post.getPostListQueryBuilder(context, categoryId, false);
        posts = queryBuilder.listLazy();
    }

    @Override
    public int getCount() {
        int totalPostCount = posts.size();
        return totalPostCount > 3 ? 3 : totalPostCount;
    }

    @Override
    public Post getItem(int position) {
        return posts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.title, R.id.date, R.id.category_layout, R.id.category,
                R.id.post_item_layout, R.id.image_view };
    }

    @Override
    public void notifyDataSetChanged() {
        posts = queryBuilder.listLazy();
        super.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = super.getView(position, null, parent);
            ViewUtils.setTypeface(new TextView[] { textView(0), textView(1), textView(3) },
                    TestpressSdk.getRubikRegularFont(convertView.getContext()));

            return convertView;
        }
        return super.getView(position, convertView, parent);
    }

    @Override
    protected void update(final int position, final Post post) {
        final Context context = view(0).getContext();
        Glide.with(context)
                .load(post.getFeaturedMediaSquare())
                .placeholder(R.drawable.placeholder_icon)
                .error(R.mipmap.ic_launcher)
                .into(imageView(5));

        setText(0, Html.fromHtml(post.getTitle()));
        setText(1, FormatDate.getAbbreviatedTimeSpan(post.getDate().getTime()));
        setGone(2, true);
        view(4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra(POST_SLUG, post.getSlug());
                context.startActivity(intent);
            }
        });
    }

}
