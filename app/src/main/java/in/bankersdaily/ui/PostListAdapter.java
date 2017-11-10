package in.bankersdaily.ui;

import android.app.Activity;
import android.content.Intent;
import android.text.Html;
import android.view.View;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

import in.bankersdaily.R;
import in.bankersdaily.model.Category;
import in.bankersdaily.model.JoinPostsWithCategories;
import in.bankersdaily.model.JoinPostsWithCategoriesDao;
import in.bankersdaily.model.Post;
import in.bankersdaily.model.PostDao;
import in.bankersdaily.util.FormatDate;
import in.bankersdaily.util.SingleTypeAdapter;

public class PostListAdapter extends SingleTypeAdapter<Post> {

    private Activity activity;
    private PostDao postDao;
    private int categoryId;
    private int count;
    private QueryBuilder<Post> queryBuilder;

    PostListAdapter(Activity activity, PostDao postDao, int categoryId) {
        super(activity, R.layout.post_list_item);
        this.activity = activity;
        this.postDao = postDao;
        this.categoryId = categoryId;
        queryBuilder = Post.getPostListQueryBuilder(postDao, categoryId);
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
                R.id.ripple_layout };
    }

    @Override
    public void notifyDataSetChanged() {
        count = (int) queryBuilder.count();
        super.notifyDataSetChanged();
    }

    @Override
    protected void update(final int position, final Post post) {
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
                intent.putExtra(BaseToolBarActivity.ACTIONBAR_TITLE, post.getTitle());
                intent.putExtra(PostsListFragment.CATEGORY_ID, categoryId);
                activity.startActivity(intent);
            }
        });
    }

}
