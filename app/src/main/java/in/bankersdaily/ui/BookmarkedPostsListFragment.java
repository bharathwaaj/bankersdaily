package in.bankersdaily.ui;

import android.os.Bundle;
import android.support.v4.content.Loader;

import java.util.List;

import in.bankersdaily.R;
import in.bankersdaily.model.Post;
import in.bankersdaily.util.SingleTypeAdapter;

public class BookmarkedPostsListFragment extends BaseListViewFragment<Post> {

    @Override
    public void onResume() {
        super.onResume();
        getListAdapter().notifyDataSetChanged();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && getActivity() != null ) {
            getListAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        swipeRefreshLayout.setEnabled(false);
        if (firstCallBack) {
            // Avoid network request
            firstCallBack = false;
            listShown = true;
        }
        getListAdapter().notifyDataSetChanged();
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected SingleTypeAdapter<Post> createAdapter(List<Post> items) {
        return new PostListAdapter(getActivity(), 0, true);
    }

    @Override
    protected void setNoItemsText() {
        setEmptyText(R.string.no_bookmarks, R.string.no_bookmarks_description, R.drawable.bookmark_flat_icon);
    }

    @Override
    public Loader<List<Post>> onCreateLoader(int id, Bundle args) {
        return null;
    }
}
