package io.alstonlin.thelearninglock.lockscreen;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;

import io.alstonlin.thelearninglock.shared.Const;
import io.alstonlin.thelearninglock.shared.ML;
import io.alstonlin.thelearninglock.shared.SharedUtils;
import io.alstonlin.thelearninglock.pattern.OnPatternSelectListener;
import io.alstonlin.thelearninglock.R;
import io.alstonlin.thelearninglock.pattern.PatternUtils;
import io.alstonlin.thelearninglock.pin.OnPINSelectListener;
import io.alstonlin.thelearninglock.pin.PINUtils;
import me.zhanghai.android.patternlock.PatternView;

/**
 * Manages all the interactions with the View for the lock screen. Similar to a Fragment for it.
 */
public class LockScreen {
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

    // Listeners
    private OnPatternSelectListener patternListener = new OnPatternSelectListener() {
        @Override
        public void onPatternSelect(List<int[]> pattern, final double[] timeBetweenNodeSelects, PatternView patternView) {
            patternView.clearPattern();
            if (SharedUtils.compareObjectToHash(context, pattern, patternHash)){
                if (ml.predictImposter(timeBetweenNodeSelects)){
                    LockScreen.this.timeBetweenNodeSelects = timeBetweenNodeSelects;
                    showPINScreen();
                } else {
                    ml.addEntry(timeBetweenNodeSelects, true); // update training set with new data
                    unlock();
                }
            } else{
                PatternUtils.setPatternLayoutTitle(patternLayout, "Wrong Pattern!");
            }
        }
    };
    private NotificationSelectListener notificationListener = new NotificationSelectListener() {
        @Override
        public void onNotificationSelected(Notification notification) {
            PendingIntent intent = notification.contentIntent;
            try {
                intent.send();
                showUnlockScreen();
            } catch (PendingIntent.CanceledException e) {
            }
        }
    };

    public LockScreen(LockScreenService service){
        // Attrs
        this.service = service;
        this.context = service;
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
        LockUtils.lock(context, lockView, backgroundView);
        statusBar = new StatusBar(context, backgroundView);
        // Dismiss keyboard
        InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    public void unlock(){
        LockUtils.unlock(context, lockView, backgroundView);
        service.destroyLockScreen();
    }

    /**
     * Change the notifications displayed on the lock screen.
     * @param notifications The new notifications
     */
    public void updateNotifications(Notification[] notifications){
        // Filters out secret notifications
        ArrayList<Notification> publicNotifications = new ArrayList<>();
        for (Notification notification : notifications) {
            // TODO: Have a setting where the user decides which ones to show?
            if (notification.visibility != Notification.VISIBILITY_SECRET) {
                publicNotifications.add(notification);
            }
        }
        notificationsAdapter.setNotifications(publicNotifications);
    }

    /**
     * Hides the unlock Popup.
     */
    public void onScreenOff(){
        LockUtils.setVisibleScreen(lockView, R.id.lock_screen);
    }

    /**
     * Updates the status bar when the screen turns on
     */
    public void onScreenOn(){
        statusBar.updateStatusBar();
    }

    /**
     * Notifies that charging state has changed and re-draws status bar
     */
    public void onChargingStateChanged(){
        statusBar.updateStatusBar();
    }

    /**
     * Helper method to set up the View of the Lock Screen itself.
     * @param view The Lock Screen's View
     */
    private void setupLockContainer(View view){
        // Lock screen
        SlideButton seekBar  = (SlideButton) view.findViewById(R.id.lock_screen_slider);
        seekBar.setSlideButtonListener(new SlideButtonListener() {
            @Override
            public void handleSlide() {
                showUnlockScreen();
            }
        });
        notificationsList = (ListView) view.findViewById(R.id.lock_screen_notifications_list);
        notificationsAdapter = new LockScreenNotificationsAdapter(context, notificationListener);
        notificationsList.setAdapter(notificationsAdapter);
        // Unlock screen
        patternLayout = lockView.findViewById(R.id.unlock_screen);
        PatternUtils.setupPatternLayout(patternLayout, patternListener, "Enter your pattern");
        // PIN screen
        final View pinLayout = lockView.findViewById(R.id.pin_screen);
        PINUtils.setupPINView(pinLayout, new OnPINSelectListener() {
            @Override
            public void onPINSelected(String PIN) {
                if (SharedUtils.compareObjectToHash(context, PIN, PINHash)){
                    // Shows a dialog to confirm unlock
                    // unless the user has requested not to show it (do not ask again)
                    String savedRetrainConfirm = PreferenceManager.getDefaultSharedPreferences(context)
                                                                .getString(Const.SAVED_RETRAIN_CONFIRM, null);
                    if (savedRetrainConfirm != null) {
                        if (savedRetrainConfirm.equals("true") && timeBetweenNodeSelects != null ) {
                            ml.addEntry(timeBetweenNodeSelects, true);
                        }
                        unlock();
                    } else {
                        showConfirmRetrain();
                    }
                } else{
                    PINUtils.setPINTitle(pinLayout, "Wrong PIN!");
                    PINUtils.clearPIN(pinLayout);
                }
            }
        }, "That was suspicious! Enter your PIN to confirm you're the owner");
        // Add entry dialog
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

    /**
     * Shows the unlock Popup.
     */
    private void showUnlockScreen(){
        if (ml == null){ // This really should be been set up
            Snackbar.make(lockView, "You have no set up the lock screen yet!", Snackbar.LENGTH_SHORT).show();
        }
        LockUtils.setVisibleScreen(lockView, R.id.unlock_screen);
    }

    /**
     * Shows the PIN keypad popup if the user seems suspicious.
     * Entering the PIN successfully will result in unlock and retraining of the algorithm
     */
    private void showPINScreen(){
        LockUtils.setVisibleScreen(lockView, R.id.pin_screen);
    }

    /**
     * Shows a dialog prompting if the user wants to retrain ML, and unlocks after
     */
    private void showConfirmRetrain(){
        LockUtils.setVisibleScreen(lockView, R.id.add_dialog);
    }
}
