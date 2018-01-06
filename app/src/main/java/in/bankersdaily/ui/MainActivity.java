package in.bankersdaily.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import in.bankersdaily.R;
import in.bankersdaily.util.Preferences;
import in.testpress.core.TestpressSdk;

import static in.bankersdaily.ui.LoginActivity.AUTHENTICATE_REQUEST_CODE;

public class MainActivity extends BaseToolBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_layout);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        checkAuth();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu);
        return true;
    }

    void checkAuth() {
        String wordPressToken = Preferences.getWordPressToken(this);
        if (!wordPressToken.isEmpty() && TestpressSdk.hasActiveSession(this)) {
            displayHomeScreen();
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, AUTHENTICATE_REQUEST_CODE);
        }
    }

    void displayHomeScreen() {
        HomeTabFragment fragment = new HomeTabFragment();
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTHENTICATE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                displayHomeScreen();
            } else {
                finish();
            }
        }
    }

}
