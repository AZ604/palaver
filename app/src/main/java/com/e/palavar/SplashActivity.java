package com.e.palavar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


public class SplashActivity extends AppCompatActivity {

    private Handler mHandler;
    private Runnable mRunnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        mRunnable = new Runnable() {
            @Override
            public void run() {
                finish();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        };
        mHandler = new Handler();
        mHandler.postDelayed(mRunnable, getApplicationContext().getResources().getInteger(R.integer.splash_time_in_millis));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null && mRunnable != null)
            mHandler.removeCallbacks(mRunnable);
    }
}
