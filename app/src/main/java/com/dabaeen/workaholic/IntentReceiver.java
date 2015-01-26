package com.dabaeen.workaholic;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
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

        stopAlarm(context);

        Intent i = new Intent(context, NotificationActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    private void stopAlarm(Context context){

        AlarmManager alarmMgr;
        PendingIntent alarmIntent;

        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context.getApplicationContext(), NotificationActivity.class);
        alarmIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, intent, 0);

        alarmMgr.cancel(alarmIntent);
    }
}
