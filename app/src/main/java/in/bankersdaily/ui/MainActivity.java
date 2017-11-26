package in.bankersdaily.ui;

import android.content.Intent;
import android.os.Bundle;

import in.bankersdaily.R;
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

    void checkAuth() {
        if (TestpressSdk.hasActiveSession(this)) {
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
