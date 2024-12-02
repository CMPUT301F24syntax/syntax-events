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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The {@code AdminUsersActivity} class displays a list of users to administrators.
 * It provides functionality to view all users and navigate back to the previous screen.
 */
public class AdminUsersActivity extends AppCompatActivity {
    private final String TAG="AdminUsersActivity";

    private ListView listViewUsers;
    private AdminUserAdapter userAdapter;
    private List<User> userList;
    private Button backButton;
    private UserController userController;

    /**
     * Called when the activity is first created.
     * Initializes UI components, sets up the back button functionality, and loads users from the database.
     *
     * @param savedInstanceState The saved instance state, or {@code null} if none exists.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_users_main);

        // Initialize ListView, Back button
        listViewUsers = findViewById(R.id.listViewUsers);
        backButton = findViewById(R.id.backButton);

        // initialize controller
        userController = new UserController(new UserRepository());


        // Set up Back button to close the activity
        backButton.setOnClickListener(v -> finish());

        // Initialize user list and adapter
        userList = new ArrayList<>();
        userAdapter = new AdminUserAdapter(this, userList);
        listViewUsers.setAdapter(userAdapter);

        // Load most updated users
        loadUsersFromDatabase();
    }

    /**
     * Loads the most updated list of users from the database
     * and refreshes the user list in the adapter.
     */
    private void loadUsersFromDatabase() {
        // refresh user repository to get most updated data
        userController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                userList.clear();
                ArrayList<User> refreshedUsers = userController.getLocalUsersList();
                userList.addAll(refreshedUsers);
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "error refreshing user repository", e);
                Toast.makeText(AdminUsersActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}