package in.bankersdaily.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tobishiba.circularviewpager.library.CircularViewPagerHandler;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.bankersdaily.BankersDailyApp;
import in.bankersdaily.R;
import in.bankersdaily.model.Category;
import in.bankersdaily.model.CategoryDao;
import in.bankersdaily.model.DaoSession;
import in.bankersdaily.model.JoinPostsWithCategories;
import in.bankersdaily.model.JoinPostsWithCategoriesDao;
import in.bankersdaily.model.Post;
import in.bankersdaily.model.PostDao;
import in.bankersdaily.network.ApiClient;
import in.bankersdaily.network.RetrofitCall;
import in.bankersdaily.network.RetrofitCallback;
import in.bankersdaily.network.RetrofitException;
import in.bankersdaily.util.ViewUtils;
import in.testpress.core.TestpressSdk;
import in.testpress.util.UIUtils;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

import static android.support.design.widget.Snackbar.LENGTH_SHORT;
import static in.bankersdaily.ui.BaseToolBarActivity.ACTIONBAR_TITLE;
import static in.bankersdaily.ui.PostListActivity.CATEGORY_SLUG;

public class HomePromotionsFragment extends BaseFragment {

    private static final int DAILY_CURRENT_AFFAIRS_ID = 13;
    public static final int CURRENT_AFFAIRS_QUIZ_ID = 111;
    public static final int NOTIFICATIONS_ID = 32;
    public static final int HOME_SLIDER_PREVIEW_PADDING = 50;

    @BindView(R.id.horizontal_progress_bar) MaterialProgressBar horizontalProgressBar;
    @BindView(R.id.promotions_pager) ViewPager promotionsPager;
    @BindView(R.id.promotions_view) RelativeLayout promotionsView;
    @BindView(R.id.content) LinearLayout content;
    @BindView(R.id.current_affairs_list) ListView currentAffairsListView;
    @BindView(R.id.daily_quiz_list) ListView dailyQuizListView;
    @BindView(R.id.notifications_list) ListView notificationListView;
    @BindView(R.id.current_affairs_card_view) CardView currentAffairsCardView;
    @BindView(R.id.daily_quiz_card_view) CardView notificationCardView;
    @BindView(R.id.notifications_card_view) CardView dailyQuizCardView;
    @BindView(R.id.more_current_affairs_text) TextView moreCurrentAffairsText;
    @BindView(R.id.more_notifications_text) TextView moreNotificationText;
    @BindView(R.id.more_daily_quiz_text) TextView moreDailyQuizText;
    @BindView(R.id.empty_container) LinearLayout emptyView;
    @BindView(R.id.empty_title) protected TextView emptyTitleView;
    @BindView(R.id.empty_description) protected TextView emptyDescView;
    @BindView(R.id.retry_button) Button retryButton;
    @BindView(R.id.scroll_view) NestedScrollView scrollView;
    @BindView(R.id.swipe_container) SwipeRefreshLayout swipeRefresh;

