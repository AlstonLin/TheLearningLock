package io.alstonlin.thelearninglock.lockscreen;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.SeekBar;

/**
 * Slide to Unlocker for lock screen
 * Source: http://stackoverflow.com/questions/14910226/how-to-make-slide-to-unlock-button-in-android
 */

public class SlideButton extends SeekBar {
    private static final float HITBOX_DISPLAY_FRACTION = 0.35f;

    private int hitboxPadding;
    private Drawable thumb;
    private SlideButtonListener listener;

    public SlideButton(Context context, AttributeSet attrs) {
        super(context, attrs);        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point displaySize = new Point();
        windowManager.getDefaultDisplay().getRealSize(displaySize);
        hitboxPadding = (int)(HITBOX_DISPLAY_FRACTION * displaySize.x);
    }

    @Override
    public void setThumb(Drawable thumb) {
        super.setThumb(thumb);
        this.thumb = thumb;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            Rect hitbox = new Rect(thumb.getBounds());
            hitbox.left -= hitboxPadding;
            hitbox.right += hitboxPadding;
            if (hitbox.contains((int) event.getX(), (int) event.getY())) {
                super.onTouchEvent(event);
            } else
                return false;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (getProgress() > 80) {
                handleSlide();
            }
            setProgress(0);
        } else {
            super.onTouchEvent(event);
        }
        return true;
    }

    private void handleSlide() {
        listener.handleSlide();
    }

    public void setSlideButtonListener(SlideButtonListener listener) {
        this.listener = listener;
    }
}