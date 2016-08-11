package io.alstonlin.thelearninglock.setup;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import io.alstonlin.thelearninglock.FragmentChangable;
import io.alstonlin.thelearninglock.R;
import io.alstonlin.thelearninglock.pattern.OnPatternSelectListener;
import io.alstonlin.thelearninglock.pattern.PatternUtils;


/**
 * The Fragment that the user will select their pattern, and train it
 */
public class PatternSetupFragment extends Fragment implements OnPatternSelectListener {

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
        PatternUtils.setupPatternLayout(getContext(), view, this, "Select your pattern");
        return view;
    }

    @Override
    public void onPatternSelect(List<int[]> pattern, double[] timeBetweenNodeSelects) {
        ((FragmentChangable)getActivity()).changeFragment(PINSetupFragment.newInstance());
    }
}
