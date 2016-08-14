package io.alstonlin.thelearninglock.lockscreen;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import io.alstonlin.thelearninglock.Const;
import io.alstonlin.thelearninglock.pattern.OnPatternSelectListener;
import io.alstonlin.thelearninglock.R;
import io.alstonlin.thelearninglock.pattern.PatternUtils;

/**
 * Manages all the interactions with the View for the lock screen. Similar to a Fragment for it.
 */
public class LockScreen {
    private View lockView;
    private LockScreenNotificationsAdapter notificationsAdapter;
    private ListView notificationsList;
    private Context context;
    // Unlocking
    private PopupWindow unlockScreen;
    private View patternLayout;
    private List<int[]> actualPattern;

    // Listeners
    private OnPatternSelectListener patternListener = new OnPatternSelectListener() {
        @Override
        public void onPatternSelect(List<int[]> pattern, double[] timeBetweenNodeSelects) {
            if (actualPattern == null){ // There was a problem loading the pattern, so we'll pretend what they entered was right
                // TODO: Is this really what should be done?
                unlock();
                return;
            }
            if (PatternUtils.arePatternsEqual(actualPattern, pattern)){
                unlock();
            } else{
                PatternUtils.setPatternLayoutTitle(context, patternLayout, "Invalid Pattern! Please try again.");
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
        if (actualPattern == null){
            this.actualPattern = loadPattern();
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
     * Hides the unlock Popup.
     */
    public void hideUnlockScreen(){
        if (unlockScreen != null){
            unlockScreen.dismiss();
            unlockScreen = null;
        }
    }

    /**
     * Loads the pattern from a file
     * @return The pattern, or null if an error occurred
     */
    private List<int[]> loadPattern(){
        List<int[]> pattern = null;
        FileInputStream fis = null;
        ObjectInputStream is = null;
        try {
            fis = context.openFileInput(Const.PATTERN_FILENAME);
            is = new ObjectInputStream(fis);
            pattern = (List<int[]>) is.readObject();
        } catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
            Toast.makeText(context, "An Error has occurred loading the lock screen! Please try again later.", Toast.LENGTH_LONG).show();
        } finally {
            try {
                if (is != null) is.close();
                if (fis != null) fis.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        return pattern;
    }

}
