package in.bankersdaily.ui;

import android.os.Bundle;

import junit.framework.Assert;

import in.bankersdaily.BankersDailyApp;
import in.bankersdaily.R;

public class CategoryListActivity extends BaseToolBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_layout);
        String title = getIntent().getStringExtra(ACTIONBAR_TITLE);
        Assert.assertNotNull("ACTIONBAR_TITLE must not be null.", title);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(title);
        CategoryListFragment fragment = new CategoryListFragment();
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    protected String getScreenName() {
        return BankersDailyApp.CATEGORIES_LIST_ACTIVITY;
    }
}