    List<Post> promotions = new ArrayList<>();
    PromotionPagerAdapter promotionPagerAdapter;
    int noOfItemsLoaded;
    ApiClient apiClient;
    RetrofitCall<List<Post>> currentAffairsLoader;
    RetrofitCall<List<Post>> dailyQuizLoader;
    RetrofitCall<List<Post>> notificationLoader;
    HomePostListAdapter currentAffairsAdapter;
    HomePostListAdapter dailyQuizAdapter;
    HomePostListAdapter notificationAdapter;
    private DaoSession daoSession;
    private PostDao postDao;
    private boolean isNetworkError;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trackScreenViewAnalytics();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_promotions_home, container, false);
        ButterKnife.bind(this, view);
        ViewUtils.setTypeface(
                new TextView[] { moreCurrentAffairsText, moreDailyQuizText, moreNotificationText,
                        emptyDescView },
                TestpressSdk.getRubikRegularFont(view.getContext())
        );
        ViewUtils.setTypeface(
                new TextView[] { emptyTitleView, retryButton },
                TestpressSdk.getRubikMediumFont(view.getContext())
        );
        noOfItemsLoaded = 0;
        promotionPagerAdapter = new PromotionPagerAdapter(getFragmentManager(), promotions);
        promotionsPager.setAdapter(promotionPagerAdapter);
        setPromotionsHeight();
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        swipeRefresh.measure(View.MEASURED_SIZE_MASK, View.MEASURED_HEIGHT_STATE_SHIFT);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        apiClient = new ApiClient(getActivity());
        daoSession = BankersDailyApp.getDaoSession(getContext());
        postDao = daoSession.getPostDao();
        currentAffairsAdapter = new HomePostListAdapter(getActivity(), DAILY_CURRENT_AFFAIRS_ID);
        dailyQuizAdapter = new HomePostListAdapter(getActivity(), CURRENT_AFFAIRS_QUIZ_ID);
        notificationAdapter = new HomePostListAdapter(getActivity(), NOTIFICATIONS_ID);
        currentAffairsListView.setAdapter(currentAffairsAdapter);
        dailyQuizListView.setAdapter(dailyQuizAdapter);
        notificationListView.setAdapter(notificationAdapter);
        displayItems();
        if (promotions.isEmpty()) { // Show progress bar if no promotions available in DB.
            horizontalProgressBar.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(true);
        }
        loadCurrentAffairs();
        loadDailyQuiz();
        loadNotifications();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setPromotionsHeight();
    }

    public RetrofitCall<List<Post>> loadPosts(int categoryId) {
        return Post.loadLatest(apiClient, categoryId,
                new RetrofitCallback<List<Post>>() {
                    @Override
                    public void onSuccess(List<Post> posts) {
                        if (getActivity() == null)
                            return;

                        writeToDB(posts);
                        hideProgress();
                    }

                    @Override
                    public void onException(RetrofitException exception) {
                        if (getActivity() == null)
                            return;
                        
                        handleException(exception);
                    }
                });
    }

    public void loadCurrentAffairs() {
        if (currentAffairsLoader != null) {
            currentAffairsLoader.cancel();
        }
        currentAffairsLoader = loadPosts(DAILY_CURRENT_AFFAIRS_ID);
    }

    public void loadDailyQuiz() {
        if (dailyQuizLoader != null) {
            dailyQuizLoader.cancel();
        }
        dailyQuizLoader = loadPosts(CURRENT_AFFAIRS_QUIZ_ID);
    }

    public void loadNotifications() {
        if (notificationLoader != null) {
            notificationLoader.cancel();
        }
        notificationLoader = loadPosts(NOTIFICATIONS_ID);
    }

    protected synchronized void writeToDB(List<Post> posts) {
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
        }
        postDao.insertOrReplaceInTx(posts);
    }

    @OnClick(R.id.more_current_affairs) void showCurrentAffairs() {
        showCategoryPostsList(DAILY_CURRENT_AFFAIRS_ID, R.string.daily_current_affairs);
    }

    @OnClick(R.id.more_daily_quiz) void showDailyQuiz() {
        showCategoryPostsList(CURRENT_AFFAIRS_QUIZ_ID, R.string.current_affairs_quiz);
    }

    @OnClick(R.id.more_notifications) void showNotifications() {
        showCategoryPostsList(NOTIFICATIONS_ID, R.string.notification);
    }

    void showCategoryPostsList(int categoryId, @StringRes int title) {
        Intent intent = new Intent(getActivity(), PostListActivity.class);
        intent.putExtra(PostsListFragment.CATEGORY_ID, categoryId);
        intent.putExtra(ACTIONBAR_TITLE, getString(title));
        startActivity(intent);
    }

    @OnClick(R.id.retry_button) void refresh() {
        if (horizontalProgressBar.getVisibility() == View.VISIBLE) {
            horizontalProgressBar.setVisibility(View.GONE);
        }
        swipeRefresh.post(new Runnable() {
            @Override
            public void run() {
                swipeRefresh.setRefreshing(true);
            }
        });
        noOfItemsLoaded = 0;
        promotions.clear();
        loadCurrentAffairs();
        loadDailyQuiz();
        loadNotifications();
        emptyView.setVisibility(View.GONE);
    }

    void displayPromotions() {
        promotionPagerAdapter.setItems(new ArrayList<>(promotions));
        promotionPagerAdapter.notifyDataSetChanged();
        promotionsPager.addOnPageChangeListener(new CircularViewPagerHandler(promotionsPager) {
            @Override
            public void onPageScrollStateChanged(int state) {
                swipeRefresh.setEnabled(state == ViewPager.SCROLL_STATE_IDLE);
                scrollView.setEnabled(state == ViewPager.SCROLL_STATE_IDLE);
                super.onPageScrollStateChanged(state);
            }
        });
        promotionsPager.setClipToPadding(false);
        promotionsPager.setPadding(HOME_SLIDER_PREVIEW_PADDING, 0, HOME_SLIDER_PREVIEW_PADDING, 0);
        promotionsView.setVisibility(View.VISIBLE);
        ViewUtils.slide_down(getActivity(), promotionsView);
    }

    void setPromotionsHeight() {
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) promotionsPager.getLayoutParams();

        params.width = RelativeLayout.LayoutParams.WRAP_CONTENT;
        int heightOfTitle = (int) UIUtils.getPixelFromDp(getActivity(), 80);
        params.height = ViewUtils.getImageHeightWithRespectToDevice(getActivity()) + heightOfTitle;
        promotionsPager.setLayoutParams(params);
    }

    void handleException(RetrofitException exception) {
        if (exception.getMessage().equals("Socket closed")) {
            return;
        }
        if (exception.isNetworkError()) {
            retryButton.setVisibility(View.VISIBLE);
            isNetworkError = true;
        }
        hideProgress();
    }

    void hideProgress() {
        int noOfItems = 3;
        if(++noOfItemsLoaded == noOfItems) {
            displayItems();
            if (promotions.isEmpty()) {
                content.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            } else if (isNetworkError) {
                isNetworkError = false;
                Snackbar.make(swipeRefresh, R.string.testpress_no_internet_connection, LENGTH_SHORT)
                        .show();
            } else {
                content.setVisibility(View.VISIBLE);
            }
            scrollView.smoothScrollTo(0, 0);
            if (horizontalProgressBar.getVisibility() == View.VISIBLE) {
                horizontalProgressBar.setVisibility(View.GONE);
            }
            swipeRefresh.setRefreshing(false);
        }
    }

    void displayItems() {
        QueryBuilder<Post> queryBuilder = getQueryBuilder(DAILY_CURRENT_AFFAIRS_ID);
        if (queryBuilder.count() > 0) {
            promotions.add(queryBuilder.list().get(0));
            currentAffairsAdapter.notifyDataSetChanged();
            displayCardView(currentAffairsCardView, currentAffairsListView);
        }
        queryBuilder = getQueryBuilder(CURRENT_AFFAIRS_QUIZ_ID);
        if (queryBuilder.count() > 0) {
            promotions.add(queryBuilder.list().get(0));
            dailyQuizAdapter.notifyDataSetChanged();
            displayCardView(dailyQuizCardView, dailyQuizListView);
        }
        queryBuilder = getQueryBuilder(NOTIFICATIONS_ID);
        if (queryBuilder.count() > 0) {
            promotions.add(queryBuilder.list().get(0));
            notificationAdapter.notifyDataSetChanged();
            displayCardView(notificationCardView, notificationListView);
        }
        if (!ViewUtils.isTabletDevice(getActivity()) && !promotions.isEmpty()) {
            displayPromotions();
        }
        scrollView.smoothScrollTo(0, 0);
    }

    QueryBuilder<Post> getQueryBuilder(int categoryId) {
        QueryBuilder<Post> queryBuilder = postDao.queryBuilder().orderDesc(PostDao.Properties.Date);
        queryBuilder
                .join(JoinPostsWithCategories.class, JoinPostsWithCategoriesDao.Properties.PostId)
                .where(JoinPostsWithCategoriesDao.Properties.CategoryId.eq(categoryId));

        return queryBuilder;
    }

    void displayCardView(CardView cardView, ListView listView) {
        if (getActivity() == null || listView == null)
            return;

        ViewUtils.setListViewHeightBasedOnItems(listView);
        cardView.setVisibility(View.VISIBLE);
        ViewUtils.slide_down(getActivity(), cardView);
    }

    @Override
    protected String getScreenName() {
        return BankersDailyApp.HOME_TAB;
    }

    @Override
    public void onDestroyView() {
        if (currentAffairsLoader != null) {
            currentAffairsLoader.cancel();
        }
        if (dailyQuizLoader != null) {
            dailyQuizLoader.cancel();
        }
        if (notificationLoader != null) {
            notificationLoader.cancel();
        }
        super.onDestroyView();
    }
}
