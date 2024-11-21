package com.example.syntaxeventlottery;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.junit.Before;

public class EventRepositoryTest {
    private FirebaseFirestore testDb;
    private FirebaseStorage testStorage;
    private CollectionReference testEventsRef;
    private StorageReference testStorageRef;

    @Before
    public void setUpTestRepo() {
        this.testDb = FirebaseFirestore.getInstance();
        this.testStorage = FirebaseStorage.getInstance();
        this.testEventsRef = testDb.collection("test_events");
        this.testStorageRef = testStorageRef.child("test_poster_urls");

    }



}
