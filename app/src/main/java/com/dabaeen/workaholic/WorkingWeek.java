package com.dabaeen.workaholic;

/**
 * Created by redtroops on 1/21/15.
 */
public class WorkingWeek {

    public int Week = -1;
    public long SecondsWorked = 0;

    public WorkingWeek(){};

    public WorkingWeek(int week, long secondsWorked){
        this.Week = week;
        this.SecondsWorked = secondsWorked;
    }

    public String getDuration(){

        long hours = SecondsWorked / 3600;
        long  minutes = (SecondsWorked % 3600) / 60;

        return String.format("%02d:%02d", hours, minutes);

    }
}
