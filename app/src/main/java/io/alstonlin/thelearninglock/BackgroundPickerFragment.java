package io.alstonlin.thelearninglock;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * A Fragment where the user can select the Lock screen background
 */
public class BackgroundPickerFragment extends Fragment {

    /**
     * Factory method to create a new instance of this Fragment
     * @return A new instance of fragment BackgroundPickerFragment.
     */
    public static BackgroundPickerFragment newInstance() {
        BackgroundPickerFragment fragment = new BackgroundPickerFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_background_picker, container, false);
        Button select = (Button) view.findViewById(R.id.selectButton);
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickSelect();
            }
        });
        return view;
    }

    public void clickSelect(){
        // TODO: Find and select the actual picture that was selected
        ((FragmentChangable)getActivity()).changeFragment(PatternSetupFragment.newInstance());
    }
}
