package com.dabaeen.workaholic;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by redtroops on 1/24/15.
 */
public class Profile implements Serializable{

    static final long serialVersionUID = 566940470597768071L;
    public ArrayList<WorkingDay> WorkingDays;
    public long DailyWork = 8*60*60;
    public long WeeklyWork = 24*60*60;
    public String Name;

    public Profile(String name, ArrayList<WorkingDay> workingDays){

        Name = name;
        WorkingDays = workingDays;

    }

}
