package io.alstonlin.thelearninglock;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


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
        View view = inflater.inflate(R.layout.fragment_pinsetup, container, false);
        Button next = (Button) view.findViewById(R.id.finishButton);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickFinish();
            }
        });
        return view;
    }

    public void clickFinish() {
        Toast.makeText(getActivity(), "All set up!", Toast.LENGTH_SHORT).show();
        getActivity().startService(new Intent(getActivity(), LockScreenService.class));
        getActivity().finish();
    }
}
