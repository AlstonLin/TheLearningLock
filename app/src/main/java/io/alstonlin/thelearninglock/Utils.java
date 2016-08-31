package io.alstonlin.thelearninglock;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.View;

import java.io.File;

/**
 * Common helper methods that are used by multiple parts of the app
 */
public class Utils {
    /**
     * Sets up the background that the user selected for the given View.
     * @param context The context that the Background path will be retrieved from
     * @param view The View that will have the background changed
     */
    public static void setupBackground(Context context, View view){
        String backgroundPath = PreferenceManager.getDefaultSharedPreferences(context).getString(Const.BACKGROUND_URI_KEY, null);
        if (backgroundPath == null){
            // TODO: A better default background
            view.setBackgroundColor(Color.BLUE);
        } else {
            File file = new File(backgroundPath);
            view.setBackground(Drawable.createFromPath(file.getAbsolutePath()));
        }
    }
}
