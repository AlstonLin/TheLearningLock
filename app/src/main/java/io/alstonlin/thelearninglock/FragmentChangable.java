package io.alstonlin.thelearninglock;

import android.support.v4.app.Fragment;

/**
 * Interface to allow an Activity's current Fragment to be changed by a Fragment
 */
public interface FragmentChangable {
    public void changeFragment(Fragment fragment);
}
