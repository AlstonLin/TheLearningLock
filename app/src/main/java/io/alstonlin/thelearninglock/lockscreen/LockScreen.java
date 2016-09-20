package io.alstonlin.thelearninglock.lockscreen;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

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
    private View lockView;
    private LockScreenNotificationsAdapter notificationsAdapter;
    private ListView notificationsList;
    private Context context;
    // Unlocking
    private ML ml;
    private PopupWindow unlockScreen;
    private PopupWindow pinScreen;
    private View patternLayout;

    // Listeners
    private OnPatternSelectListener patternListener = new OnPatternSelectListener() {
        @Override
        public void onPatternSelect(List<int[]> pattern, final double[] timeBetweenNodeSelects, PatternView patternView) {
            patternView.clearPattern();
            if (SharedUtils.compareToSecureObject(Const.PATTERN_FILENAME, context, pattern)){
                if (ml.predictImposter(timeBetweenNodeSelects)){
                    if (unlockScreen != null) unlockScreen.dismiss();
                    showPINScreen(timeBetweenNodeSelects);
                } else {
                    unlock();
                }
            } else{
                PatternUtils.setPatternLayoutTitle(patternLayout, "Invalid Pattern! Please try again.");
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

    public LockScreen(Context context){
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View lockView = layoutInflater.inflate(R.layout.lock_screen, null);
        this.lockView = lockView;
        this.context = context;
        setupLockView(lockView);
    }

    public void lock(){
        ml = ML.loadFromFile(context);
        LockUtils.lock(context, lockView);
    }

    public void unlock(){
        LockUtils.unlock(context, lockView);
    }

    /**
     * Change the notifications displayed on the lock screen.
     * @param notifications The new notifications
     */
    public void updateNotifications(Notification[] notifications){
        // Filters out secret notifications
        ArrayList<Notification> publicNotifications = new ArrayList<>();
        for (int i = 0; i < notifications.length; i++){
            Notification notification = notifications[i];
            // TODO: Have a setting where the user decides which ones to show?
            if (notification.visibility != Notification.VISIBILITY_SECRET){
                publicNotifications.add(notification);
            }
        }
        notificationsAdapter.setNotifications(publicNotifications);
    }

    /**
     * Hides the unlock Popup.
     */
    public void hideUnlockScreen(){
        if (unlockScreen != null){
            unlockScreen.dismiss();
            unlockScreen = null;
        }
        if (pinScreen != null){
            pinScreen.dismiss();
            pinScreen = null;
        }
    }


    /**
     * Helper method to set up the View of the Lock Screen itself.
     * @param view The Lock Screen's View
     */
    private void setupLockView(View view){
        Button unlock = (Button) view.findViewById(R.id.unlockButton);
        unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUnlockScreen();
            }
        });
        notificationsList = (ListView) view.findViewById(R.id.lock_screen_notifications_list);
        notificationsAdapter = new LockScreenNotificationsAdapter(context, notificationListener);
        notificationsList.setAdapter(notificationsAdapter);
    }

    /**
     * Shows the unlock Popup.
     */
    private void showUnlockScreen(){
        if (ml == null){ // This really should be been set up
            Snackbar.make(lockView, "You have no set up the lock screen yet!", Snackbar.LENGTH_SHORT).show();
        }
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        patternLayout = inflater.inflate(R.layout.layout_pattern, null, false);
        PatternUtils.setupPatternLayout(context, patternLayout, patternListener, "Draw your pattern to unlock");
        unlockScreen = new PopupWindow(
                patternLayout,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true
        );
        unlockScreen.showAtLocation(lockView, Gravity.CENTER, 0, 0);
    }

    /**
     * Shows the PIN keypad popup if the user seems suspicious.
     * Entering the PIN successfully will result in unlock and retraining of the algorithm
     * @param timeBetweenNodeSelects The data that will be used to retrain if PIN unlocks this
     */
    private void showPINScreen(final double[] timeBetweenNodeSelects){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View pinLayout = inflater.inflate(R.layout.layout_pin, null, false);
        PINUtils.setupPINView(context, pinLayout, new OnPINSelectListener() {
            @Override
            public void onPINSelected(String PIN) {
                if (SharedUtils.compareToSecureObject(Const.PASSCODE_FILENAME, context, PIN)){
                    unlock();
                    // Retrains algorithm
                    ml.addEntry(timeBetweenNodeSelects, true);
                } else{
                    PINUtils.setPINTitle(pinLayout, "Wrong PIN!");
                }
            }
        }, "That was a suspicious unlock! Please enter your PIN to confirm you're the owner");
        pinScreen = new PopupWindow(
                pinLayout,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true
        );
        pinScreen.showAtLocation(lockView, Gravity.CENTER, 0, 0);
    }

}
