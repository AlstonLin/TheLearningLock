package io.alstonlin.thelearninglock.setup;

import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import io.alstonlin.thelearninglock.shared.OnFragmentFinishedListener;
import io.alstonlin.thelearninglock.R;

/**
 * The activity that goes through the set up process of the lock screen.
 */
public class SetupActivity extends FragmentActivity implements OnFragmentFinishedListener {
    private Bundle savedInstanceState;
    private int fragmentStateIndex;

     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        startApp();
    }

    private void startApp(){
        fragmentStateIndex = 0;
        setContentView(R.layout.activity_setup);
        if (savedInstanceState != null) {
            return;
        }
        // Permission Check first
        BackgroundPickerFragment firstFragment = new BackgroundPickerFragment();
        firstFragment.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction()
                .add(R.id.activity_setup_fragment_container, firstFragment).commit();
    }

    /*
     * Fragment life cycle management
     */

    private enum FragmentStates {
        // The order of which the Fragments are shown
        BACKGROUND_PICKER {
            @Override
            protected Fragment newFragment() {
                return BackgroundPickerFragment.newInstance();
            }
        },
        PATTERN_SETUP {
            @Override
            protected Fragment newFragment() {
                return PatternSetupFragment.newInstance();
            }
        },
        PIN_SETUP {
            @Override
            protected Fragment newFragment() {
                return PINSetupFragment.newInstance();
            }
        };
        protected abstract Fragment newFragment();
    }

    private void changeFragment(Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.activity_setup_fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onFragmentFinished() {
        fragmentStateIndex++;
        if (fragmentStateIndex >= FragmentStates.values().length){ // Last step. Finished set up
            Snackbar.make(findViewById(R.id.activity_setup_fragment_container), "All set up!", Snackbar.LENGTH_SHORT).show();
            // TODO: Maybe just go to the settings page instead?
            finish();
        } else {
            changeFragment(FragmentStates.values()[fragmentStateIndex].newFragment());
        }
    }
}
