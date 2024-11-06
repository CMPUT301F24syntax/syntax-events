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

public class AdminUsersActivity extends AppCompatActivity {

    private ListView listViewUsers;
    private AdminUserAdapter userAdapter;
    private List<User> userList;
    private Button backButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_users_main);

        // Initialize views and Firestore instance
        listViewUsers = findViewById(R.id.listViewUsers);
        backButton = findViewById(R.id.backButton);
        db = FirebaseFirestore.getInstance();

        // Back button to close the activity
        backButton.setOnClickListener(v -> finish());

        // Set up user list and adapter
        userList = new ArrayList<>();
        userAdapter = new AdminUserAdapter(this, userList);
        listViewUsers.setAdapter(userAdapter);

        // Load users from Firestore
        loadUsersFromDatabase();
    }

    private void loadUsersFromDatabase() {
        // Reference to the "Users" collection in Firestore
        CollectionReference usersRef = db.collection("Users");

        usersRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            // Clear existing list to avoid duplicates
                            userList.clear();
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                // Retrieve data and create User object
                                String username = document.getString("username");
                                String userID = document.getString("userID");
                                String email = document.getString("email");
                                String phoneNumber = document.getString("phoneNumber");
                                String profilePhotoUrl = document.getString("profilePhotoUrl");

                                // Ensure required fields are present before creating a User
                                if (userID != null && username != null) {
                                    User user = new User(userID, username, email, phoneNumber, profilePhotoUrl);
                                    userList.add(user);
                                }
                            }
                            // Notify adapter about data changes
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
