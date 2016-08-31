package io.alstonlin.thelearninglock;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import io.alstonlin.thelearninglock.lockscreen.LockScreenService;
import io.alstonlin.thelearninglock.pin.OnPINSelectListener;
import io.alstonlin.thelearninglock.pin.PINUtils;

/**
 * Entry point to the app.
 * The first thing this will do is to check the non-super permissions, and request them.
 * It will then show a Settings page showing options to configure the app if already set up.
 * If the app is not set up, then it will show a welcome screen that will then prompt the user
 * to set up.
 * To set up, the app will start the LockScreenService with the OPEN_SETUP_ACTIVITY flag, which
 * will run the super permissions checks followed by starting the SetupActivity.
 */
public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSIONS_CODE = 3;
    private static String[] PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private boolean setup;
    private boolean enabled;
    private PopupWindow authCheckPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Basic permission checks
        ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSIONS_CODE);
        // Sets the content view depending on if this is already set up
        setup = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Const.SETUP_FLAG, false);
        enabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Const.ENABLED, false);
        if (setup){
            setContentView(R.layout.settings_page);
            // Sets up the enabled switch
            Switch enabledSwitch = (Switch) findViewById(R.id.settings_page_enable);
            enabledSwitch.setChecked(enabled);
            enabledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    enabled = b;
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                    editor.putBoolean(Const.ENABLED, enabled);
                    editor.commit();
                }
            });
        } else {
            setContentView(R.layout.welcome_page);
        }
    }

    /**
     * Goes through the setup process. Can either been called from the Welcome or Settings pages.
     * @param v Unused
     */
    public void clickSetup(View v){
        runAuthenticated(new Runnable() {
            @Override
            public void run() {
                setupLockScreen();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_CODE){
            if (grantResults.length != PERMISSIONS.length){
                Toast.makeText(this, "All the permissions must be accepted before the lock screen can be started", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            for (int i = 0; i < grantResults.length; i++){
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "All the permissions must be accepted before the lock screen can be started", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            }
        }
    }

    private void setupLockScreen(){
        Intent intent = new Intent(this, LockScreenService.class);
        intent.addFlags(LockScreenService.OPEN_SETUP_ACTIVITY);
        startService(intent);
        finish();
    }


    /**
     * Runs the given task after checking for PIN, if already set up
     * @param task Task to run once authenticated
     */
    private void runAuthenticated(final Runnable task){
        if (!setup){
            task.run();
            return;
        }
        // Loads the real PIN
        final String realPIN = (String) Utils.loadObjectFromFile(this, Const.PASSCODE_FILENAME);
        // Sets up the popup window
        // TODO: This would probably look better as a Dialog maybe?
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View pinView = inflater.inflate(R.layout.layout_pin, null);
        PINUtils.setupPINView(this, pinView, new OnPINSelectListener() {
            @Override
            public void onPINSelected(String PIN) {
                if (PIN.equals(realPIN)){
                    task.run();
                    authCheckPopup.dismiss();
                } else {
                    PINUtils.setPINTitle(pinView, "Wrong PIN!");
                }
            }
        }, "Enter your PIN");
        authCheckPopup = new PopupWindow(
                pinView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true
        );
        // Adds a small margin so that it looks like an actual popup\
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        final int MARGIN = 125;
        authCheckPopup.setWidth(width - MARGIN);
        authCheckPopup.setHeight(height - 2 * MARGIN);
        authCheckPopup.showAtLocation(findViewById(R.id.activity_main_root), Gravity.CENTER, 0, 0);
    }
}
