// EventController.java
package com.example.syntaxeventlottery;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.util.List;

/**
 * The EventController class acts as a mediator between the View (Activity) and the Model (Event and EventRepository).
 * It handles the business logic and coordinates data interactions.
 */
public class EventController {

    private static final String TAG = "EventController";
    private EventRepository eventRepository;
    private EventControllerListener listener;
    private static Context context;


    /**
     * Constructor for EventController.
     *
     * @param listener The listener to handle callbacks.
     */
    public EventController(EventControllerListener listener) {
        this.eventRepository = new EventRepository();
        this.listener = listener;
    }


    /**
     * Constructor for EventController.
     *
     * @param context  The context from which the controller is instantiated.
     * @param listener The listener to handle callbacks.
     */
    public EventController(Context context, EventControllerListener listener) {
        this.context = context;
        this.eventRepository = new EventRepository();
        this.listener = listener;
    }

    /**
     * Loads event details by event ID.
     *
     * @param eventID The ID of the event to load.
     */
    public void loadEventDetails(String eventID) {
        eventRepository.getEventById(eventID, new EventRepository.EventCallback() {
            @Override
            public void onSuccess(Event event) {
                Log.d(TAG, "TTTTTTTTTTTTTT11111" + eventID);
                if (listener != null) {
                    listener.onEventLoaded(event);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG, "TTTTTTTTTTTTTT222222" + eventID);
                Log.e(TAG, "Error loading event details", e);
                if (listener != null) {
                    listener.onError("Event not found. It may have been deleted.");
                }
            }
        });
    }

