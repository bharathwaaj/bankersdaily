package in.bankersdaily.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;

import org.greenrobot.greendao.query.QueryBuilder;

import in.bankersdaily.BankersDailyApp;
import in.bankersdaily.model.Post;
import in.bankersdaily.model.PostDao;

import static in.bankersdaily.ui.PostsListFragment.CATEGORY_ID;

public class PostDetailViewPagerAdapter extends FragmentPagerAdapter {

    private Activity activity;
    private QueryBuilder<Post> queryBuilder;
    private int count;

    PostDetailViewPagerAdapter(AppCompatActivity activity) {
        super(activity.getSupportFragmentManager());
        this.activity = activity;
        PostDao postDao = BankersDailyApp.getDaoSession(activity).getPostDao();
        int categoryId = activity.getIntent().getIntExtra(CATEGORY_ID, 0);
        queryBuilder = Post.getPostListQueryBuilder(postDao, categoryId);
        count = (int) queryBuilder.count();
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public Fragment getItem(int position) {
        PostDetailFragment fragment = new PostDetailFragment();
        Post post = queryBuilder.listLazy().get(position);
        Bundle bundle = activity.getIntent().getExtras();
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putString(PostDetailFragment.POST_SLUG, post.getSlug());
        fragment.setArguments(bundle);
        return fragment;
    }

}
