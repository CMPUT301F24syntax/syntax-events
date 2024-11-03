package com.example.syntaxeventlottery;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventList;  // List of events to display
    private Context context;

    // Constructor to initialize event list and context
    public EventAdapter(List<Event> eventList, Context context) {
        this.eventList = eventList;
        this.context = context;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout for each event in the list
        View view = LayoutInflater.from(context).inflate(R.layout.event_list_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        // Get the current event from the list
        Event event = eventList.get(position);

        // Set the event name in the TextView
        holder.eventNameTextView.setText(event.getEventName());

        // Load event poster using Glide
        Glide.with(context)
                .load(event.getPosterUrl()) // Load poster URL from event object
                .into(holder.eventPosterImageView);

        // Set click listener for each event item to open EventDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventDetailActivity.class);
            intent.putExtra("event_id", event.getEventID()); // Pass the event ID to the detail activity
            intent.putExtra("poster_url", event.getPosterUrl()); // Pass poster URL
            intent.putExtra("qr_url", event.getQrCodeUrl()); // Pass QR code URL
            intent.putExtra("event_name", event.getEventName());
            intent.putExtra("event_start_date", event.getStartDate().toString());
            intent.putExtra("event_end_date", event.getEndDate().toString());
            intent.putExtra("event_facility", event.getFacility());
            intent.putExtra("event_capacity", event.getCapacity());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        // Return the number of events in the list
        return eventList.size();
    }

    // ViewHolder class to hold references to the UI components for each item in the list
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        ImageView eventPosterImageView;
        TextView eventNameTextView;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize the ImageView and TextView for event poster and name
            eventPosterImageView = itemView.findViewById(R.id.eventPosterImageView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
        }
    }
}
