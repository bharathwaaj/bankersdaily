package in.bankersdaily.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.tobishiba.circularviewpager.library.BaseCircularViewPagerAdapter;

import java.util.List;

import in.bankersdaily.model.Post;

public class PromotionPagerAdapter extends BaseCircularViewPagerAdapter<Post> {

    PromotionPagerAdapter(final FragmentManager fragmentManager, final List<Post> posts) {
        super(fragmentManager, posts);
    }

    @Override
    protected Fragment getFragmentForItem(Post post) {
        PromotionFragment promotionFragment = new PromotionFragment();
        Bundle args = new Bundle();
        args.putParcelable("post", post);
        promotionFragment.setArguments(args);
        return promotionFragment;
    }

}
