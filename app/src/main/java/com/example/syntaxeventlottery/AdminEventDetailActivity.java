package com.example.syntaxeventlottery;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AdminEventDetailActivity extends AppCompatActivity {
    private Button backButton;
    private TextView eventName, eventDescription, eventFacility, eventCapacity, eventStartDate, eventEndDate;
    private ImageView eventPoster;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_event_detail);

        //Back Button
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish()); // Finish the activity to go back

        // Initialize views
        eventName = findViewById(R.id.eventName);
        eventDescription = findViewById(R.id.eventDescription);
        eventFacility = findViewById(R.id.eventFacility);
        eventCapacity = findViewById(R.id.eventCapacity);
        eventStartDate = findViewById(R.id.eventStartDate);
        eventEndDate = findViewById(R.id.eventEndDate);
        eventPoster = findViewById(R.id.eventPoster);

        // Get the event data from intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            eventName.setText(extras.getString("eventName"));
            eventDescription.setText(extras.getString("description"));
            eventFacility.setText("Facility: " + extras.getString("facility"));
            eventCapacity.setText("Capacity: " + extras.getInt("capacity"));
            eventStartDate.setText("Start: " + dateFormat.format((Date) extras.getSerializable("startDate")));
            eventEndDate.setText("End: " + dateFormat.format((Date) extras.getSerializable("endDate")));

            // Load the poster image
            String posterUrl = extras.getString("posterUrl");
            Glide.with(this).load(posterUrl).placeholder(R.drawable.ic_placeholder_poster_image).into(eventPoster);
        }
    }
}
