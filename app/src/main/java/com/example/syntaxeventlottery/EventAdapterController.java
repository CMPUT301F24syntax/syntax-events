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
        intent.putExtra("event_id", event.getEventID());
        intent.putExtra("poster_url", event.getPosterUrl());
        intent.putExtra("qr_url", event.getQrCode());
        intent.putExtra("event_name", event.getEventName());
        intent.putExtra("event_description", event.getDescription());
        intent.putExtra("event_start_date", event.getStartDate().toString());
        intent.putExtra("event_end_date", event.getEndDate().toString());
        intent.putExtra("event_capacity", event.getCapacity());
        context.startActivity(intent);
    }
}