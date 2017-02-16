package io.alstonlin.thelearninglock.lockscreen;

import android.app.Notification;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import java.util.ArrayList;

public class LockScreenNotificationsAdapter extends ArrayAdapter<Notification>{
    private Context context;
    private NotificationSelectListener selectListener;
    private ArrayList<ViewGroup> createdViews;

    public LockScreenNotificationsAdapter(Context context, NotificationSelectListener selectListener){
        super(context, 0);
        this.context = context;
        this.createdViews = new ArrayList<>();
        this.selectListener = selectListener;
    }

    /**
     * Sets and updates the notifications this shows on the attached ListView.
     * @param notifications The new notifications to show
     */
    public void setNotifications(ArrayList<Notification> notifications){
        onDestroy();
        clear();
        addAll(notifications);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView = new LinearLayout(context);
            createdViews.add((ViewGroup) convertView);
        }
        // TODO: Make a style for the Notification and create it with it for consistency
        final Notification notification = getItem(i);
        View view = notification.contentView.apply(context, parent);
        view.setBackgroundColor(Color.parseColor("#88FFFFFF"));
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

    public void onDestroy(){
        for (ViewGroup v : createdViews){
            // Removes all onClickListeners to prevent leaking by context
            for (int i = 0; i < v.getChildCount(); i++){
                v.getChildAt(i).setOnClickListener(null);
            }
            v.removeAllViews();
        }
        createdViews.clear();
    }

}