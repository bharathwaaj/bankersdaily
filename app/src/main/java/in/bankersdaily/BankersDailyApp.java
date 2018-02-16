package in.bankersdaily;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import org.greenrobot.greendao.database.Database;

import java.util.HashMap;
import java.util.Map;

import in.bankersdaily.model.DaoMaster;
import in.bankersdaily.model.DaoSession;

public class BankersDailyApp extends Application {

    public static final String MIXPANEL_TOKEN = "7fa1f690e4e6eb65f22039be795c31dc";

    public static final String HOME_TAB = "Home Tab";
    public static final String LATEST_POSTS_TAB = "Latest posts Tab";
    public static final String CATEGORIES_TAB = "Categories Tab";
    public static final String BOOKMARKS_TAB = "Bookmarks Tab";
    public static final String POST_DETAIL = "Post Detail";
    public static final String SEARCH_SCREEN = "Search Screen";
    public static final String CATEGORIES_LIST_ACTIVITY = "Categories List Activity";
    public static final String POSTS_LIST_ACTIVITY = "Posts List Activity";
    public static final String LOGIN_SCREEN = "Login Screen";

    public static final String SCREEN_NAME = "ScreenName";
    public static final String VALUE = "Value";
    public static final String POST_SLUG = "PostSlug";
    public static final String COMMENT_ID = "Comment";
    public static final String LOGGED_IN = "LoggedIn";
    public static final String LOGGED_OUT = "LoggedOut";
    public static final String LOGGED_IN_VIA_FB = "LoggedInViaFB";
    public static final String LOGGED_IN_VIA_GOOGLE = "LoggedInViaGoogle";
    public static final String EMAIL_NOT_FOUND = "EmailNotFound";
    public static final String COMMENTED = "Commented";
    public static final String BOOKMARKED = "Bookmarked";
    public static final String SHARED_POST = "SharedPost";
    public static final String SHARED_APP = "SharedApp";
    public static final String CLICKED_RATE_US = "ClickedRateUs";
    public static final String SENT_FEEDBACK = "SentFeedBack";
    public static final String SENT_SUGGESTION = "SentSuggestion";
    public static final String LOGIN_STATE = "LoginState";
    public static final String AUTHORIZED = "Authorized";
    public static final String UNAUTHORIZED = "Unauthorized";

    private static BankersDailyApp instance;

    private Database database;
    private DaoSession daoSession;
    private FirebaseAnalytics mFirebaseAnalytics;
    private MixpanelAPI mMixpanel;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "bankers-daily-db");
        database = helper.getWritableDb();
        daoSession = new DaoMaster(database).newSession();

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mMixpanel = MixpanelAPI.getInstance(this, MIXPANEL_TOKEN);
    }

    public static BankersDailyApp getInstance() {
        return instance;
    }

    public  DaoSession getDaoSession() {
        return daoSession;
    }

    public static DaoSession getDaoSession(Context context) {
        return ((BankersDailyApp) context.getApplicationContext()).getDaoSession();
    }

    public void clearDatabase() {
        DaoMaster.dropAllTables(database, true);
        DaoMaster.createAllTables(database, true);
        daoSession.clear();
    }

    public  void setLoginState(boolean isUserAuthenticated) {
        Map<String, Object> props = new HashMap<>();
        String state = isUserAuthenticated ? AUTHORIZED : UNAUTHORIZED;
        props.put(LOGIN_STATE, state);
        mMixpanel.registerSuperPropertiesMap(props);
        mFirebaseAnalytics.setUserProperty(LOGIN_STATE, state);
    }

    public void trackEvent(String screenName, String event, String value) {
        Bundle bundle = new Bundle();
        bundle.putString(SCREEN_NAME, screenName);
        Map<String, Object> props = new HashMap<>();
        props.put(SCREEN_NAME, screenName);
        if (value != null) {
            bundle.putString(VALUE, value);
            props.put(VALUE, value);
        }
        mFirebaseAnalytics.logEvent(event, bundle);
        mMixpanel.trackMap(event, props);
    }

    public void trackEvent(String screenName, String event, String value, @NonNull Bundle bundle) {
        bundle.putString(SCREEN_NAME, screenName);
        if (value != null) {
            bundle.putString(VALUE, value);
        }
        mFirebaseAnalytics.logEvent(event, bundle);
    }

    public void trackScreenView(Activity activity, String screenName) {
        mFirebaseAnalytics.setCurrentScreen(activity, screenName, null);
        mMixpanel.track(screenName);
    }

    public void submitMixpanelData() {
        mMixpanel.flush();
    }

}
