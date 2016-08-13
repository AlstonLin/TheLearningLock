package io.alstonlin.thelearninglock.setup;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import io.alstonlin.thelearninglock.FragmentChangable;
import io.alstonlin.thelearninglock.R;

/**
 * The activity that goes through the set up process of the lock screen.
 */
public class SetupActivity extends FragmentActivity implements FragmentChangable {
    private Bundle savedInstanceState;

     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        startApp();
    }

    private void startApp(){
        setContentView(R.layout.activity_setup);
        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            // Permission Check first
            BackgroundPickerFragment firstFragment = new BackgroundPickerFragment();
            firstFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, firstFragment).commit();
        }
    }

    public void changeFragment(Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
