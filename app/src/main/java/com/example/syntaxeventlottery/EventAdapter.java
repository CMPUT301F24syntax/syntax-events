package com.example.syntaxeventlottery;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code EventAdapter} class extends {@link RecyclerView.Adapter} to provide a custom adapter
 * for displaying event items in a {@link RecyclerView}. It binds event data to the views in each item.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    /** List of events to be displayed in the RecyclerView. */
    private List<Event> eventsList;

    /** Context from which the adapter is created, used for inflating layouts and starting activities. */
    private static Context context;

    /**
     * Constructs a new {@code EventAdapter}.
     *
     * @param eventsList List of {@link Event} objects to display.
     * @param context    The context in which the adapter is operating.
     */
    public EventAdapter(List<Event> eventsList, Context context) {
        this.context = context;
        this.eventsList = (eventsList != null) ? eventsList : new ArrayList<>();
    }

    /**
     * Called when RecyclerView needs a new {@link EventViewHolder} of the given type to represent an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new {@link EventViewHolder} that holds a View for each event item.
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method updates the contents of the {@link EventViewHolder#itemView} to reflect the item at the given position.
     *
     * @param holder   The {@link EventViewHolder} which should be updated to represent the contents of the item at the given position.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = eventsList.get(position);
        holder.eventNameTextView.setText(event.getEventName());

        // Check if posterUrl is null or empty and set the image accordingly
        if (event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
            Glide.with(context)
                    .load(event.getPosterUrl())
                    .into(holder.eventPosterImageView);
        } else {
            // Use ic_avatar_placeholder.png as the default image if posterUrl is null or empty
            Glide.with(context)
                    .load(R.drawable.ic_avatar_placeholder)
                    .into(holder.eventPosterImageView);
        }

        // Set click listener for each event item to open EventDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventDetailActivity.class);
            intent.putExtra("eventID", event.getEventID()); // Pass the event ID to the detail activity
            intent.putExtra("posterUrl", event.getPosterUrl()); // Pass poster URL
            intent.putExtra("qrCode", event.getQrCode()); // Pass QR code URL
            intent.putExtra("eventName", event.getEventName());
            intent.putExtra("description", event.getDescription()); // Pass description
            intent.putExtra("startDate", event.getStartDate().toString());
            intent.putExtra("endDate", event.getEndDate().toString());
            intent.putExtra("capacity", event.getCapacity());
            context.startActivity(intent);
        });
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of events.
     */
    @Override
    public int getItemCount() {
        return (eventsList != null) ? eventsList.size() : 0;
    }

    /**
     * Updates the entire list of events and refreshes the RecyclerView.
     *
     * @param newEvents A new list of {@link Event} objects to replace the current list.
     */
    public void updateEvents(List<Event> newEvents) {
        eventsList.clear();
        if (newEvents != null) {
            eventsList.addAll(newEvents);
        }
        notifyDataSetChanged();
    }


    /**
     * The {@code EventViewHolder} class holds references to the views for each event item.
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {
        /** TextView displaying the event name. */
        TextView eventNameTextView;

        /** ImageView displaying the event poster image. */
        ImageView eventPosterImageView;

        /**
         * Constructs a new {@code EventViewHolder}.
         *
         * @param itemView The view of the event item.
         */
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
            eventPosterImageView = itemView.findViewById(R.id.eventPosterImageView);

        }
    }
}


