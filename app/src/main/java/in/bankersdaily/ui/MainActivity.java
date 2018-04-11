package in.bankersdaily.ui;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.facebook.login.LoginManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import in.bankersdaily.BankersDailyApp;
import in.bankersdaily.R;
import in.bankersdaily.util.Assert;
import in.bankersdaily.util.Preferences;
import in.testpress.core.TestpressSDKDatabase;
import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.course.TestpressCourse;
import in.testpress.exam.TestpressExam;
import in.testpress.models.InstituteSettings;
import in.testpress.store.TestpressStore;
import io.doorbell.android.Doorbell;
import io.doorbell.android.callbacks.OnFeedbackSentCallback;

import static android.support.design.widget.Snackbar.LENGTH_SHORT;
import static in.bankersdaily.ui.LoginActivity.AUTHENTICATE_REQUEST_CODE;

public class MainActivity extends BaseToolBarActivity {

    @BindView(R.id.drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.navigation_view) NavigationView navigationView;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(android.R.id.content) View rootView;

    private ActionBarDrawerToggle drawerToggle;
    private int selectedItem;
    private boolean backPressedOnce = false;
    private boolean isUserAuthenticated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_drawer_activity);
        ButterKnife.bind(this);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(drawerToggle);
        initScreen();
    }

    void updateAuthenticationState() {
        isUserAuthenticated = checkAuthentication();
        BankersDailyApp.getInstance().setLoginState(isUserAuthenticated);
        navigationView.getMenu().findItem(R.id.logout).setVisible(isUserAuthenticated);
        if (isUserAuthenticated) {
            checkInstituteSettings();
        }
    }

    void initScreen() {
        updateAuthenticationState();
        selectedItem = R.id.home;
        displayHomeScreen();
    }

    boolean checkAuthentication() {
        String wordPressToken = Preferences.getWordPressToken(this);
        return !wordPressToken.isEmpty() && TestpressSdk.hasActiveSession(this);
    }

    public void selectDrawerItem(MenuItem menuItem) {
        switch(menuItem.getItemId()) {
            case R.id.home:
                displayHomeScreen();
                selectedItem = R.id.home;
                break;
            case R.id.courses:
                selectedItem = R.id.courses;
                showAuthenticatedItem();
                removeSelectedBackground();
                break;
            case R.id.exams:
                selectedItem = R.id.exams;
                showAuthenticatedItem();
                removeSelectedBackground();
                break;
            case R.id.store:
                selectedItem = R.id.store;
                showAuthenticatedItem();
                removeSelectedBackground();
                break;
            case R.id.feedback:
                showDoorBellDialog(R.string.feedback);
                removeSelectedBackground();
                break;
            case R.id.request_feature:
                showDoorBellDialog(R.string.request_feature);
                removeSelectedBackground();
                break;
            case R.id.rate_us:
                BankersDailyApp.getInstance().trackEvent(
                        getScreenName(),
                        BankersDailyApp.CLICKED_RATE_US,
                        null
                );
                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                }
                removeSelectedBackground();
                break;
            case R.id.share:
                BankersDailyApp.getInstance().trackEvent(
                        getScreenName(),
                        BankersDailyApp.SHARED_APP,
                        null
                );
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT,
                        getString(R.string.share_message) + getPackageName());

                startActivity(Intent.createChooser(share, "Share with"));
                removeSelectedBackground();
                break;
            case R.id.logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
                builder.setTitle(R.string.logout);
                builder.setMessage(R.string.logout_alert);
                builder.setPositiveButton(getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                logout();
                            }
                });
                builder.setNegativeButton(getResources().getString(R.string.no), null);
                builder.show();
                removeSelectedBackground();
                break;
        }
        drawerLayout.closeDrawers();
    }

    void removeSelectedBackground() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                navigationView.getMenu().findItem(R.id.home).setChecked(true);
            }
        });
    }

    void showAuthenticatedItem() {
        if (isUserAuthenticated) {
            TestpressSession session = TestpressSdk.getTestpressSession(this);
            Assert.assertNotNull("TestpressSession must not be null.", session);
            switch (selectedItem) {
                case R.id.courses:
                    TestpressCourse.show(this, session);
                    break;
                case R.id.exams:
                    TestpressExam.show(this, session);
                    break;
                case R.id.store:
                    TestpressStore.show(this, session);
                    break;
            }
        } else {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, AUTHENTICATE_REQUEST_CODE);
        }
    }

    void showDoorBellDialog(final int title) {
        Doorbell doorbellDialog = new Doorbell(this, 7839,
                "dd67sU6xK7IkC5JoogKdRpWsaAlCKt2WVvIpXXrVkiyH5IhRZ14Q166Sqzt7Y56g");

        if (isUserAuthenticated) {
            doorbellDialog.setName(Preferences.getWordpressUsername(this));
        }
        doorbellDialog.setTitle(title);
        doorbellDialog.setPoweredByVisibility(View.GONE);
        doorbellDialog.setOnFeedbackSentCallback(
                new OnFeedbackSentCallback() {
                    @Override
                    public void handle(String message) {
                        String action;
                        if (title == R.string.feedback) {
                            action = BankersDailyApp.SENT_FEEDBACK;
                        } else {
                            action = BankersDailyApp.SENT_SUGGESTION;
                            message = "Thanks for your suggestion!";
                        }
                        BankersDailyApp.getInstance().trackEvent(getScreenName(), action, null);
                        Snackbar.make(rootView, message, LENGTH_SHORT).show();
                    }
        });
        doorbellDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search, menu);
        return true;
    }

    void displayHomeScreen() {
        HomeTabFragment fragment = new HomeTabFragment();
        fragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    void logout() {
        Preferences.clearAll(this);
        TestpressSdk.clearActiveSession(this);
        TestpressSDKDatabase.clearDatabase(this);
        LoginManager.getInstance().logOut();
        BankersDailyApp.getInstance().clearDatabase();
        isUserAuthenticated = false;
        BankersDailyApp.getInstance().setLoginState(false);
        BankersDailyApp.getInstance().trackEvent(getScreenName(), BankersDailyApp.LOGGED_OUT, null);
        initScreen();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.search:
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        //check drawer is open
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
        } else {
            // Backpress twice to exit the app
            if (backPressedOnce) {
                super.onBackPressed();
                return;
            }
            this.backPressedOnce = true;
            Snackbar.make(rootView, R.string.press_again_to_exit, LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    backPressedOnce = false;
                }
            }, 2000);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTHENTICATE_REQUEST_CODE && resultCode == RESULT_OK) {
            updateAuthenticationState();
            showAuthenticatedItem();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isUserAuthenticated && checkAuthentication()) {
            updateAuthenticationState();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        BankersDailyApp.getInstance().submitMixpanelData();
        super.onDestroy();
    }

    @Override
    protected String getScreenName() {
        return "";
    }

    @Override
    protected void trackScreenViewAnalytics() {
    }

    void checkInstituteSettings() {
        // This will fix crash when user comment in review question for users who installed app
        // version below 1.0.8
        TestpressSession session = TestpressSdk.getTestpressSession(this);
        //noinspection ConstantConditions
        InstituteSettings settings = session.getInstituteSettings();
        if (settings.isCommentsVotingEnabled() == null) {
            settings.setCommentsVotingEnabled(false);
            session.setInstituteSettings(settings);
            TestpressSdk.setTestpressSession(this, session);
        }
    }
}
