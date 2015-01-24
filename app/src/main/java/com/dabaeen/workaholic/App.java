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
    public static final String PROFILELOG = "profileLog";
    public static final String INITIAL_COUNT = "initialcount";
    public static final String IS_COUNTING = "iscounting";
    public static final String CURRENT_PROFILE = "current_profile";
    public static ArrayList<WorkingDay> WorkingDays;
    public static ArrayList<WorkingWeek> WorkingWeeks;
    public static ArrayList<Profile> Profiles;
    public static Profile CurrentProfile;
    public static SharedPreferences prefs;
    public static long dailyWork = 60*60*8;
    public static long weeklyWork = dailyWork*3;
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        WorkingWeeks = new ArrayList<WorkingWeek>();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        context = this;

        getSettings();

        if(isFirstRun()){
            Profiles = new ArrayList<Profile>();
            WorkingDays = new ArrayList<WorkingDay>();
            WorkingDays.add(new WorkingDay(getDate(Calendar.getInstance().getTime()), 0));
            Profiles.add(new Profile("None", WorkingDays));
            saveLog();
        } else {
            try {
                loadLog();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        loadCurrentProfile();
        refreshProfile();
    }

    public void getSettings(){

        dailyWork = Integer.valueOf(prefs.getString("setting_day_hour", "8")) * 60 * 60;
        weeklyWork = Integer.valueOf(prefs.getString("setting_week_hour", "24")) * 60 * 60;

    }

    public static boolean saveLog(){

        Log.d(TAG, "Saving...");
        ObjectOutputStream outputStream;
        try {
            outputStream = new ObjectOutputStream(context.openFileOutput(PROFILELOG, Context.MODE_PRIVATE));//new FileOutputStream(workingLog));
            outputStream.writeObject(Profiles);
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
        inputStream = new ObjectInputStream(openFileInput(PROFILELOG));//new FileInputStream(workingLog));
        Profiles = (ArrayList<Profile>)inputStream.readObject();
        inputStream.close();
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

    public static WorkingWeek thisWeek(){

        return WorkingWeeks.get(WorkingWeeks.size()-1);

    }

    public static String getDate(Date date){
        SimpleDateFormat df = new SimpleDateFormat("dd MMM yy");
        return df.format(date.getTime());
    }

    public static void printWorkingDays(){

        String output = "";
        for(WorkingDay day: WorkingDays){
            output += day.WorkDate + "|" + day.SecondsWorked + ",";
        }

        Log.d(TAG, output);
    }

    public Profile loadCurrentProfile(){
        String profileName = prefs.getString(CURRENT_PROFILE, null);

        CurrentProfile = getProfileWithName(profileName);
        if(CurrentProfile==null) CurrentProfile = Profiles.get(0);
        return CurrentProfile;

    }

    public static void setCurrentProfile(Profile currentProfile){

        CurrentProfile = currentProfile;
        prefs.edit().putString(CURRENT_PROFILE, currentProfile.Name).commit();
        refreshProfile();
    }

    public static Profile getProfileWithName(String name){

        for(Profile profile : Profiles){
            if(profile.Name.equals(name)) {
                return profile;
            }
        }

        return null;

    }

    public static void refreshProfile(){

        WorkingDays = CurrentProfile.WorkingDays;
        printWorkingDays();

    }


}
