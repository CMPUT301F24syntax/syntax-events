package com.example.syntaxeventlottery;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying notifications in a RecyclerView.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<Notification> notificationList;
    private OnItemClickListener listener;

    public NotificationAdapter(Context context) {
        this.context = context;
        this.notificationList = new ArrayList<>();
    }

    /**
     * Sets the list of notifications and notifies the adapter.
     *
     * @param notifications List of notifications to display.
     */
    public void setNotifications(List<Notification> notifications) {
        this.notificationList = notifications;
        notifyDataSetChanged(); // Notify the adapter of data changes
    }

    /**
     * Removes a notification by its ID and notifies the adapter.
     *
     * @param notificationId The ID of the notification to remove.
     */
    public void removeNotificationById(String notificationId) {
        for (int i = 0; i < notificationList.size(); i++) {
            if (notificationList.get(i).getId().equals(notificationId)) {
                notificationList.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    /**
     * Adds a notification back to the list and notifies the adapter.
     *
     * @param notification The notification to add.
     */
    public void addNotification(Notification notification) {
        notificationList.add(0, notification); // Add to the top of the list
        notifyItemInserted(0);
        if (context instanceof NotificationCenterActivity) {
            ((NotificationCenterActivity) context).notificationRecyclerView.scrollToPosition(0);
        }
    }

    /**
     * Sets the item click listener.
     *
     * @param listener Listener to handle item clicks.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notification_item, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        holder.messageTextView.setText(notification.getMessage());
        holder.timestampTextView.setText(notification.getFormattedTimestamp());

        // Optional: Change text color based on notification type
        if (notification.getMessage().startsWith("🎉")) {
            holder.messageTextView.setTextColor(context.getResources().getColor(R.color.colorWin));
        } else if (notification.getMessage().startsWith("😞")) {
            holder.messageTextView.setTextColor(context.getResources().getColor(R.color.colorLose));
        } else {
            holder.messageTextView.setTextColor(Color.BLACK);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(notification);
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    /**
     * ViewHolder class for notifications.
     */
    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView, timestampTextView;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.notificationMessageTextView);
            timestampTextView = itemView.findViewById(R.id.notificationTimestampTextView);
        }
    }

    /**
     * Interface for handling item clicks.
     */
    public interface OnItemClickListener {
        void onItemClick(Notification notification);
    }
}