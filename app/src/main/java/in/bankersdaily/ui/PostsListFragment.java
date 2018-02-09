package in.bankersdaily.ui;

import android.os.Bundle;

import org.greenrobot.greendao.AbstractDao;

import java.util.Date;
import java.util.List;

import in.bankersdaily.BankersDailyApp;
import in.bankersdaily.R;
import in.bankersdaily.model.Category;
import in.bankersdaily.model.CategoryDao;
import in.bankersdaily.model.FetchedPostsTracker;
import in.bankersdaily.model.FetchedPostsTrackerDao;
import in.bankersdaily.model.JoinPostsWithCategories;
import in.bankersdaily.model.JoinPostsWithCategoriesDao;
import in.bankersdaily.model.Post;
import in.bankersdaily.model.PostDao;
import in.bankersdaily.network.ApiClient;
import in.bankersdaily.network.PostPager;
import in.bankersdaily.util.FormatDate;
import in.bankersdaily.util.SingleTypeAdapter;

public class PostsListFragment extends BaseDBPagedItemFragment<Post, Long> {

    public static final String CATEGORY_ID = "categoryId";

    private ApiClient apiClient;
    private PostDao postDao;
    private int categoryId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiClient = new ApiClient(getActivity());
        postDao = daoSession.getPostDao();

        if (getArguments() != null) {
            categoryId = getArguments().getInt(CATEGORY_ID);
        }
    }

    @Override
    protected AbstractDao<Post, Long> getDao() {
        return postDao;
    }

    @Override
    protected PostPager getPager() {
        if (pager == null) {
            pager = new PostPager(apiClient);
            if (categoryId != 0) {
                pager.setQueryParams(ApiClient.CATEGORY, categoryId);
            }
            Date oldestPostDate = getDateFromTracker(Post.OLDEST);
            if (oldestPostDate != null) {
                pager.setQueryParams(ApiClient.BEFORE, FormatDate.getISODateString(oldestPostDate));
            }
        }
        return (PostPager) pager;
    }

    @Override
    protected PostPager getRefreshPager() {
        if (refreshPager == null) {
            refreshPager = new PostPager(apiClient);
            if (categoryId != 0) {
                refreshPager.setQueryParams(ApiClient.CATEGORY, categoryId);
            }
            Date latestPostDate = getDateFromTracker(Post.LATEST);
            if (latestPostDate != null) {
                refreshPager.setQueryParams(ApiClient.DATE_QUERY_COLUMN, ApiClient.MODIFIED);
                refreshPager.setQueryParams(ApiClient.AFTER,
                        FormatDate.getISODateString(latestPostDate));
            }
        }
        return (PostPager) refreshPager;
    }

    @Override
    protected SingleTypeAdapter<Post> createAdapter(List<Post> items) {
        return new PostListAdapter(getActivity(), categoryId, false);
    }

    @Override
    protected void writeToDBAndDisplay(List<Post> posts, int loaderId) {
        CategoryDao categoryDao = daoSession.getCategoryDao();
        JoinPostsWithCategoriesDao joiningDao = daoSession.getJoinPostsWithCategoriesDao();
        for (int i = 0; i < posts.size(); i++) {
            Post post = posts.get(i);
            for (Category category : post.getCategories()) {
                List<Category> categories = categoryDao.queryBuilder()
                        .where(CategoryDao.Properties.Id.eq(category.getId())).list();

                if (categories.isEmpty()) {
                    categoryDao.insertOrReplaceInTx(category);
                }
                joiningDao.insertOrReplace(new JoinPostsWithCategories(post.getId(), category.getId()));
            }
            FetchedPostsTrackerDao fetchedPostsTrackerDao = daoSession.getFetchedPostsTrackerDao();
            if (i == 0 && loaderId == LOADER_ID) {
                FetchedPostsTracker latestPostTracker = getFetchedPostsTracker(Post.LATEST);
                // Insert latest post in tracker if not exist, update otherwise.
                FetchedPostsTracker.insertOrUpdate(latestPostTracker, fetchedPostsTrackerDao,
                        Post.LATEST, post.getId(), categoryId);

            } else if (i + 1 == posts.size()) {
                FetchedPostsTracker oldestPostTracker = getFetchedPostsTracker(Post.OLDEST);
                switch (loaderId) {
                    case LOADER_ID:
                        // Insert oldest post in tracker only if not exist already
                        FetchedPostsTracker.insertIfNotExist(oldestPostTracker,
                                fetchedPostsTrackerDao, Post.OLDEST, post.getId(), categoryId);

                        break;
                    case MORE_ITEMS_LOADER_ID:
                        FetchedPostsTracker.insertOrUpdate(oldestPostTracker,
                                fetchedPostsTrackerDao, Post.OLDEST, post.getId(), categoryId);

                        break;
                }
            }
        }
        super.writeToDBAndDisplay(posts, loaderId);
    }

    @Override
    protected boolean isItemsExistsInDB() {
        // Check latest post is available in tracker
        Date latestPostDate = getDateFromTracker(Post.LATEST);
        return latestPostDate != null;
    }

    private FetchedPostsTracker getFetchedPostsTracker(String state) {
        return FetchedPostsTracker.getTracker(getActivity(), categoryId, state);
    }

    private Date getDateFromTracker(String state) {
        return FetchedPostsTracker.getDate(getActivity(), categoryId, state);
    }


    @Override
    void clearDB() {
        super.clearDB();
        daoSession.getJoinPostsWithCategoriesDao().deleteAll();
        daoSession.getFetchedPostsTrackerDao().deleteAll();
    }

    @Override
    protected void setNoItemsText() {
        setEmptyText(R.string.no_posts, R.string.no_posts_description, R.drawable.no_news);
    }

    @Override
    protected String getScreenName() {
        if (categoryId == 0) {
            return BankersDailyApp.LATEST_POSTS_TAB;
        }
        return "";
    }

    @Override
    protected void trackScreenViewAnalytics() {
        if (categoryId == 0) {
            super.trackScreenViewAnalytics();
        }
    }
}
