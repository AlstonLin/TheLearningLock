package io.alstonlin.thelearninglock;

/**
 * Contains any hardcoded constants for the app that is shared between files
 */
public class Const {
    // Flag names
    public static final String SETUP_FLAG = "SETUPFLAG";
    // File names
    public static final String PATTERN_FILENAME = "pattern.dat";
    public static final String PASSCODE_FILENAME = "passcode.dat";
    public static final String ML_FILENAME = "learning_lock_saved.eg";
    // The number of patterns that will be requested at the beginning
    public static final int STARTING_TRAINING_SIZE = 10;
    // The percentage of the training data that will be used to calculate what epsilon should be.
    public static final float CROSS_VALIDATION_FACTOR = 0.3f;
    // The reasoning for this would be that if the user's behavior change, this will start
    // invalidating their old behavior. This is prevents it to become impossible to change it
    public static final int MAX_TRAINING_SIZE = 100;
}
