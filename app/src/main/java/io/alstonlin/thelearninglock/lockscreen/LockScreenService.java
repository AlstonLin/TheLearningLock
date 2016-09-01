package io.alstonlin.thelearninglock.lockscreen;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.content.ServiceConnection;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationManagerCompat;

import android.telephony.TelephonyManager;

import java.util.Set;

import io.alstonlin.thelearninglock.shared.Const;
import io.alstonlin.thelearninglock.setup.SetupActivity;

/**
 * The Service that runs in the background to "lock" and "unlock" the screen by attaching a View
 * over the WindowManager whenever the power button is pressed.
 */
public class LockScreenService extends Service implements NotificationsUpdateListener{
    // Constants
    public static final int UNLOCK_FLAG = 69;
    public static final int OPEN_SETUP_ACTIVITY = 70;

    // Fields
    private Handler uiHandler; // Allows sending messages to the "UI" thread (Service's main Thread)
    private LockScreen lockScreen;
    private BroadcastReceiver receiver;
    private LockScreenNotificationService notificationService;
    /**
     * The connection to the Notification service
     */
    private ServiceConnection notificationConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            LockScreenNotificationService.ServiceBinder notifBinder = (LockScreenNotificationService.ServiceBinder) iBinder;
            notificationService = notifBinder.getService();
            notifBinder.setNotificationsUpdateListener(LockScreenService.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    /**
     * Called when the Service is started. Either runs the initial setup, or simply locks the screen.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        // Starts the notification service
        Intent intent = new Intent(this, LockScreenNotificationService.class);
        bindService(intent, notificationConnection, Context.BIND_AUTO_CREATE);
        startService(intent);
        // Registers the receiver to detect when screen is turned off and on
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.setPriority(999);
        receiver = new LockScreenReceiver();
        registerReceiver(receiver, filter);
        // Creates the Lock Screen
        lockScreen = new LockScreen(this);
        // Creates the handler
        uiHandler = new Handler();
    }

    /**
     * Called every time the screen is locked, or during setup.
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
                notifyNotificationsUpdated();
                break;
            default:
                // Checks if set up
                boolean setup = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Const.SETUP_FLAG, false);
                boolean enabled = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Const.ENABLED, false);
                if (!setup || !enabled) return START_STICKY;
                // Checks if currently in a phone call
                TelephonyManager ts = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (ts.getCallState() != TelephonyManager.CALL_STATE_OFFHOOK) {
                    lockScreen.hideUnlockScreen(); // If they had the Popup open before
                    lockScreen.lock();
                    notifyNotificationsUpdated();
                }
        }
        return START_STICKY;
    }

    /**
     * Re-fetches all the notifications and updates the list.
     */
    public void notifyNotificationsUpdated(){
        uiHandler.post(new Runnable() {
            @Override
            public void run() {
                if (notificationService != null){
                    lockScreen.updateNotifications(notificationService.getNotifications());
                }
            }
        });
    }


    /*
        Permission Checks
    */

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

    /**
     * Unregisters the resources used
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        lockScreen.unlock();
        unregisterReceiver(receiver);
        unbindService(notificationConnection);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
