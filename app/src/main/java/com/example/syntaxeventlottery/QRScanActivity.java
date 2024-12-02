// QRScanActivity.java
package com.example.syntaxeventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

/**
 * The {@code QRScanActivity} class is responsible for handling QR code scanning functionality.
 * It utilizes the ZXing library for scanning QR codes and interacts with the {@code QRCodeController}
 * for processing scanned data.
 */
public class QRScanActivity extends AppCompatActivity implements QRCodeController.QRCodeScanCallback {

    private QRCodeController qrCodeController; // Controller for handling QR code operations
    private ImageButton backButton; // Back button to return to the previous screen

    private static final String TAG = "QRScanActivity"; // Tag for logging

    /**
     * Called when the activity is first created.
     * Initializes the QR scanner and sets up the back button functionality.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down, this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan);

        // Initialize the back button
        backButton = findViewById(R.id.backButton);

        // Set up back button to finish the activity and go back
        backButton.setOnClickListener(v -> finish());

        // Initialize the QR code controller with a callback
        qrCodeController = new QRCodeController(this);

        // Initialize ZXing scanner
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE); // Set the scanner to recognize QR codes
        integrator.setPrompt("Scan a QR code"); // Display a scanning prompt
        integrator.setCameraId(0); // Use a specific camera (default is 0)
        integrator.setBeepEnabled(true); // Enable beep sound upon successful scan
        integrator.setBarcodeImageEnabled(true); // Enable saving scanned barcode images
        integrator.setOrientationLocked(false); // Allow orientation changes
        integrator.initiateScan(); // Start the QR code scanning process
    }

    /**
     * Handles the result of the QR code scanning activity.
     *
     * @param requestCode The request code originally supplied to {@link #startActivityForResult(Intent, int)}.
     * @param resultCode  The result code returned by the child activity.
     * @param data        An Intent that carries the result data.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Retrieve the result of the QR code scan
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                // Pass the QR code result to the controller for handling
                qrCodeController.handleQRCode(result.getContents());
            } else {
                onError("No QR code found");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Callback invoked when a QR code is successfully scanned.
     *
     * @param qrCode The content of the scanned QR code.
     */
    @Override
    public void onQRCodeScanned(String qrCode) {
        // Log the scanned event ID
        Log.d(TAG, "Scanned eventID: " + qrCode);

        // Navigate to EventDetailActivity and pass the scanned event ID
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra("eventID", qrCode);
        startActivity(intent);
        finish(); // Close QRScanActivity after navigation
    }

    /**
     * Callback invoked when there is an error scanning the QR code.
     *
     * @param errorMessage The error message describing the issue.
     */
    @Override
    public void onError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        finish(); // Close the activity on error
    }
}
