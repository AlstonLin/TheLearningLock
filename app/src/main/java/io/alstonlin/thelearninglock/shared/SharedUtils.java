package io.alstonlin.thelearninglock.shared;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Common helper methods that are used by multiple parts of the app
 */
public class SharedUtils {
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

    /**
     * Loads an arbitrary Object from the given filename safely.
     * @param context The context this file is being opened in
     * @param fileName The filename to load
     * @return The Object loaded, or null if an error occurred while reading.
     */
    public static Object loadObjectFromFile(Context context, String fileName){
        Object obj = null;
        FileInputStream fis = null;
        ObjectInputStream is = null;
        try {
            fis = context.openFileInput(fileName);
            is = new ObjectInputStream(fis);
            obj = is.readObject();
        } catch(IOException | ClassNotFoundException e){
            e.printStackTrace();
            Toast.makeText(context, "An error has occurred while loading a file!", Toast.LENGTH_LONG).show();
        } finally {
            try {
                if (is != null) is.close();
                if (fis != null) fis.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        return obj;
    }
}
