package io.alstonlin.thelearninglock.lockscreen;

import android.app.Notification;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class LockScreenNotificationsAdapter extends ArrayAdapter<Notification>{
    private Context context;

    public LockScreenNotificationsAdapter(Context context){
        super(context, 0);
        this.context = context;
    }

    public void setNotifications(Notification[] notifications){
        clear();
        addAll(notifications);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        Notification notification = getItem(i);
        View view = notification.contentView.apply(context, parent);
        return view;
    }
}