    /**
     * Saves updated event details.
     *
     * @param event The event object containing updated details.
     */
    public void saveEventDetails(Event event) {
        eventRepository.updateEvent(event, new EventRepository.EventUpdateCallback() {
            @Override
            public void onSuccess() {
                if (listener != null) {
                    listener.onEventSaved();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Failed to update event", e);
                if (listener != null) {
                    listener.onError("Failed to update event");
                }
            }
        });
    }

    /**
     * Handles the event item click from the adapter.
     *
     * @param event The event that was clicked.
     */
    public static void handleEventItemClick(Event event) {
        Log.d("EventController", "TTTTTTTTTTTTTTAAAAABBBB" + event.getEventID());
        Intent intent = new Intent(context, EventDetailActivity.class);
        intent.putExtra("eventID", event.getEventID());
        context.startActivity(intent);
    }

    /**
     * Loads events by organizer ID.
     *
     * @param organizerId The ID of the organizer.
     */
    public void loadEventsByOrganizerId(String organizerId) {
        eventRepository.getEventsByOrganizerId(organizerId, new EventRepository.EventListCallback() {
            @Override
            public void onSuccess(List<Event> eventList) {
                if (listener != null) {
                    listener.onEventListLoaded(eventList);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error loading events for organizerId: " + organizerId, e);
                if (listener != null) {
                    listener.onError("Failed to load events");
                }
            }
        });
    }

    /**
     * Creates a new event.
     *
     * @param event    The event object to create.
     * @param imageUri The URI of the event image.
     * @param callback The callback to handle success or failure.
     */
    public void createEvent(Event event, Uri imageUri, EventCreateCallback callback) {
        eventRepository.saveNewEventWithImage(event, imageUri, new EventRepository.EventUpdateCallback() {
            @Override
            public void onSuccess() {
                if (callback != null) {
                    callback.onEventCreated();
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (callback != null) {
                    callback.onError("Failed to create event: " + e.getMessage());
                }
            }
        });
    }


    /**
     * Interface for handling callbacks from the EventController.
     */
    public interface EventControllerListener {
        void onEventLoaded(Event event);
        void onEventSaved();
        void onEventListLoaded(List<Event> eventList);
        void onParticipantStatusChecked(boolean isInWaitingList, boolean isSelected, Event event);
        void onWaitingListJoined();
        void onWaitingListLeft();
        void onInvitationAccepted();
        void onInvitationDeclined();
        void onDrawPerformed();
        void onError(String errorMessage);

        void onPosterUpdated();
    }

    /**
     * Interface for event creation callbacks.
     */
    public interface EventCreateCallback {
        void onEventCreated();
        void onError(String errorMessage);
    }

    /**
     * Checks if the participant is in the event's waiting list.
     *
     * @param eventId       The ID of the event.
     * @param participantId The ID of the participant.
     */
    public void checkParticipantStatus(String eventId, String participantId) {
        eventRepository.getEventById(eventId, new EventRepository.EventCallback() {
            @Override
            public void onSuccess(Event event) {
                boolean isInWaitingList = event.getParticipants().contains(participantId);
                boolean isSelected = event.getSelectedParticipants().contains(participantId);
                if (listener != null) {
                    listener.onParticipantStatusChecked(isInWaitingList, isSelected, event);
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (listener != null) {
                    listener.onError("Failed to check participant status: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Participant joins the waiting list.
     *
     * @param eventId       The ID of the event.
     * @param participantId The ID of the participant.
     */
    public void joinWaitingList(String eventId, String participantId) {
        eventRepository.addParticipantToEvent(eventId, participantId, new EventRepository.EventUpdateCallback() {
            @Override
            public void onSuccess() {
                if (listener != null) {
                    listener.onWaitingListJoined();
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (listener != null) {
                    listener.onError("Failed to join waiting list: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Participant leaves the waiting list.
     *
     * @param eventId       The ID of the event.
     * @param participantId The ID of the participant.
     */
    public void leaveWaitingList(String eventId, String participantId) {
        eventRepository.removeParticipantFromEvent(eventId, participantId, new EventRepository.EventUpdateCallback() {
            @Override
            public void onSuccess() {
                if (listener != null) {
                    listener.onWaitingListLeft();
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (listener != null) {
                    listener.onError("Failed to leave waiting list: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Organizer performs the draw to select participants.
     *
     * @param eventId The ID of the event.
     */
    /**
     * Organizer performs the draw to select participants.
     *
     * @param eventId The ID of the event.
     */
    public void performDraw(String eventId) {
        eventRepository.getEventById(eventId, new EventRepository.EventCallback() {
            @Override
            public void onSuccess(Event event) {
                if (event.isDrawed()) {
                    if (listener != null) {
                        listener.onError("Draw has already been performed.");
                    }
                } else {
                    eventRepository.performDraw(eventId, new EventRepository.EventUpdateCallback() {
                        @Override
                        public void onSuccess() {
                            if (listener != null) {
                                listener.onDrawPerformed();
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            if (listener != null) {
                                listener.onError("Failed to perform draw: " + e.getMessage());
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (listener != null) {
                    listener.onError("Failed to load event: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Participant accepts the invitation.
     *
     * @param eventId       The ID of the event.
     * @param participantId The ID of the participant.
     */
    public void acceptInvitation(String eventId, String participantId) {
        eventRepository.acceptInvitation(eventId, participantId, new EventRepository.EventUpdateCallback() {
            @Override
            public void onSuccess() {
                if (listener != null) {
                    listener.onInvitationAccepted();
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (listener != null) {
                    listener.onError("Failed to accept invitation: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Participant declines the invitation.
     *
     * @param eventId       The ID of the event.
     * @param participantId The ID of the participant.
     */
    public void declineInvitation(String eventId, String participantId) {
        eventRepository.declineInvitation(eventId, participantId, new EventRepository.EventUpdateCallback() {
            @Override
            public void onSuccess() {
                if (listener != null) {
                    listener.onInvitationDeclined();
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (listener != null) {
                    listener.onError("Failed to decline invitation: " + e.getMessage());
                }
            }
        });
    }

    public void updateEventPoster(String eventId, Uri imageUri) {
        eventRepository.updateEventPoster(eventId, imageUri, new EventRepository.EventUpdateCallback() {
            @Override
            public void onSuccess() {
                if (listener != null) {
                    listener.onPosterUpdated();
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (listener != null) {
                    listener.onError("Failed to update poster: " + e.getMessage());
                }
            }
        });
    }





}