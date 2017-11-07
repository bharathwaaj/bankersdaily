package in.bankersdaily.ui;

import android.content.Context;
import android.text.Html;

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

    private Context context;
    private PostDao postDao;
    private int categoryId;
    private int count;
    private QueryBuilder<Post> queryBuilder;

    PostListAdapter(final Context context, final PostDao postDao, int categoryId) {
        super(context, R.layout.post_list_item);
        this.context = context;
        this.postDao = postDao;
        this.categoryId = categoryId;
        queryBuilder = getQueryBuilder();
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

    private QueryBuilder<Post> getQueryBuilder() {
        QueryBuilder<Post> queryBuilder = postDao.queryBuilder().orderDesc(PostDao.Properties.Date);
        if (categoryId != 0) {
            queryBuilder
                    .join(JoinPostsWithCategories.class, JoinPostsWithCategoriesDao.Properties.PostId)
                    .where(JoinPostsWithCategoriesDao.Properties.CategoryId.eq(categoryId));
        }
        return queryBuilder;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.title, R.id.date, R.id.category_layout, R.id.category };
    }

    @Override
    public void notifyDataSetChanged() {
        count = (int) getQueryBuilder().count();
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
    }

}
