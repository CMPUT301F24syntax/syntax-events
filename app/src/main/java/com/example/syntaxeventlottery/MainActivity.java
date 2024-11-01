package com.example.syntaxeventlottery;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    /* set logged_in val to false for now */
    private boolean logged_in = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        if (logged_in) {
            setContentView(R.layout.activity_main);
        } else {
            setContentView(R.layout.login_activity);
        }
        /* get button views for entrant, organizer and admin login */
        Button entrant_login_btn = findViewById(R.id.entrant_login_btn);
        Button organizer_login_btn = findViewById(R.id.organizer_login_btn);
        Button admin_login_btn = findViewById(R.id.admin_login_btn);

        Users testuser = new Users("test code", "test email", "test number", "test url", "test_user_1234");

        Event testEvent = new Event(testuser,"test facility",new Date(), new Date(), 0);
        testEvent.EventIdGenerator(testuser.getUsername());

        EventRepository repo = new EventRepository();
        repo.addEventToRepo(testEvent);
    }
}