package com.example.syntaxeventlottery;

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.zxing.integration.android.IntentIntegrator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class QRScanActivityTest {

    @Before
    public void setUp() {

        // Initialize Espresso Intents
        Intents.init();
    }

    @After
    public void tearDown() {

        // Release Espresso Intents
        Intents.release();
    }

    @Test
    public void testQRCodeScanNavigatesToEventDetailActivity() {

        // Launch the QRScanActivity
        ActivityScenario<QRScanActivity> scenario = ActivityScenario.launch(QRScanActivity.class);

        // Mock a QR code
        String fakeQRCodeContent = "Bitcoin";

        // Create an Intent for scan
        Intent fakeResultIntent = new Intent();

        // Add the scanned QR code content to the Intent
        fakeResultIntent.putExtra("SCAN_RESULT", fakeQRCodeContent);

        // Specify that the format of the scanned code is a QR code
        fakeResultIntent.putExtra("SCAN_RESULT_FORMAT", IntentIntegrator.QR_CODE);

        // Simulate calling onActivityResult with the fake QR code
        scenario.onActivity(activity -> {activity.onActivityResult(IntentIntegrator.REQUEST_CODE, AppCompatActivity.RESULT_OK, fakeResultIntent);});

        // Verify that the EventDetailActivity was started
        intended(hasComponent(EventDetailActivity.class.getName()));
    }
}