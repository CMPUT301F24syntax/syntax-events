package com.example.syntaxeventlottery;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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

    private final Context context;
    private final List<User> userList;
    private final FirebaseFirestore db;

    public AdminUserAdapter(Context context, List<User> userList) {
        super(context, R.layout.admin_user_item, userList);
        this.context = context;
        this.userList = userList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.admin_user_item, parent, false);
        }

        // Get the current user based on position
        User user = userList.get(position);

        // Find and set views
        TextView userName = convertView.findViewById(R.id.userName);
        ImageView userImage = convertView.findViewById(R.id.userImage);
        Button deleteButton = convertView.findViewById(R.id.deleteButton);

        userName.setText(user.getUsername());

        // Load user profile image using Glide
        Glide.with(context).load(user.getProfilePhotoUrl()).into(userImage);

        // Set click listener to fetch and show event details from the database
        convertView.setOnClickListener(v -> fetchUserDetailsAndShow(user.getUserID()));

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

    private void fetchUserDetailsAndShow(String userID) {
        db.collection("Users").document(userID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Prepare intent to start AdminEventDetailActivity
                        Intent intent = new Intent(context, AdminUserDetailActivity.class);
                        intent.putExtra("userID", userID);
                        intent.putExtra("username", documentSnapshot.getString("username"));
                        intent.putExtra("email", documentSnapshot.getString("email"));
                        intent.putExtra("facility", documentSnapshot.getString("facility"));
                        intent.putExtra("phoneNumber", documentSnapshot.getString("phoneNumber"));
                        intent.putExtra("profilePhotoUrl", documentSnapshot.getString("profilePhotoUrl"));
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "User not found in the database.", Toast.LENGTH_SHORT).show();
                        Log.d("AdminUserAdapter","user ID is :"+userID);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to load user details", Toast.LENGTH_SHORT).show());

    }

    private void deleteUserProfile(String userID) {
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