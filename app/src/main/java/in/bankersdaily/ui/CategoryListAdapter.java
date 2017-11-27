package in.bankersdaily.ui;

import android.app.Activity;
import android.content.Intent;
import android.text.Html;
import android.view.View;

import org.greenrobot.greendao.query.LazyList;
import org.greenrobot.greendao.query.QueryBuilder;

import in.bankersdaily.R;
import in.bankersdaily.model.Category;
import in.bankersdaily.model.CategoryDao;
import in.bankersdaily.util.SingleTypeAdapter;

public class CategoryListAdapter extends SingleTypeAdapter<Category> {

    private Activity activity;
    private CategoryDao categoryDao;
    private int parentId;
    private LazyList<Category> categories;

    CategoryListAdapter(Activity activity, CategoryDao categoryDao, int parentId) {
        super(activity, R.layout.category_list_item);
        this.activity = activity;
        this.categoryDao = categoryDao;
        this.parentId = parentId;
        categories = getQueryBuilder().listLazy();
    }

    @Override
    public int getCount() {
        return categories.size();
    }

    @Override
    public Category getItem(int position) {
        return categories.get(position);
    }

    private QueryBuilder<Category> getQueryBuilder() {
        return categoryDao.queryBuilder()
                .orderAsc(CategoryDao.Properties.Name)
                .where(CategoryDao.Properties.Parent.eq(parentId));
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getId();
    }

    @Override
    public void notifyDataSetChanged() {
        categories = getQueryBuilder().listLazy();
        super.notifyDataSetChanged();
    }

    @Override
    protected int[] getChildViewIds() {
        return new int[] { R.id.title, R.id.ripple_layout };
    }

    @Override
    protected void update(final int position, final Category category) {
        setText(0, Html.fromHtml(category.getName()));
        view(1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int childCount = (int) categoryDao.queryBuilder()
                        .where(CategoryDao.Properties.Parent.eq(category.getId()))
                        .count();

                Intent intent;
                if (childCount > 0) {
                    intent = new Intent(activity, CategoryListActivity.class);
                    intent.putExtra(CategoryListFragment.PARENT_ID, category.getId().intValue());
                } else {
                    intent = new Intent(activity, PostListActivity.class);
                    intent.putExtra(PostsListFragment.CATEGORY_ID, category.getId().intValue());
                }
                intent.putExtra(BaseToolBarActivity.ACTIONBAR_TITLE, category.getName());
                activity.startActivity(intent);
            }
        });
    }

}
