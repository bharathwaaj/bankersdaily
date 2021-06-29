package in.bankersdaily.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.loader.content.Loader;

import org.greenrobot.greendao.AbstractDao;

import java.util.List;

import in.bankersdaily.network.BaseResourcePager;
import in.bankersdaily.network.RetrofitException;
import in.bankersdaily.util.ThrowableLoader;

/**
 *
 * @param <T> Entity type
 * @param <K> Primary key (PK) type; use Void if entity does not have exactly one PK
 */
public abstract class BaseDBListViewFragment<T, K> extends BaseListViewFragment<T> {

    protected BaseResourcePager<T> pager;

    protected abstract BaseResourcePager<T> getPager();

    protected abstract AbstractDao<T, K> getDao();

    protected DBItemsLoader<T, K> loader;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListAdapter().notifyDataSetChanged(); // Display data from db
    }

    protected void displayDataFromDB() {
        Log.d("displayDataFromDB","Adapter notifyDataSetChanged");
        getListAdapter().notifyDataSetChanged();

        if (isListItemsEmpty()) {
            setNoItemsText();
            retryButton.setVisibility(View.GONE);
        }
    }

    @Override
    public Loader<List<T>> onCreateLoader(int id, Bundle args) {
        loader = new DBItemsLoader<>(this, items);
        return loader;
    }

    private static class DBItemsLoader<T, K> extends ThrowableLoader<List<T>> {

        private BaseDBListViewFragment<T, K> fragment;

        DBItemsLoader(BaseDBListViewFragment<T, K> fragment, List<T> data) {
            super(fragment.getContext(), data);
            this.fragment = fragment;
        }

        @Override
        public List<T> loadData() throws RetrofitException {
            do {
                fragment.getPager().next();
            } while (fragment.getPager().hasNext());
            return fragment.getPager().getResources();
        }
    }

    @Override
    public void onLoadFinished(Loader<List<T>> loader, List<T> items) {
        RetrofitException exception = getException(loader);
        if (exception != null) {
            this.exception = exception;
            int errorMessage = getErrorMessage(exception);
            if (!isListItemsEmpty()) {
                showError(errorMessage);
            }
            showList();
            getLoaderManager().destroyLoader(loader.getId());
            return;
        }

        this.exception = null;
        this.items = items;
        if (!items.isEmpty()) {
            getDao().insertOrReplaceInTx(items);
        }
        displayDataFromDB();
        showList();
    }

    @Override
    public void refreshWithProgress() {
        getPager().reset();
        super.refreshWithProgress();
    }

    @Override
    public void onDestroy() {
        if (loader != null) {
            getLoaderManager().destroyLoader(loader.getId());
            loader.cancelLoad();
            loader.cancelLoadInBackground();
        }
        super.onDestroy();
    }
}
