package io.alstonlin.thelearninglock.lockscreen;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.pwittchen.weathericonview.WeatherIconView;
import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.snapshot.LocationResult;
import com.google.android.gms.awareness.snapshot.WeatherResult;
import com.google.android.gms.awareness.state.Weather;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.hudomju.swipe.SwipeToDismissTouchListener;
import com.hudomju.swipe.adapter.ListViewAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import io.alstonlin.thelearninglock.R;
import io.alstonlin.thelearninglock.pattern.OnPatternSelectListener;
import io.alstonlin.thelearninglock.pattern.PatternUtils;
import io.alstonlin.thelearninglock.pin.OnPINSelectListener;
import io.alstonlin.thelearninglock.pin.PINUtils;
import io.alstonlin.thelearninglock.shared.Const;
import io.alstonlin.thelearninglock.shared.ML;
import io.alstonlin.thelearninglock.shared.SharedUtils;
import me.zhanghai.android.patternlock.PatternView;

/**
 * Manages all the interactions with the View for the lock screen. Similar to a Fragment for it.
 */
public class LockScreen {
    // Weather mapping
    private static final HashMap<Integer, Integer> weatherCodeToIcon;

    static {
        weatherCodeToIcon = new HashMap<>();
        weatherCodeToIcon.put(Weather.CONDITION_UNKNOWN, null);
        weatherCodeToIcon.put(Weather.CONDITION_CLEAR, R.string.wi_day_sunny);
        weatherCodeToIcon.put(Weather.CONDITION_CLOUDY, R.string.wi_day_cloudy);
        weatherCodeToIcon.put(Weather.CONDITION_FOGGY, R.string.wi_day_fog);
        weatherCodeToIcon.put(Weather.CONDITION_HAZY, R.string.wi_day_haze);
        weatherCodeToIcon.put(Weather.CONDITION_ICY, R.string.wi_day_hail);
        weatherCodeToIcon.put(Weather.CONDITION_RAINY, R.string.wi_day_rain);
        weatherCodeToIcon.put(Weather.CONDITION_SNOWY, R.string.wi_day_snow);
        weatherCodeToIcon.put(Weather.CONDITION_STORMY, R.string.wi_day_thunderstorm);
        weatherCodeToIcon.put(Weather.CONDITION_WINDY, R.string.wi_day_windy);
    }

    private ViewGroup lockView;
    private View backgroundView;
    private LockScreenNotificationsAdapter notificationsAdapter;
    private ListView notificationsList;
    private Context context;
    private LockScreenService service;
    // Unlocking
    private double[] timeBetweenNodeSelects;
    private byte[] PINHash;
    private byte[] patternHash;
    private ML ml;
    private View patternLayout;
    private StatusBar statusBar;
    // Notifications + dismissing
    private SwipeToDismissTouchListener<ListViewAdapter> onDismissListener;
    // APIs
    private Geocoder geocoder;
    private GoogleApiClient googleApi;
    // Listeners
    private OnPatternSelectListener patternListener = new OnPatternSelectListener() {
        @Override
        public void onPatternSelect(List<int[]> pattern, final double[] timeBetweenNodeSelects, PatternView patternView) {
            patternView.clearPattern();
            if (SharedUtils.compareObjectToHash(context, pattern, patternHash)) {
                if (ml.predictImposter(timeBetweenNodeSelects)) {
                    LockScreen.this.timeBetweenNodeSelects = timeBetweenNodeSelects;
                    showPINScreen();
                } else {
                    ml.addEntry(timeBetweenNodeSelects, true); // update training set with new data
                    unlock();
                }
            } else {
                PatternUtils.setPatternLayoutTitle(patternLayout, "Wrong Pattern!");
            }
        }
    };

    public LockScreen(LockScreenService service, GoogleApiClient googleApi) {
        // Attrs
        this.service = service;
        this.context = service;
        this.googleApi = googleApi;
        this.geocoder = new Geocoder(service, Locale.getDefault());
        ;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View lockView = layoutInflater.inflate(R.layout.lock_container, null);
        this.lockView = (ViewGroup) lockView;
        this.backgroundView = new LinearLayout(context);
        // Loads data
        ml = ML.loadFromFile(context);
        PINHash = SharedUtils.loadHashFromFiledFile(context, Const.PASSCODE_FILENAME, true);
        patternHash = SharedUtils.loadHashFromFiledFile(context, Const.PATTERN_FILENAME, true);
        // Setup and lock
        setupLockContainer(lockView);
        try {
            LockUtils.lock(context, lockView, backgroundView);
            statusBar = new StatusBar(context, backgroundView);
            // Dismiss keyboard
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        } catch (WindowManager.BadTokenException e) {
            e.printStackTrace();
        }
    }

    public void unlock() {
        LockUtils.unlock(context, lockView, backgroundView);
        // Make absolutely sure there is no memory leak
        notificationsAdapter.detachCreatedViews();
        lockView.removeAllViews();
        statusBar.onDestroy();
        service.destroyLockScreen();
    }

