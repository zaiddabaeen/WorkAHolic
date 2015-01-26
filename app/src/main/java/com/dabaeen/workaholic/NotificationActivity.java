package com.dabaeen.workaholic;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;


/**
 * Created by redtroops on 1/26/15.
 */
public class NotificationActivity extends Activity {

    ProgressWheel wheel;
    TextView tvDuration, tvToday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        tvDuration = (TextView) findViewById(R.id.tvDuration);
        tvToday = (TextView) findViewById(R.id.tvToday);
        wheel = (ProgressWheel) findViewById(R.id.progress_wheel);

        wheel.setRimColor(getResources().getColor(R.color.accent));
        wheel.setBarWidth(20);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if(hasFocus){
            setWorkingProgress(App.today().SecondsWorked);
        }
        super.onWindowFocusChanged(hasFocus);
    }

    private void setWorkingProgress(long seconds){

        float progress;

        if(App.prefs.getBoolean(App.IS_COUNTING, false)){
            tvDuration.setText("Reached Day Goal!");
            progress = 1;
        } else {
            tvDuration.setText(App.today().getDuration());
            progress = (float) seconds/App.dailyWork;
        }

        if(progress>=1) {
            progress = 1;
            wheel.setBarColor(getResources().getColor(R.color.gplus_color_4));
        } else {
            wheel.setBarColor(getResources().getColor(R.color.primaryDark));
        }
        wheel.setSpinSpeed(progress);
        wheel.setProgress(progress);

       Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        },4000);
    }

    public void CloseThis(View view){
        finish();
    }

}
