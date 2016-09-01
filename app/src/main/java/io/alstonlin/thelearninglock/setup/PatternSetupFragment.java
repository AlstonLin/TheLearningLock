package io.alstonlin.thelearninglock.setup;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

import io.alstonlin.thelearninglock.shared.Const;
import io.alstonlin.thelearninglock.shared.OnFragmentFinishedListener;
import io.alstonlin.thelearninglock.shared.ML;
import io.alstonlin.thelearninglock.R;
import io.alstonlin.thelearninglock.pattern.OnPatternSelectListener;
import io.alstonlin.thelearninglock.pattern.PatternUtils;


/**
 * The Fragment that the user will select their pattern, and train it
 */
public class PatternSetupFragment extends Fragment implements OnPatternSelectListener {

    private ML ml;
    private List<int[]> pattern;
    private int patternsLeft = Const.STARTING_TRAINING_SIZE;

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
        View view = inflater.inflate(R.layout.layout_pattern, container, false);
        PatternUtils.setupPatternLayout(getContext(), view, this, "Select your pattern");
        return view;
    }

    @Override
    public void onPatternSelect(List<int[]> pattern, double[] timeBetweenNodeSelects) {
        // TODO: Something to reset this in case they screw up their first entry
        if (this.pattern == null) { // First pattern
            if (savePattern(pattern)) {
                this.pattern = pattern;
                ml = new ML(getContext(), timeBetweenNodeSelects.length);
            } else {
                Toast.makeText(getContext(), "An error occurred! Please try again", Toast.LENGTH_LONG).show();
                return;
            }
        } else {
            // Checks if it's the same pattern
            if (!PatternUtils.arePatternsEqual(pattern, this.pattern)){
                Toast.makeText(getContext(), "Pattern does not match you first one!", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        // Training
        ml.addEntry(timeBetweenNodeSelects, false);
        patternsLeft--;
        // Updates UI
        if (patternsLeft == 0){
            finished();
        } else {
            PatternUtils.setPatternLayoutTitle(getContext(), getView(), "Please enter your pattern " + patternsLeft + " more times.");
        }
    }

    private void finished(){
        ml.train();
        ((OnFragmentFinishedListener)getActivity()).onFragmentFinished();
    }

    private boolean savePattern(List<int[]> pattern) {
        FileOutputStream fos = null;
        ObjectOutputStream os = null;
        boolean success = true;
        try {
            fos = getContext().openFileOutput(Const.PATTERN_FILENAME, Context.MODE_PRIVATE);
            os = new ObjectOutputStream(fos);
            os.writeObject(pattern);
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
