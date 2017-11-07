package in.bankersdaily.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.List;

import in.bankersdaily.BankersDailyApp;
import in.bankersdaily.R;
import in.bankersdaily.model.Category;
import in.bankersdaily.model.CategoryDao;
import in.bankersdaily.network.ApiClient;
import in.bankersdaily.network.CategoryPager;
import in.bankersdaily.util.SingleTypeAdapter;

public class CategoryListFragment extends BaseDBListViewFragment<Category, Long> {

    public static final String PARENT_ID = "parentId";

    private CategoryPager pager;
    private CategoryDao categoryDao;
    private int parentId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        categoryDao = BankersDailyApp.getDaoSession(getContext()).getCategoryDao();
        if (getArguments() != null) {
            parentId = getArguments().getInt(PARENT_ID);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        if (parentId != 0) {
            // Avoid network request on display sub categories.
            firstCallBack = false;
            swipeRefreshLayout.setEnabled(false);
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected CategoryPager getPager() {
        if (pager == null) {
            pager = new CategoryPager(new ApiClient(getContext()));
        }
        return pager;
    }

    @Override
    protected CategoryDao getDao() {
        return categoryDao;
    }

    @Override
    protected SingleTypeAdapter<Category> createAdapter(List<Category> items) {
        return new CategoryListAdapter(getActivity(), categoryDao, parentId);
    }

    @Override
    protected void setNoItemsText() {
        setEmptyText(R.string.no_categories, R.string.no_categories_description, R.drawable.no_news);
    }

}
