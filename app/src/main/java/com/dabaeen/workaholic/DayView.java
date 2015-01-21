package com.dabaeen.workaholic;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gc.materialdesign.views.ProgressBarDeterminate;
import com.gc.materialdesign.views.Slider;

/**
 * Created by redtroops on 1/20/15.
 */
public class DayView extends LinearLayout {

    TextView tvDate, tvDuration;
    ProgressBarDeterminate slider;

    public DayView(Context context, AttributeSet attrs, int defStyle, WorkingDay workingDay){
        super(context, attrs, defStyle);
        createLayout(workingDay);
    }

    public DayView(Context context, WorkingDay workingDay){
        super(context);
        createLayout(workingDay);
    }

    private void createLayout(WorkingDay workingDay){

        inflate(getContext(), R.layout.view_day, this);
        tvDate = (TextView) findViewById(R.id.tvDate);
        tvDuration = (TextView) findViewById(R.id.tvDuration);
        slider = (ProgressBarDeterminate) findViewById(R.id.slider);

        tvDate.setText(workingDay.WorkDate);
        tvDuration.setText(workingDay.getDuration());
        slider.setProgress((int) (workingDay.getRatio() * 100));

        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return false;
            }
        });
    }

}
