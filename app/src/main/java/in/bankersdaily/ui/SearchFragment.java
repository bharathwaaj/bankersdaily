package in.bankersdaily.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import in.bankersdaily.R;
import in.bankersdaily.model.Post;
import in.bankersdaily.network.ApiClient;
import in.bankersdaily.network.PostPager;
import in.bankersdaily.network.RetrofitException;
import in.bankersdaily.util.SingleTypeAdapter;
import in.bankersdaily.util.ThrowableLoader;
import in.testpress.ui.HeaderFooterListAdapter;
import in.testpress.util.UIUtils;
import in.testpress.util.ViewUtils;

public class SearchFragment extends Fragment implements AbsListView.OnScrollListener,
        LoaderManager.LoaderCallbacks<List<Post>> {
    
    private static final int SPEECH_RESULT = 111;

    @BindView(R.id.empty_container) protected View emptyView;
    @BindView(R.id.empty_title) protected TextView emptyTitleView;
    @BindView(R.id.empty_description) protected TextView emptyDescView;
    @BindView(R.id.image_view) protected ImageView emptyImageView;
    @BindView(R.id.retry_button) protected Button retryButton;
    @BindView(R.id.pb_loading) protected ProgressBar progressBar;
    @BindView(R.id.search_bar) protected EditText searchBar;
    @BindView(R.id.left_drawable) protected ImageView leftDrawable;
    @BindView(R.id.right_drawable) protected ImageView rightDrawable;
    @BindView(R.id.result_list_card) protected CardView resultsLayout;
    @BindView(android.R.id.list) protected ListView listView;

    private View searchLayout;
    String queryText = "";
    private List<Post> items = Collections.emptyList();
    private PostPager pager;
    private View loadingLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pager = new PostPager(new ApiClient(getActivity()));
    }

    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_search,
                container, false);

        ButterKnife.bind(this, view);
        //noinspection ConstantConditions
        UIUtils.setIndeterminateDrawable(getActivity(), progressBar, 4);
        searchLayout = view;
        leftDrawable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (searchBar.getText().toString().isEmpty()) {
                    //Work as back arrow
                    getActivity().finish();
                } else {
                    // Work as search icon
                    onClickSearch();
                }
            }
        });
        rightDrawable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickRightDrawable();
            }
        });
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshWithProgress();
            }
        });
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (searchBar.getText().toString().isEmpty()) {
                    leftDrawable.setImageDrawable(ContextCompat.getDrawable(getActivity(),
                            R.drawable.ic_arrow_back_black_24dp));
                    if (!SpeechRecognizer.isRecognitionAvailable(getActivity())) {
                        rightDrawable.setVisibility(View.GONE);
                    } else {
                        rightDrawable.setImageDrawable(ContextCompat.getDrawable(getActivity(),
                                R.drawable.ic_mic_black_24dp));
                    }
                } else {
                    if (rightDrawable.getVisibility() == View.GONE) {
                        rightDrawable.setVisibility(View.VISIBLE);
                    }
                    rightDrawable.setImageDrawable(ContextCompat.getDrawable(getActivity(),
                            R.drawable.ic_close_black_24dp));
                    leftDrawable.setImageDrawable(ContextCompat.getDrawable(getActivity(),
                            R.drawable.ic_search_white_24dp));
                }
            }
        });
        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    onClickSearch();
                    return true;
                }
                return false;
            }
        });
        PostSearchListAdapter wrapped = new PostSearchListAdapter(getActivity());
        listView.setAdapter(new HeaderFooterListAdapter<SingleTypeAdapter<Post>>(listView, wrapped));
        loadingLayout = LayoutInflater.from(getActivity()).inflate(R.layout.testpress_loading_layout, null);
        listView.setOnScrollListener(this);
        listView.setFastScrollEnabled(true);
        if (!SpeechRecognizer.isRecognitionAvailable(getActivity())) {
            rightDrawable.setVisibility(View.GONE);
        }
        return view;
    }

    private void onClickSearch() {
        queryText = searchBar.getText().toString().trim();
        if (queryText.length() != 0) {
            ViewUtils.hideSoftKeyboard(getActivity());
            refreshWithProgress();
        }
    }

    private void onClickRightDrawable() {
        if (searchBar.getText().toString().isEmpty()) {
            //Work as speech recognizer
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
            try {
                startActivityForResult(intent, SPEECH_RESULT);
                searchBar.setText("");
            } catch (ActivityNotFoundException a) {
                Snackbar.make(searchLayout, R.string.testpress_speech_recognition_not_supported,
                        Snackbar.LENGTH_LONG).show();
            }
        } else {
            //Work as clear button
            if (!SpeechRecognizer.isRecognitionAvailable(getActivity())) {
                rightDrawable.setVisibility(View.GONE);
            } else {
                rightDrawable.setImageDrawable(ContextCompat.getDrawable(getActivity(),
                        R.drawable.ic_mic_black_24dp));
            }
            searchBar.setText("");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (SPEECH_RESULT == requestCode && resultCode == Activity.RESULT_OK && null != data) {
            searchBar.setText(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0));
            searchBar.setSelection(searchBar.getText().length());
            searchBar.requestFocus();
            onClickSearch();
        }
    }

    @Override
    public Loader<List<Post>> onCreateLoader(int id, Bundle args) {
        return new SearchItemsLoader(this, items);
    }

    private static class SearchItemsLoader extends ThrowableLoader<List<Post>> {

        private SearchFragment fragment;

        SearchItemsLoader(SearchFragment fragment, List<Post> data) {
            //noinspection ConstantConditions
            super(fragment.getContext(), data);
            this.fragment = fragment;
        }

        @Override
        public List<Post> loadData() throws RetrofitException {
            fragment.pager.setQueryParams(ApiClient.SEARCH_QUERY, fragment.queryText);
            fragment.pager.next();
            return fragment.pager.getResources();
        }
    }

    @Override
    public void onLoadFinished(Loader<List<Post>> loader, List<Post> items) {
        if (pager != null && !pager.hasMore()) {
            if(getListAdapter().getFootersCount() != 0) {
                // If pager reached last page remove footer if footer added already
                getListAdapter().removeFooter(loadingLayout);
            }
        }
        final RetrofitException exception = getException(loader);
        if (exception != null) {
            if (!items.isEmpty()) {
                Snackbar.make(searchLayout, getErrorMessage(exception), Snackbar.LENGTH_LONG).show();
            }
            setListShown(true);
            return;
        }
        if (items.isEmpty()) {
            setEmptyText(R.string.testpress_no_results_found, R.string.testpress_try_with_other_keyword,
                    R.drawable.no_news);
            retryButton.setVisibility(View.GONE);
        }
        this.items = items;
        getListAdapter().getWrappedAdapter().setItems(items.toArray());
        setListShown(true);
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount)
    {
        if (pager != null && !pager.hasMore()) {
            if(getListAdapter().getFootersCount() != 0) {
                // If pager reached last page remove footer if footer added already
                getListAdapter().removeFooter(loadingLayout);
            }
            return;
        }
        if (getLoaderManager().hasRunningLoaders())
            return;
        if (listView != null && pager != null
                && (listView.getLastVisiblePosition() + 3) >= pager.size()) {
            if(getListAdapter().getFootersCount() == 0) {
                // Display loading footer if not present when loading next page
                getListAdapter().addFooter(loadingLayout);
            }
            refresh();
        }
    }

    private void refreshWithProgress() {
        if (pager != null) {
            pager.reset();
        }
        items.clear();
        setListShown(false);
        refresh();
    }

    private void refresh() {
        getLoaderManager().restartLoader(0, null, this);
    }

    private void setListShown(final boolean shown) {
        if (shown) {
            if (!items.isEmpty()) {
                hide(progressBar).hide(emptyView).show(resultsLayout);
            } else {
                hide(progressBar).hide(resultsLayout).show(emptyView);
            }
        } else {
            hide(resultsLayout).hide(emptyView).show(progressBar);
        }
    }

    private int getErrorMessage(RetrofitException exception) {
        if(exception.isUnauthenticated()) {
            return R.string.testpress_authentication_failed;
        } else if (exception.isNetworkError()) {
            setEmptyText(R.string.testpress_network_error, R.string.testpress_no_internet_try_again,
                    R.drawable.ic_error_outline_black_18dp);
            return R.string.testpress_no_internet_try_again;
        } else {
            setEmptyText(R.string.testpress_error_loading_exams,
                    R.string.testpress_some_thing_went_wrong_try_again,
                    R.drawable.ic_error_outline_black_18dp);
        }
        return R.string.testpress_error_loading_exams;
    }

    private void show(final View view) {
        ViewUtils.setGone(view, false);
    }

    private SearchFragment hide(final View view) {
        ViewUtils.setGone(view, true);
        return this;
    }

    private HeaderFooterListAdapter<SingleTypeAdapter<Post>> getListAdapter() {
        //noinspection unchecked
        return (HeaderFooterListAdapter<SingleTypeAdapter<Post>>) listView.getAdapter();
    }

    private RetrofitException getException(final Loader<List<Post>> loader) {
        if (loader instanceof ThrowableLoader) {
            return ((ThrowableLoader<List<Post>>) loader).clearException();
        } else {
            return null;
        }
    }

    private void setEmptyText(int title, int description, int imageResId) {
        emptyView.setVisibility(View.VISIBLE);
        emptyTitleView.setText(title);
        emptyDescView.setText(description);
        emptyImageView.setImageResource(imageResId);
        retryButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<List<Post>> loader) {
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

}
