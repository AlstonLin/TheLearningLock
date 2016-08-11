package io.alstonlin.thelearninglock.lockscreen;

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
public class LockUtils {
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
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.OPAQUE);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;
        params.screenOrientation = Configuration.ORIENTATION_PORTRAIT;
        // Adds the view to the Window Manager
        if (attach.getWindowToken() == null) { // If not already attached
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            windowManager.addView(attach, params);
        }
    }

}
