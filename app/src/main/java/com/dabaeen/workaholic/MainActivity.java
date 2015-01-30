package com.dabaeen.workaholic;

import android.animation.TimeInterpolator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.DropBoxManager;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v7.internal.view.menu.MenuBuilder;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextThemeWrapper;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.gc.materialdesign.views.ButtonFloat;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.dabaeen.workaholic.Constants.*;

public class MainActivity extends Activity  {

    public final static int PENDING_CODE = 1542;

    boolean isRunning = false;
    long initialTime;
    int currentMode = Mode.DAY;
    long total = 0;

    LinearLayout master;
    FrameLayout wheelFrame;
    ScrollView scroll;
    TextView tvDuration, tvToday, tvTotal;
    ProgressWheel wheel;
    ImageButton btnProfiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        master = (LinearLayout) findViewById(R.id.master);
        wheelFrame = (FrameLayout) findViewById(R.id.wheelFrame);
        scroll = (ScrollView) findViewById(R.id.scroll);
        tvDuration = (TextView) findViewById(R.id.tvDuration);
        tvToday = (TextView) findViewById(R.id.tvToday);
        tvTotal = (TextView) findViewById(R.id.tvTotal);
        wheel = (ProgressWheel) findViewById(R.id.progress_wheel);
        btnProfiles = (ImageButton) findViewById(R.id.btnProfiles);

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

