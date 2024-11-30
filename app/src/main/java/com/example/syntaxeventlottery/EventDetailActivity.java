// EventDetailActivity.java
package com.example.syntaxeventlottery;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Activity to display event details and manage event actions.
 */
public class EventDetailActivity extends AppCompatActivity {
    private static final String TAG = "EventDetailActivity";

    // UI Components
    private ImageView posterImageView, qrCodeImageView;
    private TextView eventNameTextView, eventDescriptionTextView, eventStartDateTextView, eventEndDateTextView, eventCapacityTextView, eventFacilityLocationTextView, eventFacilityNameTextView, eventDrawedStatusTextView;
    private TextView eventActionsTextView;
    private Button joinWaitingListButton, acceptInvitationButton, declineInvitationButton, leaveWaitingListButton;
    private Button manageParticipantsButton, editInfoButton, drawButton;
    private ImageButton backButton;
    private Button notifyWaitingListButton;
    private Button notifySelectedEntrantsButton;
    private Button notifyCancelledEntrantsButton;

    // Controller and Data
    private EventController eventController;
    private String eventID, deviceID;
    private Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        // initialize controller
        eventController = new EventController(new EventRepository());

        // Get device ID
        deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Get event ID from intent
        eventID = getIntent().getStringExtra("eventID");

        // set up back button listener
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        if (eventID == null || eventID.isEmpty()) { // finish activity if there is error getting the event
            Log.e(TAG, "Couldn't get eventID from event");
            finish();
        } else {
            loadEvent();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // reload all event details when resuming activity to get most updated information
        loadEvent();
    }

