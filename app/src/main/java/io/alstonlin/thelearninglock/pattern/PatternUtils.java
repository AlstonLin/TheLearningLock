package io.alstonlin.thelearninglock.pattern;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.alstonlin.thelearninglock.R;
import me.zhanghai.android.patternlock.PatternView;

/**
 * Contains various static helper methods for the Pattern unlock.
 */
public class PatternUtils {

    /**
     * Manages the PatternView, listens to it, and handles / process the pattern received from the
     * listener and returns it to the given listener.
     * @param context The context this View is being displayed in
     * @param layout An instance of the layout defined in pattern_view.xml
     * @param listener An instance of OnPatternSelectListener that will receive the processed
     *                 pattern once it has been selected
     * @param title The title that should be displayed on the top of this Layout
     */
    public static void setupPatternLayout(Context context, final View layout, final OnPatternSelectListener listener, String title){
        // Title Setup
        TextView titleView = (TextView) layout.findViewById(R.id.pattern_view_title);
        titleView.setText(title);
        // Pattern
        final ArrayList<Double> selectTimes = new ArrayList<>(); // Keeps track of the times between each node select
        final PatternView patternView = (PatternView) layout.findViewById(R.id.pattern_view_pattern);
        patternView.setOnPatternListener(new PatternView.OnPatternListener() {
            @Override
            public void onPatternStart() {
            }

            @Override
            public void onPatternCleared() {
            }

            @Override
            public void onPatternCellAdded(List<PatternView.Cell> pattern) {
                selectTimes.add((double) System.currentTimeMillis());
            }

            @Override
            public void onPatternDetected(List<PatternView.Cell> pattern) {
                patternView.clearPattern();
                double[] timeBetweenPatternNodes = calculateTimeElapsed(selectTimes);
                selectTimes.clear();
                listener.onPatternSelect(PatternUtils.serializePattern(pattern), timeBetweenPatternNodes);
            }
        });
    }

    /**
     * Converts the list of Cells representing patterns to a list of int[2] with the same row/col info
     * @param pattern The pattern_view to convert
     * @return The converted list of int[2]
     */
    public static ArrayList<int[]> serializePattern(List<PatternView.Cell> pattern){
        ArrayList<int[]> list = new ArrayList<>();
        for (PatternView.Cell cell : pattern){
            int[] a = new int[2];
            a[0] = cell.getRow();
            a[1] = cell.getColumn();
            list.add(a);
        }
        return list;
    }

    /**
     * Determines if two lists of int[2] are equal, because apparently Java can't check that for us.
     * @param l1 The first list
     * @param l2 The second list
     * @return If the lists are equal
     */
    public static boolean arePatternsEqual(List<int[]> l1, List<int[]> l2){
        if (l1.size() != l2.size()) return false;
        for (int i = 0; i < l1.size(); i++){
            int[] e1 = l1.get(i);
            int[] e2 = l2.get(i);
            if (e1[0] != e2[0] || e1[1] != e2[1]) return false;
        }
        return true;
    }

    /**
     * Calculates the time between each unlock.
     * @param timeAtClick A list of times in milliseconds for each click
     * @return The time between each click
     */
    private static double[] calculateTimeElapsed(ArrayList<Double> timeAtClick){
        double[] elapsedTimes = new double[timeAtClick.size()-1];
        for (int i = 0; i < timeAtClick.size() - 1; i++) {
            elapsedTimes[i] = timeAtClick.get(i + 1) - timeAtClick.get(i);
        }
        return elapsedTimes;
    }
}
