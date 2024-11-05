package com.example.syntaxeventlottery;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.syntaxeventlottery.R;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> eventsList;
    private Context context;

    // 构造函数，接收 List<Event> 和 Context
    public EventAdapter(List<Event> eventsList, Context context) {
        this.eventsList = eventsList;
        this.context = context;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

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

        // Optional log to debug binding of each event
        Log.d("EventAdapter", "Binding event: " + event.getEventName());
    }

    @Override
    public int getItemCount() {
        return eventsList.size();
    }

    // Method to update the entire list
    public void updateEvents(List<Event> newEvents) {
        this.eventsList.clear();
        this.eventsList.addAll(newEvents);
        notifyDataSetChanged();
    }

    // Method to add a single event
    public void addEvent(Event event) {
        this.eventsList.add(event);
        notifyItemInserted(eventsList.size() - 1);
    }

    // Method to remove an event by position
    public void removeEvent(int position) {
        if (position >= 0 && position < eventsList.size()) {
            this.eventsList.remove(position);
            notifyItemRemoved(position);
        }
    }

    // ViewHolder class to hold views for each item
    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView eventNameTextView;
        ImageView eventPosterImageView;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            eventNameTextView = itemView.findViewById(R.id.eventNameTextView);
            eventPosterImageView = itemView.findViewById(R.id.eventPosterImageView);
        }
    }
}
