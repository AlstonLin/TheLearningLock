package io.alstonlin.thelearninglock;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;

/**
 * Manages all the interactions with the View for the lock screen. Similar to a Fragment for it.
 */
public class LockScreen implements OnPatternSelectListener{
    private View lockView;
    private Context context;
    private PatternViewManager manager;
    private PopupWindow unlockScreen;

    public LockScreen(Context context){
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View lockView = layoutInflater.inflate(R.layout.lock_screen, null);
        setupView(lockView);
        this.lockView = lockView;
        this.context = context;
    }

    public void hideUnlockScreen(){
        if (unlockScreen != null){
            unlockScreen.dismiss();
            unlockScreen = null;
        }
    }

    public void lock(){
        Util.lock(context, lockView);
    }

    public void unlock(){
        Util.unlock(context, lockView);
    }

    private void setupView(View view){
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
        View unlockView = inflater.inflate(R.layout.pattern_view, null, false);
        manager = new PatternViewManager(unlockView, this);
        unlockScreen = new PopupWindow(
                unlockView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true
        );
        unlockScreen.showAtLocation(lockView, Gravity.CENTER, 0, 0);
    }

    @Override
    public void onPatternSelect() {
        unlock();
    }

}
