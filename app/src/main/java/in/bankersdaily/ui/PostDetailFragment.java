package in.bankersdaily.ui;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
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

import junit.framework.Assert;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import in.bankersdaily.BankersDailyApp;
import in.bankersdaily.R;
import in.bankersdaily.model.Category;
import in.bankersdaily.model.Post;
import in.bankersdaily.util.ViewUtils;
import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.exam.TestpressExam;

public class PostDetailFragment extends Fragment {

    public static final String POST_SLUG = "postSlug";
    public static final String POST = "post";

    Post post;

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
        if(getArguments() != null && getArguments().getParcelable(POST) != null) {
            post = getArguments().getParcelable(POST);
            post.__setDaoSession(BankersDailyApp.getDaoSession(getActivity()));
            displayPost(post);
        } else {
            setEmptyText(R.string.invalid_post, R.string.try_after_sometime, R.drawable.alert_warning);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void displayPost(Post post) {
        postDetails.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
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
        content.setWebViewClient(new WebViewClient() {
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
                if ((pathSegments.size() == 2) &&
                        (uri.getHost().equals(getString(R.string.testpress_host_url)) ||
                                uri.getPathSegments().get(0).equals("exams"))) {

                    if (TestpressSdk.hasActiveSession(getActivity())) {
                        TestpressSession testpressSession = TestpressSdk.getTestpressSession(getActivity());
                        Assert.assertNotNull("TestpressSession must not be null.", testpressSession);
                        TestpressExam.showExamAttemptedState(
                                getActivity(),
                                pathSegments.get(1),
                                testpressSession
                        );
                        return true;
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
        });
        content.loadDataWithBaseURL("file:///android_asset/", getHeader() + post.getContent(),
                "text/html", "UTF-8", null);
    }

    String getHeader() {
        return "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, user-scalable=no\" />" +
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"typebase.css\" />" +
                "<style>img{display: inline;height: auto;max-width: 100%;}</style>";
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
        }
    }

    @Override
    public void onDestroy () {
        super.onDestroy ();
    }

}
