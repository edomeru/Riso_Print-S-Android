package jp.co.riso.smartdeviceapp.common;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.camera.core.Preview;
import androidx.camera.view.PreviewView;
import androidx.fragment.app.FragmentActivity;

import jp.co.riso.smartdeviceapp.view.MainActivity;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.ActivityInstrumentationTestCase2;

import com.google.mlkit.vision.common.InputImage;

import java.io.IOException;
import java.io.InputStream;

public class QRImageScannerTest extends ActivityInstrumentationTestCase2<MainActivity> {
    private static final String TEST_IMAGE_INVALID = "qr/invalid.png";
    private static final String TEST_IMAGE_VALID_IP = "qr/valid_ip.png";
    private static final String TEST_IMAGE_VALID_TEXT = "qr/valid_text.png";
    private static final String TEST_RESULT_VALID_IP = "192.168.0.1";
    private static final String TEST_RESULT_VALID_TEXT = "test";

    public QRImageScannerTest() {
        super(MainActivity.class);
    }

    public QRImageScannerTest(Class<MainActivity> activityClass) {
        super(activityClass);
    }

    // ================================================================================
    // Tests - capture
    // ================================================================================
    public void testCapture_NullActivity() {
        FragmentActivity activity = null;
        PreviewView previewView = new PreviewView(getActivity());
        QRImageScanner.QRImageScanResultCallback callback = new MockCallback();
        boolean result = QRImageScanner.capture(activity, previewView, callback);
        assertFalse(result);
    }

    public void testCapture_NullContext() {
        FragmentActivity activity = new MockActivity();
        PreviewView previewView = new PreviewView(getActivity());
        QRImageScanner.QRImageScanResultCallback callback = new MockCallback();
        boolean result = QRImageScanner.capture(activity, previewView, callback);
        assertFalse(result);
    }

    public void testCapture_NullPreview() {
        FragmentActivity activity = getActivity();
        PreviewView previewView = null;
        QRImageScanner.QRImageScanResultCallback callback = new MockCallback();
        boolean result = QRImageScanner.capture(activity, previewView, callback);
        assertFalse(result);
    }

    public void testCapture_NullCallback() {
        FragmentActivity activity = getActivity();
        PreviewView previewView = new PreviewView(activity);
        QRImageScanner.QRImageScanResultCallback callback = null;
        boolean result = QRImageScanner.capture(activity, previewView, callback);
        assertFalse(result);
    }

    public void testCapture_CameraCaptureFailed() {
        FragmentActivity activity = getActivity();
        PreviewView previewView = new MockPreviewView(activity);
        QRImageScanner.QRImageScanResultCallback callback = new MockCallback();
        boolean result = QRImageScanner.capture(activity, previewView, callback);
        waitUntilTimeout(5);
        QRImageScanner.stop(activity);
        assertTrue(result);
    }

    public void testCapture_CameraCaptureSuccess() {
        FragmentActivity activity = getActivity();
        PreviewView previewView = new PreviewView(activity);
        QRImageScanner.QRImageScanResultCallback callback = new MockCallback();
        boolean result = QRImageScanner.capture(activity, previewView, callback);
        waitUntilTimeout(5);
        QRImageScanner.stop(activity);
        assertTrue(result);
    }

    // ================================================================================
    // Tests - close
    // ================================================================================
    public void testClose_CameraCaptureStarted() {
        FragmentActivity activity = getActivity();
        PreviewView previewView = new PreviewView(activity);
        QRImageScanner.QRImageScanResultCallback callback = new MockCallback();
        boolean result = QRImageScanner.capture(activity, previewView, callback);
        waitUntilTimeout(5);
        QRImageScanner.stop(activity);
        assertTrue(result);
        waitUntilTimeout(5);
        assertNull(QRImageScanner.sCameraProviderFuture);
        assertNull(QRImageScanner.sCameraProvider);
    }

    public void testClose_NoCameraCapture() {
        QRImageScanner.stop(getActivity());
        assertNull(QRImageScanner.sCameraProviderFuture);
        assertNull(QRImageScanner.sCameraProvider);
    }

    // ================================================================================
    // Tests - scanImage
    // =====================================================================
    public void testScanImage_NullCallback() {
        QRImageScanner.QRImageScanResultCallback callback = null;
        QRImageScanner scanner = new QRImageScanner(callback);
        InputImage image = getInputImage(TEST_IMAGE_VALID_IP);
        scanner.scanImage(image);
        waitUntilTimeout(5);
        assertNull(callback);
    }

    public void testScanImage_Invalid() {
        QRImageScanner.QRImageScanResultCallback callback = new MockCallback();
        QRImageScanner scanner = new QRImageScanner(callback);
        InputImage image = getInputImage(TEST_IMAGE_INVALID);
        scanner.scanImage(image);
        waitUntilTimeout(5);
        assertNull(((MockCallback) callback).result);
    }

    public void testScanImage_ValidText() {
        QRImageScanner.QRImageScanResultCallback callback = new MockCallback();
        QRImageScanner scanner = new QRImageScanner(callback);
        InputImage image = getInputImage(TEST_IMAGE_VALID_TEXT);
        scanner.scanImage(image);
        waitUntilTimeout(5);
        assertEquals(((MockCallback) callback).result, TEST_RESULT_VALID_TEXT);
    }

    public void testScanImage_ValidIpAddress() {
        QRImageScanner.QRImageScanResultCallback callback = new MockCallback();
        QRImageScanner scanner = new QRImageScanner(callback);
        InputImage image = getInputImage(TEST_IMAGE_VALID_IP);
        scanner.scanImage(image);
        waitUntilTimeout(5);
        assertEquals(((MockCallback) callback).result, TEST_RESULT_VALID_IP);
    }

    // ================================================================================
    // Private Methods
    // ================================================================================
    private void waitUntilTimeout(long seconds) {
        try {
           Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private InputImage getInputImage(String assetFile) {
        AssetManager assetManager = getInstrumentation().getContext().getAssets();
        try {
            InputStream input = assetManager.open(assetFile);
            Bitmap bitmap = BitmapFactory.decodeStream(input);
            input.close();
            return InputImage.fromBitmap(bitmap, 0);
        } catch (IOException e) {
            return null;
        }
    }

    // ================================================================================
    // MOCK - Mock classes for testing
    // ================================================================================
    private class MockActivity extends FragmentActivity {
        @Override
        public Context getApplicationContext() {
            return null;
        }
    }

    private class MockCallback implements QRImageScanner.QRImageScanResultCallback {
        public String result = null;

        @Override
        public void onScanQRCode(String result) {
            this.result = result;
        }
    }

    private class MockPreviewView extends PreviewView {
        public MockPreviewView(@NonNull Context context) {
            super(context);
        }

        @NonNull
        @Override
        public Preview.SurfaceProvider createSurfaceProvider() {
            throw new NullPointerException();
        }
    }
}

