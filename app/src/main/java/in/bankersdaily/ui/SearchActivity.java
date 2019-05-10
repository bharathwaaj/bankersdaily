package in.bankersdaily.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import static android.view.WindowManager.LayoutParams.FLAG_SECURE;

import in.bankersdaily.R;

public class SearchActivity extends AppCompatActivity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testpress_container_layout_without_toolbar);
        getWindow().setFlags(FLAG_SECURE,FLAG_SECURE);
        SearchFragment searchFragment = new SearchFragment();
        searchFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, searchFragment).commitAllowingStateLoss();
    }

}
