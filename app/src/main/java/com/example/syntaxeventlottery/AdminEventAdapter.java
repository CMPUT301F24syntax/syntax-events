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
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AdminEventAdapter extends ArrayAdapter<Event> {

    private Context context;
    private List<Event> eventList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public AdminEventAdapter(Context context, List<Event> eventList) {
        super(context, R.layout.admin_event_item, eventList);
        this.context = context;
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.admin_event_item, parent, false);
        }

        Event event = eventList.get(position);

        // Find and set views
        TextView eventName = convertView.findViewById(R.id.eventName);
        TextView eventDescription = convertView.findViewById(R.id.eventDescription);
        TextView eventStartDate = convertView.findViewById(R.id.eventStartDate);
        ImageView posterImage = convertView.findViewById(R.id.eventPoster);

        eventName.setText(event.getEventName());
        eventDescription.setText(event.getDescription());
        eventStartDate.setText("Start: " + dateFormat.format(event.getStartDate()));

        Glide.with(context).load(event.getPosterUrl()).into(posterImage);

        // Set click listener for item to show details
        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(context, com.example.syntaxeventlottery.AdminEventDetailActivity.class);
            intent.putExtra("eventID", event.getEventID());
            intent.putExtra("eventName", event.getEventName());
            intent.putExtra("description", event.getDescription());
            intent.putExtra("startDate", dateFormat.format(event.getStartDate()));
            intent.putExtra("endDate", dateFormat.format(event.getEndDate()));
            context.startActivity(intent);
        });

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

    private void deleteEvent(String eventID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
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
