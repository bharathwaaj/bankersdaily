package in.bankersdaily.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;

import org.greenrobot.greendao.AbstractDao;

import java.util.List;

import in.bankersdaily.BankersDailyApp;
import in.bankersdaily.R;
import in.bankersdaily.model.DaoSession;
import in.bankersdaily.network.BaseResourcePager;
import in.bankersdaily.network.RetrofitException;
import in.bankersdaily.util.ThrowableLoader;

public abstract class BaseDBPagedItemFragment<T, K> extends BaseListViewFragment<T>
        implements AbsListView.OnScrollListener {

    // Loader to load bottom items
    protected static final int MORE_ITEMS_LOADER_ID = 1;

    // Number of maximum items which can be missed from the latest before the db will get reset
    private static final int MISSED_ITEMS_THRESHOLD = 20;

    protected View loadingLayout;

    protected BaseResourcePager<T> refreshPager;
    protected BaseResourcePager<T> pager;

    protected DaoSession daoSession;
    protected List<T> items;

    protected int lastFirstVisibleItem;
    protected boolean isScrollingUp;
    protected boolean isUserRefreshed;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        daoSession = BankersDailyApp.getDaoSession(getContext());
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadingLayout = LayoutInflater.from(getActivity())
                .inflate(R.layout.loading_layout, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListAdapter().notifyDataSetChanged(); // Display data from db
        listView.setOnScrollListener(this);
        listView.setFastScrollEnabled(true);
    }

    @Override
    public Loader<List<T>> onCreateLoader(int loaderID, Bundle args) {
        return new PaginatableItemsLoader<T, K>(this, items, loaderID);
    }

    private static class PaginatableItemsLoader<T, K> extends ThrowableLoader<List<T>> {

        private BaseDBPagedItemFragment<T, K> fragment;
        private int loaderID;

        PaginatableItemsLoader(BaseDBPagedItemFragment<T, K> fragment, List<T> data, int loaderID) {
            super(fragment.getContext(), data);
            this.fragment = fragment;
            this.loaderID = loaderID;
        }

        @Override
        public List<T> loadData() throws RetrofitException {
            switch (loaderID) {
                case LOADER_ID:
                    fragment.getRefreshPager().next();
                    return fragment.getRefreshPager().getResources();
                case MORE_ITEMS_LOADER_ID:
                    fragment.getPager().next();
                    return fragment.getPager().getResources();
                default:
                    //An invalid id was passed
                    return null;
            }
        }
    }

    @Override
    public void onLoadFinished(Loader<List<T>> loader, List<T> items) {
        if (!isUsable())
            return;

        RetrofitException exception = getException(loader);
        if (exception != null) {
            this.exception = exception;
            int errorMessage = getErrorMessage(exception);
            if (!isListItemsEmpty()) {
                showError(errorMessage);
            }

            hideLoadingFooter();
            showList();
            getLoaderManager().destroyLoader(loader.getId());
            return;
        }

        this.exception = null;
        this.items = items;
        switch (loader.getId()) {
            case LOADER_ID:
                onRefreshLoadFinished(items);
                break;
            case MORE_ITEMS_LOADER_ID:
                onMoreItemsLoadFinished(items);
                break;
            default:
                break;
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
        int currentFirstVisibleItem = listView.getFirstVisiblePosition();
        if (currentFirstVisibleItem > lastFirstVisibleItem) {
            isScrollingUp = false;
        } else if (currentFirstVisibleItem < lastFirstVisibleItem) {
            isScrollingUp = true;
        }
        lastFirstVisibleItem = currentFirstVisibleItem;
    }

    @Override
    public void onScroll(AbsListView absListView, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {

        if (!isUsable())
            return;

        if (getLoaderManager().hasRunningLoaders())
            return;

        if (!isScrollingUp && isItemsExistsInDB() && listView != null &&
                ((listView.getLastVisiblePosition() + 3) >=
                        getListAdapter().getWrappedAdapter().getCount())) {

            if (pager == null) {
                if (listView.getVisibility() != View.VISIBLE) {
                    listView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.GONE);
                }
                if (getListAdapter().getFootersCount() == 0) {
                    // Display loading footer if not present
                    getListAdapter().addFooter(loadingLayout);
                }
            } else if (!pager.hasMore()) {
                hideLoadingFooter();
                if (getListAdapter().getCount() == 0) {
                    setNoItemsText();
                    retryButton.setVisibility(View.GONE);
                }
                return;
            }
            getLoaderManager().restartLoader(MORE_ITEMS_LOADER_ID, null, this);
        }
    }

    @Override
    public void refreshWithProgress() {
        stickyView.setVisibility(View.GONE);
        isUserRefreshed = true;
        refreshPager = null;
        super.refreshWithProgress();
    }

    void displayDataFromDB() {
        // This just notifies the adapter that new data is now available in db
        getListAdapter().notifyDataSetChanged();
        if (isListItemsEmpty()) {
            setNoItemsText();
            retryButton.setVisibility(View.GONE);
        }
        showList();
    }

    void onRefreshLoadFinished(List<T> items) {
        // If no data is available in the local database, directly insert & display from database
        if (!isItemsExistsInDB() || items == null || items.isEmpty()) {

            // Remove the sticky notification
            stickyView.setVisibility(View.GONE);

            // Return if no new items are available
            if (items == null || items.isEmpty()) {
                displayDataFromDB();
                return;
            }

            // Insert the items to the database & display
            writeToDBAndDisplay(items, LOADER_ID);
        } else {
            // Data is already available in the local database.
            // Check new posts count is less than threshold.
            if (MISSED_ITEMS_THRESHOLD >= refreshPager.getTotalCount()) {
                if (refreshPager.hasNext()) {
                    getLoaderManager().restartLoader(LOADER_ID, null, this);
                    return;
                }
            }
            if (isUserRefreshed || (lastFirstVisibleItem == 0)) {
                // Directly update UI
                displayNewItems();
            } else {
                // Notify user about the new data to view latest data
                stickyView.setVisibility(View.VISIBLE);
            }
        }
    }

    void onMoreItemsLoadFinished(List<T> items) {

        if (pager != null && !pager.hasMore()) {
            // If pager reached last page remove footer if footer exists.
            hideLoadingFooter();
        }
        
        if (items.isEmpty())
            return;

        writeToDBAndDisplay(items, MORE_ITEMS_LOADER_ID);
    }

    protected void writeToDBAndDisplay(List<T> items, int loaderId) {
        getDao().insertOrReplaceInTx(items);
        displayDataFromDB();
    }

    public void displayNewItems() {
        stickyView.setVisibility(View.GONE);
        if (MISSED_ITEMS_THRESHOLD < refreshPager.getTotalCount()) {
            // New items are greater then threshold, so clear existing items &
            // Store the items loaded in first page & display
           clearDB();
            pager = null;
        }
        writeToDBAndDisplay(refreshPager.getResources(), LOADER_ID);
    }

    void clearDB() {
        getDao().deleteAll();
    }

    void hideLoadingFooter() {
        if (getListAdapter().getFootersCount() != 0) {
            // Remove footer if footer exists.
            getListAdapter().removeFooter(loadingLayout);
        }
    }

    protected abstract boolean isItemsExistsInDB();

    protected abstract AbstractDao<T, K> getDao();

    protected abstract BaseResourcePager<T> getPager();

    protected abstract BaseResourcePager<T> getRefreshPager();

}
