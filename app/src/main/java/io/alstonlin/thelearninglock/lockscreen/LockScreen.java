package io.alstonlin.thelearninglock.lockscreen;

import android.app.Notification;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import io.alstonlin.thelearninglock.Const;
import io.alstonlin.thelearninglock.pattern.OnPatternSelectListener;
import io.alstonlin.thelearninglock.R;
import io.alstonlin.thelearninglock.pattern.PatternUtils;

/**
 * Manages all the interactions with the View for the lock screen. Similar to a Fragment for it.
 */
public class LockScreen implements OnPatternSelectListener {
    private View lockView;
    private Context context;
    // Unlocking
    private PopupWindow unlockScreen;
    private View patternLayout;
    private List<int[]> actualPattern;

    public LockScreen(Context context){
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View lockView = layoutInflater.inflate(R.layout.lock_screen, null);
        setupLockView(lockView);
        this.lockView = lockView;
        this.context = context;
        this.actualPattern = loadPattern();
    }

    public void lock(){
        LockUtils.lock(context, lockView);
    }

    public void unlock(){
        LockUtils.unlock(context, lockView);
    }

    public void updateNotifications(Notification[] notifications){
    }

    private void setupLockView(View view){
        Button unlock = (Button) view.findViewById(R.id.unlockButton);
        unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUnlockScreen();
            }
        });
    }

    private void showUnlockScreen(){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        patternLayout = inflater.inflate(R.layout.layout_pattern, null, false);
        PatternUtils.setupPatternLayout(context, patternLayout, this, "Draw your pattern to unlock");
        unlockScreen = new PopupWindow(
                patternLayout,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true
        );
        unlockScreen.showAtLocation(lockView, Gravity.CENTER, 0, 0);
    }

    public void hideUnlockScreen(){
        if (unlockScreen != null){
            unlockScreen.dismiss();
            unlockScreen = null;
        }
    }

    @Override
    public void onPatternSelect(List<int[]> pattern, double[] timeBetweenNodeSelects) {
        if (this.actualPattern == null){ // There was a problem loading the pattern, so we'll pretend what they entered was right
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
