package io.alstonlin.thelearninglock;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

/**
 * Manages all the interactions with the View for the lock screen. Similar to a Fragment for it.
 */
public class LockScreen {
    private View lockView;
    private Context context;

    public LockScreen(Context context){
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View lockView = layoutInflater.inflate(R.layout.lock_screen, null);
        setupView(lockView);
        this.lockView = lockView;
        this.context = context;
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
                unlock();
            }
        });
    }
}
