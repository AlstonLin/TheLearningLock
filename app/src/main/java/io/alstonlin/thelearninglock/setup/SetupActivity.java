package io.alstonlin.thelearninglock.setup;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import io.alstonlin.thelearninglock.FragmentChangable;
import io.alstonlin.thelearninglock.R;
import io.alstonlin.thelearninglock.setup.BackgroundPickerFragment;

public class SetupActivity extends FragmentActivity implements FragmentChangable {
    public final static int REQUEST_CODE = 5463;
    private Bundle savedInstanceState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkDrawOverlayPermission();
        this.savedInstanceState = savedInstanceState;
    }

    public void changeFragment(Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }


    private void startApp(){
        setContentView(R.layout.activity_setup);
        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            // Permission Check first
            BackgroundPickerFragment firstFragment = new BackgroundPickerFragment();
            firstFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, firstFragment).commit();
        }
    }

    /*
        Permission Checks
    */

    private void checkDrawOverlayPermission() {
        if (!Settings.canDrawOverlays(this) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE);
        } else {
            startApp();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requestCode == REQUEST_CODE && Settings.canDrawOverlays(this)) {
                startApp();
            }
        }
    }
}
