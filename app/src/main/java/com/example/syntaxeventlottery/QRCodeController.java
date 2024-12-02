// QRCodeController.java
package com.example.syntaxeventlottery;

/**
 * The {@code QRCodeController} class is responsible for handling QR code processing.
 * It works with a callback interface to notify the caller of scan results or errors.
 */
public class QRCodeController {

    private final QRCodeScanCallback callback; // Callback for QR code scan results

    /**
     * Callback interface for handling QR code scan results or errors.
     */
    public interface QRCodeScanCallback {
        /**
         * Called when a QR code is successfully scanned.
         *
         * @param qrCode The content of the scanned QR code.
         */
        void onQRCodeScanned(String qrCode);

        /**
         * Called when there is an error processing the QR code.
         *
         * @param errorMessage A message describing the error.
         */
        void onError(String errorMessage);
    }

    /**
     * Constructs a new {@code QRCodeController} with the specified callback.
     *
     * @param callback The callback to notify about scan results or errors.
     */
    public QRCodeController(QRCodeScanCallback callback) {
        this.callback = callback;
    }

    /**
     * Processes the scanned QR code content. If valid, the result is passed to the callback.
     * Otherwise, an error is reported.
     *
     * @param qrCodeContent The content of the scanned QR code.
     */
    public void handleQRCode(String qrCodeContent) {
        if (qrCodeContent != null && !qrCodeContent.isEmpty()) {
            // Assuming the QR code content is the eventID
            callback.onQRCodeScanned(qrCodeContent); // Passes the result to the callback
        } else {
            callback.onError("QR code is empty or invalid"); // Error if QR code is empty
        }
    }
}
