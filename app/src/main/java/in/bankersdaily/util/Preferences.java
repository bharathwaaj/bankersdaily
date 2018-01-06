package in.bankersdaily.util;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

    public static final String KEY_AUTH_SHARED_PREFS = "authSharedPreferences";
    public static final String KEY_WORDPRESS_TOKEN = "wordPressToken";

    public static String getWordPressToken(Context context) {
        SharedPreferences pref =
                context.getSharedPreferences(KEY_AUTH_SHARED_PREFS, Context.MODE_PRIVATE);

        return pref.getString(KEY_WORDPRESS_TOKEN, "");
    }
}
