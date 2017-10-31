package in.bankersdaily.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;

import java.util.List;

import in.bankersdaily.R;
import in.bankersdaily.network.BaseResourcePager;
import in.bankersdaily.network.RetrofitException;
import in.bankersdaily.util.ThrowableLoader;

public abstract class PagedItemFragment<E> extends BaseListViewFragment<E>
        implements AbsListView.OnScrollListener {

    /**
     * Resource pager
     */
    protected BaseResourcePager<E> pager;

    /**
     * Create pager that provides resources
     *
     * @return pager
     */
    protected abstract BaseResourcePager<E> getPager();

    View loadingLayout;
    int lastFirstVisibleItem;
    int lastFirstVisibleItemTop;
    boolean isScrollingUp;

    @SuppressLint("InflateParams")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadingLayout = LayoutInflater.from(getActivity()).inflate(R.layout.loading_layout, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setOnScrollListener(this);
        getListView().setFastScrollEnabled(true);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                         int totalItemCount) {

        if (!isUsable())
            return;
        if (pager != null && !pager.hasMore()) {
            if(getListAdapter().getFootersCount() != 0) {
                // If pager reached last page remove footer if footer added already
                getListAdapter().removeFooter(loadingLayout);
            }
            return;
        }
        if (getLoaderManager().hasRunningLoaders())
            return;
        if (listView.getChildAt(0) != null) {
            // Detect scrolling up or down
            int currentFirstVisibleItem = listView.getFirstVisiblePosition();
            int currentFirstVisibleItemTop = Math.abs(listView.getChildAt(0).getTop());
            if (currentFirstVisibleItem > lastFirstVisibleItem) {
                isScrollingUp = false;
            } else if (currentFirstVisibleItem < lastFirstVisibleItem) {
                isScrollingUp = true;
            } else if (currentFirstVisibleItemTop > lastFirstVisibleItemTop) {
                isScrollingUp = false;
            } else if (currentFirstVisibleItemTop < lastFirstVisibleItemTop) {
                isScrollingUp = true;
            }
        }
        if (pager != null && !items.isEmpty() && !isScrollingUp
                && (listView.getLastVisiblePosition() + 3) >= pager.size()) {
            if(getListAdapter().getFootersCount() == 0) {
                // Display loading footer in bottom of listView if not present when loading next page
                getListAdapter().addFooter(loadingLayout);
            }
            showMore();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        lastFirstVisibleItem = listView.getFirstVisiblePosition();
        lastFirstVisibleItemTop = Math.abs(listView.getChildAt(0).getTop());
        // While previously loading the next page any error happens, this ensures to try
        // to load the next page if available, while scrolling
        pager.setHasMore(true);
    }

    @Override
    public void onLoadFinished(final Loader<List<E>> loader, List<E> items) {
        if (!getPager().hasMore()) {
            if(getListAdapter().getFootersCount() != 0) {
                // If pager reached last page remove footer if footer added already
                getListAdapter().removeFooter(loadingLayout);
            }
        }
        super.onLoadFinished(loader, items);
    }

    @Override
    public Loader<List<E>> onCreateLoader(int id, Bundle bundle) {
        return new PagedItemsLoader<E>(getActivity(), items, this);
    }

    static class PagedItemsLoader<E> extends ThrowableLoader<List<E>> {

        private PagedItemFragment<E> fragment;

        /**
         * Create loader for context and seeded with initial data
         *
         * @param context
         * @param data
         */
        PagedItemsLoader(Context context, List<E> data, PagedItemFragment<E> fragment) {
            super(context, data);
            this.fragment = fragment;
        }

        @Override
        public List<E> loadData() throws RetrofitException {
            fragment.getPager().next();
            return fragment.getPager().getResources();
        }
    }

    /**
     * Show more events while retaining the current pager state
     */
    private void showMore() {
        refresh();
    }

    @Override
    public void refreshWithProgress() {
        getPager().reset();
        super.refreshWithProgress();
    }
}

