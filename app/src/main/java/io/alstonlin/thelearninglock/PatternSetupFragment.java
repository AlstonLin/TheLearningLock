package io.alstonlin.thelearninglock;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * The Fragment that the user will select their pattern, and train it
 */
public class PatternSetupFragment extends Fragment implements OnPatternSelectListener{
    private PatternViewManager manager;

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
        View view = inflater.inflate(R.layout.pattern_view, container, false);
        manager = new PatternViewManager(view, this);
        return view;
    }

    @Override
    public void onPatternSelect() {
        ((FragmentChangable)getActivity()).changeFragment(PINSetupFragment.newInstance());
    }
}
