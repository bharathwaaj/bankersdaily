package in.bankersdaily.ui;

import android.support.v4.app.Fragment;

import in.bankersdaily.BankersDailyApp;

public abstract class BaseFragment extends Fragment {

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && getActivity() != null) {
            trackScreenViewAnalytics();
        }
    }

    protected abstract String getScreenName();

    protected void trackScreenViewAnalytics() {
        BankersDailyApp.getInstance().trackScreenView(getActivity(), getScreenName());
    }

}
