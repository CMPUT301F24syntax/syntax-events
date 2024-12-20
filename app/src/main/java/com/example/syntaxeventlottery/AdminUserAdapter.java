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

/**
 * The {@code AdminUserAdapter} class extends {@link ArrayAdapter} to provide a custom adapter
 * for displaying and managing user profiles in a list view for administrators.
 */
public class AdminUserAdapter extends ArrayAdapter<User> {
    private final String TAG="AdminUserAdapter";

    private final Context context;
    private List<User> userList;
    private UserController userController;

    /**
     * Constructs a new {@code AdminUserAdapter}.
     *
     * @param context  The current context.
     * @param userList The list of users to display.
     */
    public AdminUserAdapter(Context context, List<User> userList) {
        super(context, R.layout.admin_user_item, userList);
        this.context = context;
        this.userList = userList;
    }

    /**
     * Provides a view for an AdapterView.
     *
     * @param position    The position of the item within the adapter's data set.
     * @param convertView The old view to reuse, if possible.
     * @param parent      The parent that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position.
     */
    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.admin_user_item, parent, false);
        }

        // Get the current user based on position
        User user = userList.get(position);

        // initalize user controller
        userController = new UserController(new UserRepository());

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
                    .setPositiveButton("Yes", (dialog, which) -> deleteUserProfile(user))
                    .setNegativeButton("No", null)
                    .show();
        });

        return convertView;
    }

    /**
     * Fetches the details of a user and starts the {@code AdminUserDetailActivity}.
     *
     * @param userID The unique identifier of the user.
     */
    private void fetchUserDetailsAndShow(String userID) {
        Intent intent = new Intent(context, AdminUserDetailActivity.class);
        intent.putExtra("userID", userID);
        context.startActivity(intent);
    }

    /**
     * Deletes a user's profile from the database and updates the adapter.
     *
     * @param user The user object to delete.
     */
    private void deleteUserProfile(User user) {
        userController.deleteUser(user, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d(TAG, "User deleted");
                userList.remove(user);
                notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error deleting user", e);
                Toast.makeText(AdminUserAdapter.this.getContext(), "Error deleting user: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}