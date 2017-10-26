package in.bankersdaily;

import android.app.Application;
import android.content.Context;

import in.bankersdaily.model.DaoMaster;
import in.bankersdaily.model.DaoSession;

public class BankersDailyApp extends Application {

    private DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "bankers-daily-db");
        daoSession = new DaoMaster(helper.getWritableDb()).newSession();
    }

    public  DaoSession getDaoSession() {
        return daoSession;
    }

    public static DaoSession getDaoSession(Context context) {
        return ((BankersDailyApp) context.getApplicationContext()).getDaoSession();
    }

}
