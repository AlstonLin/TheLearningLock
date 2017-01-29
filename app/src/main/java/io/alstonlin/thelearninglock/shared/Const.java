package io.alstonlin.thelearninglock.shared;

/**
 * Contains any hardcoded constants for the app that is shared between files
 */
public class Const {
    // Security
    public static final int NUM_SEED_BYTES = 32;
    public static final int NUM_HASH_BYTES = 32;
    public static final int NUM_HASH_ITERATIONS = 512;
    // Flag names
    public static final String SETUP_FLAG = "SETUPFLAG";
    public static final String ENABLED = "ENABLED";
    public static final String BACKGROUND_URI_KEY = "BACKGROUNDURI";
    public static final String SALT = "SALT";
    public static final String SAVED_RETRAIN_CONFIRM = "SAVEDRETRAINCONFIRM";
    // File names
    public static final String PATTERN_FILENAME = "pattern.dat";
    public static final String PASSCODE_FILENAME = "passcode.dat";
    public static final String ML_FILENAME = "learning_lock_saved.eg";
    // The number of patterns that will be requested at the beginning
    public static final int STARTING_TRAINING_SIZE = 10;
    public static final int CHANGE_FINGERS_MESSAGE_AT = 5;
    // The percentage of the training data that will be used to calculate what epsilon should be.
    public static final float CROSS_VALIDATION_FACTOR = 0.3f;
    // The reasoning for this would be that if the user's behavior change, this will start
    // invalidating their old behavior. This is prevents it to become impossible to change it
    public static final int MAX_TRAINING_SIZE = 100;
}
