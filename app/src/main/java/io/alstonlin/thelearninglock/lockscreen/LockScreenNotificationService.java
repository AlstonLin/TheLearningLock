package io.alstonlin.thelearninglock.lockscreen;

import android.app.Notification;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

/**
 * Service that the lock screen gets notifications from.
 *
 * NOTE (Bug in the Android SDK) - This service will ONLY work if the SERVICE_INTERFACE binds to it.
 * The problem is this only happens when the Device either reboots, or when the permissions is
 * granted to it, and not if the Service is restarted from ADB. This means that the app must be
 * uninstalled every single time so that the NotificationsManager can re-bind to it and everything
 * works.
 */
public class LockScreenNotificationService extends NotificationListenerService {
    private final ServiceBinder binder = new ServiceBinder();
    private boolean ready = false;

    public class ServiceBinder extends Binder {
        private NotificationsUpdateListener listener;

        public LockScreenNotificationService getService() {
            return LockScreenNotificationService.this;
        }
        public void setNotificationsUpdateListener(NotificationsUpdateListener listener){
            this.listener = listener;
        }
    }

    @Override
    public IBinder onBind(Intent intent){
        String action = intent.getAction();
        if (SERVICE_INTERFACE.equals(action)) {
            return super.onBind(intent);
        } else {
            return binder;
        }
    }

    public void onListenerConnected(){
        ready = true;
    }

    public Notification[] getNotifications(){
        if (!ready) return new Notification[0];
        StatusBarNotification[] statusNotifications = getActiveNotifications();
        Notification[] notifications = new Notification[statusNotifications.length];
        for (int i = 0; i < statusNotifications.length; i++) {
            notifications[i] = statusNotifications[i].getNotification();
        }
        return notifications;
    }

    /*
        Stuff below is not actually used
    */

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        binder.listener.notifyNotificationsUpdated();
    }
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        binder.listener.notifyNotificationsUpdated();
    }
}
