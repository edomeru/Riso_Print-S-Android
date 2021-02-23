package jp.co.riso.smartdeviceapp.view.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import jp.co.riso.android.dialog.ConfirmDialogFragment;
import jp.co.riso.android.dialog.DialogUtils;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.common.JniUtils;
import jp.co.riso.smartdeviceapp.common.QRImageScanner;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartprint.R;

/**
 * @class QRScanFragment
 *
 * @brief Fragment which contains the QR Code Scan Screen
 */
public class QRScanFragment extends BaseFragment implements View.OnClickListener, QRImageScanner.QRImageScanResultCallback, PrinterManager.PrinterSearchCallback, ConfirmDialogFragment.ConfirmDialogListener {
    private static final String KEY_ADD_PRINTER_DIALOG = "add_printer_dialog";
    private static final int REQUEST_CAMERA = 1;

    private PreviewView mViewFinder;
    private ProgressBar mProgressBar;
    private TextView mTextView;
    private PrinterManager mPrinterManager;
    private boolean mPermissionDenied = false;
    private boolean mError = false;
    private boolean mAdded = false;

    public QRScanFragment() {
        // Required empty public constructor
    }

    // ================================================================================
    // INTERFACE - BaseFragment
    // ================================================================================

    @Override
    public int getViewLayout() {
        return R.layout.fragment_q_r_scan;
    }

    @Override
    public void initializeFragment(Bundle savedInstanceState) {
        mPrinterManager = PrinterManager.getInstance(SmartDeviceApp.getAppContext());
        mPrinterManager.setPrinterSearchCallback(this);
    }

    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        mViewFinder = view.findViewById(R.id.qr_view_finder);
        mProgressBar = view.findViewById(R.id.qr_progress_bar);
        mTextView = view.findViewById(R.id.qr_result_text);
        mProgressBar.setVisibility(View.INVISIBLE);
        mTextView.setVisibility(View.INVISIBLE);

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                mPermissionDenied = true;
                showMessage(R.string.ids_err_msg_camera_permission_not_allowed);
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
            }
        } else {
            startCameraCapture();
        }
    }

    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        String title = getResources().getString(R.string.ids_lbl_search_printers);
        TextView textView = view.findViewById(R.id.actionBarTitle);
        textView.setText(title);
        addMenuButton(view, R.id.leftActionLayout, R.id.menu_id_back_button, R.drawable.selector_actionbar_back, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAdded = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mPermissionDenied = false;
            // permission was granted, start camera
            startCameraCapture();
        } else {
            closeScreen();
        }
    }

    // ================================================================================
    // INTERFACE - View.OnClickListener
    // ================================================================================

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.menu_id_back_button) {
            closeScreen();
        }
    }

    // ================================================================================
    // INTERFACE - QRImageScanner.QRImageScanResultCallback
    // ================================================================================

    @Override
    public void onScanQRCode(String result) {
        // If the previous QR code scan results are still being processed, ignore the new results.
        // The new results are likely a repeat of the previous / processing results
        if (mPrinterManager.isSearching() || mAdded || mError || (result == null)) {
            return;
        }

        // Display the result
        getActivity().runOnUiThread(() -> {
            mProgressBar.setVisibility(View.VISIBLE);
            mTextView.setVisibility(View.VISIBLE);
            mTextView.setText(result);
        });

        // Confirm that the result is an IP address
        String ipAddress = JniUtils.validateIpAddress(result);
        if (ipAddress != null) {
            mPrinterManager.searchPrinter(ipAddress);
        } else {
            mError = true;
            showMessage(R.string.ids_err_msg_invalid_ip_address);
        }
    }

    // ================================================================================
    // INTERFACE - PrinterManager.PrinterSearchCallback
    // ================================================================================
    @Override
    public void onPrinterAdd(Printer printer) {
        if (mPrinterManager.isCancelled()) {
            return;
        }

        if (mPrinterManager.isExists(printer)) {
            mError = true;
            showMessage(R.string.ids_err_msg_cannot_add_printer);
        } else if (mPrinterManager.savePrinterToDB(printer, true)) {
            mAdded = true;
        } else {
            mError = true;
            showMessage(R.string.ids_err_msg_db_failure);
        }
    }

    @Override
    public void onSearchEnd() {
        if (mAdded) {
            showMessage(R.string.ids_info_msg_printer_add_successful);
        } else if (!mError) {
            mError = true;
            showMessage(R.string.ids_info_msg_warning_cannot_find_printer);
        }
    }

    // ================================================================================
    // INTERFACE - ConfirmDialogFragment.ConfirmDialogListener
    // ================================================================================

    @Override
    public void onConfirm() {
        if (mPermissionDenied) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA);
        } else {
            finishProcessing();
        }
    }

    @Override
    public void onCancel() {
        finishProcessing();
    }

    // ================================================================================
    // Private Methods
    // ================================================================================
    /**
     * @brief Close the QR Scan screen
     */
    private void closeScreen() {
        FragmentManager fm = getFragmentManager();
        if (fm != null) {
            FragmentTransaction ft = fm.beginTransaction();
            QRImageScanner.stop(getActivity());

            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStack();
                ft.commit();
            }
        }
    }

    /**
     * @brief Start thread to capture images from the camera
     */
    private void startCameraCapture() {
        QRImageScanner.capture(getActivity(), mViewFinder, this);
    }

    /**
     * @brief Display a message in a dialog
     */
    private void showMessage(int messageId) {
        ConfirmDialogFragment info = ConfirmDialogFragment.newInstance(
                getResources().getString(R.string.ids_lbl_add_printer),
                getResources().getString(messageId),
                getResources().getString(R.string.ids_lbl_ok), null);
        info.setTargetFragment(this, 0);
        DialogUtils.displayDialog(getActivity(), KEY_ADD_PRINTER_DIALOG, info);
    }

    /**
     * @brief Finish processing the QR code scan results
     */
    private void finishProcessing() {
        // This method should already be running in the UI thread (from confirm dialog callback)
        mProgressBar.setVisibility(View.INVISIBLE);
        mTextView.setVisibility(View.INVISIBLE);
        mTextView.setText("");

        if (mAdded) {
            closeScreen();
        }
        mError = false;
    }
}