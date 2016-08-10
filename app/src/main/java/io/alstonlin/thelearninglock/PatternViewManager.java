package io.alstonlin.thelearninglock;

import android.view.View;
import android.widget.Button;

/**
 * This class manages the Pattern View
 */
public class PatternViewManager {
    private View patternView;
    private OnPatternSelectListener listener;

    public PatternViewManager(View patternView, final OnPatternSelectListener listener){
        this.patternView = patternView;
        this.listener = listener;
        // TEMPORARY
        Button tmp = (Button) patternView.findViewById(R.id.unlockButton);
        tmp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onPatternSelect();
            }
        });
    }
}