        getActionBar().setTitle(getResources().getString(R.string.app_name) + ": " + App.CurrentProfile.Name);
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
            setWorkingProgress(App.today().SecondsWorked);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(App.prefs.getBoolean(App.IS_COUNTING, true)){
            isRunning = true;
            cancelTimer = false;

                initialTime = App.prefs.getLong(App.INITIAL_COUNT, 0);
                if (initialTime == 0) master.setBackgroundColor(Color.RED);

            startTimer();
            return;
        } else {
            isRunning = false;
            cancelTimer = true;
            setWorkingProgress(App.today().SecondsWorked);
            btnProfiles.setEnabled(true);
            btnProfiles.setClickable(true);

        }


    }

    public void addDays(){

        int currentWeek = -1;
        total = 0;
        master.removeAllViews();

        for(WorkingDay day : App.WorkingDays){

            int week = day.getWeek();

            Log.i("workaholic", day.WorkDate + " " + day.getWeek() + " " + GregorianCalendar.getInstance().getFirstDayOfWeek());
            DayView view = new DayView(this, day);
            total += day.SecondsWorked;

            if(day.isToday() || day.SecondsWorked > 0) {
                if (week == currentWeek) {

                    WeekView wv = (WeekView) ((CardView) master.getChildAt(0)).getChildAt(0);
                    wv.addSeconds(day.SecondsWorked);

                    wv.addDay(view);
                } else {

                    // Creating Week View
                    CardView cv = new CardView(this);
                    cv.setRadius(10);
                    LinearLayout.LayoutParams parm = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    parm.setMargins(10,10,10,10);
                    cv.setLayoutParams(parm);
                    WeekView wv = new WeekView(this, day.SecondsWorked, week);
                    cv.addView(wv);
                    master.addView(cv, 0);

                    App.WorkingWeeks.add(wv.Week);

                    wv.addDay(view);
                }
            }
            currentWeek = week;
            //master.addView(view, 1);

            addContextMenu(view);

        }

        tvTotal.setText(String.format("%02d:%02d", total/3600, (total%3600)/60));

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
                        final WorkingDay wDay = ((DayView) view).workingDay;
                        switch(item.getItemId()){
                            case R.id.context_delete:
                                master.removeViewInLayout(view);
                                view.animate().alpha(0).setDuration(2000).withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        App.WorkingDays.remove(wDay);
                                        App.saveLog();
                                        master.requestLayout();
                                    }
                                }).start();
                                break;
                            case R.id.context_edit:
                                TimePickerDialog timePickerDialog = new TimePickerDialog(
                                        MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker timePicker, int hours, int minutes) {
                                        long seconds = hours * 60 * 60 + minutes * 60;
                                        wDay.SecondsWorked = seconds;
                                        ((DayView) view).refreshDay(wDay);
                                        App.saveLog();
                                    }
                                }, wDay.getHours(), wDay.getMinutes(), true);
                                timePickerDialog.setTitle("Work duration for " + wDay.WorkDate);
                                timePickerDialog.show();
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
            setWorkingProgress(App.today().SecondsWorked);
            App.saveLog();
            hideNotification();
            btnProfiles.setEnabled(true);
            btnProfiles.setClickable(true);
        }

    }

    private Thread timer;
    private Handler tHandler;
    private boolean cancelTimer = false;
    private void startTimer(){

        tHandler = new Handler();
        cancelTimer = false;
        btnProfiles.setEnabled(false);
        btnProfiles.setClickable(false);

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

        tvDuration.setText(App.today().getDuration());
        float progress = (float) seconds/App.dailyWork;
        if(progress>1) {
            progress = 1;
            wheel.setBarColor(getResources().getColor(R.color.gplus_color_4));
        } else {
            wheel.setBarColor(getResources().getColor(R.color.primaryDark));
        }
        wheel.setSpinSpeed(progress);
        wheel.setProgress(progress);

    }

    private void setWeeklyProgress(){

        tvDuration.setText(App.thisWeek().getDuration());
        float progress = (float) App.thisWeek().SecondsWorked/App.weeklyWork;
        if(progress>1) {
            progress = 1;
            wheel.setBarColor(getResources().getColor(R.color.gplus_color_4));
        } else {
            wheel.setBarColor(getResources().getColor(R.color.primaryDark));
        }
        wheel.setSpinSpeed(progress);
        wheel.setProgress(progress);

    }

    private String formatSeconds(long seconds){
        long hours = seconds / 3600;
        long  minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void showNotification(){

        if(!App.prefs.getBoolean("setting_notif_menu", true)) return;

        Intent dismissIntent = new Intent(this.getPackageName() + ".STOP");
        //dismissIntent.setAction(this.getPackageName() + ".STOP");
        PendingIntent piDismiss = PendingIntent.getBroadcast(this, 0, dismissIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Intent openIntent = new Intent(this, MainActivity.class);
        PendingIntent piOpen = PendingIntent.getActivity(this, 0, openIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Constructs the Builder object.
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setContentTitle("WorkAHolic")
                        .setContentText("Working: " + App.CurrentProfile.Name)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("Working: " + App.CurrentProfile.Name))
                        .addAction (android.R.drawable.ic_delete,
                                "Stop", piDismiss)
                        .setAutoCancel(true)
                        .setColor(getResources().getColor(R.color.primaryDark))
                        .setSmallIcon(R.drawable.ic_stat)
                        .setOngoing(true);

        builder.setContentIntent(piOpen);

        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(PENDING_CODE, builder.build());

        setAlarm();

    }

    private void setAlarm(){

        AlarmManager alarmMgr;
        PendingIntent alarmIntent;

        alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, NotificationActivity.class);
        alarmIntent = PendingIntent.getActivity(this, 0, intent, 0);

        long secondsLeft = App.dailyWork - App.today().SecondsWorked;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, (int) secondsLeft);

        alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);

    }

    private void stopAlarm(){

        AlarmManager alarmMgr;
        PendingIntent alarmIntent;

        alarmMgr = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), NotificationActivity.class);
        alarmIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

        alarmMgr.cancel(alarmIntent);
    }

    private void hideNotification(){

        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(PENDING_CODE);

        stopAlarm();
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

    private void newProfile(){

        final EditText input = new EditText(this);
        input.setTextColor(getResources().getColor(R.color.primary));

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.Dialogs))
                .setTitle("New profile name")
                .setView(input)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ArrayList<WorkingDay> newWorkingDays = new ArrayList<WorkingDay>();
                        newWorkingDays.add(new WorkingDay(App.getDate(Calendar.getInstance().getTime()), 0));
                        App.Profiles.add(new Profile(input.getText().toString(), newWorkingDays));
                        App.saveLog();
                        App.setCurrentProfile(App.Profiles.get(App.Profiles.size()-1));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

        builder.show();

    }

    public void showProfiles(View view){

        PopupMenu popup = new PopupMenu(MainActivity.this, view);
        for(Profile profile : App.Profiles) {
            popup.getMenu().add(profile.Name);
        }

        SubMenu subMenu = popup.getMenu().addSubMenu(0, 0, 0, "Manage Profile");
        //getItem(popup.getMenu().size()-1).getSubMenu();
        subMenu.add("Create");
        subMenu.add("Rename");
        subMenu.add("Remove");
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                String option = item.getTitle().toString();
                if (option.equals("Manage Profile")) {
                    return true;
                } else if(option.equals("Create")){
                    newProfile();
                    return true;
                } else if(option.equals("Rename")){
                    renameProfile();
                    return true;
                } else if(option.equals("Remove")){
                    removeProfile();
                    return true;
                }

                App.setCurrentProfile(App.getProfileWithName(option));
                App.WorkingWeeks.clear();

                wheelFrame.animate().alpha(0).setDuration(150).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        wheelFrame.animate().alpha(1).setDuration(250).start();
                    }
                }).start();
                master.animate().alpha(0).setDuration(150).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        addDays();
                        setWorkingProgress(App.today().SecondsWorked);
                        master.animate().alpha(1).setDuration(250).start();
                    }
                }).start();

                getActionBar().setTitle(getResources().getString(R.string.app_name) + ": " + App.CurrentProfile.Name);

                return true;
            }
        });
        popup.show();

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.menu_manage, menu);
    }

    private ActionMode.Callback manageProfilesCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_manage, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
//                case R.id.:
//                    shareCurrentItem();
//                    mode.finish(); // Action picked, so close the CAB
//                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
//            mActionMode = null;
        }
    };

    public void renameProfile(){

        final EditText input = new EditText(this);
        input.setTextColor(getResources().getColor(R.color.primary));

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.Dialogs))
                .setTitle("Rename profile " + App.CurrentProfile.Name)
                .setView(input)
                .setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        App.CurrentProfile.Name = input.getText().toString();
                        App.saveLog();
                        getActionBar().setTitle(getResources().getString(R.string.app_name) + ": " + App.CurrentProfile.Name);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

        builder.show();

    }

    public void removeProfile(){

        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.Dialogs))
                .setTitle("Remove profile " + App.CurrentProfile.Name)
                .setMessage("This cannot be undone")
                .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        App.Profiles.remove(App.CurrentProfile);
                        App.setCurrentProfile(App.Profiles.get(0));
                        App.saveLog();
                        addDays();
                        setWorkingProgress(App.today().SecondsWorked);
                        getActionBar().setTitle(getResources().getString(R.string.app_name) + ": " + App.CurrentProfile.Name);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
