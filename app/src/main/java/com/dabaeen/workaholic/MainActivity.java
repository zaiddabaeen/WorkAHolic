package com.dabaeen.workaholic;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends Activity {

    public final static int PENDING_CODE = 1542;

    boolean isRunning = false;
    long initialTime;

    LinearLayout master;
    TextView tvDuration;
    ProgressWheel wheel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayShowTitleEnabled(false);
        setContentView(R.layout.activity_main);

        master = (LinearLayout) findViewById(R.id.master);
        tvDuration = (TextView) findViewById(R.id.tvDuration);
        wheel = (ProgressWheel) findViewById(R.id.progress_wheel);

        wheel.setBarWidth(50);

        if(App.WorkingDays == null){
            System.exit(1);
        }

        if(App.prefs.getBoolean(App.IS_COUNTING, false)){

            isRunning = true;
            initialTime = App.prefs.getLong(App.INITIAL_COUNT, 0);
            if(initialTime==0) master.setBackgroundColor(Color.RED);

            startTimer();

        } else {
            setWorkingProgress(App.today().SecondsWorked);
        }

        tvDuration.setText(App.today().getDuration());
        addDays();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(isRunning && App.prefs.getBoolean(App.IS_COUNTING, false)){
            isRunning = false;
            cancelTimer = true;
            setWorkingProgress(App.today().SecondsWorked);
            tvDuration.setText(App.today().getDuration());
        }

    }

    public void addDays(){

        for(WorkingDay day : App.WorkingDays){
            master.addView(new DayView(this, day), 1);
        }

    }

    public void MeasureDuration(View view){

        if(!isRunning){
            isRunning = true;
            initialTime = (new Date()).getTime();

            App.prefs.edit().putBoolean(App.IS_COUNTING, true)
                    .putLong(App.INITIAL_COUNT, initialTime).commit();
            startTimer();
            showNotification();
            return;
        } else {
            isRunning = false;
            cancelTimer = true;

            App.prefs.edit().putBoolean(App.IS_COUNTING, false)
                    .putLong(App.INITIAL_COUNT, 0).commit();

            long duration = (new Date()).getTime() - initialTime;
            duration /= 1000;

            App.today().SecondsWorked += duration;
            tvDuration.setText(App.today().getDuration());
            setWorkingProgress(App.today().SecondsWorked);
            App.saveLog();
            hideNotification();
        }

    }

    private Thread timer;
    private Handler tHandler;
    private boolean cancelTimer = false;
    private void startTimer(){

        tHandler = new Handler();
        cancelTimer = false;

        wheel.setSpinSpeed(0.1f);
        wheel.spin();

        timer = new Thread(new Runnable() {
            @Override
            public void run() {
                if(cancelTimer) return;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        long duration = (new Date()).getTime() - initialTime;
                        duration /= 1000;

                        tvDuration.setText(formatSeconds(duration + App.today().SecondsWorked));
                    }
                });

                tHandler.postDelayed(this, 1000);
            }
        });

        timer.start();
    }

    private void setWorkingProgress(long seconds){

        wheel.setSpinSpeed((float) seconds/App.dailyWork);
        wheel.setProgress((float) seconds/App.dailyWork);

    }

    private String formatSeconds(long seconds){
        long hours = seconds / 3600;
       long  minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void showNotification(){

        Intent dismissIntent = new Intent(this.getPackageName() + ".STOP");
        //dismissIntent.setAction(this.getPackageName() + ".STOP");
        PendingIntent piDismiss = PendingIntent.getBroadcast(this, 0, dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Constructs the Builder object.
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setContentTitle("WorkAHolic")
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("Working"))
                        .addAction (android.R.drawable.ic_delete,
                                "Stop", piDismiss)
                        .setAutoCancel(true);

        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(PENDING_CODE, builder.build());

    }

    private void hideNotification(){

        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(PENDING_CODE);

    }

    private void clearData(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Are you sure you want to clear all statistics?")
                .setPositiveButton("Clear Statistics", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        App.WorkingDays.clear();
                        App.saveLog();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id){
            case R.id.action_settings:
                break;
            case R.id.action_clear:
                clearData();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
