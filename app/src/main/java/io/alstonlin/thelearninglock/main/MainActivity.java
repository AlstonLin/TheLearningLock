package io.alstonlin.thelearninglock.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import io.alstonlin.thelearninglock.setup.BackgroundPickerFragment;
import io.alstonlin.thelearninglock.setup.PINSetupFragment;
import io.alstonlin.thelearninglock.setup.PatternSetupFragment;
import io.alstonlin.thelearninglock.shared.Const;
import io.alstonlin.thelearninglock.R;
import io.alstonlin.thelearninglock.shared.OnFragmentFinishedListener;
import io.alstonlin.thelearninglock.shared.SharedUtils;
import io.alstonlin.thelearninglock.lockscreen.LockScreenService;
import io.alstonlin.thelearninglock.pin.OnPINSelectListener;
import io.alstonlin.thelearninglock.pin.PINUtils;
import io.fabric.sdk.android.Fabric;

/**
 * Warning: This flow is VERY complicated due to Android's new permission system.
 *
 * Entry point to the app (Activity that is launched when the app is started).
 * The first thing this will do is to check the non-super permissions, and request them.
 * It will then show a Settings Fragment showing options to configure the app if already set up.
 * If the app is not set up, then it will show a welcome screen that will then prompt the user
 * to set up.
 * To set up, the app will start the LockScreenService with the OPEN_SETUP_ACTIVITY flag, which
 * will run the super permissions checks followed by starting the SetupActivity.
 */
public class MainActivity extends FragmentActivity implements OnFragmentFinishedListener {
    private static final int REQUEST_PERMISSIONS_CODE = 3;
    private static String[] PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private boolean setup;
    private PopupWindow authCheckPopup;
    private byte[] PINHash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        // Crashlytics
        Fabric.with(this, new Crashlytics());
        // Sets up the salt for security
        SharedUtils.setupSalt(this);
        // Basic permission checks
        ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSIONS_CODE);
        // Sets the Fragment to show based on if Lockscreen has already been set up
        setup = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Const.SETUP_FLAG, false);
        Fragment fragment;
        if (setup){
            fragment = SettingsFragment.newInstance();
            setTitle("Settings");
        } else {
            fragment = WelcomeFragment.newInstance();
        }
        getSupportFragmentManager().beginTransaction()
                .add(R.id.activity_main_fragment_container, fragment).commit();
    }

    @Override
    public void onResume(){
        super.onResume();
        PINHash = SharedUtils.loadHashFromFiledFile(this, Const.PASSCODE_FILENAME, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_CODE){
            if (grantResults.length != PERMISSIONS.length){
                Snackbar.make(findViewById(R.id.activity_main_fragment_container), "All the permissions must be accepted before the lock screen can be started", Snackbar.LENGTH_SHORT).show();
                finish();
                return;
            }
            for (int i = 0; i < grantResults.length; i++){
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                    Snackbar.make(findViewById(R.id.activity_main_fragment_container), "All the permissions must be accepted before the lock screen can be started", Snackbar.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            }
        }
    }

    /*
     * Fragment Flow
     */

    private void changeFragment(Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.activity_main_fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onFragmentFinished() {
        // Always return back to the setup screen when finished
        changeFragment(SettingsFragment.newInstance());
    }

    /*
     * onClick methods from SettingsFragment
     */

    public void clickChangeBackground(View view){
        changeFragment(BackgroundPickerFragment.newInstance());
    }

    public void clickChangePattern(View view){
        runAuthenticated(new Runnable() {
            @Override
            public void run() {
                changeFragment(PatternSetupFragment.newInstance());
            }
        });
    }

    public void clickChangePIN(View view){
        runAuthenticated(new Runnable() {
            @Override
            public void run() {
                changeFragment(PINSetupFragment.newInstance());
            }
        });
    }

    /**
     * Goes through the setup process.
     * Can be called from both the SettingsFragment and WelcomeFragment.
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

    /**
     * Launches the SetupActivity through the LockScreenService (so it can permission check first).
     */
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
        // Sets up the popup window
        // TODO: This would probably look better as a Dialog maybe?
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View pinView = inflater.inflate(R.layout.layout_pin, null);
        SharedUtils.setupBackground(this, pinView);
        PINUtils.setupPINView(pinView, new OnPINSelectListener() {
            @Override
            public void onPINSelected(String PIN) {
                if (SharedUtils.compareObjectToHash(getApplicationContext(), PIN, PINHash)){
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
        // Adds a small margin so that it looks like an actual popup
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        final int MARGIN = 125;
        authCheckPopup.setWidth(width - MARGIN);
        authCheckPopup.setHeight(height - 2 * MARGIN);
        authCheckPopup.showAtLocation(findViewById(R.id.activity_main_fragment_container), Gravity.CENTER, 0, 0);
    }
}