    private void loadEvent() {
        // Refresh repository to get the latest data
        eventController.refreshRepository(new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {

                // Fetch the event by ID from the controller
                event = eventController.getEventById(eventID);
                Log.d(TAG, "Refreshed repository, event details:"+event);
                // If the event is not found, show an error and finish
                if (event == null) {
                    Log.e(TAG, "Failed to find event in repository");
                    Toast.makeText(EventDetailActivity.this, "Failed to find the event", Toast.LENGTH_SHORT).show();
                    finish();  // Exit the activity as the event wasn't found
                    return;    // Early return to prevent the rest of the code from executing
                }
                updateUI(event); // display the most updated event details
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, e.toString());
                Toast.makeText(EventDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateUI(Event event) {
        this.event = event;
        initializeUI(event);
        displayEventDetails(event);
    }

    private void displayEventDetails(Event event) {
        eventNameTextView.setText(event.getEventName());
        eventDescriptionTextView.setText(event.getDescription());
        eventFacilityNameTextView.setText("Facility Name: " +event.getFacilityName());
        eventFacilityLocationTextView.setText("Location: " + event.getFacilityLocation());
        eventStartDateTextView.setText("Start: "+ event.getStartDate().toString());
        eventEndDateTextView.setText("End: " + event.getEndDate().toString());
        eventCapacityTextView.setText("Capacity: " + String.valueOf(event.getCapacity()));
        eventDrawedStatusTextView.setText("Drawed: "+event.isDrawed());


        if (event.getPosterUrl() != null && !event.getPosterUrl().isEmpty()) {
            Glide.with(this).load(event.getPosterUrl()).into(posterImageView);
            Log.d(TAG, "Loaded poster image. poster url: " + event.getPosterUrl());
        } else {
            // load default poster
            Glide.with(this).load(R.drawable.ic_default_poster).into(posterImageView);
        }


        if (event.getQrCode() != null && !event.getQrCode().isEmpty()) {
            Glide.with(this).load(event.getQrCode()).into(qrCodeImageView);
            Log.d(TAG, "Loaded QR code image. qr code url: " + event.getQrCode());
        } else {
            // load missing qr code image
            Glide.with(this).load(R.drawable.default_qrcode).into(qrCodeImageView);
        }
    }


    private void initializeUI(Event event) {
        posterImageView = findViewById(R.id.eventPosterImageView);
        qrCodeImageView = findViewById(R.id.eventQRCodeImageView);
        eventNameTextView = findViewById(R.id.eventNameTextView);
        eventDescriptionTextView = findViewById(R.id.eventDescriptionTextView);
        eventStartDateTextView = findViewById(R.id.eventStartDateTextView);
        eventEndDateTextView = findViewById(R.id.eventEndDateTextView);
        eventCapacityTextView = findViewById(R.id.eventCapacityTextView);
        eventFacilityLocationTextView = findViewById(R.id.eventFLocationTextView);
        eventFacilityNameTextView = findViewById(R.id.eventFNameTextView);
        eventDrawedStatusTextView = findViewById(R.id.eventDrawedTextView);
        eventActionsTextView = findViewById(R.id.eventActionsTextView);
        joinWaitingListButton = findViewById(R.id.joinEventButton);
        leaveWaitingListButton = findViewById(R.id.leaveEventButton);
        acceptInvitationButton = findViewById(R.id.acceptButton);
        declineInvitationButton = findViewById(R.id.declineInvitationButton);

        manageParticipantsButton = findViewById(R.id.manageParticipantsButton);
        editInfoButton = findViewById(R.id.editInfoButton);
        drawButton = findViewById(R.id.drawParticipantsButton);

        // notification part
        notifyWaitingListButton = findViewById(R.id.notifyWaitingListButton);
        notifySelectedEntrantsButton = findViewById(R.id.notifySelectedEntrantsButton);
        notifyCancelledEntrantsButton = findViewById(R.id.notifyCancelledEntrantsButton);


        configureButtonVisibility();
        setupButtonListeners();
    }

    private void configureButtonVisibility() {
        boolean isOrganizer = event.getOrganizerId().equals(deviceID);

        // Organizer buttons
        editInfoButton.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);
        manageParticipantsButton.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);
        drawButton.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);
        notifyWaitingListButton.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);
        notifySelectedEntrantsButton.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);
        notifyCancelledEntrantsButton.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);


        // Determine which buttons to display
        if (!isOrganizer) {
            displayParticipantButtons();
            showAllParticipantButtons();
        } else {
            eventActionsTextView.setText("You are the organizer of this event!\n"
                    +"Edit event details, perfom the event draw or manage entrants who have joined");
            hideAllParticipantButtons();
        }

        // Draw button state
        if (isOrganizer && event.isDrawed()) {
            drawButton.setText("Draw Replacement Participants");
        }
    }

    private void setupButtonListeners() {
        // user joins waiting list
        joinWaitingListButton.setOnClickListener(v -> {
            if (event.locationRequired()) {
                // Show an AlertDialog if geolocation is required
                new AlertDialog.Builder(EventDetailActivity.this)
                        .setTitle("Geolocation Required")
                        .setMessage("This event requires your geolocation to join the waiting list. Do you want to proceed?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            eventController.addUserToWaitingList(event, deviceID, new DataCallback<Event>() {
                                @Override
                                public void onSuccess(Event result) {
                                    Toast.makeText(EventDetailActivity.this, "You have joined the waiting list", Toast.LENGTH_SHORT).show();
                                    // Refresh event data
                                    loadEvent();
                                }

                                @Override
                                public void onError(Exception e) {
                                    Toast.makeText(EventDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                        .show();
            } else {
                // Proceed to join the waiting list without showing a dialog
                eventController.addUserToWaitingList(event, deviceID, new DataCallback<Event>() {
                    @Override
                    public void onSuccess(Event result) {
                        Toast.makeText(EventDetailActivity.this, "You have joined the waiting list", Toast.LENGTH_SHORT).show();
                        // Refresh event data
                        loadEvent();
                    }

                    @Override
                    public void onError(Exception e) {
                        Toast.makeText(EventDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        // user leaves the waiting list, remove user from all event data
        leaveWaitingListButton.setOnClickListener(v -> {
            // Create and show a confirmation dialog
            new AlertDialog.Builder(EventDetailActivity.this)
                    .setTitle("Leave Waiting List")
                    .setMessage("Are you sure you want to leave the waiting list? If you leave, you will need to rejoin to be considered for the lottery and have a chance to participate in the event.")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // User confirmed; remove all user information from the event
                        eventController.removeUserFromEvent(event, deviceID, new DataCallback<Event>() {
                            @Override
                            public void onSuccess(Event result) {
                                Toast.makeText(EventDetailActivity.this, "You have left waiting list for this event", Toast.LENGTH_SHORT).show();
                                loadEvent();
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e(TAG, "Error leaving the Event", e);
                                Toast.makeText(EventDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // User canceled; dismiss the dialog
                        dialog.dismiss();
                    })
                    .show();
        });

        // user declines their invitation
        declineInvitationButton.setOnClickListener(v -> {
            // Create and show a confirmation dialog
            new AlertDialog.Builder(EventDetailActivity.this)
                    .setTitle("Decline Invitation")
                    .setMessage("Are you sure you decline your invitation? You will not be allowed to rejoin the waiting list for this event")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // User confirmed; remove all user information from the event
                        eventController.setUserCancelled(event, deviceID, new DataCallback<Event>() {
                            @Override
                            public void onSuccess(Event result) {
                                Toast.makeText(EventDetailActivity.this, "You have rejected your invitation", Toast.LENGTH_SHORT).show();
                                loadEvent();
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e(TAG, "Error leaving the Event", e);
                                Toast.makeText(EventDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // User canceled; dismiss the dialog
                        dialog.dismiss();
                    })
                    .show();
        });


        // user accepts invitation
        acceptInvitationButton.setOnClickListener(v -> eventController.addUserToConfirmedList(event, deviceID, new DataCallback<Event>() {
            @Override
            public void onSuccess(Event result) {
                Toast.makeText(EventDetailActivity.this, "Successfully Enrolled in the Event!", Toast.LENGTH_SHORT).show();
                loadEvent();
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error adding user to confirmed list", e);
                Toast.makeText(EventDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }));


        // perform event draw
        // Inside the drawButton.setOnClickListener

        drawButton.setOnClickListener(v -> {
            if (!event.isDrawed()) {
                // perform initial event draw
                eventController.performDraw(event, EventDetailActivity.this, new DataCallback<Event>() {
                    @Override
                    public void onSuccess(Event result) {
                        Log.d(TAG, "Event draw performed: updated event info: " + result);
                        Toast.makeText(EventDetailActivity.this, "Draw performed successfully", Toast.LENGTH_SHORT).show();
                        loadEvent();
                        checkForNewNotifications();
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.d(TAG, "Event draw error");
                        Toast.makeText(EventDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // allow redraw for users still in the waiting list
                eventController.performRedraw(event, EventDetailActivity.this, new DataCallback<Event>() {
                    @Override
                    public void onSuccess(Event result) {
                        Log.d(TAG, "Event redraw performed: updated event info: "+  result);
                        Toast.makeText(EventDetailActivity.this, "Redraw perfomed successfully", Toast.LENGTH_SHORT).show();
                        loadEvent();
                        checkForNewNotifications();
                    }
                    // Inside the drawButton.setOnClickListener

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "Event draw error", e);
                        Toast.makeText(EventDetailActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        editInfoButton.setOnClickListener(v -> {
            Intent intent = new Intent(EventDetailActivity.this, EditEventActivity.class);
            intent.putExtra("event", event); // pass event object
            startActivity(intent);
        });

        manageParticipantsButton.setOnClickListener(v -> {
            if (event != null && deviceID.equals(event.getOrganizerId())) {
                Intent intent = new Intent(EventDetailActivity.this, EventParticipantsListActivity.class);
                intent.putExtra("eventID", eventID);
                startActivity(intent);
            } else {
                Toast.makeText(EventDetailActivity.this, "You are not authorized to view the waiting list.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "User is not authorized to view the waiting list.");
            }
        });

        notifyWaitingListButton.setOnClickListener(v -> sendNotificationToGroup("waitingList"));
        notifySelectedEntrantsButton.setOnClickListener(v -> sendNotificationToGroup("selectedParticipants"));
        notifyCancelledEntrantsButton.setOnClickListener(v -> sendNotificationToGroup("cancelledParticipants"));
    }

    private void sendNotificationToGroup(String group) {
        // Optional: Prompt organizer for custom message
        final EditText input = new EditText(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Send Notification")
                .setMessage("Enter the notification message:")
                .setView(input)
                .setPositiveButton("Send", (dialog, which) -> {
                    String customMessage = input.getText().toString();
                    sendNotifications(group, customMessage);
                })
                .setNegativeButton("Cancel", null);
        builder.show();
    }

    private void sendNotifications(String group, String message) {
        eventController.sendNotificationsToGroup(event, group, message, this, new DataCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(EventDetailActivity.this, "Notifications sent.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(EventDetailActivity.this, "Failed to send notifications.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error sending notifications", e);
            }
        });
    }

    private void checkForNewNotifications() {
        NotificationController notificationController = new NotificationController();
        notificationController.fetchUnreadNotificationsForUser(deviceID, new DataCallback<List<Notification>>() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                for (Notification notification : notifications) {
                    // Display the notification
                    NotificationUtils.sendNotification(EventDetailActivity.this, "Event Notification", notification.getMessage(), notification.generateNotificationId(), notification.getEventId());

                    // Mark the notification as read in the database
                    notificationController.markNotificationAsRead(notification);
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error fetching notifications", e);
            }
        });
    }

    //------------ helper methods for displaying UI --------------//
    private void displayParticipantButtons() {
        boolean isInWaitingList = eventController.isUserInWaitingList(event, deviceID);
        boolean isInSelectedList = eventController.isUserInSelectedList(event, deviceID);
        boolean isInConfirmedList = eventController.isUserInConfirmedList(event, deviceID);
        boolean isInCancelledList = eventController.isUserInCancelledList(event, deviceID);

        // if user is not associated with the event
        if (!isInWaitingList && !isInSelectedList && !isInCancelledList) {
            // if the waiting list is full
            if (event.getWaitingListFull()) {
                eventActionsTextView.setText("Event waiting list is currently full, come back later");
            }
            if (event.isDrawed()) {
                eventActionsTextView.setText("Event draw has already occurred, check out another event!");
            }

            joinWaitingListButton.setVisibility(View.GONE);
            acceptInvitationButton.setVisibility(View.GONE);
            declineInvitationButton.setVisibility(View.GONE);
            leaveWaitingListButton.setVisibility(View.GONE);
            return;
        }

        // Handle Waiting List buttons
        if (isInWaitingList && !isInSelectedList) {
            eventActionsTextView.setText("You are currently in the waiting list for this event!");
            joinWaitingListButton.setVisibility(View.GONE);
            acceptInvitationButton.setVisibility(View.GONE);
            declineInvitationButton.setVisibility(View.GONE);
            leaveWaitingListButton.setVisibility(View.VISIBLE);


        } else {
            eventActionsTextView.setText("Join the waiting list for a chance to be selected to participate!");
            leaveWaitingListButton.setVisibility(View.GONE);
            declineInvitationButton.setVisibility(View.GONE);
            acceptInvitationButton.setVisibility(View.GONE);
            joinWaitingListButton.setVisibility(View.VISIBLE);
        }

        // Handle Selected List buttons
        if (isInSelectedList && !isInConfirmedList) {
            eventActionsTextView.setText("You have been selected to participate!\n" +
                    "Please accept or decline your invitation as soon as possible");
            joinWaitingListButton.setVisibility(View.GONE);
            leaveWaitingListButton.setVisibility(View.GONE);
            declineInvitationButton.setVisibility(View.VISIBLE);
            acceptInvitationButton.setVisibility(View.VISIBLE);
        }

        // Handle Confirmed List buttons
        if (isInConfirmedList) {
            eventActionsTextView.setText("You are currently enrolled for this event");
            joinWaitingListButton.setVisibility(View.GONE);
            acceptInvitationButton.setVisibility(View.GONE);
            leaveWaitingListButton.setVisibility(View.GONE);
            declineInvitationButton.setVisibility(View.GONE);
        }

        // if user has been cancelled
        if (isInCancelledList) {
            eventActionsTextView.setText("Either you have been removed by the creator of this event or you have previously declined your invitation");
            joinWaitingListButton.setVisibility(View.GONE);
            acceptInvitationButton.setVisibility(View.GONE);
            leaveWaitingListButton.setVisibility(View.GONE);
            declineInvitationButton.setVisibility(View.GONE);
        }
    }

    private void hideAllParticipantButtons() {
        joinWaitingListButton.setVisibility(View.GONE);
        acceptInvitationButton.setVisibility(View.GONE);
        leaveWaitingListButton.setVisibility(View.GONE);
        declineInvitationButton.setVisibility(View.GONE);
    }

    private void showAllParticipantButtons() {
        joinWaitingListButton.setVisibility(View.VISIBLE);
        acceptInvitationButton.setVisibility(View.VISIBLE);
        leaveWaitingListButton.setVisibility(View.VISIBLE);
        declineInvitationButton.setVisibility(View.VISIBLE);
    }

}
