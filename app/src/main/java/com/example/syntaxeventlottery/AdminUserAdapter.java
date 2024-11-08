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

import java.util.List;

public class AdminUserAdapter extends ArrayAdapter<User> {

    private Context context;
    private List<User> userList;

    public AdminUserAdapter(Context context, List<User> userList) {
        super(context, R.layout.admin_user_item, userList);
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.admin_user_item, parent, false);
        }

        User user = userList.get(position);

        // Find and set views
        TextView userName = convertView.findViewById(R.id.userName);
        ImageView userImage = convertView.findViewById(R.id.userImage);
        Button deleteButton = convertView.findViewById(R.id.deleteButton);

        userName.setText(user.getUsername());

        // Load user profile image using Glide
        Glide.with(context)
                .load(user.getProfilePhotoUrl())
                .placeholder(R.drawable.placeholder) // Optional placeholder
                .error(R.drawable.error)             // Optional error image
                .into(userImage);

        // Set click listener to view user details
        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(context, com.example.syntaxeventlottery.AdminUserDetailActivity.class);
            intent.putExtra("userID", user.getUserID());
            intent.putExtra("username", user.getUsername());
            intent.putExtra("email", user.getEmail());
            intent.putExtra("phoneNumber", user.getPhoneNumber());
            intent.putExtra("profilePhotoUrl", user.getProfilePhotoUrl());
            context.startActivity(intent);
        });

        // Delete button functionality
        deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Profile")
                    .setMessage("Are you sure you want to delete this profile?")
                    .setPositiveButton("Yes", (dialog, which) -> deleteUserProfile(user.getUserID()))
                    .setNegativeButton("No", null)
                    .show();
        });

        return convertView;
    }

    private void deleteUserProfile(String userID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(userID)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Profile deleted", Toast.LENGTH_SHORT).show();
                    userList.removeIf(user -> user.getUserID().equals(userID));
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete profile", Toast.LENGTH_SHORT).show());
    }
}
