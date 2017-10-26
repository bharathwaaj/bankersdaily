package in.bankersdaily.ui;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import in.bankersdaily.BankersDailyApp;
import in.bankersdaily.R;
import in.bankersdaily.model.DaoSession;
import in.bankersdaily.model.Post;
import in.bankersdaily.network.ApiClient;
import in.bankersdaily.network.PostPager;
import in.bankersdaily.network.RetrofitException;
import in.bankersdaily.util.FormatDate;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new PostAsyncTask().execute();
    }

    private class PostAsyncTask extends AsyncTask<Object, Object, List<Post>> {

        @Override
        protected List<Post> doInBackground(Object[] params) {
            PostPager pager = new PostPager(new ApiClient(MainActivity.this));
            List<Post> posts = new ArrayList<>();
            boolean hasMore;
            try {
                do {
                    hasMore = pager.next();
                    posts = pager.getResources();
                } while (hasMore && posts.size() < 30);
            } catch (RetrofitException e) {
                e.printStackTrace();
            }
            DaoSession daoSession = BankersDailyApp.getDaoSession(MainActivity.this);
            daoSession.getPostDao().insertOrReplaceInTx(posts);
            Log.e("posts.size", "" + posts.size());
            for (Post post : posts) {
                Log.d("post-title:", post.getTitle() + "");
                Log.d("Date:", FormatDate.getISODateString(post.getDate()));
                Log.d("Date:", post.getDate().toString());
            }
            return posts;
        }

    }
}
