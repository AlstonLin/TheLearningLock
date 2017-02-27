package io.alstonlin.thelearninglock.main;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import io.alstonlin.thelearninglock.R;

/**
 * The Fragment that is shown when the user opens the app for the first time.
 * Prompts the user to go through the Setup flow.
 */
public class WelcomeFragment extends Fragment {
    public static WelcomeFragment newInstance() {
        WelcomeFragment fragment = new WelcomeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome_page, container, false);
        return view;
    }
}
