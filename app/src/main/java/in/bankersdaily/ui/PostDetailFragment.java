package in.bankersdaily.ui;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableString;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.greenrobot.greendao.query.LazyList;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.bankersdaily.BankersDailyApp;
import in.bankersdaily.R;
import in.bankersdaily.model.Bookmark;
import in.bankersdaily.model.Category;
import in.bankersdaily.model.CategoryDao;
import in.bankersdaily.model.Comment;
import in.bankersdaily.model.CreateCommentResponse;
import in.bankersdaily.model.DaoSession;
import in.bankersdaily.model.JoinPostsWithCategories;
import in.bankersdaily.model.Post;
import in.bankersdaily.model.PostDao;
import in.bankersdaily.network.ApiClient;
import in.bankersdaily.network.CommentsPager;
import in.bankersdaily.network.RetrofitCall;
import in.bankersdaily.network.RetrofitCallback;
import in.bankersdaily.network.RetrofitException;
import in.bankersdaily.util.Assert;
import in.bankersdaily.util.FormatDate;
import in.bankersdaily.util.Preferences;
import in.bankersdaily.util.ShareUtil;
import in.bankersdaily.util.ThrowableLoader;
import in.bankersdaily.util.ViewUtils;
import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.exam.TestpressExam;
import in.testpress.util.UIUtils;

import static android.app.Activity.RESULT_OK;
import static in.bankersdaily.ui.LoginActivity.AUTHENTICATE_REQUEST_CODE;
import static in.bankersdaily.ui.PostListActivity.CATEGORY_SLUG;
import static in.bankersdaily.util.ThrowableLoader.getException;

public class PostDetailFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<List<Comment>> {

    public static final String POST_SLUG = "postSlug";
    public static final String POST = "post";
    public static final String UPDATE_TIME_SPAN = "updateTimeSpan";
    public static final int NEW_COMMENT_SYNC_INTERVAL = 10000; // 10 sec
    private static final int PREVIOUS_COMMENTS_LOADER_ID = 0;
    private static final int NEW_COMMENTS_LOADER_ID = 1;

    private PostDao postDao;
    private Post post;
    private DaoSession daoSession;
    private CommentsPager previousCommentsPager;
    private CommentsPager newCommentsPager;
    private CommentsListAdapter commentsAdapter;
    private ProgressDialog progressDialog;
    private boolean postedNewComment;
    private ApiClient apiClient;
    private View rootLayout;
    private String comment = "";
    private List<String> pathSegments;
    private RetrofitCall<List<Post>> postsLoader;

    @BindView(R.id.content) WebView content;
    @BindView(R.id.title) TextView title;
    @BindView(R.id.summary) TextView summary;
    @BindView(R.id.summary_layout) LinearLayout summaryLayout;
    @BindView(R.id.date) TextView date;
    @BindView(R.id.content_empty_view) TextView contentEmptyView;
    @BindView(R.id.scroll_view) NestedScrollView postDetails;
    @BindView(R.id.pb_loading) ProgressBar progressBar;
    @BindView(R.id.empty_container) LinearLayout emptyView;
    @BindView(R.id.empty_title) TextView emptyTitleView;
    @BindView(R.id.empty_description) TextView emptyDescView;
    @BindView(R.id.image_view) ImageView emptyImageView;
    @BindView(R.id.retry_button) Button retryButton;
    @BindView(R.id.comments_layout) LinearLayout commentsLayout;
    @BindView(R.id.loading_previous_comments_layout) LinearLayout previousCommentsLoadingLayout;
    @BindView(R.id.loading_new_comments_layout) LinearLayout newCommentsLoadingLayout;
    @BindView(R.id.comments_list_view) RecyclerView listView;
    @BindView(R.id.load_previous_comments_layout) LinearLayout loadPreviousCommentsLayout;
    @BindView(R.id.load_previous_comments) TextView loadPreviousCommentsText;
    @BindView(R.id.load_new_comments_layout) LinearLayout loadNewCommentsLayout;
    @BindView(R.id.load_new_comments_text) TextView loadNewCommentsText;
    @BindView(R.id.comments_label) TextView commentsLabel;
    @BindView(R.id.comments_empty_view) TextView commentsEmptyView;
    @BindView(R.id.comment_box) EditText commentsEditText;
    @BindView(R.id.comment_box_layout) LinearLayout commentBoxLayout;
    @BindView(R.id.new_comments_available_label) LinearLayout newCommentsAvailableLabel;
    @BindView(R.id.scroll_to_button) LinearLayout scrollToButton;
    @BindView(R.id.scroll_to_direction) TextView scrollToDirection;
    @BindView(R.id.scroll_to_direction_icon) ImageView scrollToDirectionIcon;

