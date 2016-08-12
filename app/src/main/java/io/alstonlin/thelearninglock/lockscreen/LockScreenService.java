package io.alstonlin.thelearninglock.lockscreen;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.telephony.TelephonyManager;

import io.alstonlin.thelearninglock.setup.SetupActivity;

/**
 * The Service that runs in the background to "lock" and "unlock" the screen by attaching a View
 * over the WindowManager whenever the power button is pressed.
 */
public class LockScreenService extends Service {
    // Constants

    // Fields
    private LockScreen lockScreen;
    private BroadcastReceiver receiver;

    /**
     * Called when the Service is started. Either runs the initial setup, or simply locks the screen.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        // Registers the receiver to detect when screen is turned off and on
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.setPriority(999);
        receiver = new LockScreenReceiver();
        registerReceiver(receiver, filter);
        // Creates the Lock Screen
        lockScreen = new LockScreen(this);
    }

    /**
     * Called everytime the screen is locked, or during setup.
     * @param intent The intent this was called by
     * @param flags Flags passed
     * @param startId Service ID
     * @return The result
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Intent nextIntent;
        // FIXME: Delete comment
        if (/*!ML.isSetup() && !ML.setup(this)*/false) { // First time
            nextIntent = new Intent(this, SetupActivity.class);
            nextIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(nextIntent);
        } else {
            // Checks if currently in a phone call
            TelephonyManager ts = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            if (ts.getCallState() != TelephonyManager.CALL_STATE_OFFHOOK) {
                lockScreen.hideUnlockScreen(); // If they had the Popup open before
                lockScreen.lock();
            }
        }
        return START_STICKY;
    }

    /**
     * Unregisters receiver.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        lockScreen.unlock();
        unregisterReceiver(receiver);
    }

    /**
     * Not Used.
     * @param intent The intent this was started from
     * @return Not Used.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
