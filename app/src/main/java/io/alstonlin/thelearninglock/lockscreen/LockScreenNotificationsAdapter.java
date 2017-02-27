package io.alstonlin.thelearninglock.lockscreen;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;

import java.util.ArrayList;

import io.alstonlin.thelearninglock.R;

public class LockScreenNotificationsAdapter extends ArrayAdapter<LockScreenNotificationService.LockScreenNotification> {
    private Context context;
    private ArrayList<ViewGroup> createdViews;
    private Runnable undoPendingNotification;

    /**
     * The constructor
     *
     * @param context                 Context where this adapter is being created
     * @param undoPendingNotification A runnable that will undo the Pending notification (in the dismissTouchListener)
     */
    public LockScreenNotificationsAdapter(Context context, Runnable undoPendingNotification) {
        super(context, 0);
        this.context = context;
        this.createdViews = new ArrayList<>();
        this.undoPendingNotification = undoPendingNotification;
    }

    /**
     * Sets and updates the notifications this shows on the attached ListView.
     *
     * @param notifications The new notifications to show
     */
    public void setNotifications(ArrayList<LockScreenNotificationService.LockScreenNotification> notifications) {
        detachCreatedViews();
        clear();
        addAll(notifications);
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        // Also forces re-inflation and discards convertView if the convertView has been detached explicitly
        if (convertView == null || !createdViews.contains(convertView)) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.notification_list_item, parent, false);
            createdViews.add((ViewGroup) convertView);
        }
        // TODO: Make a style for the Notification and create it with it for consistency
        final LockScreenNotificationService.LockScreenNotification notification = getItem(i);
        View view = notification.getNotification().contentView.apply(context, parent);
        view.setBackgroundColor(Color.parseColor("#88FFFFFF"));
        // Sets up the view
        View undoText = convertView.findViewById(R.id.undo_text);
        undoText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undoPendingNotification.run();
            }
        });
        // Replaces the notification in the notification container
        FrameLayout container = (FrameLayout) convertView.findViewById(R.id.notification_container);
        container.removeAllViews();
        container.addView(view);
        // Refresh params to recalculate height
        convertView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
        return convertView;
    }

    /**
     * Fix for https://github.com/AlstonLin/TheLearningLock/issues/41
     */
    public void detachCreatedViews() {
        for (ViewGroup v : createdViews) {
            FrameLayout container = (FrameLayout) v.findViewById(R.id.notification_container);
            container.removeAllViews();
        }
        createdViews.clear();
    }

    public void dismissNotification(int position) {
        final LockScreenNotificationService.LockScreenNotification notification = getItem(position);
        detachCreatedViews();
        remove(notification);
        // Actually removes it once animation is finished
        new android.os.Handler().postDelayed(new Runnable() {
            public void run() {
                notification.cancel();
            }
        }, 200);
    }
}