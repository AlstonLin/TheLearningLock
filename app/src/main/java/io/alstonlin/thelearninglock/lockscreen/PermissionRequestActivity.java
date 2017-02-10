package io.alstonlin.thelearninglock.lockscreen;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.os.Bundle;

import io.alstonlin.thelearninglock.lockscreen.LockScreenService;

/**
 * An Activity that's called from LockScreenService to Request a strict permission, and then
 * delegate back to the Service. The only reason this exists is because startActivityForResult or
 * onResume apparently not a thing for services. Yay, Android Dev!
 */
public class PermissionRequestActivity extends Activity {
    public static final int OVERLAY_FLAG = 23;
    public static final int NOTIFICATIONS_FLAG = 29;

    private static final int OVERLAY_REQUEST_CODE = 5463;
    private static final int NOTIFICATIONS_REQUEST_CODE = 5464;

    private boolean requested = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int flag = getIntent().getFlags() - Intent.FLAG_ACTIVITY_NEW_TASK;
        switch (flag){
            case OVERLAY_FLAG:
                requestOverlayPermission();
                break;
            case NOTIFICATIONS_FLAG:
                requestNotificationsPermission();
                break;
            default:
                throw new IllegalStateException("Can only start this activity with the " +
                        "OVERLAY_FLAG or NOTIFICATIONS_FLAG flag!");
        }
    }

    private void requestOverlayPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_REQUEST_CODE);
        } else{
            throw new IllegalStateException("Why is this Activity being started on an API level " + Build.VERSION.SDK_INT);
        }
    }

    private void requestNotificationsPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivityForResult(intent, NOTIFICATIONS_REQUEST_CODE);
        } else{
            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if (requested) {
            Intent intent = new Intent(this, LockScreenService.class);
            intent.addFlags(LockScreenService.OPEN_SETUP_ACTIVITY);
            startService(intent);
            finish();
        } else{
            requested = true;
        }
    }
}
