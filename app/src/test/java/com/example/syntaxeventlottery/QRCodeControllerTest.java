package com.example.syntaxeventlottery;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class QRCodeControllerTest {

    // Initialize variables
    private QRCodeController qrCodeController;
    private QRCodeController.QRCodeScanCallback mockCallback;

    @Before
    public void setUp() {

        // Create a fake version of the QRCodeScanCallback to test how the real code interacts with it
        mockCallback = mock(QRCodeController.QRCodeScanCallback.class);

        // Create the QRCodeController and give it the fake callback
        qrCodeController = new QRCodeController(mockCallback);
    }

    @Test
    public void testHandleQRCode_ValidQRCode() {

        // Create a test QR code
        String testQRCode = "Event123";

        // Call the handleQRCode method with the test QR code
        qrCodeController.handleQRCode(testQRCode);

        // Check if the callback's success method onQRCodeScanned was called with the QR code
        verify(mockCallback).onQRCodeScanned(testQRCode);

        // Check that the error method onError was never called
        verify(mockCallback, never()).onError(anyString());
    }

    @Test
    public void testHandleQRCode_EmptyQRCode() {

        // Create a test QR code that is empty
        String emptyQRCode = "";

        // Call the handleQRCode method with the test QR code
        qrCodeController.handleQRCode(emptyQRCode);

        // Check if the callback's error method onError was called with the correct error message
        verify(mockCallback).onError("QR code is empty");

        // Check that the success method onQRCodeScanned was never called
        verify(mockCallback, never()).onQRCodeScanned(anyString());
    }

    @Test
    public void testHandleQRCode_NullQRCode() {

        // Call the handleQRCode method with a null QR code
        qrCodeController.handleQRCode(null);

        // Check if the callback's error method onError was called with the correct error message
        verify(mockCallback).onError("QR code is invalid");

        // Check that the success method onQRCodeScanned was never called
        verify(mockCallback, never()).onQRCodeScanned(anyString());
    }
}