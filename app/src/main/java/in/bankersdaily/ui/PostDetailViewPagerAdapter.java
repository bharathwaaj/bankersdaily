package in.bankersdaily.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;

import org.greenrobot.greendao.query.LazyList;

import in.bankersdaily.model.Post;

public class PostDetailViewPagerAdapter extends FragmentPagerAdapter {

    private Activity activity;
    private LazyList<Post> posts;

    PostDetailViewPagerAdapter(AppCompatActivity activity, LazyList<Post> posts) {
        super(activity.getSupportFragmentManager());
        this.activity = activity;
        this.posts = posts;
    }

    @Override
    public int getCount() {
        return posts.size();
    }

    @Override
    public Fragment getItem(int position) {
        PostDetailFragment fragment = new PostDetailFragment();
        Post post = posts.get(position);
        Bundle bundle = activity.getIntent().getExtras();
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putParcelable(PostDetailFragment.POST, post);
        fragment.setArguments(bundle);
        return fragment;
    }

}
