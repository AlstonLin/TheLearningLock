package io.alstonlin.thelearninglock.pin;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import io.alstonlin.thelearninglock.R;
import io.alstonlin.thelearninglock.shared.SharedUtils;

/**
 * This class provides helper methods that has to do with the PIN and it's view
 */
public class PINUtils {

    private enum KeypadButton {
        ONE(R.id.pin_view_button_one, '1'),
        TWO(R.id.pin_view_button_two, '2'),
        THREE(R.id.pin_view_button_three, '3'),
        FOUR(R.id.pin_view_button_four, '4'),
        FIVE(R.id.pin_view_button_five, '5'),
        SIX(R.id.pin_view_button_six, '6'),
        SEVEN(R.id.pin_view_button_seven, '7'),
        EIGHT(R.id.pin_view_button_eight, '8'),
        NINE(R.id.pin_view_button_nine, '9'),
        ZERO(R.id.pin_view_button_zero, '0');

        private int id;
        private char character;
        KeypadButton(int id, char character){
            this.id = id;
            this.character = character;
        }
    }

    /**
     * Sets up listeners for all the buttons for the PIN layout.
     * @param context The context this View is being set up in
     * @param PINView The View for the layout itself
     * @param listener A listener that gets called when the user selected a PIN
     * @param title The title of the PIN View
     */
    public static void setupPINView(Context context, View PINView, final OnPINSelectListener listener, String title){
        final StringBuilder PINBuilder = new StringBuilder();
        final TextView tv = (TextView) PINView.findViewById(R.id.pin_view_display);
        tv.setText("");
        // Listeners for the keypad buttons
        for (final KeypadButton b : KeypadButton.values()) {
            PINView.findViewById(b.id).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PINBuilder.append(b.character);
                    String str = new String(new char[PINBuilder.length()]).replace('\0','*');
                    tv.setText(str);
                }
            });
        }
        Button backspaceButton = (Button) PINView.findViewById(R.id.pin_view_button_backspace);
        backspaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PINBuilder.length() > 0) {
                    PINBuilder.deleteCharAt(PINBuilder.length() - 1);
                    String str = new String(new char[PINBuilder.length()]).replace('\0','*');
                    tv.setText(str);
                }
            }
        });
        Button doneButton = (Button) PINView.findViewById(R.id.pin_view_button_done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onPINSelected(PINBuilder.toString());
            }
        });
        setPINTitle(PINView, title);
        // Sets up background
        SharedUtils.setupBackground(context, PINView);
    }

    public static void setPINTitle(View PINView, String newTitle){
        TextView titleView = (TextView) PINView.findViewById(R.id.pin_view_title);
        titleView.setText(newTitle);
    }
}
