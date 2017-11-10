package in.bankersdaily.ui;

import android.os.Bundle;
import android.support.v4.view.ViewPager;

import butterknife.BindView;
import butterknife.ButterKnife;
import in.bankersdaily.BankersDailyApp;
import in.bankersdaily.R;
import in.bankersdaily.model.CategoryDao;

import static in.bankersdaily.ui.PostsListFragment.CATEGORY_ID;

public class PostDetailActivity extends BaseToolBarActivity {

    public static final String POST_POSITION = "postPosition";

    @BindView(R.id.viewpager) ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        ButterKnife.bind(this);
        int categoryId = getIntent().getIntExtra(CATEGORY_ID, 0);
        if (categoryId != 0) {
            String title = BankersDailyApp.getDaoSession(this).getCategoryDao().queryBuilder()
                .where(CategoryDao.Properties.Id.eq(categoryId)).list().get(0).getName();

            //noinspection ConstantConditions
            getSupportActionBar().setTitle(title);
        }
        PostDetailViewPagerAdapter adapter = new PostDetailViewPagerAdapter(this);
        viewPager.setAdapter(adapter);
        int currentPosition = getIntent().getIntExtra(POST_POSITION, 0);
        if (currentPosition < adapter.getCount()) {
            viewPager.setCurrentItem(currentPosition);
        }
    }

}
