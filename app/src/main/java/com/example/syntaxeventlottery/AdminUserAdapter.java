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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AdminUserAdapter extends ArrayAdapter<User> {

    private Context context;
    private List<User> userList;

    public AdminUserAdapter(Context context, List<User> userList) {
        super(context, R.layout.item_admin_user, userList);
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_admin_user, parent, false);
        }

        // Retrieve the current user from the list
        User user = userList.get(position);

        // Locate the views in the item layout
        TextView userName = convertView.findViewById(R.id.userName);
        ImageView userImage = convertView.findViewById(R.id.userImage);
        Button deleteButton = convertView.findViewById(R.id.deleteButton);

        // Set username
        if (user.getUsername() != null && !user.getUsername().isEmpty()) {
            userName.setText(user.getUsername());
        } else {
            userName.setText("Unknown User"); // Fallback if username is null
        }

        // Load profile photo using Glide, add a placeholder and error image for feedback
        if (user.getProfilePhotoUrl() != null && !user.getProfilePhotoUrl().isEmpty()) {
            Glide.with(context)
                    .load(user.getProfilePhotoUrl())
                    .placeholder(R.drawable.placeholder) // Placeholder while loading
                    .error(R.drawable.error)             // Error image if loading fails
                    .into(userImage);
        } else {
            userImage.setImageResource(R.drawable.placeholder); // Set a default placeholder if URL is null
        }

        // Click listener for detailed view
        convertView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AdminUserDetailActivity.class);
            intent.putExtra("userID", user.getUserID());
            intent.putExtra("username", user.getUsername());
            intent.putExtra("email", user.getEmail());
            intent.putExtra("phoneNumber", user.getPhoneNumber());
            intent.putExtra("profilePhotoUrl", user.getProfilePhotoUrl());
            context.startActivity(intent);
        });

        // Delete button with confirmation dialog
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

    // Method to delete a user profile from Firestore and update the list
    private void deleteUserProfile(String userID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(userID)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Profile deleted", Toast.LENGTH_SHORT).show();
                    // Remove the user from the list and notify adapter
                    userList.removeIf(user -> user.getUserID().equals(userID));
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to delete profile", Toast.LENGTH_SHORT).show());
    }
}
