package in.bankersdaily.ui;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;

import org.greenrobot.greendao.query.LazyList;

import java.lang.reflect.Field;

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
