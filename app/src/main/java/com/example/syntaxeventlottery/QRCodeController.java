// QRCodeController.java
package com.example.syntaxeventlottery;

public class QRCodeController {
    private final QRCodeScanCallback callback;

    // Callback interface for QR code scan results
    public interface QRCodeScanCallback {
        void onQRCodeScanned(String qrCode); // Called when QR code is successfully scanned
        void onError(String errorMessage);    // Called when there is an error
    }

    // Constructor initializes the controller with a callback
    public QRCodeController(QRCodeScanCallback callback) {
        this.callback = callback;
    }

    // Processes the scanned QR code
    public void handleQRCode(String qrCodeContent) {
        if (qrCodeContent != null && !qrCodeContent.isEmpty()) {
            // Assuming the QR code content is the eventID
            callback.onQRCodeScanned(qrCodeContent); // Passes the result to the callback
        } else {
            callback.onError("QR code is empty or invalid"); // Error if QR code is empty
        }
    }
}
