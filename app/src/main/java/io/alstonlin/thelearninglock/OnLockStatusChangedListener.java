package io.alstonlin.thelearninglock;

/**
 * A listener that listens for Lock Status changes.
 */
public interface OnLockStatusChangedListener {
    void onLockStatusChanged(boolean isLocked);
}