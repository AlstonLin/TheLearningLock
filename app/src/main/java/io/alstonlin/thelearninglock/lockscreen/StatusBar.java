package io.alstonlin.thelearninglock.lockscreen;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;

import io.alstonlin.thelearninglock.R;

/**
 * Contains functions to set up a status bar
 */

public class StatusBar {
    private static final HashMap<Integer, Integer> maxBatteryToIcon = new HashMap<>();
    private static final HashMap<Integer, Integer> maxBatteryChargingToIcon = new HashMap<>();
    static {
        // Icon displayed will be the one where the key is closest value that is higher than the pct
        maxBatteryToIcon.put(10, R.drawable.ic_battery_alert_white_36dp);
        maxBatteryToIcon.put(20, R.drawable.ic_battery_20_white_36dp);
        maxBatteryToIcon.put(30, R.drawable.ic_battery_30_white_36dp);
        maxBatteryToIcon.put(50, R.drawable.ic_battery_50_white_36dp);
        maxBatteryToIcon.put(60, R.drawable.ic_battery_60_white_36dp);
        maxBatteryToIcon.put(80, R.drawable.ic_battery_80_white_36dp);
        maxBatteryToIcon.put(90, R.drawable.ic_battery_90_white_36dp);
        maxBatteryToIcon.put(100, R.drawable.ic_battery_full_white_36dp);
        maxBatteryChargingToIcon.put(20, R.drawable.ic_battery_charging_20_white_36dp);
        maxBatteryChargingToIcon.put(30, R.drawable.ic_battery_charging_30_white_36dp);
        maxBatteryChargingToIcon.put(50, R.drawable.ic_battery_charging_50_white_36dp);
        maxBatteryChargingToIcon.put(60, R.drawable.ic_battery_charging_60_white_36dp);
        maxBatteryChargingToIcon.put(80, R.drawable.ic_battery_charging_80_white_36dp);
        maxBatteryChargingToIcon.put(90, R.drawable.ic_battery_charging_90_white_36dp);
        maxBatteryChargingToIcon.put(100, R.drawable.ic_battery_charging_full_white_36dp);
    }

    public static void setup(Context context, View background){
        // TODO: Also need something to update it whenever screen turns on
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View statusBar = layoutInflater.inflate(R.layout.status_bar, (ViewGroup) background);
        // Sets status bar height to what the system's is
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                getStatusBarHeight(context)
        );
        statusBar.findViewById(R.id.status_bar).setLayoutParams(layoutParams);
        updateStatusBar(context, background);
    }

    public static void updateStatusBar(Context context, View background){
        View statusBar = background.findViewById(R.id.status_bar);
        setBatteryPct(context, statusBar);
    }

    private static void setBatteryPct(Context context, View statusBar){
        // Sets battery percentage
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int batteryPct = Math.round(level / (float)scale * 100);
        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean charging = chargePlug == BatteryManager.BATTERY_PLUGGED_USB || chargePlug == BatteryManager.BATTERY_PLUGGED_AC;
        // Text
        ((TextView)statusBar.findViewById(R.id.battery_percentage)).setText(Integer.toString(batteryPct) + "%");
        // Icon
        int iconPct = 101;
        int iconId = -1;
        HashMap<Integer, Integer> iconMap = charging ? maxBatteryChargingToIcon : maxBatteryToIcon;
        for (int key : iconMap.keySet()){
            if (key < iconPct && batteryPct <= key){
                iconId = iconMap.get(key);
                iconPct = key;
            }
        }
        ((ImageView)statusBar.findViewById(R.id.battery_icon)).setImageResource(iconId);
    }

    private static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
