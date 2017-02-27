package io.alstonlin.thelearninglock.pattern;

import java.util.List;

import me.zhanghai.android.patternlock.PatternView;

/**
 * A listener for a PatternView
 */
public interface OnPatternSelectListener {
    /**
     * The method that is called when a Pattern has been selected.
     *
     * @param pattern                 A list of int[2] representing the pattern drawn, where the array contains
     *                                the x and y values.
     * @param timeBetweenPatternNodes The time in milliseconds between each selection of the nodes
     *                                in the pattern.
     * @param patternView             The PatternView that this pattern got selected on
     */
    void onPatternSelect(List<int[]> pattern, double[] timeBetweenPatternNodes, PatternView patternView);
}
