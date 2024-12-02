package com.example.syntaxeventlottery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Adapter for managing and displaying the waiting list in a RecyclerView.
 * This adapter is used to display user information in different participant lists (e.g., waiting list, selected participants).
 */
public class WaitingListAdapter extends RecyclerView.Adapter<WaitingListAdapter.ViewHolder> {

    private List<User> waitingList;
    private OnClickListener cancelEntrantClickListener;
    private String currentListType;

    /**
     * Interface for handling click events on the cancel button.
     */
    public interface OnClickListener {
        /**
         * Called when the cancel button is clicked.
         *
         * @param position The position of the clicked item in the list.
         */
        void onClick(int position);
    }

    /**
     * Constructor for WaitingListAdapter.
     *
     * @param waitingList The list of users to display.
     * @param listener    The click listener for the cancel button.
     * @param listType    The type of the current list (e.g., "Waiting List", "Selected Participants").
     */
    public WaitingListAdapter(List<User> waitingList, OnClickListener listener, String listType) {
        this.waitingList = waitingList;
        this.cancelEntrantClickListener = listener;
        this.currentListType = listType;
    }

    /**
     * Updates the current list type.
     *
     * @param listType The new list type (e.g., "Waiting List", "Selected Participants").
     */
    public void setCurrentListType(String listType) {
        this.currentListType = listType;
    }

    /**
     * Inflates the layout for each item in the RecyclerView.
     *
     * @param parent   The parent ViewGroup into which the new View will be added.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder for the inflated item layout.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.waitling_list_item, parent, false); // Corrected filename
        return new ViewHolder(view);
    }

    /**
     * Binds data to the views in the ViewHolder for a given position.
     *
     * @param holder   The ViewHolder to bind data to.
     * @param position The position of the item in the list.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = waitingList.get(position);
        holder.usernameTextView.setText("Username: " + user.getUsername());
        holder.phoneNumberTextView.setText("Phone: " + user.getPhoneNumber());
        holder.emailTextView.setText("Email: " + user.getEmail());

        // Load user profile image using Glide
        if (user.getProfilePhotoUrl() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(user.getProfilePhotoUrl())
                    .into(holder.userProfileImageView);
        }

        // Set up the cancel entrant button
        if (currentListType.equals("Waiting List") || currentListType.equals("Selected Participants")) {
            holder.cancelEntrantButton.setVisibility(View.VISIBLE);
            holder.cancelEntrantButton.setOnClickListener(v -> {
                if (cancelEntrantClickListener != null) {
                    cancelEntrantClickListener.onClick(holder.getBindingAdapterPosition());
                }
            });
        } else {
            holder.cancelEntrantButton.setVisibility(View.GONE);
        }
    }

    /**
     * Returns the total number of items in the waiting list.
     *
     * @return The size of the waiting list.
     */
    @Override
    public int getItemCount() {
        return waitingList.size();
    }

    /**
     * ViewHolder for holding views of an individual item in the waiting list.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView usernameTextView, phoneNumberTextView, emailTextView;
        public ImageView userProfileImageView;
        public ImageButton cancelEntrantButton;

        /**
         * Constructor for the ViewHolder.
         *
         * @param itemView The root view of the item layout.
         */
        public ViewHolder(View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            phoneNumberTextView = itemView.findViewById(R.id.phoneNumberTextView);
            emailTextView = itemView.findViewById(R.id.emailTextView);
            userProfileImageView = itemView.findViewById(R.id.userProfileImageView);
            cancelEntrantButton = itemView.findViewById(R.id.cancelEntrantButton);
        }
    }
}
