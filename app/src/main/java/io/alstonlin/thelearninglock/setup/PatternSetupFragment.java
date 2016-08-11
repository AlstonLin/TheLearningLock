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

import io.alstonlin.thelearninglock.Const;
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
        View view = inflater.inflate(R.layout.layout_pattern, container, false);
        PatternUtils.setupPatternLayout(getContext(), view, this, "Select your pattern");
        return view;
    }

    @Override
    public void onPatternSelect(List<int[]> pattern, double[] timeBetweenNodeSelects) {
        if (savePattern(pattern)) {
            // TODO: Want to repeat this a few times
            ((FragmentChangable) getActivity()).changeFragment(PINSetupFragment.newInstance());
        } else {
            Toast.makeText(getContext(), "An error occurred! Please try again", Toast.LENGTH_LONG).show();
        }
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
