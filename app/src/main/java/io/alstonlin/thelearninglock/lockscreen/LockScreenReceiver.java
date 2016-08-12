package io.alstonlin.thelearninglock.lockscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * The Receiver that starts shows the Lockscreen when the phone is locked
 */
public class LockScreenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("TAG", ">>>>>>>>>>>>>>>>>>>>>>>>>>.RECEIVED ACTION: " + intent.getAction() + "<<<<<<<<<<<<<<<<<<<<");
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF) ||
                intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.e("LockScreenReceiver", ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>..RECEIVED INTENT<<<<<<<<<<<<<<<<<<<<<<<<<<");
            startLockscreen(context);
        }
    }

    private void startLockscreen(Context context) {
        // It's safe to call this multiple times; It will not start if already running
        context.startService(new Intent(context, LockScreenService.class));
    }

}