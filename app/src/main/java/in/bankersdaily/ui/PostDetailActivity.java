package in.bankersdaily.ui;

import android.os.Bundle;

import in.bankersdaily.R;

public class PostDetailActivity extends BaseToolBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_layout);
        PostDetailFragment fragment = new PostDetailFragment();
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

}
