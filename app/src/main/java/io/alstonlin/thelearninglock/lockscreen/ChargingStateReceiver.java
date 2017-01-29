package io.alstonlin.thelearninglock.lockscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 *  Triggered by a change in the charging state. Used to update the status bar.
 */
public class ChargingStateReceiver extends BroadcastReceiver {
    private Runnable onStateChanged;

    public ChargingStateReceiver(Runnable onStateChanged){
        this.onStateChanged = onStateChanged;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        onStateChanged.run();
    }
}
