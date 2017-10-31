package in.bankersdaily.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import in.bankersdaily.R;
import in.bankersdaily.network.RetrofitException;
import in.bankersdaily.util.SingleTypeAdapter;
import in.bankersdaily.util.ThrowableLoader;
import in.bankersdaily.util.ViewUtils;

/**
 * Base fragment for displaying a list of items with swipe refresh facility & progress for loading
 *
 * @param <T>
 */
public abstract class BaseListViewFragment<T> extends Fragment
        implements LoaderManager.LoaderCallbacks<List<T>> {

    protected static final int LOADER_ID = 0;

    private static final String FORCE_REFRESH = "forceRefresh";

    /**
     * @param args bundle passed to the loader by the LoaderManager
     * @return true if the bundle indicates a requested forced refresh of the
     * items
     */
    protected static boolean isForceRefresh(final Bundle args) {
        return args != null && args.getBoolean(FORCE_REFRESH, false);
    }

    /**
     * List items provided to {@link #onLoadFinished(Loader, List)}
     */
    protected List<T> items = Collections.emptyList();

    @BindView(android.R.id.list) protected ListView listView;

    @BindView(R.id.empty_container) protected View emptyView;
    @BindView(R.id.empty_title) protected TextView emptyTitleView;
    @BindView(R.id.empty_description) protected TextView emptyDescView;
    @BindView(R.id.image_view) protected ImageView emptyImageView;
    @BindView(R.id.retry_button) protected Button retryButton;

    @BindView(R.id.swipe_container) protected SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.sticky) protected TextView stickyView;

    /**
     * Is the list currently shown?
     */
    protected boolean listShown;

    protected RetrofitException exception;
    boolean firstCallBack = true;
    boolean needRetryButton;
    
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_list_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshWithProgress();
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        if(!listShown) {
            // Show loading progress
            swipeRefreshLayout.measure(View.MEASURED_SIZE_MASK, View.MEASURED_HEIGHT_STATE_SHIFT);
            swipeRefreshLayout.setRefreshing(true);
        }
        configureList(getActivity(), getListView());
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Init loader only for the first time of creating the fragment after that if user come to
        // the same tab don't init the loader
        if (firstCallBack) {
            getLoaderManager().initLoader(LOADER_ID, null, this);
            firstCallBack = false;
        } else {
            if (items.isEmpty()) {
                if (exception != null) {
                    // Set error message in empty view
                    getErrorMessage(exception);
                } else {
                    // Set no items message in empty view
                    setNoItemsText();
                    if (!needRetryButton) {
                        retryButton.setVisibility(View.GONE);
                    }
                }
            }
            setListShown(listShown);
        }
    }

    /**
     * Configure list after view has been created
     *
     * @param activity
     * @param listView
     */
    protected void configureList(final Activity activity, final ListView listView) {
        listView.setAdapter(createAdapter());
    }
    
    /**
     * Force a refresh of the items displayed ignoring any cached items
     */
    protected void forceRefresh() {
        final Bundle bundle = new Bundle();
        bundle.putBoolean(FORCE_REFRESH, true);
        refresh(bundle);
    }

    /**
     * Refresh the fragment's list
     */
    public void refresh() {
        refresh(null);
    }

    private void refresh(final Bundle args) {
        if (!isUsable()) {
            return;
        }
        getLoaderManager().restartLoader(LOADER_ID, args, this);
    }

    public void clearItemsAndRefresh() {
        items.clear();
        getListAdapter().getWrappedAdapter().setItems(items);
        refreshWithProgress();
    }

    public void onLoadFinished(final Loader<List<T>> loader, final List<T> items) {
        final RetrofitException exception = getException(loader);
        if (exception != null) {
            this.exception = exception;
            int errorMessage = getErrorMessage(exception);
            if (!items.isEmpty()) {
                showError(errorMessage);
            }
            showList();
            getLoaderManager().destroyLoader(loader.getId());
            return;
        }
        this.exception = null;
        this.items = items;
        if (items.isEmpty()) {
            setNoItemsText();
            if (!needRetryButton) {
                retryButton.setVisibility(View.GONE);
            }
        }
        getListAdapter().getWrappedAdapter().setItems(items.toArray());
        showList();
    }

    protected abstract void setNoItemsText();

    /**
     * Create adapter to display items
     *
     * @return adapter
     */
    protected HeaderFooterListAdapter<SingleTypeAdapter<T>> createAdapter() {
        final SingleTypeAdapter<T> wrapped = createAdapter(items);
        return new HeaderFooterListAdapter<SingleTypeAdapter<T>>(getListView(), wrapped);
    }

    /**
     * Create adapter to display items
     *
     * @param items
     * @return adapter
     */
    protected abstract SingleTypeAdapter<T> createAdapter(final List<T> items);

    /**
     * Set the list to be shown
     */
    protected void showList() {
        setListShown(true);
    }

    @Override
    public void onLoaderReset(Loader<List<T>> loader) {
        // left empty by default
    }

    /**
     * Show exception in a Snackbar
     *
     * @param message
     */
    protected void showError(final int message) {
        Snackbar.make(swipeRefreshLayout, message, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Get exception from loader if it provides one by being a
     * {@link ThrowableLoader}
     *
     * @param loader
     * @return exception or null if none provided
     */
    protected RetrofitException getException(final Loader<List<T>> loader) {
        if (loader instanceof ThrowableLoader) {
            return ((ThrowableLoader<List<T>>) loader).clearException();
        } else {
            return null;
        }
    }

    /**
     * Refresh the list with the progress bar showing
     */
    @OnClick(R.id.retry_button) public void refreshWithProgress() {
        swipeRefreshLayout.setEnabled(true);
        setListShown(false);
        refresh();
    }

    /**
     * Get {@link ListView}
     *
     * @return listView
     */
    public ListView getListView() {
        return listView;
    }

    /**
     * Get list adapter
     *
     * @return list adapter
     */
    @SuppressWarnings("unchecked")
    protected HeaderFooterListAdapter<SingleTypeAdapter<T>> getListAdapter() {
        if (listView != null) {
            return (HeaderFooterListAdapter<SingleTypeAdapter<T>>) listView.getAdapter();
        }
        return null;
    }

    /**
     * Set list adapter to use on list view
     *
     * @param adapter
     * @return this fragment
     */
    protected BaseListViewFragment<T> setListAdapter(final ListAdapter adapter) {
        if (listView != null) {
            listView.setAdapter(adapter);
        }
        return this;
    }

    private BaseListViewFragment<T> show(final View view) {
        ViewUtils.setGone(view, false);
        return this;
    }

    private BaseListViewFragment<T> hide(final View view) {
        ViewUtils.setGone(view, true);
        return this;
    }

    protected boolean isListItemsEmpty() {
        return getListAdapter().getWrappedAdapter().getCount() == 0;
    }

    /**
     * Set list shown or show progress bar
     *
     * @param shown
     * @return this fragment
     */
    public BaseListViewFragment<T> setListShown(final boolean shown) {
        if (!isUsable()) {
            return this;
        }
        listShown = shown;
        if (shown) {
            if (isListItemsEmpty()) {
                show(emptyView);
            } else {
                hide(emptyView).show(listView);
            }
            swipeRefreshLayout.setRefreshing(false);
        } else {
            hide(emptyView);
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });
        }
        return this;
    }

    /**
     * Set empty text on list fragment
     *
     * @param message
     * @return this fragment
     */
    protected BaseListViewFragment<T> setEmptyText(final String message) {
        if (emptyView != null) {
            emptyTitleView.setText(message);
            emptyDescView.setVisibility(View.GONE);
        }
        return this;
    }

    /**
     * Set empty text on list fragment
     *
     * @param resId
     * @return this fragment
     */
    protected BaseListViewFragment<T> setEmptyText(final int resId) {
        if (emptyView != null) {
            emptyTitleView.setText(resId);
            emptyDescView.setVisibility(View.GONE);
        }
        return this;
    }

    /**
     * Set empty text on list fragment
     *
     * @param title
     * @return this fragment
     */
    protected BaseListViewFragment<T> setEmptyText(@StringRes final int title,
                                                   @StringRes  final int description,
                                                   @DrawableRes final int imageResId) {

        if (emptyView != null) {
            if (isListItemsEmpty()) {
                swipeRefreshLayout.setEnabled(false);
            }
            emptyTitleView.setText(title);
            emptyDescView.setText(description);
            emptyImageView.setImageResource(imageResId);
            retryButton.setVisibility(View.VISIBLE);
        }
        return this;
    }

    /**
     * Get error message to display for exception
     *
     * @param exception RetrofitException
     * @return string resource id
     */
    protected int getErrorMessage(RetrofitException exception) {
        if (exception.isUnauthenticated()) {
            setEmptyText(R.string.authentication_failed, R.string.please_login,
                    R.drawable.alert_warning);
            return R.string.authentication_failed;
        } else if (exception.isNetworkError()) {
            setEmptyText(R.string.network_error, R.string.no_internet_try_again,
                    R.drawable.no_wifi);
            return R.string.no_internet_try_again;
        }
        setEmptyText(R.string.loading_failed, R.string.some_thing_went_wrong_try_again,
                R.drawable.alert_warning);
        return R.string.some_thing_went_wrong_try_again;
    }

    /**
     * Detach from list view.
     */
    @Override
    public void onDestroyView() {
        emptyView = null;
        swipeRefreshLayout = null;
        listView = null;
        super.onDestroyView();
    }

    /**
     * Callback when a list view item is clicked
     *
     * @param l
     * @param v
     * @param position
     * @param id
     */
    @OnItemClick(android.R.id.list)
    public void onListItemClick(final ListView l, final View v,
                                final int position, final long id) {
    }

    /**
     * Is this fragment still part of an activity and usable from the UI-thread?
     *
     * @return true if usable on the UI-thread, false otherwise
     */
    protected boolean isUsable() {
        return getActivity() != null;
    }
}