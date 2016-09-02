package com.appvirality.appviralitytest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.appvirality.AppVirality;

/**
 * Created by AppVirality on 3/10/2016.
 */
public class SplashActivity extends Activity {

    private static final int SPLASH_TIMEOUT = 3000;
    AppVirality appVirality;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        appVirality = AppVirality.getInstance(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, InitActivity.class));
                finish();
            }
        }, SPLASH_TIMEOUT);
    }
}
