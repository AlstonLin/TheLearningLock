package io.alstonlin.thelearninglock.setup;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import io.alstonlin.thelearninglock.Const;
import io.alstonlin.thelearninglock.R;
import io.alstonlin.thelearninglock.pin.OnPINSelectListener;
import io.alstonlin.thelearninglock.pin.PINUtils;


/**
 * A Fragment where the user would set up their PIN
 */
public class PINSetupFragment extends Fragment {
    /**
     * Factory method to create a new instance of this Fragment
     * @return A new instance of fragment PINSetupFragment.
     */
    public static PINSetupFragment newInstance() {
        PINSetupFragment fragment = new PINSetupFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_pin, container, false);
        PINUtils.setupPINView(view, new OnPINSelectListener() {
            @Override
            public void onPINSelected(String PIN) {
                // TODO: Probably want to confirm this
                savePIN(PIN);
                finished();
            }
        }, "Please select a backup PIN. This will be used when the pattern was drawn suspiciously.");
        return view;
    }

    public void finished() {
        // Sets the flag
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
        editor.putBoolean(Const.SETUP_FLAG, true);
        editor.commit();
        // Finished this activity
        Toast.makeText(getActivity(), "All set up!", Toast.LENGTH_SHORT).show();
        getActivity().finish();
    }

    // TODO: This is insecure. we should so something like a salted hash. Same thing for patterns just in case
    // Or something like this: http://android-developers.blogspot.ca/2013/02/using-cryptography-to-store-credentials.html
    private boolean savePIN(String PIN) {
        FileOutputStream fos = null;
        ObjectOutputStream os = null;
        boolean success = true;
        try {
            fos = getContext().openFileOutput(Const.PASSCODE_FILENAME, Context.MODE_PRIVATE);
            os = new ObjectOutputStream(fos);
            os.writeObject(PIN);
        } catch (IOException e) {
            e.printStackTrace();
            success = false;
        } finally {
            try {
                if (os != null) os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fos != null) fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return success;
    }
}
