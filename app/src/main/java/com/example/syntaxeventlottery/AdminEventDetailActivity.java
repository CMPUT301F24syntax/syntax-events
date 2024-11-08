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

/**
 * The {@code AdminEventDetailActivity} class displays detailed information about an event
 * to administrators, including event name, description, facility, capacity, start date,
 * end date, and poster image.
 */
public class AdminEventDetailActivity extends AppCompatActivity {

    /** Button to navigate back to the previous activity. */
    private Button backButton;

    /** TextView for displaying the event name. */
    private TextView eventName;

    /** TextView for displaying the event description. */
    private TextView eventDescription;

    /** TextView for displaying the event facility. */
    private TextView eventFacility;

    /** TextView for displaying the event capacity. */
    private TextView eventCapacity;

    /** TextView for displaying the event start date. */
    private TextView eventStartDate;

    /** TextView for displaying the event end date. */
    private TextView eventEndDate;

    /** ImageView for displaying the event poster image. */
    private ImageView eventPoster;

    /** Date format used to format date objects into strings. */
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

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
        setContentView(R.layout.activity_admin_event_detail);

        // Back Button
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
            Glide.with(this)
                    .load(posterUrl)
                    .placeholder(R.drawable.ic_placeholder_poster_image)
                    .into(eventPoster);
        }
    }
}
