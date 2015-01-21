package com.dabaeen.workaholic;

import android.util.Log;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by redtroops on 1/20/15.
 */
public class WorkingDay implements Serializable{

    public String WorkDate = null;
    public long SecondsWorked = 0;

    public WorkingDay(){};

    public WorkingDay(String workDate, int secondsWorked){
        this.WorkDate = workDate;
        this.SecondsWorked = secondsWorked;
    }

    public boolean isToday(){

        return WorkDate.equals(App.getDate(Calendar.getInstance().getTime()));

    }

    public String getDuration(){

        long hours = SecondsWorked / 3600;
        long  minutes = (SecondsWorked % 3600) / 60;

        return String.format("%02d:%02d", hours, minutes);

    }

    public float getRatio(){

        return (SecondsWorked/(float) App.dailyWork);

    }

    public int getWeek(){

        SimpleDateFormat df = new SimpleDateFormat("dd MMM yy");
        try {
            Date date = df.parse(WorkDate);

            SimpleDateFormat wf = new SimpleDateFormat("ww");
            String week = wf.format(date);
            return Integer.valueOf(week);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return -1;
    }
}
