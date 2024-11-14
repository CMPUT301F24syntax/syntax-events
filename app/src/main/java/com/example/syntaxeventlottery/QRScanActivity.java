// QRScanActivity.java
package com.example.syntaxeventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class QRScanActivity extends AppCompatActivity implements QRCodeController.QRCodeScanCallback {
    private QRCodeController qrCodeController;
    private TextView qrCodeResult;
    private ImageButton backButton; // Back button to return to the previous screen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan);

        qrCodeResult = findViewById(R.id.qrCodeResult);
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
        // Assuming qrCode contains the eventID
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra("event_id", qrCode);
        startActivity(intent);
        finish(); // Close QRScanActivity if you don't want to return to it
    }

    // Callback when there is an error scanning QR code
    @Override
    public void onError(String errorMessage) {
        qrCodeResult.setText("Error: " + errorMessage);
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }
}
