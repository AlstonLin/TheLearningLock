package io.alstonlin.thelearninglock;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import io.alstonlin.thelearninglock.lockscreen.LockScreenService;

/**
 * Entry point to the app. The only purpose of this Activity is to check the non-super permissions,
 * and start the LockScreenService with the OPEN_SETUP_ACTIVITY flag, which will run the super
 * permissions checks followed by starting the SetupActivity.
 */
public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS_CODE = 3;
    private static String[] PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Basic permission checks
        ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSIONS_CODE);
    }

    private void startService(){
        Intent intent = new Intent(this, LockScreenService.class);
        intent.addFlags(LockScreenService.OPEN_SETUP_ACTIVITY);
        startService(intent);
        finish();
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
            startService();
        }
    }
}