    /**
     * Change the notifications displayed on the lock screen.
     *
     * @param notifications The new notifications
     */
    public void updateNotifications(LockScreenNotificationService.LockScreenNotification[] notifications) {
        // Filters out secret notifications
        ArrayList<LockScreenNotificationService.LockScreenNotification> publicNotifications = new ArrayList<>();
        for (LockScreenNotificationService.LockScreenNotification notification : notifications) {
            // TODO: Have a setting where the user decides which ones to show?
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP
                    || notification.getNotification().visibility != Notification.VISIBILITY_SECRET) {
                publicNotifications.add(notification);
            }
        }
        notificationsAdapter.setNotifications(publicNotifications);
    }

    /**
     * Hides the unlock Popup.
     */
    public void onScreenOff() {
        LockUtils.setVisibleScreen(lockView, R.id.lock_screen);
    }

    /**
     * Updates the status bar when the screen turns on
     */
    public void onScreenOn() {
        if (statusBar == null) {
            Toast.makeText(context, "Permission to draw over apps must be granted for LearningLock to function", Toast.LENGTH_LONG);
            return;
        }
        statusBar.updateStatusBar();
        updateWeather(lockView);
    }

    /**
     * Notifies that charging state has changed and re-draws status bar
     */
    public void onChargingStateChanged() {
        statusBar.updateStatusBar();
    }

    /**
     * Helper method to set up the View of the Lock Screen itself.
     *
     * @param view The Lock Screen's View
     */
    private void setupLockContainer(View view) {
        setupSlidebar(view);
        setupNotificationList(view);
        setupPatternScreen(view);
        setupPINScreen(view);
        setupAddEntryDialog(view);
    }

    private void setupSlidebar(View view){
        SlideButton seekBar = (SlideButton) view.findViewById(R.id.lock_screen_slider);
        seekBar.setSlideButtonListener(new SlideButtonListener() {
            @Override
            public void handleSlide() {
                showUnlockScreen();
            }
        });
        // Increases the vertical touch hitbox
        Rect delegateArea = new Rect();
        seekBar.getHitRect(delegateArea);
        delegateArea.top -= 600;
        delegateArea.bottom += 600;
        TouchDelegate expandedArea = new TouchDelegate(delegateArea, seekBar);
        if (View.class.isInstance(seekBar.getParent())) {
            ((View) seekBar.getParent()).setTouchDelegate(expandedArea);
        }
    }

    private void setupNotificationList(View lockView) {
        notificationsList = (ListView) lockView.findViewById(R.id.lock_screen_notifications_list);
        notificationsAdapter = new LockScreenNotificationsAdapter(context, new Runnable() {
            @Override
            public void run() {
                onDismissListener.undoPendingDismiss();
            }
        });
        notificationsList.setAdapter(notificationsAdapter);
        onDismissListener = new SwipeToDismissTouchListener<>(
                new ListViewAdapter(notificationsList),
                new SwipeToDismissTouchListener.DismissCallbacks<ListViewAdapter>() {
                    @Override
                    public boolean canDismiss(int position) {
                        return notificationsAdapter.getItem(position).isDeletable();
                    }

                    @Override
                    public void onPendingDismiss(ListViewAdapter recyclerView, int position) {
                    }

                    @Override
                    public void onDismiss(ListViewAdapter view, int position) {
                        notificationsAdapter.dismissNotification(position);
                    }
                }
        );
        onDismissListener.setDismissDelay(1000);
        notificationsList.setOnTouchListener(onDismissListener);
        notificationsList.setOnScrollListener((AbsListView.OnScrollListener) onDismissListener.makeScrollListener());
        notificationsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PendingIntent intent = notificationsAdapter.getItem(position).getNotification().contentIntent;
                try {
                    intent.send();
                    showUnlockScreen();
                } catch (PendingIntent.CanceledException e) {
                }
            }
        });
    }

    private void setupPatternScreen(View lockView) {
        patternLayout = lockView.findViewById(R.id.unlock_screen);
        PatternUtils.setupPatternLayout(patternLayout, patternListener, "Enter your pattern");
    }

    /**
     * Shows the unlock Popup.
     */
    private void showUnlockScreen() {
        if (ml == null) { // This really should be been set up
            Snackbar.make(lockView, "You have no set up the lock screen yet!", Snackbar.LENGTH_SHORT).show();
        }
        LockUtils.setVisibleScreen(lockView, R.id.unlock_screen);
    }

    private void setupPINScreen(View lockView) {
        final View pinLayout = lockView.findViewById(R.id.pin_screen);
        PINUtils.setupPINView(pinLayout, new OnPINSelectListener() {
            @Override
            public void onPINSelected(String PIN) {
                if (SharedUtils.compareObjectToHash(context, PIN, PINHash)) {
                    // Shows a dialog to confirm unlock
                    // unless the user has requested not to show it (do not ask again)
                    String savedRetrainConfirm = PreferenceManager.getDefaultSharedPreferences(context)
                            .getString(Const.SAVED_RETRAIN_CONFIRM, null);
                    if (savedRetrainConfirm != null) {
                        if (savedRetrainConfirm.equals("true") && timeBetweenNodeSelects != null) {
                            ml.addEntry(timeBetweenNodeSelects, true);
                        }
                        unlock();
                    } else {
                        showConfirmRetrain();
                    }
                } else {
                    PINUtils.setPINTitle(pinLayout, "Wrong PIN!");
                    PINUtils.clearPIN(pinLayout);
                }
            }
        }, "That was suspicious! Enter your PIN to confirm you're the owner");
    }

    private void setupAddEntryDialog(View lockView) {
        final View confirmLayout = lockView.findViewById(R.id.add_dialog);
        Button yesBtn = (Button) confirmLayout.findViewById(R.id.layout_add_entry_yes);
        Button noBtn = (Button) confirmLayout.findViewById(R.id.layout_add_entry_no);
        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkBox = (CheckBox) confirmLayout.findViewById(R.id.do_not_ask_to_retrain);
                if (checkBox.isChecked()) {
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                    editor.putString(Const.SAVED_RETRAIN_CONFIRM, "true");
                    editor.apply();
                }
                unlock();
                if (timeBetweenNodeSelects != null) ml.addEntry(timeBetweenNodeSelects, true);
            }
        });
        noBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox checkBox = (CheckBox) confirmLayout.findViewById(R.id.do_not_ask_to_retrain);
                if (checkBox.isChecked()) {
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                    editor.putString(Const.SAVED_RETRAIN_CONFIRM, "false");
                    editor.apply();
                }
                unlock();
                timeBetweenNodeSelects = null;
            }
        });
    }

    private void updateWeather(final View view) {
        if (statusBar == null) return; // Did not load screen
        try {
            Awareness.SnapshotApi.getWeather(googleApi)
                    .setResultCallback(new ResultCallback<WeatherResult>() {
                        @Override
                        public void onResult(WeatherResult weatherResult) {
                            if (weatherResult.getStatus().isSuccess()) {
                                Weather weather = weatherResult.getWeather();
                                // Temperature
                                int tempC = Math.round(weather.getTemperature(Weather.CELSIUS));
                                int tempF = Math.round(weather.getTemperature(Weather.FAHRENHEIT));
                                TextView tempView = (TextView) view.findViewById(R.id.temperature);
                                tempView.setVisibility(View.VISIBLE);
                                tempView.setText(String.format("%d℃ / %d℉", tempC, tempF));
                                // Weather icons
                                LinearLayout iconsContainer = (LinearLayout) view.findViewById(R.id.weather_icons);
                                iconsContainer.removeAllViews();
                                for (Integer i : weather.getConditions()) {
                                    WeatherIconView weatherIconView = new WeatherIconView(context);
                                    Integer resource = weatherCodeToIcon.get(i);
                                    if (resource != null) {
                                        weatherIconView.setIconResource(context.getString(resource));
                                        weatherIconView.setIconSize(16);
                                        weatherIconView.setIconColor(Color.WHITE);
                                        iconsContainer.addView(weatherIconView);
                                    }
                                }
                            }
                        }
                    });
            Awareness.SnapshotApi.getLocation(googleApi)
                    .setResultCallback(new ResultCallback<LocationResult>() {
                        @Override
                        public void onResult(LocationResult locationResult) {
                            final Location location = locationResult.getLocation();
                            // Async Task because this network call is not synchronous
                            new AsyncTask<Void, Void, String>() {
                                @Override
                                protected String doInBackground(Void... params) {
                                    try {
                                        return getCity(location.getLatitude(), location.getLongitude());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        return null;
                                    }
                                }

                                @Override
                                protected void onPostExecute(String cityName) {
                                    super.onPostExecute(cityName);
                                    TextView cityView = (TextView) view.findViewById(R.id.cityName);
                                    if (cityName == null || cityView == null) return;
                                    cityView.setVisibility(View.VISIBLE);
                                    cityView.setText(cityName);
                                }
                            }.execute();
                        }
                    });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows the PIN keypad popup if the user seems suspicious.
     * Entering the PIN successfully will result in unlock and retraining of the algorithm
     */
    private void showPINScreen() {
        LockUtils.setVisibleScreen(lockView, R.id.pin_screen);
    }

    /**
     * Shows a dialog prompting if the user wants to retrain ML, and unlocks after
     */
    private void showConfirmRetrain() {
        LockUtils.setVisibleScreen(lockView, R.id.add_dialog);
    }

    private String getCity(double lat, double lon) throws IOException {
        List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
        if (addresses != null && addresses.size() > 0) {
            return addresses.get(0).getLocality();
        }
        return null;
    }
}
