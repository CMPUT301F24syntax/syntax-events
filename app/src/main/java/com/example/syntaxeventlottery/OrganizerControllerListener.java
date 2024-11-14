package com.example.syntaxeventlottery;

import java.util.List;

public interface OrganizerControllerListener{
    public void onEventsLoaded(List<Event> eventList);
    public void onEventSaved();
    public void onError(String errorMessage);
}
