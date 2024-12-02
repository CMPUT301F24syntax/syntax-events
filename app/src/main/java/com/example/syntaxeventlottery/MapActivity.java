package com.example.syntaxeventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";
    private MapView mapView;
    private GoogleMap googleMap;
    private String eventID;
    private ImageButton backButton;
    private Event event;
    // Back Button for returning to the previous screen

    private EventController eventController;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Get the eventID passed from EventDetailActivity
        eventID = getIntent().getStringExtra("eventID");
        if (eventID == null || eventID.isEmpty()) {
            Toast.makeText(this, "Invalid event ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize EventController
        eventController = new EventController(new EventRepository());
        eventController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                event = eventController.getEventById(eventID);
                if (event == null) {
                    Toast.makeText(MapActivity.this, "Event not found.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(MapActivity.this, "Failed to load events.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });


        // Initialize MapView
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this); // Load the map asynchronously

        // Set up Back Button functionality
        backButton = findViewById(R.id.backButton); // Find the back button by its ID
        backButton.setOnClickListener(v -> {
            // Navigate back to the previous screen
            Intent intent = new Intent(MapActivity.this, EventDetailActivity.class);
            intent.putExtra("eventID", eventID); // Pass the event ID back
            startActivity(intent);
            finish(); // End the current activity
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        Log.d(TAG, "Map is ready");

        // Load participant locations onto the map

            loadParticipantLocations();


    }

    private void loadParticipantLocations() {
        // Use EventController to get participant locations
        eventController.getAllParticipantLocations(eventID, new DataCallback<List<LatLng>>() {
            @Override
            public void onSuccess(List<LatLng> participantLocations) {
                if (participantLocations != null && !participantLocations.isEmpty()) {
                    for (LatLng latLng : participantLocations) {
                        // Add a marker for each participant's location on the map
                        googleMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title("Participant"));
                        // Move the camera to the first location
                        if (participantLocations.indexOf(latLng) == 0) {
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                        }
                    }
                } else {
                    Toast.makeText(MapActivity.this, "No participant locations found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Failed to load participant locations", e);
                Toast.makeText(MapActivity.this, "Failed to load participant locations.", Toast.LENGTH_SHORT).show();
            }
        });
    }
//    private void loadSelectedParticipantLocations() {
//        // Use EventController to get participant locations
//        eventController.getSelectedParticipantLocations(eventID, new DataCallback<List<LatLng>>() {
//            @Override
//            public void onSuccess(List<LatLng> participantLocations) {
//                if (participantLocations != null && !participantLocations.isEmpty()) {
//                    for (LatLng latLng : participantLocations) {
//                        // Add a marker for each participant's location on the map
//                        googleMap.addMarker(new MarkerOptions()
//                                .position(latLng)
//                                .title("Participant"));
//                        // Move the camera to the first location
//                        if (participantLocations.indexOf(latLng) == 0) {
//                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
//                        }
//                    }
//                } else {
//                    Toast.makeText(MapActivity.this, "No participant locations found.", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onError(Exception e) {
//                Log.e(TAG, "Failed to load participant locations", e);
//                Toast.makeText(MapActivity.this, "Failed to load participant locations.", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
