package io.alstonlin.thelearninglock.setup;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import io.alstonlin.thelearninglock.R;
import io.alstonlin.thelearninglock.main.MainActivity;
import io.alstonlin.thelearninglock.shared.OnFragmentFinishedListener;

/**
 * The activity that goes through the set up process of the lock screen.
 */
public class SetupActivity extends AppCompatActivity implements OnFragmentFinishedListener {
    private Bundle savedInstanceState;
    private int fragmentStateIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        this.savedInstanceState = savedInstanceState;
        startApp();
    }

    private void startApp() {
        fragmentStateIndex = 0;
        setContentView(R.layout.activity_setup);
        if (savedInstanceState != null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window w = getWindow();
            w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
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

    private void changeFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.activity_setup_fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (fragmentStateIndex > 0) {
            fragmentStateIndex--;
        }
    }

    @Override
    public void onFragmentFinished() {
        fragmentStateIndex++;
        if (fragmentStateIndex >= FragmentStates.values().length) { // Last step. Finished set up
            Snackbar.make(findViewById(R.id.activity_setup_fragment_container), "All set up!", Snackbar.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            changeFragment(FragmentStates.values()[fragmentStateIndex].newFragment());
        }
    }

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
}
