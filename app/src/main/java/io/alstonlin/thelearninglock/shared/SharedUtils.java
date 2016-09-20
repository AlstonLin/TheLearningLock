package io.alstonlin.thelearninglock.shared;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Arrays;

import org.bouncycastle.crypto.generators.PKCS5S2ParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;

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
     * Sets up and stores the salt used for secure hashing
     * @param context The context of this app
     */
    public static void setupSalt(Context context){
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
     * @param filename The file to store the hash from
     * @param context The context this is being called from
     * @param obj The object to store as bytes
     * @return If the store was successful
     */
    public static boolean storeObjectSecurely(String filename, Context context, Object obj){
        if (!(obj instanceof Serializable)){
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
     * Compared the given Serializable object to the one stored in a salted hash from the given file
     * @param filename The file name to retrieve the hash from
     * @param context The context of the app
     * @param object The object that will have the hashes compared to
     * @return If the hashes matches size
     */
    // TODO: Find some way to cache this so this doesnt have to run every time (maybe just return the hash instead?)
    public static boolean compareToSecureObject(String filename, Context context, Object object) {
        if (!(object instanceof Serializable)){
            throw new IllegalArgumentException("Given object must be Serializable!");
        }
        // Loads the salt
        byte[] salt = Base64.decode(PreferenceManager.getDefaultSharedPreferences(context)
                .getString(Const.SALT, null), Base64.NO_WRAP);
        // Streams
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        FileInputStream fis = null;
        try {
            // Gets bytes
            fis = context.openFileInput(filename);
            byte[] hash = new byte[(int) new File(context.getFilesDir() + "/" + filename).length()];
            fis.read(hash);
            // Converts checking object to bytes
            out = new ObjectOutputStream(byteStream);
            out.writeObject(object);
            out.flush();
            byte[] bytes = byteStream.toByteArray();
            // Generates the hash to compare
            PKCS5S2ParametersGenerator kdf = new PKCS5S2ParametersGenerator();
            kdf.init(bytes, salt, Const.NUM_HASH_ITERATIONS);
            byte[] hashToCheck = ((KeyParameter) kdf.generateDerivedMacParameters(8 * Const.NUM_HASH_BYTES)).getKey();
            // Compares the hashes
            return Arrays.equals(hash, hashToCheck);
        } catch (IOException e){
            e.printStackTrace();
            Toast.makeText(context, "An error has occurred while loading a file!", Toast.LENGTH_LONG).show();
        } finally {
            try {
                if (fis != null) fis.close();
            } catch (IOException e){
                e.printStackTrace();
            }
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
        return false;
    }
}
