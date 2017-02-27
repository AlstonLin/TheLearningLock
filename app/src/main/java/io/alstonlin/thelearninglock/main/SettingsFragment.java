package io.alstonlin.thelearninglock.main;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import io.alstonlin.thelearninglock.R;
import io.alstonlin.thelearninglock.lockscreen.LockScreenService;
import io.alstonlin.thelearninglock.shared.Const;

/**
 * The Fragment shown by the MainActivity that allows the user to configure Settings.
 */
public class SettingsFragment extends Fragment {
    private boolean enabled;
    private float tolerance;

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enabled = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(Const.ENABLED, false);
        tolerance = PreferenceManager.getDefaultSharedPreferences(getContext()).getFloat(Const.EPSILON_TOL, 1f);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_page, container, false);
        // Sets up the enabled switch
        Switch enabledSwitch = (Switch) view.findViewById(R.id.settings_page_enable);
        enabledSwitch.setChecked(enabled);
        enabledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                enabled = b;
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
                editor.putBoolean(Const.ENABLED, enabled);
                editor.commit();
                // Starts the service as well
                Intent intent = new Intent(getContext(), LockScreenService.class);
                intent.addFlags(LockScreenService.UNLOCK_FLAG);
                getActivity().startService(intent);
            }
        });
        // Sets up the epsilon multiplier
        final int MAX_PROGRESS = 100;
        final float MIN = 0.975f, RANGE = 0.05f; // Want the range to be (0.975, 1.025)
        final TextView multDisplay = (TextView) view.findViewById(R.id.epsilonMultVal);
        SeekBar multBar = (SeekBar) view.findViewById(R.id.epsilonMult);
        multBar.setProgress(Math.round(MAX_PROGRESS * (tolerance - MIN) / RANGE));
        multDisplay.setText(tolerance + "x");
        multBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float calculated = (progress / (float) MAX_PROGRESS) * RANGE + MIN;
                // Rounds to 2 decimals
                tolerance = Math.round(calculated * 1000) / 1000f;
                // Display and saves
                multDisplay.setText(tolerance + "x");
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
                editor.putFloat(Const.EPSILON_TOL, tolerance);
                editor.commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        return view;
    }
}
