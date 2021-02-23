package jp.co.riso.smartdeviceapp.common;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.nio.ByteBuffer;
import java.util.concurrent.Executor;

/**
 * @class QRImageScanner
 *
 * @brief Analyzes an image with a QR Code and returns the result via callback
 */
public class QRImageScanner implements ImageAnalysis.Analyzer {
    private static final String SCHEME = "://";

    public static ListenableFuture<ProcessCameraProvider> sCameraProviderFuture = null;
    public static ProcessCameraProvider sCameraProvider = null;

    /**
     * @interface QRImageScanner.QRImageScanResultCallback
     *
     * @brief Contains the callback definition for indicating that a QR Code image was scanned
     */
    public  interface QRImageScanResultCallback {
        void onScanQRCode(String result);
    }

    private final QRImageScanResultCallback mCallback;

    public QRImageScanner(QRImageScanResultCallback callback) {
        mCallback = callback;
    }

    /**
     * @brief Begin camera capture for QR code scanning
     * @param activity The lifecycle owner
     * @param previewView The view to display the camera captures on
     * @param callback The callback class to handle the camera capture results
     * @return true If the camera capture was successfully started
     * @return false If the camera capture failed
     */
    public static boolean capture(FragmentActivity activity, PreviewView previewView, QRImageScanResultCallback callback) {
        if (activity == null || previewView == null || callback == null) {
            return false;
        }

        Context context = activity.getApplicationContext();
        if (context == null) {
            return false;
        }

        Executor mainExecutor = ContextCompat.getMainExecutor(context);
        QRImageScanner imageScanner = new QRImageScanner(callback);

        sCameraProviderFuture = ProcessCameraProvider.getInstance(context);
        sCameraProviderFuture.addListener(() -> {
            try {
                sCameraProvider = sCameraProviderFuture.get();

                LifecycleOwner owner = activity;

                CameraSelector cameraSelector = (new CameraSelector.Builder()).build();

                Preview preview =  (new Preview.Builder()).build();
                preview.setSurfaceProvider(previewView.createSurfaceProvider());

                ImageAnalysis imageAnalyzer = (new ImageAnalysis.Builder()).build();
                imageAnalyzer.setAnalyzer(mainExecutor, imageScanner);

                sCameraProvider.unbindAll();
                sCameraProvider.bindToLifecycle(owner, cameraSelector, preview,  imageAnalyzer);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, mainExecutor);
        return true;
    }

    /**
     * @brief Stop camera capture for QR code scanning
     * @param activity The lifecycle owner
     */
    public static void stop(FragmentActivity activity) {
        if (sCameraProvider != null) {
            Context context = activity.getApplicationContext();
            Executor mainExecutor = ContextCompat.getMainExecutor(context);
            mainExecutor.execute(() -> {
                sCameraProvider.unbindAll();
                sCameraProvider = null;
            });
        }
        if (sCameraProviderFuture != null) {
            sCameraProviderFuture.cancel(true);
            sCameraProviderFuture = null;
        }
    }

    // ================================================================================
    // INTERFACE - ImageAnalysis.Analyzer
    // ================================================================================
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        ByteBuffer byteBuffer = imageProxy.getPlanes()[0].getBuffer();
        InputImage image = InputImage.fromByteBuffer(byteBuffer,
                imageProxy.getWidth(),
                imageProxy.getHeight(),
                imageProxy.getImageInfo().getRotationDegrees(),
                InputImage.IMAGE_FORMAT_NV21 // or IMAGE_FORMAT_YV12
        );
        scanImage(image);
        imageProxy.close();
    }

    /**
     * @brief Scans a QR Code image
     * @param image The image to scan
     */
    public void scanImage(InputImage image) {
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder().setBarcodeFormats(
                Barcode.FORMAT_QR_CODE).build();
        BarcodeScanner scanner = BarcodeScanning.getClient(options);
        scanner.process(image).addOnSuccessListener(barcodeList -> {
            for (Barcode barcode: barcodeList) {
                if (mCallback != null) {
                    String rawValue = barcode.getRawValue();
                    // No need to check if rawValue is null; barcodeList will be empty on invalid scan
                    // Remove any URL schema from the result
                    String[] temp = rawValue.split(SCHEME);
                    if (temp.length > 0) {
                        rawValue = temp[temp.length - 1];
                    }
                    mCallback.onScanQRCode(rawValue.trim());
                }
            }
        }).addOnFailureListener(e -> e.printStackTrace());
    }
}
