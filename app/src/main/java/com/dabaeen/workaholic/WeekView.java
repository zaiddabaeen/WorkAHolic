package com.dabaeen.workaholic;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gc.materialdesign.views.ProgressBarDeterminate;

/**
 * Created by redtroops on 1/21/15.
 */
public class WeekView  extends LinearLayout {

    WorkingWeek Week;
    TextView tvWeek, tvDuration;
    ProgressBarDeterminate slider;
    LinearLayout daysContainer;

    public WeekView(Context context, long secondsWorked, int weekNumber){
        super(context);
        createLayout(secondsWorked, weekNumber);
    }

    private void createLayout(long secondsWorked, int weekNumber){

        inflate(getContext(), R.layout.view_week, this);
        tvWeek = (TextView) findViewById(R.id.tvWeek);
        tvDuration = (TextView) findViewById(R.id.tvDuration);
        slider = (ProgressBarDeterminate) findViewById(R.id.slider);
        daysContainer = (LinearLayout) findViewById(R.id.daysContainer);

//        setTranslationZ(20f);

        Week = new WorkingWeek(weekNumber, secondsWorked);

        tvWeek.setText("Week " + weekNumber);
        tvDuration.setText(Week.getDuration());
        slider.setProgress((int) (Week.getRatio() * 100));
        if(Week.getRatio() > 1) slider.setBackgroundColor(getResources().getColor(R.color.gplus_color_4));

        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return false;
            }
        });
    }

    public void addSeconds(long secondsWorked){

        Week.SecondsWorked += secondsWorked;
        tvDuration.setText(Week.getDuration());
        slider.setProgress((int) (Week.SecondsWorked / (float) App.weeklyWork * 100));

    }

    public void addDay(DayView day){

        daysContainer.addView(day, 0);

    }
}
