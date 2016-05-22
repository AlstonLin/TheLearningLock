package io.alstonlin.thelearninglock;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * The Fragment where the user would select thier pattern lock
 */
public class PatternSetupFragment extends Fragment {
    /**
     * Factory method to create a new instance of this Fragment
     * @return A new instance of fragment PatternSetupFragment.
     */
    public static PatternSetupFragment newInstance() {
        PatternSetupFragment fragment = new PatternSetupFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pattern_setup, container, false);
        Button next = (Button) view.findViewById(R.id.nextButton);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickNext();
            }
        });
        return view;
    }

    public void clickNext() {
        ((FragmentChangable)getActivity()).changeFragment(PINSetupFragment.newInstance());
    }
}
