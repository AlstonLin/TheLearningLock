package io.alstonlin.thelearninglock.pin;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import io.alstonlin.thelearninglock.R;

/**
 * This class provides helper methods that has to do with the PIN and it's view
 */
public class PINUtils {

    /**
     * Sets up listeners for all the buttons for the PIN layout.
     *
     * @param PINView  The View for the layout itself
     * @param listener A listener that gets called when the user selected a PIN
     * @param title    The title of the PIN View
     */
    public static void setupPINView(View PINView, final OnPINSelectListener listener, String title) {
        final TextView tv = (TextView) PINView.findViewById(R.id.pin_view_display);
        tv.setText("");
        // Listeners for the keypad buttons
        for (final KeypadButton b : KeypadButton.values()) {
            PINView.findViewById(b.id).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CharSequence chars = tv.getText();
                    tv.setText(chars.toString() + b.character);
                }
            });
        }
        ImageButton backspaceButton = (ImageButton) PINView.findViewById(R.id.pin_view_button_backspace);
        backspaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence chars = tv.getText();
                if (chars.length() > 0) {
                    tv.setText(chars.subSequence(0, chars.length() - 1));
                }
            }
        });
        ImageButton doneButton = (ImageButton) PINView.findViewById(R.id.pin_view_button_done);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence chars = tv.getText();
                listener.onPINSelected(chars.toString());
            }
        });
        setPINTitle(PINView, title);
    }

    public static void clearPIN(View PINView) {
        final TextView tv = (TextView) PINView.findViewById(R.id.pin_view_display);
        tv.setText("");
    }

    public static void setPINTitle(View PINView, String newTitle) {
        TextView titleView = (TextView) PINView.findViewById(R.id.pin_view_title);
        titleView.setText(newTitle);
    }

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

        KeypadButton(int id, char character) {
            this.id = id;
            this.character = character;
        }
    }
}
