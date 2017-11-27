package in.bankersdaily.ui;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.text.Html;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.greenrobot.greendao.query.LazyList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import in.bankersdaily.BankersDailyApp;
import in.bankersdaily.R;
import in.bankersdaily.model.Bookmark;
import in.bankersdaily.model.Category;
import in.bankersdaily.model.CategoryDao;
import in.bankersdaily.model.DaoSession;
import in.bankersdaily.model.JoinPostsWithCategories;
import in.bankersdaily.model.Post;
import in.bankersdaily.model.PostDao;
import in.bankersdaily.network.ApiClient;
import in.bankersdaily.network.RetrofitCallback;
import in.bankersdaily.network.RetrofitException;
import in.bankersdaily.util.Assert;
import in.bankersdaily.util.ShareUtil;
import in.bankersdaily.util.ViewUtils;
import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.exam.TestpressExam;

import static in.bankersdaily.network.ApiClient.SLUG;
import static in.bankersdaily.ui.PostListActivity.CATEGORY_SLUG;

public class PostDetailFragment extends Fragment {

    public static final String POST_SLUG = "postSlug";
    public static final String POST = "post";

    private PostDao postDao;
    private Post post;
    private DaoSession daoSession;;

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
                loadPost(postSlug);
            }
        } else {
            setEmptyText(R.string.invalid_post, R.string.try_after_sometime, R.drawable.alert_warning);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.bookmark_and_share, menu);
        Bookmark bookmark = post.getBookmark();
        if (bookmark != null) {
            menu.getItem(0).setTitle(R.string.unbookmark);
            menu.getItem(0).setIcon(R.drawable.ic_bookmarked);
        } else {
            menu.getItem(0).setTitle(R.string.bookmark);
            menu.getItem(0).setIcon(R.drawable.ic_bookmark);
        }
    }

    void loadPost(final String postSlug) {
        progressBar.setVisibility(View.VISIBLE);
        Map<String, Object> queryParams = new LinkedHashMap<String, Object>();
        queryParams.put(SLUG, postSlug);
        queryParams.put(ApiClient.EMBED, "1");
        new ApiClient(getActivity()).getPosts(queryParams)
                .enqueue(new RetrofitCallback<List<Post>>() {
                    @Override
                    public void onSuccess(List<Post> posts) {
                        if (!posts.isEmpty()) {
                            post = posts.get(0);
                            CategoryDao categoryDao = daoSession.getCategoryDao();
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
                            setEmptyText(R.string.post_not_available,
                                    R.string.post_not_available_description, R.drawable.alert_warning);
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
                                    loadPost(postSlug);
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
        content.loadDataWithBaseURL("file:///android_asset/", getHeader() + post.getContent(),
                "text/html", "UTF-8", null);

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
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (getActivity() == null)
                return false;

            Uri uri = Uri.parse(url);
            List<String> pathSegments = uri.getPathSegments();
            if (uri.getHost().equals(getString(R.string.testpress_host_url)) &&
                    pathSegments.size() == 2 && pathSegments.get(0).equals("exams")) {

                if (TestpressSdk.hasActiveSession(getActivity())) {
                    TestpressSession testpressSession =
                            TestpressSdk.getTestpressSession(getActivity());

                    Assert.assertNotNull("TestpressSession must not be null.", testpressSession);
                    TestpressExam.showExamAttemptedState(
                            getActivity(),
                            pathSegments.get(1),
                            testpressSession
                    );
                    return true;
                }
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

            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setToolbarColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
            CustomTabsIntent customTabsIntent = builder.build();
            try {
                customTabsIntent.launchUrl(getActivity(), Uri.parse(url));
            } catch (ActivityNotFoundException e) {
                boolean wrongUrl = !url.startsWith("http://") && !url.startsWith("https://");
                int message = wrongUrl ? R.string.wrong_url : R.string.browser_not_available;
                ViewUtils.getAlertDialog(getActivity(), R.string.not_supported, message)
                        .show();
            }
            return true;
        }
    }

    String getHeader() {
        return "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, user-scalable=no\" />" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"typebase.css\" />" +
                "<style>img{display: inline;height: auto;max-width: 100%;}</style>";
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bookmark:
                Bookmark bookmark = post.getBookmark();
                if (bookmark != null) {
                    bookmark.delete();
                    item.setTitle(R.string.bookmark);
                    item.setIcon(R.drawable.ic_bookmark);
                } else {
                    bookmark = new Bookmark(post.getId());
                    daoSession.getBookmarkDao().insertOrReplaceInTx(bookmark);
                    item.setTitle(R.string.unbookmark);
                    item.setIcon(R.drawable.ic_bookmarked);
                }
                return true;
            case R.id.share:
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
    public void onDestroy () {
        super.onDestroy ();
    }

}
