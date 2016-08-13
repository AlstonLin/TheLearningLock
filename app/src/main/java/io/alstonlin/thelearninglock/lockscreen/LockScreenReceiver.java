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
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF) ||
                intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            startLockscreen(context, false);
        } else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
            startLockscreen(context, true);
        }
    }

    private void startLockscreen(Context context, boolean screenOn) {
        // It's safe to call this multiple times; It will not start if already running
        Intent intent = new Intent(context, LockScreenService.class);
        if (screenOn) {
            intent.addFlags(LockScreenService.UNLOCK_FLAG);
        }
        context.startService(intent);
    }

}