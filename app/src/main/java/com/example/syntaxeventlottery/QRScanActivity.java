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


public class QRScanActivity extends AppCompatActivity implements QRCodeController.QRCodeScanCallback {
    private QRCodeController qrCodeController;
    private ImageButton backButton; // Back button to return to the previous screen


    private static final String TAG = "QRScanActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan);


        backButton = findViewById(R.id.backButton); // Initialize the back button


        // Set up back button to finish the activity and go back
        backButton.setOnClickListener(v -> finish());


        // Initialize the QR code controller with callback
        qrCodeController = new QRCodeController(this);


        // Initialize ZXing scanner
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan a QR code");
        integrator.setCameraId(0);  // Use a specific camera of the device
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.setOrientationLocked(false); // Allow orientation changes
        integrator.initiateScan();
    }


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


    // Callback when QR code is successfully scanned
    @Override
    public void onQRCodeScanned(String qrCode) {
        // Log the eventID
        Log.d(TAG, "Scanned eventID: " + qrCode);


        // Navigate to EventInfoActivity instead of EventDetailActivity
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra("eventID", qrCode);
        startActivity(intent);
        finish(); // Close QRScanActivity if you don't want to return to it
    }


    // Callback when there is an error scanning QR code
    @Override
    public void onError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        finish(); // Close the activity on error
    }
}

