package in.bankersdaily.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

import in.bankersdaily.R;
import in.bankersdaily.model.Post;
import in.bankersdaily.network.ApiClient;
import in.bankersdaily.network.RetrofitCallback;
import in.bankersdaily.network.RetrofitException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new ApiClient(this).getPosts().enqueue(new RetrofitCallback<List<Post>>() {
            @Override
            public void onSuccess(List<Post> posts) {
                Log.e("onSuccess ", "size-" + posts.size());
            }

            @Override
            public void onException(RetrofitException exception) {

            }
        });
    }
}
