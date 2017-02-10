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
import android.widget.Switch;

import io.alstonlin.thelearninglock.R;
import io.alstonlin.thelearninglock.lockscreen.LockScreenService;
import io.alstonlin.thelearninglock.shared.Const;

/**
 * The Fragment shown by the MainActivity that allows the user to configure Settings.
 */
public class SettingsFragment extends Fragment {
    private boolean enabled;

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enabled = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(Const.ENABLED, false);
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
        return view;
    }
}
