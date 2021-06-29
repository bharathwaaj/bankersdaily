package in.bankersdaily.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.onesignal.OSNotificationOpenedResult;
import com.onesignal.OneSignal;

import in.bankersdaily.R;

public class NotificationHandler implements OneSignal.OSNotificationOpenedHandler {

    private Context context;

    public NotificationHandler(Context context) {
        this.context = context;
    }

    // This fires when a notification is opened by tapping on it.
    @Override
    public void notificationOpened(OSNotificationOpenedResult result) {
        Intent intent;
        String launchUrl = result.getNotification().getLaunchURL();
        Uri uri = Uri.parse(launchUrl);
        if (uri != null && !uri.getHost().equals(context.getString(R.string.host_url))
                && !uri.getHost().equals(context.getString(R.string.testpress_host_url))) {

            intent= new Intent(Intent.ACTION_VIEW, uri);
        } else {
            intent = new Intent(context, SplashScreenActivity.class);
            intent.setData(Uri.parse(launchUrl));
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
