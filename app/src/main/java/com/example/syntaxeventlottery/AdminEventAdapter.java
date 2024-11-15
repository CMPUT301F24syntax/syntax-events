package com.example.syntaxeventlottery;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * The {@code AdminEventAdapter} class extends {@link ArrayAdapter} to provide a custom adapter
 * for displaying event items in a list view for administrators.
 */
public class AdminEventAdapter extends ArrayAdapter<Event> {

    private Context context;
    private List<Event> eventList;
    private FirebaseFirestore db;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    /**
     * Constructs a new {@code AdminEventAdapter}.
     *
     * @param context   The current context.
     * @param eventList The list of events to display.
     */
    public AdminEventAdapter(Context context, List<Event> eventList) {
        super(context, R.layout.admin_event_item, eventList);
        this.context = context;
        this.eventList = eventList;
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Provides a view for an AdapterView.
     *
     * @param position    The position of the item within the adapter's data set.
     * @param convertView The old view to reuse, if possible.
     * @param parent      The parent that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position.
     */
    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Inflate the view if it's not already created
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.admin_event_item, parent, false);
        }

        // Get the current event
        Event event = eventList.get(position);

        // Find and set views
        TextView eventName = convertView.findViewById(R.id.eventName);
        TextView eventDescription = convertView.findViewById(R.id.eventDescription);
        TextView eventStartDate = convertView.findViewById(R.id.eventStartDate);
        ImageView posterImage = convertView.findViewById(R.id.eventPoster);

        eventName.setText(event.getEventName());
        eventDescription.setText(event.getDescription());
        eventStartDate.setText("Start: " + dateFormat.format(event.getStartDate()));

        // Load the event poster image using Glide
        Glide.with(context).load(event.getPosterUrl()).into(posterImage);

        // Set click listener to fetch and show event details from the database
        convertView.setOnClickListener(v -> fetchEventDetailsAndShow(event.getEventID()));

        // Delete button functionality
        Button deleteButton = convertView.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Event")
                    .setMessage("Are you sure you want to delete this event?")
                    .setPositiveButton("Yes", (dialog, which) -> deleteEvent(event.getEventID()))
                    .setNegativeButton("No", null)
                    .show();
        });

        return convertView;
    }

    /**
     * Fetches event details from the database and starts the AdminEventDetailActivity to display them.
     *
     * @param eventID The unique identifier of the event to fetch.
     */
    private void fetchEventDetailsAndShow(String eventID) {
        db.collection("events").document(eventID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Prepare intent to start AdminEventDetailActivity
                        Intent intent = new Intent(context, AdminEventDetailActivity.class);
                        intent.putExtra("eventID", eventID);
                        intent.putExtra("eventName", documentSnapshot.getString("eventName"));
                        intent.putExtra("description", documentSnapshot.getString("description"));
                        intent.putExtra("facility", documentSnapshot.getString("facility"));
                        intent.putExtra("capacity", documentSnapshot.getLong("capacity").intValue());
                        intent.putExtra("startDate", documentSnapshot.getDate("startDate"));
                        intent.putExtra("endDate", documentSnapshot.getDate("endDate"));
                        intent.putExtra("posterUrl", documentSnapshot.getString("posterUrl"));
                        intent.putExtra("qrCode", documentSnapshot.getString("qrCode"));
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "Event not found in the database.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to load event details", Toast.LENGTH_SHORT).show());
    }

    /**
     * Deletes an event from the database and updates the adapter.
     *
     * @param eventID The unique identifier of the event to delete.
     */
    private void deleteEvent(String eventID) {
        db.collection("events").document(eventID)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Event deleted", Toast.LENGTH_SHORT).show();
                    eventList.removeIf(event -> event.getEventID().equals(eventID));
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete event", Toast.LENGTH_SHORT).show());
    }
}
