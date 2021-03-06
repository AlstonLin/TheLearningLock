package io.alstonlin.thelearninglock.lockscreen;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import io.alstonlin.thelearninglock.R;
import io.alstonlin.thelearninglock.pin.PINUtils;
import io.alstonlin.thelearninglock.shared.SharedUtils;

/**
 * Contains utility functions that is used by LockScreenService.
 */
public class LockUtils {
    private static final WindowManager.LayoutParams LOCK_PARAMS, BG_PARAMS;

    static {
        LOCK_PARAMS = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSPARENT);
        LOCK_PARAMS.gravity = Gravity.TOP | Gravity.LEFT;
        LOCK_PARAMS.screenOrientation = Configuration.ORIENTATION_PORTRAIT;
        BG_PARAMS = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.OPAQUE);
        BG_PARAMS.gravity = Gravity.TOP | Gravity.LEFT;
        BG_PARAMS.screenOrientation = Configuration.ORIENTATION_PORTRAIT;
    }

    /**
     * Has the effect of "unlocking" the screen.
     *
     * @param context The context of the app
     * @param attach  The View that is attached as the lock screen
     */
    public static void unlock(Context context, View attach, View background) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        try {
            if (attach != null) windowManager.removeView(attach);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        try {
            if (background != null) windowManager.removeView(background);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    /**
     * Performs the effect of "locking" the screen.
     *
     * @param context    The context of the app
     * @param attach     The View to attach as the lock screen
     * @param background The view of the separate background layout
     */
    public static void lock(Context context, View attach, View background) throws WindowManager.BadTokenException {
        // Sets up the window manage parameters for the View to add\
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        SharedUtils.setupBackground(context, background);
        // Adds the view to the Window Manager
        if (attach.getWindowToken() == null) { // If not already attached
            windowManager.addView(background, BG_PARAMS);
            windowManager.addView(attach, LOCK_PARAMS);
        }
        setVisibleScreen((ViewGroup) attach, R.id.lock_screen);
    }

    // Quick way of setting which screen is the one visible, and sets all the others to GONE
    public static void setVisibleScreen(ViewGroup lockContainer, int visibleId) {
        for (int i = 0; i < lockContainer.getChildCount(); i++) {
            View screen = lockContainer.getChildAt(i);
            screen.setVisibility(screen.getId() == visibleId ? View.VISIBLE : View.GONE);
        }
        // Clears pin screen every time
        PINUtils.clearPIN(lockContainer.findViewById(R.id.pin_screen));
    }

}
