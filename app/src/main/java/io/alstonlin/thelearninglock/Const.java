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
    // Of those number of patterns, this many will be used for (very ghetto) cross-validation to
    // calculate an epsilon. The rest will be used to actually train
    public static final int NUM_VALIDATION_ENTRIES = 3;
}
