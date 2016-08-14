package io.alstonlin.thelearninglock.lockscreen;

import android.app.Notification;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

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
        final Notification notification = getItem(i);
        View view = notification.contentView.apply(context, parent);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectListener.onNotificationSelected(notification);
            }
        });
        return view;
    }
}