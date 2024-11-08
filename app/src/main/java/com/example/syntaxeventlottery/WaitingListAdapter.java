package com.example.syntaxeventlottery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * The {@code WaitingListAdapter} class extends {@link RecyclerView.Adapter} to display a list of users
 * in a RecyclerView. Each item represents a user on the waiting list, showing their username,
 * phone number, and email address.
 */
public class WaitingListAdapter extends RecyclerView.Adapter<WaitingListAdapter.ViewHolder> {

    /** List of users in the waiting list to be displayed. */
    private List<User> waitingList;

    /**
     * Constructs a new {@code WaitingListAdapter} with the specified waiting list.
     *
     * @param waitingList The list of {@link User} objects to display.
     */
    public WaitingListAdapter(List<User> waitingList) {
        this.waitingList = waitingList;
    }

    /**
     * Called when RecyclerView needs a new {@link ViewHolder} to represent an item.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new {@link ViewHolder} that holds a View for each item.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.waitling_list_item, parent, false); // Corrected filename
        return new ViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     *
     * @param holder   The {@link ViewHolder} which should be updated to represent the contents of the item at the given position.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = waitingList.get(position);
        holder.usernameTextView.setText("Username: " + user.getUsername());
        holder.phoneNumberTextView.setText("Phone: " + user.getPhoneNumber());
        holder.emailTextView.setText("Email: " + user.getEmail());
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of users in the waiting list.
     */
    @Override
    public int getItemCount() {
        return waitingList.size();
    }

    /**
     * The {@code ViewHolder} class holds references to the views for each item in the RecyclerView.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        /** TextView displaying the user's username. */
        public TextView usernameTextView;

        /** TextView displaying the user's phone number. */
        public TextView phoneNumberTextView;

        /** TextView displaying the user's email address. */
        public TextView emailTextView;

        /**
         * Constructs a new {@code ViewHolder} and initializes the view references.
         *
         * @param itemView The View of the item.
         */
        public ViewHolder(View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            phoneNumberTextView = itemView.findViewById(R.id.phoneNumberTextView);
            emailTextView = itemView.findViewById(R.id.emailTextView);
        }
    }
}
