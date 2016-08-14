package io.alstonlin.thelearninglock.lockscreen;

import android.app.Notification;

/**
 * Interface that notifies when a Notification has been selected.
 */
public interface NotificationSelectListener {
    void onNotificationSelected(Notification notification);
}
