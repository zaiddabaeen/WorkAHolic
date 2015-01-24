package com.dabaeen.workaholic;

import android.animation.TimeInterpolator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.DropBoxManager;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.Calendar;
import java.util.Date;

import com.dabaeen.workaholic.Constants.*;

public class MainActivity extends Activity  {

    public final static int PENDING_CODE = 1542;

    boolean isRunning = false;
    long initialTime;
    int currentMode = Mode.DAY;

    LinearLayout master;
    FrameLayout wheelFrame;
    ScrollView scroll;
    TextView tvDuration, tvToday;
    ProgressWheel wheel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getActionBar().setDisplayShowTitleEnabled(false);
        setContentView(R.layout.activity_main);

        master = (LinearLayout) findViewById(R.id.master);
        wheelFrame = (FrameLayout) findViewById(R.id.wheelFrame);
        scroll = (ScrollView) findViewById(R.id.scroll);
        tvDuration = (TextView) findViewById(R.id.tvDuration);
        tvToday = (TextView) findViewById(R.id.tvToday);
        wheel = (ProgressWheel) findViewById(R.id.progress_wheel);

        setWheelGestures();

//        wheelFrame.setOnTouchListener(new OnSwipeTouchListener(this){
//
//            @Override
//            public void onSwipeRight() {
//               reverse(1);
//            }
//
//            @Override
//            public void onSwipeLeft() {
//                reverse(-1);
//            }
//
//
//        });

        wheel.setBarWidth(50);

        if(App.WorkingDays == null){
            System.exit(1);
        }

        addDays();
    }

    public void reverse(final int sign){

        if(currentMode == Mode.DAY) {
            currentMode = Mode.WEEK;

            tvToday.animate().translationXBy(sign*40f).alpha(0).setDuration(300).withEndAction(new Runnable() {
                @Override
                public void run() {
                    tvToday.setText("This Week");
                    tvToday.setX(tvToday.getX() + sign*-80f);
                    tvToday.animate().translationXBy(sign*40f).alpha(1).setDuration(300).start();
                }
            }).start();

            tvToday.setGravity(Gravity.CENTER_HORIZONTAL);
            tvDuration.setText(App.thisWeek().getDuration());
            setWeeklyProgress();

        } else {
            currentMode = Mode.DAY;
            tvToday.animate().translationXBy(sign*40f).alpha(0).setDuration(300).withEndAction(new Runnable() {
                @Override
                public void run() {
                    tvToday.setText("Today");
                    tvToday.setX(tvToday.getX() + sign*-80f);
                    tvToday.animate().translationXBy(sign*40f).alpha(1).setDuration(300).start();
                }
            }).start();

            tvToday.setGravity(Gravity.CENTER_HORIZONTAL);
            tvDuration.setText(App.today().getDuration());
            setWorkingProgress(App.today().SecondsWorked);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(isRunning || App.prefs.getBoolean(App.IS_COUNTING, true)){
            isRunning = true;
            cancelTimer = false;

            if(App.prefs.getBoolean(App.IS_COUNTING, true)) {
                initialTime = App.prefs.getLong(App.INITIAL_COUNT, 0);
                if (initialTime == 0) master.setBackgroundColor(Color.RED);
            }

            startTimer();
            return;
        }

        if(!isRunning || App.prefs.getBoolean(App.IS_COUNTING, false)){
            isRunning = false;
            cancelTimer = true;
            tvDuration.setText(App.today().getDuration());
            setWorkingProgress(App.today().SecondsWorked);
        }


    }

    public void addDays(){

        int currentWeek = -1;

        for(WorkingDay day : App.WorkingDays){

            int week = day.getWeek();

            DayView view = new DayView(this, day);

            if(day.isToday() || day.SecondsWorked > 0) {
                if (week == currentWeek) {

                    WeekView wv = (WeekView) master.getChildAt(0);
                    wv.addSeconds(day.SecondsWorked);

                    wv.addDay(view);
                } else {

                    WeekView wv = new WeekView(this, day.SecondsWorked, week);
                    master.addView(wv, 0);
                    App.WorkingWeeks.add(wv.Week);

                    wv.addDay(view);
                }
            }
            currentWeek = week;
            //master.addView(view, 1);

            addContextMenu(view);

        }

    }

    private void addContextMenu(View view){
        // Long click listener and pop up menu
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View view) {
                PopupMenu popup = new PopupMenu(MainActivity.this, view);
                MenuInflater inflater = popup.getMenuInflater();
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch(item.getItemId()){
                            case R.id.context_delete:
                                master.removeViewInLayout(view);
                                view.animate().alpha(0).setDuration(2000).withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        App.WorkingDays.remove(((DayView) view).workingDay);
                                        App.saveLog();
                                        master.requestLayout();
                                    }
                                }).start();
                                break;
                        }
                        return true;
                    }
                });
                inflater.inflate(R.menu.menu_context, popup.getMenu());
                popup.show();
                return true;
            }
        });
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

    private void setWeeklyProgress(){

        wheel.setSpinSpeed((float) App.thisWeek().SecondsWorked/App.weeklyWork);
        wheel.setProgress((float) App.thisWeek().SecondsWorked / App.weeklyWork);

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

    private void setWheelGestures(){

        final GestureDetector gestureDetector = new GestureDetector(new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent motionEvent) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent motionEvent) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent motionEvent) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent start, MotionEvent finish, float v, float v2) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent motionEvent) {

            }

            @Override
            public boolean onFling(MotionEvent start, MotionEvent finish, float v, float v2) {
                if(start.getRawX() > finish.getRawX()){
                    reverse(1);
                } else if(start.getRawX() < finish.getRawX()){
                    reverse(-1);
                }
                return true;
            }
        });
        wheelFrame.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                return gestureDetector.onTouchEvent(motionEvent);
            }
        });

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
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;
            case R.id.action_clear:
                clearData();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

}
