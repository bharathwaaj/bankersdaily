package in.bankersdaily.ui;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import in.bankersdaily.R;

public class HomeTabPagerAdapter extends FragmentPagerAdapter {
    private final Resources resources;
    private Bundle bundle;

    public HomeTabPagerAdapter(final Resources resources, final FragmentManager fragmentManager,
                               Bundle bundle) {
        super(fragmentManager);
        this.resources = resources;
        this.bundle = bundle;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new PostsListFragment();
                fragment.setArguments(bundle);
                break;
            case 1:
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
            default:
                break;
        }
        return fragment;

    }

    @Override
    public CharSequence getPageTitle(final int position) {
        switch (position) {
            case 0:
                return resources.getString(R.string.latest_articles);
            case 1:
                return resources.getString(R.string.categories);
            default:
                return null;
        }
    }

}
