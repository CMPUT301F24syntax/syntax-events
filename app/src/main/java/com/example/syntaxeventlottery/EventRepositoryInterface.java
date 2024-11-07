package com.example.syntaxeventlottery;

import android.graphics.Bitmap;
import android.net.Uri;

import java.util.ArrayList;

import javax.annotation.Nullable;

/**
 * This interface is used so that testing the controller methods does not rely on Firebase
 */
public interface EventRepositoryInterface {
    ArrayList<Event> getAllEventsList();
    void addEventToRepo(Event event, Uri imageUri, Bitmap qrCodeBitmap);
    void deleteEventFromRepo(Event event);
    void updateEventDetails(Event event, @Nullable Uri imageUri, @Nullable Bitmap qrCodeBitmap);

}
