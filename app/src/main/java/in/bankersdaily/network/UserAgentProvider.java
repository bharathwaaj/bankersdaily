package in.bankersdaily.network;

import android.content.Context;

import in.bankersdaily.BuildConfig;

/**
 * Class that builds a User-Agent that is set on all HTTP calls.
 *
 * The user agent will change depending on the version of Android that
 * the user is running, the device their running and the version of the
 * app that they're running. This will allow your remote API to perform
 * User-Agent inspection to provide different logic routes or analytics
 * based upon the User-Agent.
 *
 * Example
 *
 * Dalvik/2.1.0 (Linux; U; Android 6.0; XT1068 Build/MPB24.65-34-3) BankersDaily/1.0
 *
 */
class UserAgentProvider {

    private static String userAgent;

    static String get(Context context) {
        if (userAgent == null) {
            synchronized (UserAgentProvider.class) {
                if (userAgent == null) {
                    userAgent = String.format("%s %s/%s",
                            System.getProperty("http.agent"),
                            context.getApplicationInfo().loadLabel(context.getPackageManager()), // App name
                            BuildConfig.VERSION_NAME // App Version
                    );
                }
            }
        }
        return userAgent;
    }
}
