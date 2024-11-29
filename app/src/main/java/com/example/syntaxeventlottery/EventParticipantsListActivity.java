package com.example.syntaxeventlottery;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class EventParticipantsListActivity extends AppCompatActivity {
    private final String TAG = "EventParticipantsListActivity";

    private Event event;
    private EventController eventController;
    private UserController userController;
    private String eventId;
    private RecyclerView listRecyclerView;
    private WaitingListAdapter participantsListAdapter;
    private ArrayList<User> usersList;
    private TextView listTitle;
    private TextView listDetails;
    private Button backButton;
    private Button waitingListButton;
    private Button selectedListButton;
    private Button confirmedListButton;
    private Button cancelledListButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_participant_list);

        // Initialize controllers
        eventController = new EventController(new EventRepository());
        userController = new UserController(new UserRepository());

        // Retrieve event ID passed from EventDetailActivity
        eventId = getIntent().getStringExtra("eventID");

        // initialize text view
        listTitle = findViewById(R.id.participantsListTitle);
        listDetails = findViewById(R.id.listDetailsTextView);

        // Initialize RecyclerView and Adapter
        listRecyclerView = findViewById(R.id.waitingListRecyclerView);
        listRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersList = new ArrayList<>();
        participantsListAdapter = new WaitingListAdapter(usersList);
        listRecyclerView.setAdapter(participantsListAdapter);

        // initialize buttons
        backButton = findViewById(R.id.backButton);
        waitingListButton = findViewById(R.id.waitingListButton);
        selectedListButton = findViewById(R.id.selectedParticipantsButton);
        confirmedListButton = findViewById(R.id.confirmedParticipantsButton);
        cancelledListButton = findViewById(R.id.cancelledParticipantsButton);

        // set up button listeners
        backButton.setOnClickListener(v -> finish());
        waitingListButton.setOnClickListener(v -> loadWaitingList());
        selectedListButton.setOnClickListener(v -> loadSelectedList());
        confirmedListButton.setOnClickListener(v -> loadConfirmedList());
        cancelledListButton.setOnClickListener(v -> loadCancelledList());

        if (eventId != null) {
            loadEventData();
        } else {
            Toast.makeText(this, "Couldn't find event participants", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadEventData() {
        eventController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                event = eventController.getEventById(eventId);
                Log.d(TAG, "event loaded");
                loadWaitingList();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "error loading event", e);
            }
        });
    }

    private void loadWaitingList() {
        // set title
        listTitle.setText("Waiting List");

        // clear old list
        usersList.clear();
        ArrayList<User> waitingList = new ArrayList<>();

        // get most updated users
        userController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                for (String userId : eventController.getEventWaitingList(event)) {
                    User user = userController.getUserByDeviceID(userId);
                    if (user != null) {
                        waitingList.add(user);
                    }
                }
                Log.d(TAG, "Waiting List Array:"+ waitingList);
                // set details header
                if (waitingList.isEmpty()) {
                    listDetails.setText("No Entrants have joined the waiting list");
                } else {
                    if (event.getWaitingListLimit() == null) {
                        listDetails.setText(waitingList.size()+" Entrants in the waiting list");
                    } else {
                        listDetails.setText(waitingList.size()+" / "+ event.getWaitingListLimit()+ " Entrants in the waiting list");
                    }
                }
                usersList.addAll(waitingList);
                participantsListAdapter.notifyDataSetChanged();

            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error refreshing user repository",e);
            }
        });
    }

    private void loadSelectedList() {
        // set title
        listTitle.setText("Selected Participants");

        // clear old list
        usersList.clear();
        ArrayList<User> selectedList = new ArrayList<>();

        // get most updated users
        userController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                for (String userId : eventController.getEventSelectedList(event)) {
                    User user = userController.getUserByDeviceID(userId);
                    if (user != null) {
                        selectedList.add(user);
                    }
                }
                Log.d(TAG, "Selected List Array:"+ selectedList);
                // set details header
                if (!event.isDrawed()) {
                    listDetails.setText("Event draw has not occured");
                } else {
                    listDetails.setText(selectedList.size() + " Entrants invited");
                }

                usersList.addAll(selectedList);
                participantsListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error refreshing user repository",e);
            }
        });
    }

    private void loadConfirmedList() {
        // set title
        listTitle.setText("Confirmed Participants");
        // clear old list
        usersList.clear();
        ArrayList<User> confirmedList = new ArrayList<>();

        // get most updated users
        userController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                for (String userId : eventController.getEventConfirmedList(event)) {
                    User user = userController.getUserByDeviceID(userId);
                    if (user != null) {
                        confirmedList.add(user);
                    }
                }
                Log.d(TAG, "Confirmed List Array:"+ confirmedList);
                // set details header
                if (!event.isDrawed()) {
                    listDetails.setText("Event draw has not occured");
                } else if (confirmedList.isEmpty()) {
                    listDetails.setText("No Entrants have accepted their invitation");
                } else {
                    listDetails.setText(confirmedList.size() +" / "+ event.getCapacity() +" Entrants have joined the event");
                }

                usersList.addAll(confirmedList);
                participantsListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error refreshing user repository",e);
            }
        });
    }

    private void loadCancelledList() {
        // set title
        listTitle.setText("Cancelled Participants");
        // clear old list
        usersList.clear();
        ArrayList<User> cancelledList = new ArrayList<>();

        // get most updated users
        userController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                for (String userId : eventController.getEventCancelledList(event)) {
                    User user = userController.getUserByDeviceID(userId);
                    if (user != null) {
                        cancelledList.add(user);
                    }
                }
                Log.d(TAG, "Cancelled List Array:"+ cancelledList);
                listDetails.setText("Entrants you have cancelled or who have declined their invitation");

                usersList.addAll(cancelledList);
                participantsListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error refreshing user repository",e);
            }
        });
    }
}
