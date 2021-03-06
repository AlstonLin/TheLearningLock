package io.alstonlin.thelearninglock.setup;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.alstonlin.thelearninglock.R;
import io.alstonlin.thelearninglock.pin.OnPINSelectListener;
import io.alstonlin.thelearninglock.pin.PINUtils;
import io.alstonlin.thelearninglock.shared.Const;
import io.alstonlin.thelearninglock.shared.OnFragmentFinishedListener;
import io.alstonlin.thelearninglock.shared.SharedUtils;


/**
 * A Fragment where the user would set up their PIN
 */
public class PINSetupFragment extends Fragment {
    /**
     * Factory method to create a new instance of this Fragment
     *
     * @return A new instance of fragment PINSetupFragment.
     */
    public static PINSetupFragment newInstance() {
        PINSetupFragment fragment = new PINSetupFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Choose a PIN");
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle("Choose a PIN");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.layout_pin, container, false);
        final String[] PINToConfirm = {null};
        SharedUtils.setupBackground(getContext(), view);
        PINUtils.setupPINView(view, new OnPINSelectListener() {
            @Override
            public void onPINSelected(String PIN) {
                if (PINToConfirm[0] == null) {
                    PINToConfirm[0] = PIN;
                    PINUtils.setPINTitle(view, "Confirm your PIN");
                    PINUtils.clearPIN(view);
                } else {
                    if (PINToConfirm[0].equals(PIN)) {
                        savePIN(PIN);
                        finished();
                    } else {
                        PINToConfirm[0] = null;
                        PINUtils.setPINTitle(view, "The PIN you've entered does not match! Enter your PIN again.");
                        PINUtils.clearPIN(view);
                    }
                }
            }
        }, "Choose a backup PIN. This will be used to verify suspicious pattern draws.");
        return view;
    }

    public void finished() {
        // Sets the flag
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        editor.putBoolean(Const.SETUP_FLAG, true);
        editor.putBoolean(Const.ENABLED, true);
        editor.commit();
        ((OnFragmentFinishedListener) getActivity()).onFragmentFinished();
    }

    private boolean savePIN(String PIN) {
        return SharedUtils.storeObjectSecurely(Const.PASSCODE_FILENAME, getContext(), PIN);
    }
}
