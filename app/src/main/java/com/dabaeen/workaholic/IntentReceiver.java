package com.dabaeen.workaholic;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Date;

/**
 * Created by redtroops on 1/21/15.
 */
public class IntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        long initialTime = App.prefs.getLong(App.INITIAL_COUNT, 0);

        App.prefs.edit().putBoolean(App.IS_COUNTING, false)
                .putLong(App.INITIAL_COUNT, 0).commit();

        long duration = (new Date()).getTime() - initialTime;
        duration /= 1000;

        App.today().SecondsWorked += duration;
        App.saveLog();

        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(MainActivity.PENDING_CODE);

    }
}
