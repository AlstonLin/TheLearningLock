package io.alstonlin.thelearninglock.lockscreen;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
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
    private static final HashMap<Integer, Integer> wifiStrengthToIcon = new HashMap<>();
    private static final HashMap<Integer, Integer> signalStrengthToIcon = new HashMap<>();
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
        // Wifi
        wifiStrengthToIcon.put(0, R.drawable.ic_signal_wifi_0_bar_white_36dp);
        wifiStrengthToIcon.put(1, R.drawable.ic_signal_wifi_1_bar_white_36dp);
        wifiStrengthToIcon.put(2, R.drawable.ic_signal_wifi_2_bar_white_36dp);
        wifiStrengthToIcon.put(3, R.drawable.ic_signal_wifi_3_bar_white_36dp);
        wifiStrengthToIcon.put(4, R.drawable.ic_signal_wifi_4_bar_white_36dp);
        // Signal
        signalStrengthToIcon.put(0, R.drawable.ic_signal_cellular_0_bar_white_36dp);
        signalStrengthToIcon.put(1, R.drawable.ic_signal_cellular_1_bar_white_36dp);
        signalStrengthToIcon.put(2, R.drawable.ic_signal_cellular_2_bar_white_36dp);
        signalStrengthToIcon.put(3, R.drawable.ic_signal_cellular_3_bar_white_36dp);
        signalStrengthToIcon.put(4, R.drawable.ic_signal_cellular_4_bar_white_36dp);
    }

    private Context context;
    private View statusBar;
    private PhoneStateListener phoneListener;

    public StatusBar(Context context, View background){
        this.context = context;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.statusBar = layoutInflater.inflate(R.layout.status_bar, (ViewGroup) background);
        // Sets status bar height to what the system's is
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                getStatusBarHeight(context)
        );
        statusBar.findViewById(R.id.status_bar).setLayoutParams(layoutParams);
        setupSignalStrength(); // USes a listener so can update by itself
        updateStatusBar();
    }

    public void updateStatusBar(){
        setBatteryPct();
        setAirplaneMode();
        setWifiStrength();
    }

    private void setBatteryPct(){
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

    private void setAirplaneMode(){
        boolean airplaneMode = Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        if (airplaneMode){
            statusBar.findViewById(R.id.airplane_icon).setVisibility(View.VISIBLE);
        }
    }

    private void setWifiStrength(){
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        ImageView wifiIcon = ((ImageView)statusBar.findViewById(R.id.wifi_icon));
        if (wifiManager.isWifiEnabled()){
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 5);
            wifiIcon.setImageResource(wifiStrengthToIcon.get(level));
        }
    }

    private void setupSignalStrength(){
        final TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        final ImageView signalIcon = ((ImageView)statusBar.findViewById(R.id.signal_icon));
        phoneListener = new PhoneStateListener(){
            @Override
            public void onSignalStrengthsChanged(SignalStrength signalStrength) {
                if (manager.getNetworkOperator().equals("")){
                    signalIcon.setVisibility(View.GONE);
                } else {
                    signalIcon.setVisibility(View.VISIBLE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        signalIcon.setImageResource(signalStrengthToIcon.get(signalStrength.getLevel()));
                    } else {
                        // Just show the full icon
                        signalIcon.setImageResource(signalStrengthToIcon.get(4));
                    }
                }
            }
        };
        manager.listen(phoneListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
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
