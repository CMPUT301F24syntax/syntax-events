// EventAdapterController.java
package com.example.syntaxeventlottery;

import android.content.Context;
import android.content.Intent;

public class EventAdapterController {
    private Context context;

    public EventAdapterController(Context context) {
        this.context = context;
    }

    public void onEventItemClicked(Event event) {
        Intent intent = new Intent(context, EventDetailActivity.class);
        intent.putExtra("eventID", event.getEventID());
        intent.putExtra("posterUrl", event.getPosterUrl());
        intent.putExtra("qrCode", event.getQrCode());
        intent.putExtra("eventName", event.getEventName());
        intent.putExtra("description", event.getDescription());
        intent.putExtra("startDate", event.getStartDate().toString());
        intent.putExtra("endDate", event.getEndDate().toString());
        intent.putExtra("capacity", event.getCapacity());
        context.startActivity(intent);
    }
}