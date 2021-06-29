package in.bankersdaily.ui;

import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;

import in.bankersdaily.model.Post;

public class PromotionPagerAdapter extends FragmentStatePagerAdapter {

    List<Post> posts;
    PromotionPagerAdapter(final FragmentManager fragmentManager, final List<Post> posts) {
        super(fragmentManager);
        this.posts = posts;
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        Post post = this.posts.get(position);
        PromotionFragment promotionFragment = new PromotionFragment();
        Bundle args = new Bundle();
        args.putParcelable("post", post);
        promotionFragment.setArguments(args);
        return promotionFragment;
    }

    @Override
    public int getCount() {
        return this.posts.size();
    }
}
