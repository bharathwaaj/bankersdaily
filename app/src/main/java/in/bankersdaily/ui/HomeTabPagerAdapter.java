package in.bankersdaily.ui;

import android.content.res.Resources;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import in.bankersdaily.R;

public class HomeTabPagerAdapter extends FragmentPagerAdapter {
    private final Resources resources;
    private Bundle bundle;

    HomeTabPagerAdapter(final Resources resources, final FragmentManager fragmentManager,
                        Bundle bundle) {
        super(fragmentManager);
        this.resources = resources;
        this.bundle = bundle;
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new HomePromotionsFragment();
                fragment.setArguments(bundle);
                break;
            case 1:
                fragment = new PostsListFragment();
                fragment.setArguments(bundle);
                break;
            case 2:
                fragment = new CategoryListFragment();
                Bundle bundle;
                if (this.bundle == null) {
                    bundle = new Bundle();
                } else {
                    bundle = new Bundle(this.bundle);
                }
                bundle.putInt(CategoryListFragment.PARENT_ID, 0);
                fragment.setArguments(bundle);
                break;
            case 3:
                fragment = new BookmarkedPostsListFragment();
                break;
            default:
                break;
        }
        return fragment;
    }

    @Override
    public CharSequence getPageTitle(final int position) {
        switch (position) {
            case 0:
                return resources.getString(R.string.home);
            case 1:
                return resources.getString(R.string.latest);
            case 2:
                return resources.getString(R.string.categories);
            case 3:
                return resources.getString(R.string.bookmarks);
            default:
                return null;
        }
    }


}
