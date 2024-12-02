package com.example.syntaxeventlottery;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying notifications in a RecyclerView.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private Context context;
    private List<Notification> notificationList;
    private FirebaseFirestore db;
    private String deviceId;

    /**
     * Constructor to initialize the adapter with a context.
     *
     * @param context The application context.
     */
    public NotificationAdapter(Context context) {
        this.context = context;
        this.notificationList = new ArrayList<>();
        this.db = FirebaseFirestore.getInstance();
        this.deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * Adds a notification to the list and updates the RecyclerView.
     *
     * @param notification The notification to be added.
     */
    public void addNotification(Notification notification) {
        notificationList.add(notification);
        notifyItemInserted(notificationList.size() - 1);
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

        holder.itemView.setOnClickListener(v -> {
            // Mark notification as read
            db.collection("notifications").document(notification.getId())
                    .update("read", true)
                    .addOnSuccessListener(aVoid -> {
                        // Remove the notification from the list
                        notificationList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, notificationList.size());

                        // Navigate to event details
                        Intent intent = new Intent(context, EventDetailActivity.class);
                        intent.putExtra("event_id", notification.getEventId());
                        context.startActivity(intent);
                    })
                    .addOnFailureListener(e -> Toast.makeText(context, "Failed to update notification", Toast.LENGTH_SHORT).show());
        });
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    /**
     * ViewHolder class for holding notification item views.
     */
    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView, timestampTextView;

        /**
         * Constructor to initialize the TextViews for the notification item.
         *
         * @param itemView The item view for a notification.
         */
        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            messageTextView = itemView.findViewById(R.id.notificationMessageTextView);
            timestampTextView = itemView.findViewById(R.id.notificationTimestampTextView);
        }
    }
}
