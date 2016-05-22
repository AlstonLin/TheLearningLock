package io.alstonlin.thelearninglock;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

import me.zhanghai.android.patternlock.PatternView;

/**
 * Contains utility functions that is used by LockScreenService.
 */
public class Util {
    /**
     * Has the effect of "unlocking" the screen.
     * @param context The context of the app
     * @param attach The View that is attached as the lock screen
     */
    public static void unlock(Context context, View attach){
        try {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (attach != null) windowManager.removeView(attach);
        } catch (IllegalArgumentException e){
            e.printStackTrace();
        }
    }

    /**
     * Performs the effect of "locking" the screen.
     *
     * @param context The context of the app
     * @param attach The View to attach as the lock screen
     */
    public static void lock(Context context, View attach){
        // Sets up the window manage parameters for the View to add
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.OPAQUE);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;
        params.screenOrientation = Configuration.ORIENTATION_PORTRAIT;
        // Adds the view to the Window Manager
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(attach, params);
    }

    /**
     * Calculates the time between each unlock.
     * @param timeAtClick A list of times in milliseconds for each click
     * @return The time between each click
     */
    public static double[] calculateTimeElapsed(ArrayList<Double> timeAtClick){
        double[] elapsedTimes = new double[timeAtClick.size()-1];
        for (int i = 0; i < timeAtClick.size() - 1; i++) {
            elapsedTimes[i] = timeAtClick.get(i + 1) - timeAtClick.get(i);
        }
        return elapsedTimes;
    }

    /**
     * Converts the list of Cells representing patterns to a list of int[2] with the same row/col info
     * @param pattern The pattern to convert
     * @return The converted list of int[2]
     */
    public static ArrayList<int[]> toList(List<PatternView.Cell> pattern){
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
    public static boolean equals(List<int[]> l1, List<int[]> l2){
        if (l1.size() != l2.size()) return false;
        for (int i = 0; i < l1.size(); i++){
            int[] e1 = l1.get(i);
            int[] e2 = l2.get(i);
            if (e1[0] != e2[0] || e1[1] != e2[1]) return false;
        }
        return true;
    }
}
