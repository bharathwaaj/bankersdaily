package in.bankersdaily.ui;

import android.os.Bundle;

import java.util.List;

import in.bankersdaily.R;
import in.bankersdaily.model.Post;
import in.bankersdaily.network.ApiClient;
import in.bankersdaily.network.PostPager;
import in.bankersdaily.util.SingleTypeAdapter;

public class PostsListFragment extends PagedItemFragment<Post> {

    private ApiClient apiClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        apiClient = new ApiClient(getActivity());
        super.onCreate(savedInstanceState);
    }
    
    @Override
    protected PostPager getPager() {
        if (pager == null) {
            pager = new PostPager(apiClient);
        }
        return (PostPager)pager;
    }

    @Override
    protected SingleTypeAdapter<Post> createAdapter(List<Post> items) {
        return new PostListAdapter(getActivity(), items);
    }

    @Override
    protected void setNoItemsText() {
        setEmptyText(R.string.no_posts, R.string.no_posts_description, R.drawable.no_news);
    }

}
