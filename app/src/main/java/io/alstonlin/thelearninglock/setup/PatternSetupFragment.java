package io.alstonlin.thelearninglock.setup;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.List;

import io.alstonlin.thelearninglock.shared.Const;
import io.alstonlin.thelearninglock.shared.OnFragmentFinishedListener;
import io.alstonlin.thelearninglock.shared.ML;
import io.alstonlin.thelearninglock.R;
import io.alstonlin.thelearninglock.pattern.OnPatternSelectListener;
import io.alstonlin.thelearninglock.pattern.PatternUtils;
import io.alstonlin.thelearninglock.shared.SharedUtils;
import me.zhanghai.android.patternlock.PatternView;


/**
 * The Fragment that the user will select their pattern, and train it
 */
public class PatternSetupFragment extends Fragment {

    private ML ml;
    private List<int[]> pattern;
    private int patternsLeft;

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
        getActivity().setTitle("Choose a Pattern");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        patternsLeft = Const.STARTING_TRAINING_SIZE;
        final View layout = inflater.inflate(R.layout.layout_pattern, container, false);
        final LinearLayout confirmBar = (LinearLayout) layout.findViewById(R.id.pattern_view_confirm_bar);
        final Button confirmButton = (Button) layout.findViewById(R.id.pattern_view_confirm_button);
        final Button resetButton = (Button) layout.findViewById(R.id.pattern_view_reset_button);
        OnPatternSelectListener listener = new OnPatternSelectListener() {
            @Override
            public void onPatternSelect(final List<int[]> pattern, final double[] timeBetweenPatternNodes, final PatternView patternView) {
                if (PatternSetupFragment.this.pattern == null) { // First pattern
                    // Confirm / Reset pattern logic
                    patternView.setInputEnabled(false);
                    confirmBar.setVisibility(View.VISIBLE);
                    resetButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            confirmBar.setVisibility(View.GONE);
                            patternView.clearPattern();
                            patternView.setInputEnabled(true);
                            patternsLeft = Const.STARTING_TRAINING_SIZE;
                        }
                    });
                    confirmButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (savePattern(pattern)) {
                                PatternSetupFragment.this.pattern = pattern;
                                ml = new ML(getContext(), timeBetweenPatternNodes.length);
                                confirmBar.setVisibility(View.GONE);
                                patternView.clearPattern();
                                patternView.setInputEnabled(true);
                                // ML stuff that was skipped
                                ml.addEntry(timeBetweenPatternNodes, false);
                                patternsLeft--;
                                String title = "Enter the pattern " + patternsLeft + " more times";
                                PatternUtils.setPatternLayoutTitle(layout, title);
                            } else {
                                Snackbar.make(layout, "An error occurred! Please try again",
                                        Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    confirmBar.setVisibility(View.GONE);
                    patternView.clearPattern();
                    // Checks if it's the same pattern
                    if (!PatternUtils.arePatternsEqual(pattern, PatternSetupFragment.this.pattern)){
                        Snackbar.make(layout, "That pattern does not match your first one!",
                                Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    // Training
                    ml.addEntry(timeBetweenPatternNodes, false);
                    patternsLeft--;
                    // Updates the title
                    if (patternsLeft == 0){
                        finished();
                    } else {
                        String title = "Enter the pattern " + patternsLeft + " more times";
                        if (patternsLeft <= Const.CHANGE_FINGERS_MESSAGE_AT){
                            title += "\nTry changing fingers";
                        }
                        PatternUtils.setPatternLayoutTitle(layout,
                                "Enter the pattern " + patternsLeft + " more times");
                    }
                }
            }
        };
        PatternUtils.setupPatternLayout(getContext(), layout, listener, "Draw the pattern you want to use");
        return layout;
    }

    private void finished(){
        ml.train();
        ((OnFragmentFinishedListener)getActivity()).onFragmentFinished();
    }

    private boolean savePattern(List<int[]> pattern) {
        return SharedUtils.storeObjectSecurely(Const.PATTERN_FILENAME, getContext(), pattern);
    }
}
