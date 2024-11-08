package com.example.syntaxeventlottery;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * The {@code AdminUsersActivity} class displays a list of users to the administrator.
 * It fetches user data from Firebase Firestore and populates a ListView using an adapter.
 */
public class AdminUsersActivity extends AppCompatActivity {

    /** The ListView that displays the list of users. */
    private ListView listViewUsers;

    /** The adapter that bridges between the ListView and the data source. */
    private AdminUserAdapter userAdapter;

    /** The list of users fetched from the database. */
    private List<User> userList;

    /** The back button to navigate to the previous activity. */
    private Button backButton;

    /** The Firebase Firestore instance used to access the database. */
    private FirebaseFirestore db;

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           then this Bundle contains the data it most recently supplied in
     *                           {@link #onSaveInstanceState}. <b>Note: Otherwise, it is null.</b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_users_main);

        // Initialize ListView, Back button, and Firestore
        listViewUsers = findViewById(R.id.listViewUsers);
        backButton = findViewById(R.id.backButton);
        db = FirebaseFirestore.getInstance();

        // Set up Back button to close the activity
        backButton.setOnClickListener(v -> finish());

        // Initialize user list and adapter
        userList = new ArrayList<>();
        userAdapter = new AdminUserAdapter(this, userList);
        listViewUsers.setAdapter(userAdapter);

        // Load users from Firestore
        loadUsersFromDatabase();
    }

    /**
     * Loads users from the Firestore database and updates the ListView.
     */
    private void loadUsersFromDatabase() {
        // Reference to the "Users" collection
        CollectionReference usersRef = db.collection("Users");

        usersRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Clear the existing list to avoid duplicates
                            userList.clear();
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                // Retrieve data from each document and create User object
                                String userID = document.getString("userID");
                                String username = document.getString("username");
                                String email = document.getString("email");
                                String phoneNumber = document.getString("phoneNumber");
                                String profilePhotoUrl = document.getString("profilePhotoUrl");
                                String facility = document.getString("facility");

                                // Make sure all required fields are present
                                if (userID != null && username != null) {
                                    User user = new User(userID, username, email, phoneNumber, profilePhotoUrl, facility);
                                    userList.add(user);
                                }
                            }
                            // Notify adapter that data has changed
                            userAdapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(this, "No users found in the database.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("AdminUsersActivity", "Error getting documents: ", task.getException());
                        Toast.makeText(this, "Failed to load users from database.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
