package com.genie.simplerss;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

import com.genie.simplerss.classes.Constants;

public class LaunchActivity extends Activity {
    public static final String TAG = "LaunchActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
    }

    @Override
    protected void onResume() {
        super.onResume();

        new CountDownTimer(Constants.LAUNCH_TIMEOUT, Constants.LAUNCH_TIMEOUT) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                Intent intent = new Intent();
                intent.setClass(LaunchActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }.start();
    }

}
