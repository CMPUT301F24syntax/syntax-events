package com.example.syntaxeventlottery;

import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

    }
}