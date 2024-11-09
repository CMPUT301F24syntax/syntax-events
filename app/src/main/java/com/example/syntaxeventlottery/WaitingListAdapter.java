package com.example.syntaxeventlottery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WaitingListAdapter extends RecyclerView.Adapter<WaitingListAdapter.ViewHolder> {

    private List<User> waitingList;

    public WaitingListAdapter(List<User> waitingList) {
        this.waitingList = waitingList;
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
    }

    @Override
    public int getItemCount() {
        return waitingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView usernameTextView, phoneNumberTextView, emailTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            phoneNumberTextView = itemView.findViewById(R.id.phoneNumberTextView);
            emailTextView = itemView.findViewById(R.id.emailTextView);
        }
    }
}
