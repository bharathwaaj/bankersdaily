package in.bankersdaily.ui;

import android.app.Activity;
import android.content.Intent;
import android.text.Html;
import android.view.View;

import com.bumptech.glide.Glide;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

import in.bankersdaily.R;
import in.bankersdaily.model.Category;
import in.bankersdaily.model.Post;
import in.bankersdaily.model.PostDao;
import in.bankersdaily.util.FormatDate;
import in.bankersdaily.util.SingleTypeAdapter;

public class PostListAdapter extends SingleTypeAdapter<Post> {

    private Activity activity;
    private int categoryId;
    private boolean filterBookmarked;
    private int count;
    private QueryBuilder<Post> queryBuilder;

    PostListAdapter(Activity activity, PostDao postDao, int categoryId, boolean filterBookmarked) {
        super(activity, R.layout.post_list_item);
        this.activity = activity;
        this.categoryId = categoryId;
        this.filterBookmarked =filterBookmarked;
        queryBuilder = Post.getPostListQueryBuilder(postDao, categoryId, filterBookmarked);
        count = (int) queryBuilder.count();
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public Post getItem(int position) {
        return queryBuilder.listLazy().get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.title, R.id.date, R.id.category_layout, R.id.category,
                R.id.ripple_layout, R.id.image_view };
    }

    @Override
    public void notifyDataSetChanged() {
        count = (int) queryBuilder.count();
        super.notifyDataSetChanged();
    }

    @Override
    protected void update(final int position, final Post post) {
        Glide.with(activity)
                .load(post.getFeaturedMedia())
                .placeholder(R.drawable.placeholder_icon)
                .error(R.mipmap.ic_launcher)
                .into(imageView(5));

        setText(0, Html.fromHtml(post.getTitle()));
        setText(1, FormatDate.getAbbreviatedTimeSpan(post.getDate().getTime()));
        if (post.getCategories().isEmpty()) {
            setGone(2, true);
        } else {
            setGone(2, false);
            StringBuilder categoryString = new StringBuilder();
            List<Category> categories = post.getCategories();
            for (int i = 0; i < categories.size();) {
                categoryString.append(categories.get(i).getName());
                if (++i < categories.size()) {
                    categoryString.append(", ");
                }
            }
            setText(3, categoryString);
        }
        view(4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, PostDetailActivity.class);
                intent.putExtra(PostDetailActivity.POST_POSITION, position);
                intent.putExtra(PostsListFragment.CATEGORY_ID, categoryId);
                intent.putExtra(PostDetailActivity.FILTER_BOOKMARKED, filterBookmarked);
                activity.startActivity(intent);
            }
        });
    }

}
