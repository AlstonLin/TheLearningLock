package io.alstonlin.thelearninglock.lockscreen;

import android.app.Notification;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.crashlytics.android.Crashlytics;

import java.lang.reflect.Method;
import java.util.ArrayList;

public class LockScreenNotificationsAdapter extends ArrayAdapter<Notification>{
    private Context context;
    private NotificationSelectListener selectListener;

    public LockScreenNotificationsAdapter(Context context, NotificationSelectListener selectListener){
        super(context, 0);
        this.context = context;
        this.selectListener = selectListener;
    }

    /**
     * Sets and updates the notifications this shows on the attached ListView.
     * @param notifications The new notifications to show
     */
    public void setNotifications(ArrayList<Notification> notifications){
        clear();
        addAll(notifications);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView = new LinearLayout(context);
        }
        final Notification notification = getItem(i);
        View view = notification.contentView.apply(context, parent);
        Drawable background = view.getBackground();
        // Adds a white translucent background if there is none
        if (background == null){
            view.setBackgroundColor(Color.parseColor("#88FFFFFF"));
        }
        // TODO: Add a possibility to swiper to delete it?
        // Maybe use https://github.com/hudomju/android-swipe-to-dismiss-undo
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectListener.onNotificationSelected(notification);
            }
        });
        // Replaces the notification in the wrapper LinearLayout
        ((LinearLayout)convertView).removeAllViews();
        ((LinearLayout)convertView).addView(view);
        return convertView;
    }
}