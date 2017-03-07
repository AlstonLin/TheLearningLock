package io.alstonlin.thelearninglock.shared;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Common helper methods that are used by multiple parts of the app
 */
public class SharedUtils {
    /**
     * Sets up the background that the user selected for the given View.
     *
     * @param context The context that the Background path will be retrieved from
     * @param view    The View that will have the background changed
     */
    public static void setupBackground(Context context, View view) {
        File dir = new File(Const.BACKGROUND_DIR);
        File file = new File(dir, Const.BACKGROUND_FILE);
        if (file.exists()){
            Bitmap bmp = BitmapFactory.decodeFile(file.getAbsolutePath());
            BitmapDrawable drawable = new BitmapDrawable(context.getResources(), bmp);
            drawable.setGravity(Gravity.CENTER);
            view.setBackground(drawable);
        } else{
            view.setBackgroundColor(Color.BLUE);
        }
    }

    /**
     * Sets up and stores the salt used for secure hashing
     *
     * @param context The context of this app
     */
    public static void setupSalt(Context context) {
        // Created the seed
        if (PreferenceManager.getDefaultSharedPreferences(context).getString(Const.SALT, null) == null) {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            SecureRandom rng = new SecureRandom();
            byte[] salt = rng.generateSeed(Const.NUM_SEED_BYTES);
            editor.putString(Const.SALT, Base64.encodeToString(salt, Base64.NO_WRAP));
            editor.commit();
        }
    }

    /**
     * Stores the given Serializable Object using a salted hash to the given file
     *
     * @param filename The file to store the hash from
     * @param context  The context this is being called from
     * @param obj      The object to store as bytes
     * @return If the store was successful
     */
    public static boolean storeObjectSecurely(String filename, Context context, Object obj) {
        if (!(obj instanceof Serializable)) {
            throw new IllegalArgumentException("Given object must be Serializable!");
        }
        // Loads the salt
        byte[] salt = Base64.decode(PreferenceManager.getDefaultSharedPreferences(context)
                .getString(Const.SALT, null), Base64.NO_WRAP);
        // Streams
        boolean success = true;
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        FileOutputStream fos = null;
        try {
            // Converts to bytes
            out = new ObjectOutputStream(byteStream);
            out.writeObject(obj);
            out.flush();
            byte[] bytes = byteStream.toByteArray();
            // Changes it to a salted hash
            PKCS5S2ParametersGenerator kdf = new PKCS5S2ParametersGenerator();
            kdf.init(bytes, salt, Const.NUM_HASH_ITERATIONS);
            byte[] hash = ((KeyParameter) kdf.generateDerivedMacParameters(8 * Const.NUM_HASH_BYTES)).getKey();
            // Stores hash to file
            fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
            fos.write(hash);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            success = false;
        } finally {
            try {
                byteStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (out != null) out.close();
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

    /**
     * Fetches the byte array representing the hash from the given file
     *
     * @param context   The context of the app
     * @param filename  The file name to retrieve the hash from
     * @param logIfFail If the function should log exception
     * @return If the hashes matches size
     */
    public static byte[] loadHashFromFiledFile(Context context, String filename, boolean logIfFail) {
        // Opens the file
        FileInputStream fis = null;
        try {
            // Gets bytes
            fis = context.openFileInput(filename);
            byte[] hash = new byte[(int) new File(context.getFilesDir() + "/" + filename).length()];
            fis.read(hash);
            return hash;
        } catch (IOException e) {
            if (logIfFail) {
                e.printStackTrace();
                Toast.makeText(context, "An error has occurred while loading a file!", Toast.LENGTH_LONG).show();
            }
        } finally {
            try {
                if (fis != null) fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Checks if the given object matches the hash
     *
     * @param context The context of the app
     * @param object  The Object to check
     * @param hash    The hash to check against
     * @return If the object matches the hash
     */
    public static boolean compareObjectToHash(Context context, Object object, byte[] hash) {
        if (!(object instanceof Serializable)) {
            throw new IllegalArgumentException("Given object must be Serializable!");
        }
        // Loads the salt
        byte[] salt = Base64.decode(PreferenceManager.getDefaultSharedPreferences(context)
                .getString(Const.SALT, null), Base64.NO_WRAP);
        // Streams
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        // Converts checking object to bytes
        try {
            out = new ObjectOutputStream(byteStream);
            out.writeObject(object);
            out.flush();
            byte[] bytes = byteStream.toByteArray();
            // Generates the hash to compare
            PKCS5S2ParametersGenerator kdf = new PKCS5S2ParametersGenerator();
            kdf.init(bytes, salt, Const.NUM_HASH_ITERATIONS);
            byte[] hashToCheck = ((KeyParameter) kdf.generateDerivedMacParameters(8 * Const.NUM_HASH_BYTES)).getKey();
            // Compares
            return Arrays.equals(hash, hashToCheck);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "An error has occurred while loading a file!", Toast.LENGTH_LONG).show();
            return false;
        } finally {
            try {
                if (out != null) out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                byteStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Bitmap getResizedDrawable(Resources res, int resId, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // Calculate ratios of height and width to requested height andw   idth
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }
}
