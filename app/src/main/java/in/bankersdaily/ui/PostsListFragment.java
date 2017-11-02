package in.bankersdaily.ui;

import android.os.Bundle;

import org.greenrobot.greendao.AbstractDao;

import java.util.List;

import in.bankersdaily.R;
import in.bankersdaily.model.Post;
import in.bankersdaily.model.PostDao;
import in.bankersdaily.network.ApiClient;
import in.bankersdaily.network.PostPager;
import in.bankersdaily.util.FormatDate;
import in.bankersdaily.util.SingleTypeAdapter;

public class PostsListFragment extends BaseDBPagedItemFragment<Post, Long> {

    private ApiClient apiClient;
    private PostDao postDao;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiClient = new ApiClient(getActivity());
        postDao = daoSession.getPostDao();;
    }

    @Override
    protected AbstractDao<Post, Long> getDao() {
        return postDao;
    }

    @Override
    protected PostPager getPager() {
        if (pager == null) {
            pager = new PostPager(apiClient);
            Post lastPost = postDao.queryBuilder().orderDesc(PostDao.Properties.Date)
                    .list().get((int) postDao.count() - 1);

            refreshPager.setQueryParams(ApiClient.BEFORE,
                    FormatDate.getISODateString(lastPost.getDate()));
        }
        return (PostPager) pager;
    }

    @Override
    protected PostPager getRefreshPager() {
        if (refreshPager == null) {
            refreshPager = new PostPager(apiClient);
            if (!isItemsExistsInDB()) {
                Post latest = postDao.queryBuilder().orderDesc(PostDao.Properties.Date)
                        .list().get(0);

                refreshPager.setQueryParams(ApiClient.AFTER,
                        FormatDate.getISODateString(latest.getModified()));
            }
        }
        return (PostPager) refreshPager;
    }

    @Override
    protected boolean isItemsExistsInDB() {
        return postDao.queryBuilder().count() > 0;
    }

    @Override
    protected SingleTypeAdapter<Post> createAdapter(List<Post> items) {
        return new PostListAdapter(getActivity(), postDao);
    }

    @Override
    protected void setNoItemsText() {
        setEmptyText(R.string.no_posts, R.string.no_posts_description, R.drawable.no_news);
    }

}
