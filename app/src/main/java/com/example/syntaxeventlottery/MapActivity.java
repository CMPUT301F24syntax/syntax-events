package com.example.syntaxeventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";
    private MapView mapView;
    private GoogleMap googleMap;
    private String eventID;
    private FirebaseFirestore firestore;
    private ImageButton backButton; // Back Button for returning to the previous screen

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

        // Initialize Firebase Firestore
        firestore = FirebaseFirestore.getInstance();

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

    /**
     * Loads participant locations for the event and displays them on the map.
     */
    private void loadParticipantLocations() {
        // Fetch participants list from Firestore using the eventID
        firestore.collection("events")
                .document(eventID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> participants = (List<String>) documentSnapshot.get("participants");
                        if (participants != null && !participants.isEmpty()) {
                            // Retrieve user locations for each participant
                            fetchParticipantLocations(participants);
                        } else {
                            Toast.makeText(this, "No participants found for this event.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Event not found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load event data", e);
                    Toast.makeText(this, "Failed to load event data.", Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Fetches the locations of all participants based on their device IDs.
     *
     * @param participants A list of participant device IDs.
     */
    private void fetchParticipantLocations(List<String> participants) {
        List<LatLng> participantLocations = new ArrayList<>();

        for (String deviceID : participants) {
            firestore.collection("Users")
                    .document(deviceID)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Retrieve the location field for the participant
                            List<Double> location = (List<Double>) documentSnapshot.get("location");
                            if (location != null && location.size() == 2) {
                                double latitude = location.get(0);
                                double longitude = location.get(1);
                                LatLng latLng = new LatLng(latitude, longitude);
                                participantLocations.add(latLng);

                                // Add a marker for each participant's location on the map
                                googleMap.addMarker(new MarkerOptions()
                                        .position(latLng)
                                        .title("Participant: " + deviceID));

                                // Move the camera to the first location
                                if (participantLocations.size() == 1) {
                                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                                }
                            } else {
                                Log.d(TAG, "Location not found for user: " + deviceID);
                            }
                        } else {
                            Log.d(TAG, "User not found for deviceID: " + deviceID);
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to load user data for deviceID: " + deviceID, e));
        }
    }

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
