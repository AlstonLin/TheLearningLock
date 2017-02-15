package io.alstonlin.thelearninglock.lockscreen;

import android.app.Notification;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.crashlytics.android.Crashlytics;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

public class LockScreenNotificationsAdapter extends ArrayAdapter<Notification>{
    private static Class NotificationHeaderView;
    private static Class DatetimeView;
    private static Method onDetachedFromWindow;

    static {
        try {
            NotificationHeaderView = Class.forName("android.view.NotificationHeaderView");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try {
            DatetimeView = Class.forName("android.widget.DateTimeView");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        try {
            onDetachedFromWindow = DatetimeView.getDeclaredMethod("onDetachedFromWindow");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    private Context context;
    private NotificationSelectListener selectListener;
    private HashMap<Integer, ViewGroup> inflatedViews;

    public LockScreenNotificationsAdapter(Context context, NotificationSelectListener selectListener){
        super(context, 0);
        this.context = context;
        this.inflatedViews = new HashMap<>();
        this.selectListener = selectListener;
    }

    /**
     * Sets and updates the notifications this shows on the attached ListView.
     * @param notifications The new notifications to show
     */
    public void setNotifications(ArrayList<Notification> notifications){
        clear();
        addAll(notifications);
        try {
            onDestroy();
        } catch (IllegalAccessException e) {
            Crashlytics.logException(e);
        }
        inflatedViews.clear();
        notifyDataSetChanged();
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView = new LinearLayout(context);
        }
        if (!inflatedViews.containsKey(i)) {
            final Notification notification = getItem(i);
            View view = notification.contentView.apply(context, parent);
            inflatedViews.put(i, (ViewGroup) view);
            view.setBackgroundColor(Color.parseColor("#88FFFFFF"));
            // TODO: Add a possibility to swiper to delete it?
            // Maybe use https://github.com/hudomju/android-swipe-to-dismiss-undo
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectListener.onNotificationSelected(notification);
                }
            });
        }
        // Replaces the notification in the wrapper LinearLayout
        View v = inflatedViews.get(i);
        if (v.getParent() != null){
            ((ViewGroup) v.getParent()).removeView(v);
        }
        ((LinearLayout)convertView).addView(v);
        return convertView;
    }


    // NOTE: I am using hidden and internal classes here. This method is VERY VERY VERY FRAGILE
    // TODO: This is very shitty code. But due to a bug an Android, it's either this or a massive memory leak
    // TODO: I choose shitty code > memory leak. When possible, this code needs to be replace with code that should actually be used
    // See https://github.com/AlstonLin/TheLearningLock/issues/41
    // The Android bug that makes this code needed is this: https://code.google.com/p/android/issues/detail?id=187847
    public void onDestroy() throws IllegalAccessException {
        if (NotificationHeaderView == null || DatetimeView == null || onDetachedFromWindow == null){
            // If code reaches here, then this app is in a very very bad spot
            // It means that the code breaks for some API levels that we support and a memory leak happens
            Log.w(this.getClass().getName(), "Error loading hidden classes and methods! Can either be an older version of Android or a memory is going to occur.");
            return;
        }
        /*
         * Explanation for this shitty code / Massive memory leak post mortem
         *
         * The memory leak comes from displaying notifications in a ListView. So for a Notification,
         * Notification.NotificationHeaderView.DatetimeView (eg. shows a notification is 4h old)
         * registers to a static MessageHandler on the Android Looper when it is inflated, and
         * unregisters it when onDetachedFromWindow() is called.
         *
         * However, because the Notifications are being displayed in a ListView, which explicitly
         * never calls onDetachedFromWindow() for it's children, that method never gets called for
         * any notification. The result is that there is a static reference from the Android Looper
         * to every single Notification that passes through this adapter, which means that the GC
         * doesn't collect them and remain in the heap indefinitely (or at least the service slowly
         * runs out of memory, and a OOM occurs). Even worse, because this code has a bunch of
         * anonymous inner classes, the entire LockScreen joins the memory leak through context.
         *
         * The very hacky solution is to manually call the DatetimeView's onDetachedFromWindow()
         * method manually. A slight complication is that NotificationHeaderView and DatetimeView
         * are both hidden in the Android's API, probably because they don't want people to touch it.
         * Since there's no other option, these are imported with the reflection API.
         *
         * Here's the VERY hacky part - onDetachedFromWindow() is a protected method in a different
         * package. This means that it really shouldn't be able to be called from here because
         * information hiding / encapsulation / OOP / the entire concept of Java.
         * Luckily, the reflection API has a method that makes that method essentially public
         * so that I can invoke it. I really don't understand why this doesn't break Android's
         * security.
         *
         * @author (of this literal hack) Alston Lin
         */
        for (ViewGroup fl : inflatedViews.values()){
            if (fl instanceof FrameLayout) {
                for (int i = 0; i < fl.getChildCount(); i++) {
                    View header = fl.getChildAt(i);
                    if (NotificationHeaderView.isInstance(header)){ // header instanceof NotificationHeaderView
                        for (int j = 0; j < ((ViewGroup)header).getChildCount(); j++){
                            View datetime = ((ViewGroup)header).getChildAt(j);
                            if (DatetimeView.isInstance(datetime)){ // datetime instanceof DatetimeView
                                onDetachedFromWindow.setAccessible(true);
                                try {
                                    onDetachedFromWindow.invoke(datetime);
                                } catch (Exception ignore){
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}