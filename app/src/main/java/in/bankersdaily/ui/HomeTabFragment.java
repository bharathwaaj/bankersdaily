package in.bankersdaily.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import in.bankersdaily.R;

public class HomeTabFragment extends Fragment {

    public static final String CURRENT_ITEM = "currentItem";

    @BindView(R.id.viewpager) ViewPager viewPager;
    @BindView(R.id.tab_layout) TabLayout tabLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_tab_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        HomeTabPagerAdapter adapter =
                new HomeTabPagerAdapter(getResources(), getChildFragmentManager(), getArguments());

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setOffscreenPageLimit(4);
        Bundle data = getArguments();
        if (data != null) {
            viewPager.setCurrentItem(data.getInt(HomeTabFragment.CURRENT_ITEM, 0));
        } else {
            viewPager.setCurrentItem(0);
        }
    }

}
