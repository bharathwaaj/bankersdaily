package in.bankersdaily.ui;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.view.Menu;
import android.view.MenuItem;

import org.greenrobot.greendao.query.LazyList;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import in.bankersdaily.BankersDailyApp;
import in.bankersdaily.R;
import in.bankersdaily.model.Bookmark;
import in.bankersdaily.model.BookmarkDao;
import in.bankersdaily.model.CategoryDao;
import in.bankersdaily.model.Post;
import in.bankersdaily.util.ShareUtil;

import static in.bankersdaily.ui.PostsListFragment.CATEGORY_ID;

public class PostDetailPagerActivity extends BaseToolBarActivity {

    public static final String POST_POSITION = "postPosition";
    public static final String FILTER_BOOKMARKED = "filterBookmarked";

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    private int categoryId;
    private Menu menu;
    private LazyList<Post> posts;
    private BookmarkDao bookmarkDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        ButterKnife.bind(this);
        categoryId = getIntent().getIntExtra(CATEGORY_ID, 0);
        setActionBarTitle();
        bookmarkDao = BankersDailyApp.getDaoSession(this).getBookmarkDao();
        boolean filterBookmarked = getIntent().getBooleanExtra(FILTER_BOOKMARKED, false);
        QueryBuilder<Post> queryBuilder =
                Post.getPostListQueryBuilder(this, categoryId, filterBookmarked);

        posts = queryBuilder.listLazy();
        PostDetailViewPagerAdapter adapter = new PostDetailViewPagerAdapter(this, posts);
        viewPager.setAdapter(adapter);
        int currentPosition = getIntent().getIntExtra(POST_POSITION, 0);
        if (currentPosition < adapter.getCount()) {
            viewPager.setCurrentItem(currentPosition);
        }
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE || state == ViewPager.SCROLL_STATE_SETTLING) {
                    updateOptionsMenu();
                }
            }
        });
    }

    void updateOptionsMenu() {
        Bookmark bookmark = getBookmark(posts.get(viewPager.getCurrentItem()));
        MenuItem item = menu.getItem(0);
        Drawable drawable = item.getIcon();
        if (bookmark != null) {
            item.setTitle(R.string.unbookmark);
            drawable.mutate().setColorFilter(ContextCompat.getColor(this, R.color.dark_yellow),
                    PorterDuff.Mode.SRC_IN);
        } else {
            item.setTitle(R.string.bookmark);
            drawable.mutate().setColorFilter(ContextCompat.getColor(this, R.color.actionbar_text),
                    PorterDuff.Mode.SRC_IN);
        }
        item.setIcon(drawable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bookmark_and_share, menu);
        this.menu = menu;
        updateOptionsMenu();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Post post = posts.get(viewPager.getCurrentItem());
        switch (item.getItemId()) {
            case R.id.bookmark:
                Bookmark bookmark = getBookmark(post);
                Drawable drawable = item.getIcon();
                if (bookmark != null) {
                    bookmark.delete();
                    item.setTitle(R.string.bookmark);
                    drawable.mutate().setColorFilter(ContextCompat.getColor(this,
                            R.color.actionbar_text), PorterDuff.Mode.SRC_IN);
                } else {
                    bookmark = new Bookmark(post.getId());
                    bookmarkDao.insertOrReplaceInTx(bookmark);
                    item.setTitle(R.string.unbookmark);
                    drawable.mutate().setColorFilter(ContextCompat.getColor(this,
                            R.color.dark_yellow), PorterDuff.Mode.SRC_IN);
                }
                item.setIcon(drawable);
                return true;
            case R.id.share:
                ShareUtil.shareUrl(this, post.getTitle(), post.getLink());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Nullable
    Bookmark getBookmark(Post post) {
        List<Bookmark> bookmarks = bookmarkDao.queryBuilder()
                .where(BookmarkDao.Properties.Id.eq(post.getId()))
                .list();

        if (!bookmarks.isEmpty()) {
            return bookmarks.get(0);
        }
        return null;
    }

    @SuppressWarnings("ConstantConditions")
    void setActionBarTitle() {
        if (categoryId != 0) {
            String title = BankersDailyApp.getDaoSession(this).getCategoryDao().queryBuilder()
                    .where(CategoryDao.Properties.Id.eq(categoryId)).list().get(0).getName();

            getSupportActionBar().setTitle(title);
        } else {
            int titleRes = getIntent().getBooleanExtra(FILTER_BOOKMARKED, false) ?
                    R.string.bookmarked_articles : R.string.latest_articles;

            getSupportActionBar().setTitle(titleRes);
        }
    }

    @Override
    protected String getScreenName() {
        return BankersDailyApp.POST_DETAIL;
    }

}
