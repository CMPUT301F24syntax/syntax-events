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

public class WaitingListAdapter extends RecyclerView.Adapter<WaitingListAdapter.ViewHolder> {


    public interface OnClickListener {
        void onClick(int position);
    }

    private List<User> waitingList;
    private OnClickListener cancelEntrantClickListener;
    private String currentListType;


    public WaitingListAdapter(List<User> waitingList, OnClickListener listener, String listType) {
        this.waitingList = waitingList;
        this.cancelEntrantClickListener = listener;
        this.currentListType = listType;
    }

    public void setCurrentListType(String listType) {
        this.currentListType = listType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.waitling_list_item, parent, false); // Corrected filename
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = waitingList.get(position);
        holder.usernameTextView.setText("Username: " + user.getUsername());
        holder.phoneNumberTextView.setText("Phone: " + user.getPhoneNumber());
        holder.emailTextView.setText("Email: " + user.getEmail());

        // load user image if possible
        if (user.getProfilePhotoUrl() != null) {
            Glide.with(holder.itemView.getContext())
                    .load(user.getProfilePhotoUrl())
                    .into(holder.userProfileImageView);
        }
        // set up cancel entrant button
        // Conditionally show/hide cancel button
        if (currentListType.equals("Waiting List") || currentListType.equals("Selected Participants")) {
            holder.cancelEntrantButton.setVisibility(View.VISIBLE); // Ensure it's visible
            holder.cancelEntrantButton.setOnClickListener(v -> {
                if (cancelEntrantClickListener != null) {
                    cancelEntrantClickListener.onClick(holder.getBindingAdapterPosition());
                }
            });
        } else {
            holder.cancelEntrantButton.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return waitingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView usernameTextView, phoneNumberTextView, emailTextView;
        public ImageView userProfileImageView;
        public ImageButton cancelEntrantButton;

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
