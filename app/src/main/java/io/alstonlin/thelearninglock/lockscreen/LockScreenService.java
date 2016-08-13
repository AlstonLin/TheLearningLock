package io.alstonlin.thelearninglock.lockscreen;

import android.Manifest;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;

import java.util.Set;

import io.alstonlin.thelearninglock.PermissionRequestActivity;
import io.alstonlin.thelearninglock.setup.SetupActivity;

/**
 * The Service that runs in the background to "lock" and "unlock" the screen by attaching a View
 * over the WindowManager whenever the power button is pressed.
 */
public class LockScreenService extends NotificationListenerService {
    // Constants
    public static final int UNLOCK_FLAG = 69;
    public static final int OPEN_SETUP_ACTIVITY = 70;

    // Fields
    private LockScreen lockScreen;
    private BroadcastReceiver receiver;
    private boolean notificationsOn = false;

    /**
     * Called when the Service is started. Either runs the initial setup, or simply locks the screen.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        // Registers the receiver to detect when screen is turned off and on
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
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
        switch (intent.getFlags()){
            case OPEN_SETUP_ACTIVITY: // Setup
                if (checkDrawOverlayPermission() && checkNotificationsPermission()){
                    Intent setupIntent = new Intent(this, SetupActivity.class);
                    setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(setupIntent);
                }
                break;
            case UNLOCK_FLAG: // Update notifications when unlocking
                if (notificationsOn){
                    StatusBarNotification[] statusNotifications = getActiveNotifications();
                    Notification[] notifications = new Notification[statusNotifications.length];
                    for (int i = 0; i < statusNotifications.length; i++){
                        notifications[i] = statusNotifications[i].getNotification();
                    }
                    lockScreen.updateNotifications(notifications);
                }
                break;
            default:
                // Checks if currently in a phone call
                TelephonyManager ts = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (ts.getCallState() != TelephonyManager.CALL_STATE_OFFHOOK) {
                    lockScreen.hideUnlockScreen(); // If they had the Popup open before
                    lockScreen.lock();
                }
        }
        return START_STICKY;
    }

    private boolean checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(this, PermissionRequestActivity.class);
            intent.addFlags(PermissionRequestActivity.OVERLAY_FLAG);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return false;
        }
        return true;
    }

    private boolean checkNotificationsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            Set<String> listeners = NotificationManagerCompat.getEnabledListenerPackages(this);
            boolean enabled = false;
            for (String pack : listeners){
                if (pack.equals(getPackageName())){
                    enabled = true;
                }
            }
            if (enabled){
                return true;
            }
            Intent intent = new Intent(this, PermissionRequestActivity.class);
            intent.addFlags(PermissionRequestActivity.NOTIFICATIONS_FLAG);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return false;
        }
        return true;
    }

    @Override
    public void onListenerConnected(){
        notificationsOn = true;
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


    /*
        Stuff below is not actually used
    */

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
    }
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