    private Handler newCommentsHandler;
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            //noinspection ArraysAsListWithZeroOrOneArgument
            commentsAdapter.notifyItemRangeChanged(0, commentsAdapter.getItemCount(),
                    UPDATE_TIME_SPAN); // Update the time in comments

            getNewCommentsPager().reset();
            getLoaderManager().restartLoader(NEW_COMMENTS_LOADER_ID, null, PostDetailFragment.this);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiClient = new ApiClient(getActivity());
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getResources().getString(R.string.testpress_please_wait));
        progressDialog.setCancelable(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_post_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        rootLayout = view;
        UIUtils.setIndeterminateDrawable(view.getContext(), progressDialog, 4);
        //noinspection ConstantConditions
        ViewUtils.setTypeface(
                new TextView[] { date, summary, commentsEmptyView, commentsEditText, emptyDescView,
                        commentsEditText },
                TestpressSdk.getRubikRegularFont(getActivity())
        );
        ViewUtils.setTypeface(
                new TextView[] { title, emptyTitleView, retryButton, loadPreviousCommentsText,
                        commentsLabel, loadNewCommentsText, scrollToDirection },
                TestpressSdk.getRubikMediumFont(getActivity())
        );
        postDetails.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY,
                                       int oldScrollX, int oldScrollY) {

                int scrollViewHeight = postDetails.getHeight();
                int totalScrollViewChildHeight = postDetails.getChildAt(0).getHeight();
                // Let's assume end has reached at 50 pixels before itself(on partial visible of
                // last item)
                boolean endHasBeenReached =
                        (scrollY + scrollViewHeight + 50) >= totalScrollViewChildHeight;

                if (endHasBeenReached) {
                    newCommentsAvailableLabel.setVisibility(View.GONE);
                }
                if ((totalScrollViewChildHeight - scrollViewHeight) > 200) {
                    if (scrollY < oldScrollY) {
                        scrollToDirection.setText(R.string.top);
                        scrollToDirectionIcon
                                .setImageResource(R.drawable.ic_keyboard_arrow_up_black_18dp);
                    } else {
                        scrollToDirection.setText(R.string.bottom);
                        scrollToDirectionIcon
                                .setImageResource(R.drawable.ic_keyboard_arrow_down_black_18dp);
                    }
                    if (scrollY < 5 || endHasBeenReached) {
                        scrollToButton.setVisibility(View.GONE);
                    } else {
                        scrollToButton.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        postDetails.setVisibility(View.GONE);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Assert.assertNotNull("Post Slug must not be null", getArguments());
        if(getArguments().getParcelable(POST) != null) {
            post = getArguments().getParcelable(POST);
            post.__setDaoSession(BankersDailyApp.getDaoSession(getActivity()));
            displayPost(post);
        } else if(getArguments().getString(POST_SLUG) != null) {
            daoSession = BankersDailyApp.getDaoSession(getActivity());
            postDao = daoSession.getPostDao();
            String postSlug = getArguments().getString(POST_SLUG);
            List<Post> posts = postDao.queryBuilder()
                    .where(PostDao.Properties.Slug.eq(postSlug)).list();

            if (!posts.isEmpty()) {
                post = posts.get(0);
                displayPost(post);
                setHasOptionsMenu(true);
            } else {
                loadPost(postSlug, ApiClient.POSTS_PATH);
            }
        } else {
            setEmptyText(R.string.invalid_post, R.string.try_after_sometime, R.drawable.alert_warning);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.bookmark_and_share, menu);
        if (getActivity() == null)
            return;

        Bookmark bookmark = post.getBookmark();
        MenuItem item = menu.getItem(0);
        Drawable drawable = item.getIcon();
        if (bookmark != null) {
            item.setTitle(R.string.unbookmark);
            drawable.mutate().setColorFilter(ContextCompat.getColor(getActivity(),
                    R.color.dark_yellow), PorterDuff.Mode.SRC_IN);
        } else {
            item.setTitle(R.string.bookmark);
            drawable.mutate().setColorFilter(ContextCompat.getColor(getActivity(),
                    R.color.actionbar_text), PorterDuff.Mode.SRC_IN);
        }
        item.setIcon(drawable);
    }

    void loadPost(final String postSlug, final String postUrl) {
        if (getActivity() == null)
            return;

        progressBar.setVisibility(View.VISIBLE);
        Map<String, Object> queryParams = new LinkedHashMap<String, Object>();
        queryParams.put(ApiClient.SLUG, postSlug);
        queryParams.put(ApiClient.EMBED, "1");
        postsLoader = new ApiClient(getActivity()).getPosts(postUrl, queryParams)
                .enqueue(new RetrofitCallback<List<Post>>() {
                    @Override
                    public void onSuccess(List<Post> posts) {
                        if (!posts.isEmpty()) {
                            post = posts.get(0);
                            CategoryDao categoryDao = daoSession.getCategoryDao();
                            post.__setDaoSession(daoSession);
                            for (Category category : post.getCategories()) {
                                LazyList<Category> categories = categoryDao.queryBuilder()
                                        .where(CategoryDao.Properties.Id.eq(category.getId()))
                                        .listLazy();

                                if (categories.isEmpty()) {
                                    categoryDao.insertOrReplaceInTx(category);
                                }
                                daoSession.getJoinPostsWithCategoriesDao().insertOrReplace(
                                        new JoinPostsWithCategories(post.getId(), category.getId()));
                            }
                            postDao.insertOrReplaceInTx(post);
                            displayPost(post);
                            setHasOptionsMenu(true);
                        } else {
                            if (postUrl.equals(ApiClient.POSTS_PATH)) {
                                loadPost(postSlug, ApiClient.PAGES_PATH);
                            } else {
                                setEmptyText(R.string.post_not_available,
                                        R.string.post_not_available_description,
                                        R.drawable.alert_warning);
                            }
                        }
                    }

                    @Override
                    public void onException(RetrofitException exception) {
                        if (exception.isUnauthenticated()) {
                            setEmptyText(R.string.authentication_failed, R.string.please_login,
                                    R.drawable.alert_warning);
                        } else if (exception.isNetworkError()) {
                            setEmptyText(R.string.network_error, R.string.no_internet_try_again,
                                    R.drawable.no_wifi);

                            retryButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    progressBar.setVisibility(View.VISIBLE);
                                    emptyView.setVisibility(View.GONE);
                                    loadPost(postSlug, postUrl);
                                }
                            });
                            retryButton.setVisibility(View.VISIBLE);
                        } else  {
                            setEmptyText(
                                    R.string.loading_failed,
                                    R.string.some_thing_went_wrong_try_again,
                                    R.drawable.alert_warning
                            );
                        }
                    }
                });
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void displayPost(Post post) {
        if (getActivity() == null)
            return;

        title.setText(Html.fromHtml(post.getTitle()));
        if (post.getCategories().isEmpty()) {
            summaryLayout.setVisibility(View.GONE);
        } else {
            StringBuilder categoryString = new StringBuilder();
            List<Category> categories = post.getCategories();
            for (int i = 0; i < categories.size();) {
                categoryString.append(categories.get(i).getName());
                if (++i < categories.size()) {
                    categoryString.append(", ");
                }
            }
            summary.setText(categoryString);
            summaryLayout.setVisibility(View.VISIBLE);
        }

        date.setText(DateUtils.getRelativeTimeSpanString(post.getDate().getTime()));
        WebSettings settings = content.getSettings();
        settings.setDefaultTextEncodingName("utf-8");
        settings.setJavaScriptEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        content.setWebViewClient(new PostDetailWebViewClient());
        content.loadDataWithBaseURL("file:///android_asset/", getHtml(), "text/html", "UTF-8", null);
        postDetails.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    class PostDetailWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request,
                                    WebResourceError error) {

            super.onReceivedError(view, request, error);
            progressBar.setVisibility(View.GONE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
            if (getActivity() != null && post.getCommentStatus() != null &&
                    post.getCommentStatus().equals("open")) {

                displayComments();
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (getActivity() == null)
                return false;

            Uri uri = Uri.parse(url);
            pathSegments = uri.getPathSegments();
            if (uri.getHost().equals(getString(R.string.testpress_host_url)) &&
                    ((pathSegments.size() == 2 || pathSegments.size() == 4) &&
                            pathSegments.get(0).equals("exams"))) {

                if (TestpressSdk.hasActiveSession(getActivity())) {
                    showExamAttemptedState();
                } else {
                    if (!comment.isEmpty()) {
                        // empty comment variable to showExamAttemptedState onActivityResult
                        comment = "";
                    }
                    checkAuth();
                }
                return true;
            }
            if (uri.getHost().equals(getString(R.string.host_url)) && pathSegments.size() > 0) {
                switch (pathSegments.get(0)) {
                    case "category":
                        if (pathSegments.size() > 1) {
                            // If category slug is present, display posts of that category
                            Intent intent = new Intent(getActivity(), PostListActivity.class);
                            intent.putExtra(CATEGORY_SLUG, uri.getLastPathSegment());
                            startActivity(intent);
                            return true;
                        }
                        break;
                    default:
                        if (pathSegments.size() == 1) {
                            Intent intent = new Intent(getActivity(), PostDetailActivity.class);
                            intent.putExtra(POST_SLUG, pathSegments.get(0));
                            startActivity(intent);
                            return true;
                        }
                        break;
                }
            }

            boolean wrongUrl = !url.startsWith("http://") && !url.startsWith("https://");
            int message;
            if (!wrongUrl) {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                builder.setToolbarColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
                CustomTabsIntent customTabsIntent = builder.build();
                try {
                    customTabsIntent.launchUrl(getActivity(), Uri.parse(url));
                    return true;
                } catch (ActivityNotFoundException e) {
                    message = R.string.browser_not_available;
                }
            } else {
                message = R.string.wrong_url;
            }
            ViewUtils.getAlertDialog(getActivity(), R.string.not_supported, message).show();
            return true;
        }
    }

    void displayComments() {
        commentsAdapter = new CommentsListAdapter(getActivity());
        listView.setNestedScrollingEnabled(false);
        listView.setLayoutManager(new LinearLayoutManager(getActivity()));
        listView.setAdapter(commentsAdapter);
        loadPreviousCommentsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPreviousCommentsLayout.setVisibility(View.GONE);
                getLoaderManager()
                        .restartLoader(PREVIOUS_COMMENTS_LOADER_ID, null, PostDetailFragment.this);
            }
        });
        loadNewCommentsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadNewCommentsLayout.setVisibility(View.GONE);
                getLoaderManager()
                        .restartLoader(NEW_COMMENTS_LOADER_ID, null, PostDetailFragment.this);
            }
        });
        newCommentsAvailableLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newCommentsAvailableLabel.setVisibility(View.GONE);
                postDetails.post(new Runnable() {
                    @Override
                    public void run() {
                        postDetails.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
        commentsLayout.setVisibility(View.VISIBLE);
        getLoaderManager().initLoader(PREVIOUS_COMMENTS_LOADER_ID, null, PostDetailFragment.this);
    }

    @Override
    public Loader<List<Comment>> onCreateLoader(int loaderId, Bundle args) {
        switch (loaderId) {
            case PREVIOUS_COMMENTS_LOADER_ID:
                previousCommentsLoadingLayout.setVisibility(View.VISIBLE);
                return new CommentsLoader(this, loaderId);
            case NEW_COMMENTS_LOADER_ID:
                if (postedNewComment) {
                    newCommentsLoadingLayout.setVisibility(View.VISIBLE);
                    // if user posted a comment scroll to the bottom
                    postDetails.post(new Runnable() {
                        @Override
                        public void run() {
                            postDetails.fullScroll(View.FOCUS_DOWN);
                        }
                    });
                }
                return new CommentsLoader(this, loaderId);
            default:
                //An invalid id was passed
                return null;
        }
    }

    private static class CommentsLoader extends ThrowableLoader<List<Comment>> {

        private PostDetailFragment fragment;
        private int loaderID;

        CommentsLoader(PostDetailFragment fragment, int loaderID) {
            super(fragment.getContext(), Collections.<Comment>emptyList());
            this.fragment = fragment;
            this.loaderID = loaderID;
        }

        @Override
        public List<Comment> loadData() throws RetrofitException {
            switch (loaderID) {
                case PREVIOUS_COMMENTS_LOADER_ID:
                    fragment.getPreviousCommentsPager().clearResources().next();
                    return fragment.getPreviousCommentsPager().getResources();
                case NEW_COMMENTS_LOADER_ID:
                    do {
                        fragment.getNewCommentsPager().next();
                    } while (fragment.getNewCommentsPager().hasNext());
                    return fragment.getNewCommentsPager().getResources();
                default:
                    //An invalid id was passed
                    return null;
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    CommentsPager getPreviousCommentsPager() {
        if (previousCommentsPager == null) {
            previousCommentsPager = new CommentsPager(apiClient, post.getId());
            previousCommentsPager.queryParams
                    .put(ApiClient.BEFORE, FormatDate.getISODateString(new Date()));
        }
        return previousCommentsPager;
    }

    CommentsPager getNewCommentsPager() {
        if (newCommentsPager == null) {
            newCommentsPager = new CommentsPager(apiClient, post.getId());
        }
        List<Comment> comments = commentsAdapter.getComments();
        if (newCommentsPager.queryParams.isEmpty() && comments.size() != 0) {
            Comment latestComment = comments.get(comments.size() - 1);
            //noinspection ConstantConditions
            newCommentsPager.queryParams.put(ApiClient.AFTER, latestComment.getDate());
        }
        return newCommentsPager;
    }

    @Override
    public void onLoadFinished(Loader<List<Comment>> loader, List<Comment> comments) {
        if (getActivity() == null)
            return;

        switch (loader.getId()) {
            case PREVIOUS_COMMENTS_LOADER_ID:
                onPreviousCommentsLoadFinished(loader, comments);
                break;
            case NEW_COMMENTS_LOADER_ID:
                onNewCommentsLoadFinished(loader, comments);
                break;
        }
    }

    void onPreviousCommentsLoadFinished(Loader<List<Comment>> loader, List<Comment> comments) {
        //noinspection ThrowableResultOfMethodCallIgnored
        final Exception exception = getException(loader);
        if (previousCommentsPager == null || (exception == null && comments == null)) {
            return;
        }
        if (exception != null) {
            previousCommentsLoadingLayout.setVisibility(View.GONE);
            if ((comments == null || comments.isEmpty()) && commentsAdapter.getItemCount() == 0) {
                commentBoxLayout.setVisibility(View.VISIBLE);
                if (exception.getCause() instanceof IOException) {
                    loadPreviousCommentsText.setText(R.string.load_comments);
                    loadPreviousCommentsLayout.setVisibility(View.VISIBLE);
                }
            } else if (exception.getCause() instanceof IOException) {
                loadPreviousCommentsText.setText(R.string.load_comments);
                loadPreviousCommentsLayout.setVisibility(View.VISIBLE);
                Snackbar.make(rootLayout, R.string.testpress_no_internet_connection,
                        Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(rootLayout, R.string.network_error,
                        Snackbar.LENGTH_SHORT).show();
            }
            return;
        }

        if (!comments.isEmpty()) {
            commentsAdapter.addPreviousComments(comments);
        } else {
            commentsEmptyView.setVisibility(View.VISIBLE);
        }
        if (getPreviousCommentsPager().hasNext()) {
            loadPreviousCommentsText.setText(R.string.load_previous_comments);
            loadPreviousCommentsLayout.setVisibility(View.VISIBLE);
        } else {
            loadPreviousCommentsLayout.setVisibility(View.GONE);
        }
        if (commentBoxLayout.getVisibility() == View.GONE) {
            commentBoxLayout.setVisibility(View.VISIBLE);
        }
        previousCommentsLoadingLayout.setVisibility(View.GONE);
        if (newCommentsHandler == null) {
            newCommentsHandler = new Handler();
            newCommentsHandler.postDelayed(runnable, NEW_COMMENT_SYNC_INTERVAL);
        }
    }

    void onNewCommentsLoadFinished(Loader<List<Comment>> loader, List<Comment> comments) {
        //noinspection ThrowableResultOfMethodCallIgnored
        final Exception exception = getException(loader);
        if (exception != null) {
            newCommentsLoadingLayout.setVisibility(View.GONE);
            if (postedNewComment) {
                if (exception.getCause() instanceof IOException) {
                    Snackbar.make(rootLayout, R.string.testpress_no_internet_connection,
                            Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(rootLayout, R.string.network_error,
                            Snackbar.LENGTH_SHORT).show();
                }
                loadNewCommentsLayout.setVisibility(View.VISIBLE);
            } else {
                if (newCommentsHandler == null) {
                    newCommentsHandler = new Handler();
                }
                newCommentsHandler.postDelayed(runnable, NEW_COMMENT_SYNC_INTERVAL);
            }
            return;
        }

        if (!comments.isEmpty()) {
            commentsAdapter.addComments(comments);
        }
        if (commentsAdapter.getItemCount() != 0 && commentsEmptyView.getVisibility() == View.VISIBLE) {
            commentsEmptyView.setVisibility(View.GONE);
        }
        newCommentsLoadingLayout.setVisibility(View.GONE);
        if (postedNewComment) {
            postedNewComment = false;
        } else {
            int scrollY = postDetails.getScrollY();
            int scrollViewHeight = postDetails.getHeight();
            int totalScrollViewChildHeight = postDetails.getChildAt(0).getHeight();
            boolean endHasBeenReached = (scrollY + scrollViewHeight) >= totalScrollViewChildHeight;
            if (!comments.isEmpty() && !endHasBeenReached) {
                newCommentsAvailableLabel.setVisibility(View.VISIBLE);
            }
        }
        if (newCommentsHandler == null) {
            newCommentsHandler = new Handler();
        }
        newCommentsHandler.postDelayed(runnable, NEW_COMMENT_SYNC_INTERVAL);
    }

    @OnClick(R.id.send) void sendComment() {

        final String comment = commentsEditText.getText().toString().trim();
        if (comment.isEmpty()) {
            return;
        }
        UIUtils.hideSoftKeyboard(getActivity());
        PostDetailFragment.this.comment = Html.toHtml(new SpannableString(comment));
        checkAuth();
    }

    void postComment() {
        progressDialog.show();
        Map<String, Object> queryParam = new LinkedHashMap<>();
        queryParam.put(ApiClient.CONTENT, comment);
        queryParam.put(ApiClient.POST_ID, post.getId());
        queryParam.put(ApiClient.COMMENT_STATUS, 1);
        queryParam.put(ApiClient.INSECURE, ApiClient.COOL);
        apiClient.postComment(queryParam).enqueue(new RetrofitCallback<CreateCommentResponse>() {
            @Override
            public void onSuccess(CreateCommentResponse response) {
                commentsEditText.setText("");
                comment = "";
                listView.requestLayout();
                progressDialog.dismiss();
                Snackbar.make(rootLayout, R.string.comment_posted, Snackbar.LENGTH_SHORT).show();
                Bundle bundle = new Bundle();
                bundle.putString(BankersDailyApp.POST_SLUG, post.getSlug());
                bundle.putString(BankersDailyApp.COMMENT_ID, response.getCommentId().toString());
                BankersDailyApp.getInstance().trackEvent(
                        BankersDailyApp.POST_DETAIL,
                        BankersDailyApp.COMMENTED,
                        null,
                        bundle
                );
                if (newCommentsHandler != null) {
                    newCommentsHandler.removeCallbacks(runnable);
                }
                postedNewComment = true;
                getNewCommentsPager().reset();
                getLoaderManager()
                        .restartLoader(NEW_COMMENTS_LOADER_ID, null, PostDetailFragment.this);
            }

            @Override
            public void onException(RetrofitException exception) {
                progressDialog.dismiss();
                if (exception.getCause() instanceof IOException) {
                    Snackbar.make(rootLayout, R.string.testpress_no_internet_connection,
                            Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(rootLayout, R.string.network_error,
                            Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkAuth() {
        if (getActivity() == null)
            return;

        String wordPressToken = Preferences.getWordPressToken(getActivity());
        if (!wordPressToken.isEmpty() && TestpressSdk.hasActiveSession(getActivity())) {
            postComment();
        } else {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivityForResult(intent, AUTHENTICATE_REQUEST_CODE);
        }
    }

    private void showExamAttemptedState() {
        if (getActivity() == null)
            return;

        TestpressSession testpressSession = TestpressSdk.getTestpressSession(getActivity());
        Assert.assertNotNull("TestpressSession must not be null.", testpressSession);
        String slug;
        if (pathSegments.size() == 4) {
            slug = pathSegments.get(2);
        } else {
            slug = pathSegments.get(1);
        }
        TestpressExam.showExamAttemptedState(getActivity(), slug, testpressSession);
    }

    @OnClick(R.id.scroll_to_button) void scrollTo() {
        if (scrollToDirection.getText().equals(getString(R.string.bottom))) {
            postDetails.fullScroll(View.FOCUS_DOWN);
        } else {
            postDetails.scrollTo(0, 0);
        }
    }

    String getHtml() {
        return getHeader() +
                "<div style='margin-left: 20px; margin-right: 20px;'>" + post.getContent() + "</div>";
    }

    String getHeader() {
        return "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, user-scalable=no\" />" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"typebase.css\" />" +
                "<style>img{display: inline;height: auto;max-width: 100%;}</style>";
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTHENTICATE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (comment.isEmpty()) {
                showExamAttemptedState();
            } else {
                postComment();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bookmark:
                if (getActivity() == null)
                    return true;
                Bookmark bookmark = post.getBookmark();
                Drawable drawable = item.getIcon();
                if (bookmark != null) {
                    bookmark.delete();
                    item.setTitle(R.string.bookmark);
                    drawable.mutate().setColorFilter(ContextCompat.getColor(getActivity(),
                            R.color.actionbar_text), PorterDuff.Mode.SRC_IN);
                } else {
                    BankersDailyApp.getInstance().trackEvent(
                            BankersDailyApp.POST_DETAIL,
                            BankersDailyApp.BOOKMARKED,
                            post.getSlug()
                    );
                    bookmark = new Bookmark(post.getId());
                    daoSession.getBookmarkDao().insertOrReplaceInTx(bookmark);
                    item.setTitle(R.string.unbookmark);
                    drawable.mutate().setColorFilter(ContextCompat.getColor(getActivity(),
                            R.color.dark_yellow), PorterDuff.Mode.SRC_IN);
                }
                item.setIcon(drawable);
                return true;
            case R.id.share:
                BankersDailyApp.getInstance().trackEvent(
                        BankersDailyApp.POST_DETAIL,
                        BankersDailyApp.SHARED_POST,
                        post.getSlug()
                );
                ShareUtil.shareUrl(getActivity(), post.getTitle(), post.getLink());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void setEmptyText(final int title, final int description, final int imageResId) {
        if (post != null) {
            contentEmptyView.setText(description);
            contentEmptyView.setVisibility(View.VISIBLE);
            displayPost(post);
        } else {
            emptyView.setVisibility(View.VISIBLE);
            emptyTitleView.setText(title);
            emptyDescView.setText(description);
            emptyImageView.setImageResource(imageResId);
            retryButton.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        if (newCommentsHandler != null) {
            newCommentsHandler.removeCallbacks(runnable);
        }
        final ViewGroup viewGroup = (ViewGroup) content.getParent();
        if (viewGroup != null) {
            // Remove webView from its parent before destroy to support below kitkat
            viewGroup.removeView(content);
        }
        content.destroy();
        if (postsLoader != null) {
            postsLoader.cancel();
        }
        super.onDestroyView();
    }

    @Override
    public void onLoaderReset(Loader<List<Comment>> loader) {
    }

}
