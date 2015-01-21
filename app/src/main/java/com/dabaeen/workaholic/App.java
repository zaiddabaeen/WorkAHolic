package com.dabaeen.workaholic;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by redtroops on 1/20/15.
 */
public class App extends Application {
    public static final String TAG = "WorkAHolic";

    public static final String WORKINGLOG = "workingLog";
    public static final String INITIAL_COUNT = "initialcount";
    public static final String IS_COUNTING = "iscounting";
    public static ArrayList<WorkingDay> WorkingDays;
    public static SharedPreferences prefs;
    public static long dailyWork = 60*60*8;
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        context = this;

        if(isFirstRun()){
            WorkingDays = new ArrayList<WorkingDay>();
            WorkingDays.add(new WorkingDay(getDate(Calendar.getInstance().getTime()), 0));
            saveLog();
        } else {
            try {
                loadLog();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static boolean saveLog(){

        Log.d(TAG, "Saving...");
        ObjectOutputStream outputStream;
        try {
            outputStream = new ObjectOutputStream(context.openFileOutput(WORKINGLOG, Context.MODE_PRIVATE));//new FileOutputStream(workingLog));
            outputStream.writeObject(WorkingDays);
            outputStream.close();
            Log.i(TAG, "Saved successfully");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public ArrayList<WorkingDay> loadLog() throws Exception{

        Log.d(TAG, "Loading...");
        ObjectInputStream inputStream;
        try {
        inputStream = new ObjectInputStream(openFileInput(WORKINGLOG));//new FileInputStream(workingLog));
        WorkingDays = (ArrayList<WorkingDay>)inputStream.readObject();
        inputStream.close();
        printWorkingDays();
        Log.i(TAG, "Loaded successfully");
        return WorkingDays;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        throw new Exception();
    }

    public boolean isFirstRun(){
        File file = context.getFileStreamPath(WORKINGLOG);
        if(file == null || !file.exists()) {
            Log.i(TAG, "First run");
            return true;
        } else {
            return false;
        }
    }

    public static WorkingDay today(){

        WorkingDay today = WorkingDays.get(WorkingDays.size()-1);

        if(today.isToday()){
            return today;
        } else {
            WorkingDays.add(new WorkingDay(getDate(Calendar.getInstance().getTime()), 0));
            return WorkingDays.get(WorkingDays.size()-1);
        }

    }

    public static String getDate(Date date){
        SimpleDateFormat df = new SimpleDateFormat("dd MMM yy");
        return df.format(date.getTime());
    }

    public void printWorkingDays(){

        String output = "";
        for(WorkingDay day: WorkingDays){
            output += day.WorkDate + "|" + day.SecondsWorked + ",";
        }

        Log.d(TAG, output);
    }

}
