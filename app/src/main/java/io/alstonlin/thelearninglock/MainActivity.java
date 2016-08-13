package io.alstonlin.thelearninglock;

import android.content.Intent;
import android.os.Bundle;

import android.support.v7.app.AppCompatActivity;

import io.alstonlin.thelearninglock.lockscreen.LockScreenService;

/**
 * Entry point to the app. The only purpose of this Activity is to start the LockScreenService, with
 * the OPEN_SETUP_ACTIVITY flag, which will run a permission check followed by starting the SetupActivity
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, LockScreenService.class);
        intent.addFlags(LockScreenService.OPEN_SETUP_ACTIVITY);
        startService(intent);
        finish();
    }

}
