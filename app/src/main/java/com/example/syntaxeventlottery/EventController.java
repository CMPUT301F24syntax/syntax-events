package com.example.syntaxeventlottery;

import java.util.ArrayList;
import java.util.Date;

public class EventController {
    private EventRepository eventRepository;

    public EventController(EventRepository eventRepository){
        this.eventRepository = eventRepository;
    }

    // get a list of all events
    public ArrayList<Event> getAllEvents() {
        return eventRepository.getEventsDataList();
    }

    // get a list of events to happen
    public ArrayList<Event> getEventsToOccur() {
        ArrayList<Event> eventsToOccur = new ArrayList<>();

        for (int i = 0; i < eventRepository.getEventsDataList().size(); i++) {
            Event currEvent = eventRepository.getEventsDataList().get(i);
            // if the start date of event is after current time
            if (currEvent.getStartDate().after(new Date())) {
                eventsToOccur.add(currEvent);
            }
        }

        return eventsToOccur;
    }

    // get a list of events which have already started
    public ArrayList<Event> getEventsOccurred() {
        ArrayList<Event> eventsOccurred = new ArrayList<>();

        for (int i = 0; i < eventRepository.getEventsDataList().size(); i++) {
            Event currEvent = eventRepository.getEventsDataList().get(i);
            // if the start date of event is after current time
            if (currEvent.getStartDate().after(new Date())) {
                eventsOccurred.add(currEvent);
            }
        }

        return eventsOccurred;
    }

    // get all events where the organizer passed as argument is the event organizer
    public ArrayList<Event> getOrganizerEvent(Users organizer) {
        ArrayList<Event> organizerEvents = new ArrayList<>();

        for (int i = 0; i < eventRepository.getEventsDataList().size(); i++) {
            Event currEvent = eventRepository.getEventsDataList().get(i);
            if (currEvent.getOrganizer() == organizer) {
                organizerEvents.add(currEvent);
            }
        }
        return organizerEvents;
    }

    // get list of events where the entrant is in the waiting list
    public ArrayList<Event> getEntrantWaitingListEvents(Users entrant) {
        ArrayList<Event> entrantWaitingListEvents = new ArrayList<>();

        for (int i = 0; i < eventRepository.getEventsDataList().size(); i++) {
            Event currEvent = eventRepository.getEventsDataList().get(i);
            if (currEvent.getWaitingList().contains(entrant)) {
                entrantWaitingListEvents.add(currEvent);
            }
        }

        return entrantWaitingListEvents;
    }

    // get list of events where the entrant has been selected to participate in the event
    public ArrayList<Event> getEntrantSelectedListEvents(Users entrant) {
        ArrayList<Event> entrantSelectedListEvents = new ArrayList<>();

        for (int i = 0; i < eventRepository.getEventsDataList().size(); i++) {
            Event currEvent = eventRepository.getEventsDataList().get(i);
            if (currEvent.getSelectedList().contains(entrant)) {
                entrantSelectedListEvents.add(currEvent);
            }
        }

        return entrantSelectedListEvents;
    }

    // get list of

}
