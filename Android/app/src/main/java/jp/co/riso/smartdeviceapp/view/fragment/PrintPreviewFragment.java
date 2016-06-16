/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintPreviewFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.fragment;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.LruCache;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import jp.co.riso.android.dialog.ConfirmDialogFragment;
import jp.co.riso.android.dialog.DialogUtils;
import jp.co.riso.android.dialog.InfoDialogFragment;
import jp.co.riso.android.os.pauseablehandler.PauseableHandler;
import jp.co.riso.android.os.pauseablehandler.PauseableHandlerCallback;
import jp.co.riso.android.util.MemoryUtils;
import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManager;
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManagerInterface;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartdeviceapp.view.preview.PreviewControlsListener;
import jp.co.riso.smartdeviceapp.view.preview.PrintPreviewView;
import jp.co.riso.smartprint.R;

/**
 * @class PrintPreviewFragment
 * 
 * @brief Fragment which contains the Print Preview Screen
 */
public class PrintPreviewFragment extends BaseFragment implements Callback, PDFFileManagerInterface,
        PreviewControlsListener, OnSeekBarChangeListener, PauseableHandlerCallback, ConfirmDialogFragment.ConfirmDialogListener {

    /// Tag used to identify the error dialog
    public static final String FRAGMENT_TAG_DIALOG = "pdf_error_dialog";
    /// Tag used to identify the print settings fragment
    public static final String FRAGMENT_TAG_PRINTSETTINGS = "fragment_printsettings";
    
    /// Key used to store current page
    private static final String KEY_CURRENT_PAGE = "current_page";
    
    /// Tag used to identify the dialog message
    private static final String TAG_MESSAGE_DIALOG = "dialog_message";

    public static final int REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static final String TAG_PERMISSION_DIALOG = "external_storage_tag";
    private static final String KEY_EXTERNAL_STORAGE_DIALOG_OPEN = "key_external_storage_dialog_open";

    private PDFFileManager mPdfManager = null;
    
    private int mPrinterId = PrinterManager.EMPTY_ID;
    private PrintSettings mPrintSettings;
    
    private PrintPreviewView mPrintPreviewView = null;
    private View mPageControls = null;
    private SeekBar mSeekBar = null;
    private TextView mPageLabel = null;
    
    private ProgressBar mProgressBar = null;
    private View mOpenInView = null;
    
    private int mCurrentPage = 0;
    private float mZoomLevel = 1.0f;
    private float mPanX = 0.0f;
    private float mPanY = 0.0f;
    
    private LruCache<String, Bitmap> mBmpCache;
    
    private Handler mHandler;
    private PauseableHandler mPauseableHandler = null;
    private ConfirmDialogFragment mConfirmDialogFragment = null;
    private boolean mIsPermissionDialogOpen;
    private boolean mIsPdfInitialized;
    private boolean mShouldDisplayExplanation;
    private Uri mIntentData;
    private InputStream mInputStream;

    // BTS ID#20039: This flag controls if page index should reset to 1 on PDF
    // initialization. Currently only reset after allowing storage permission to
    // prevent setting to page zero when there is already a PDF file
    private boolean shouldResetToFirstPageOnInitialize = false;

    @Override
    public int getViewLayout() {
        return R.layout.fragment_printpreview;
    }
    
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
        // dismiss permission alert dialog if showing
        DialogUtils.dismissDialog(getActivity(), TAG_PERMISSION_DIALOG);
        setRetainInstance(true);

        if (savedInstanceState != null) {
            mCurrentPage = savedInstanceState.getInt(KEY_CURRENT_PAGE, 0);
            mIsPermissionDialogOpen = savedInstanceState.getBoolean(KEY_EXTERNAL_STORAGE_DIALOG_OPEN, false);
        }

        Intent intent = getActivity().getIntent();
        if (intent != null) {
            mIntentData = intent.getData();
            if (PDFFileManager.getSandboxPDFName(SmartDeviceApp.getAppContext()) == null
                    && mIntentData != null && !mIntentData.getScheme().equals("file")) {
                //resolve content if not from Sandbox or File
                try {
                    ContentResolver c = this.getActivity().getContentResolver();
                    mInputStream = c.openInputStream(mIntentData);
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        mHandler = new Handler(this);
        if (mPauseableHandler == null) {
            mPauseableHandler = new PauseableHandler(this);
        }

        // Initialize PDF File Manager if it has not been previously initialized yet
        if (mPdfManager == null) {

            mPdfManager = new PDFFileManager(this);

            // if has PDF to open and the Android permission dialog is not yet opened, check permission
            if (hasPdfFile() && !mIsPermissionDialogOpen) {
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                == PackageManager.PERMISSION_GRANTED) {
                    initializePdfManagerAndRunAsync();
                } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        mShouldDisplayExplanation = true;
                    } else {
                        mIsPermissionDialogOpen = true;
                        // Request the permission, no explanation needed
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                PrintPreviewFragment.REQUEST_WRITE_EXTERNAL_STORAGE);
                    }
                }
            }
        }

        if (mPrinterId == PrinterManager.EMPTY_ID) {
            mPrinterId = PrinterManager.getInstance(SmartDeviceApp.getAppContext()).getDefaultPrinter();
            
            if (mPrinterId != -1) {
                String printerType = PrinterManager.getInstance(SmartDeviceApp.getAppContext()).getPrinterType(mPrinterId);
                mPrintSettings = new PrintSettings(mPrinterId, printerType);
            }
        }
        
        // Initialize Print Settings if it has not been previously initialized yet
        if (mPrintSettings == null) {
            String printerType = AppConstants.PRINTER_MODEL_IS;
            if (mPrinterId != -1) {
                printerType = PrinterManager.getInstance(SmartDeviceApp.getAppContext()).getPrinterType(mPrinterId);
            }
            mPrintSettings = new PrintSettings(printerType);
        }
        
        if (mBmpCache == null) {
            
            int cacheSize = MemoryUtils.getCacheSizeBasedOnMemoryClass(getActivity());
            cacheSize = cacheSize >> AppConstants.APP_BMP_CACHE_PART; // 1/8
            mBmpCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap value) {
                    return value.getByteCount();
                }
            };
            mBmpCache.evictAll();
        }
    }

    /**
     * @brief Checks if there is a PDF file for display
     *
     * @return true if there is a PDF in sandbox or from open-in
     * @return false if app is opened without PDF to be displayed
     */
    private boolean hasPdfFile() {
        Intent intent = getActivity().getIntent();
        boolean isOpenIn = (intent != null && intent.getData() != null);
        boolean isInSandbox = (PDFFileManager.getSandboxPDFName(SmartDeviceApp.getAppContext()) != null);
        return (isInSandbox || isOpenIn);
    }

    /**
     * @brief Initializes the PDF Manager and runs the PDF task asynchronously
     */
    private void initializePdfManagerAndRunAsync() {
        mIsPdfInitialized = true;
        ((MainActivity) getActivity()).initializeRadaee();

        String pdfInSandbox = PDFFileManager.getSandboxPDFName(SmartDeviceApp.getAppContext());
        if (pdfInSandbox != null) {
            mPdfManager.setSandboxPDF();
            // Automatically open asynchronously
            mPdfManager.initializeAsync(null);
        } else if (mIntentData != null) {
            if (mIntentData.getScheme().equals("file")) {
                mPdfManager.setPDF(mIntentData.getPath());
                // Automatically open asynchronously
                mPdfManager.initializeAsync(null);
            } else {
                mPdfManager.setPDF(null);
                // Automatically open asynchronously
                mPdfManager.initializeAsync(mInputStream);
            }
        }
    }

    @Override
    public void initializeView(View view, Bundle savedInstanceState) {

        mPrintPreviewView = (PrintPreviewView) view.findViewById(R.id.printPreviewView);
        mPrintPreviewView.setPdfManager(mPdfManager);
        //mPrintPreviewView.setShow3Punch(isPrinterJapanese());
        mPrintPreviewView.setPrintSettings(mPrintSettings);
        mPrintPreviewView.setBmpCache(mBmpCache);
        mPrintPreviewView.setListener(this);
        mPrintPreviewView.setPans(mPanX, mPanY);
        mPrintPreviewView.setZoomLevel(mZoomLevel);
        mPrintPreviewView.setVisibility(View.GONE);
        
        mPageControls = view.findViewById(R.id.previewControls);
        
        mPageLabel = (TextView) mPageControls.findViewById(R.id.pageDisplayTextView);
        mSeekBar = (SeekBar) mPageControls.findViewById(R.id.pageSlider);
        mSeekBar.setOnSeekBarChangeListener(this);
        
        if (mCurrentPage != 0) {
            mPrintPreviewView.setCurrentPage(mCurrentPage);
        }

        if (mPdfManager.isInitialized()) {
            mPrintPreviewView.refreshView();
            setTitle(view, mPdfManager.getFileName());
        }
        
        mOpenInView = view.findViewById(R.id.openInView);
        mProgressBar = (ProgressBar) view.findViewById(R.id.pdfLoadIndicator);

        mProgressBar.setVisibility(View.GONE);

        setPrintPreviewViewDisplayed(view, mPdfManager.getFileName() != null);

        Message newMessage = Message.obtain(mHandler, 0);
        newMessage.arg1 = mPrintPreviewView.getVisibility();

        mPrintPreviewView.setVisibility(View.GONE);
        mHandler.sendMessage(newMessage);
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
        if (mPrintPreviewView != null) {
            mCurrentPage = mPrintPreviewView.getCurrentPage();
            mPrintPreviewView.freeResources();
            mPrintPreviewView = null;
        }
    }
    
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        setDefaultTitle(view);
        
        addActionMenuButton(view);
        addPrintButton(view);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        if (mPrintPreviewView != null) {
            mCurrentPage = mPrintPreviewView.getCurrentPage();
            outState.putInt(KEY_CURRENT_PAGE, mCurrentPage);
        }
        outState.putBoolean(KEY_EXTERNAL_STORAGE_DIALOG_OPEN, mIsPermissionDialogOpen);
    }
    
    @Override
    public void clearIconStates() {
        super.clearIconStates();
        setIconState(R.id.view_id_print_button, false);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        if (hasPdfFile() && !mIsPermissionDialogOpen) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                // to enable PDF display when Storage permission is switched from disabled to enabled
                if (mConfirmDialogFragment != null) {
                    DialogUtils.dismissDialog(getActivity(), TAG_PERMISSION_DIALOG);
                }
                if (!mIsPdfInitialized) {
                    initializePdfManagerAndRunAsync();
                }
            } else if (mShouldDisplayExplanation) {
                final MainActivity mainActivity = (MainActivity) getActivity();
                final Handler handler = new Handler(Looper.getMainLooper());
                final Runnable printSettingsDisposer = new Runnable() {
                    @Override
                    public void run() {
                        showPrintSettingsButton(getView(), false);
                        if(mainActivity.isDrawerOpen(Gravity.RIGHT)) {
                            mainActivity.closeDrawers();
                        }
                    }
                };
                // Call with a no-delay handler since not calling inside
                // a main looper handler does not close the drawer
                handler.postDelayed(printSettingsDisposer, 0);
                mPdfManager.clearSandboxPDFName(getActivity());

                // Display an explanation to the user that the permission is needed
                if (mConfirmDialogFragment == null) {
                    final String message = getActivity().getString(R.string.ids_err_msg_storage_permission_not_allowed);
                    final String positiveButton = getActivity().getString(R.string.ids_lbl_ok);
                    mConfirmDialogFragment = ConfirmDialogFragment.newInstance(message, positiveButton, null);
                    mConfirmDialogFragment.setTargetFragment(PrintPreviewFragment.this, 0);
                    DialogUtils.displayDialog(getActivity(), TAG_PERMISSION_DIALOG, mConfirmDialogFragment);
                }
            }
        }
        mShouldDisplayExplanation = false;

        PrinterManager printerManager = PrinterManager.getInstance(SmartDeviceApp.getAppContext());
        
        if (!printerManager.isExists(mPrinterId)) {
            setPrintId(printerManager.getDefaultPrinter());
            String printerType = PrinterManager.getInstance(SmartDeviceApp.getAppContext()).getPrinterType(mPrinterId);

            //use fallthrough printer type IS, if printer does not yet exist
            setPrintSettings(new PrintSettings(mPrinterId, printerType == null ? AppConstants.PRINTER_MODEL_IS : printerType));
        }

        if (mPauseableHandler != null) {
            mPauseableHandler.resume();
        }
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        
        if (mPauseableHandler != null) {
            mPauseableHandler.resume();
        }
        
        if (mPageControls != null) {
            LinearLayout mainView = (LinearLayout) getView().findViewById(R.id.previewView);
            mainView.removeView(mPageControls);
            
            View newView = View.inflate(getActivity(), R.layout.preview_controls, null);
            newView.setLayoutParams(mPageControls.getLayoutParams());
            newView.setVisibility(mPageControls.getVisibility());
            
            // AppUtils.changeChildrenFont((ViewGroup) newView, SmartDeviceApp.getAppFont());

            mainView.addView(newView);
            mPageControls = newView;
            
            mPageLabel = (TextView) mPageControls.findViewById(R.id.pageDisplayTextView);
            mSeekBar = (SeekBar) mPageControls.findViewById(R.id.pageSlider);
            mSeekBar.setOnSeekBarChangeListener(this);

            if (mPageControls.getVisibility() == View.VISIBLE) {
                updateSeekBar();
                updatePageLabel();
            }
        }
    }
    
    // ================================================================================
    // Public functions
    // ================================================================================
    
    /**
     * @brief Add a print button in the action bar
     * 
     * @param v Root view which contains the action bar view
     */
    public void addPrintButton(View v) {
        addMenuButton(v, R.id.rightActionLayout, R.id.view_id_print_button, R.drawable.selector_actionbar_printsettings, this);
    }
    
    /**
     * @brief Sets printer id
     * 
     * @param printerId ID of the printer to be set
     */
    public void setPrintId(int printerId) {
        mPrinterId = printerId;
        if (mPrintPreviewView != null) {
            //mPrintPreviewView.setShow3Punch(isPrinterJapanese());
            mPrintPreviewView.refreshView();
        }
    }
    
    /**
     * @brief Creates a copy of the print settings and applies it in the view
     * 
     * @param printSettings Print settings to be applied
     */
    public void setPrintSettings(PrintSettings printSettings) {
        mPrintSettings = new PrintSettings(printSettings);
        if (mPrintPreviewView != null) {
            mPrintPreviewView.setPrintSettings(mPrintSettings);
            mPrintPreviewView.refreshView();
            updateSeekBar();
            updatePageLabel();
        }
    }
    
    /**
     * @brief Checks if the printer set is Japanese. Uses the punch 3 capability
     * 
     * @return true 3-punch in the printer is available
     * @return false 3-punch in the printer is not available available or no printer is set
     */
    public boolean isPrinterJapanese() {
        if (mPrinterId == PrinterManager.EMPTY_ID) {
            return false;
        }
        
        List<Printer> mList = PrinterManager.getInstance(getActivity()).getSavedPrintersList();
        
        for (Printer printer : mList) {
            if (printer.getId() == mPrinterId) {
                return printer.getConfig().isPunch3Available();
            }
        }
        
        return false;
    }
    
    /**
     * @brief Shows/Hide the Print Preview View.
     * 
     * @param v View which contains the Print Preview components
     * @param show Show or hide the Preview
     */
    public void setPrintPreviewViewDisplayed(View v, boolean show) {
        mProgressBar.setVisibility(View.GONE);
        setDefaultTitle(v);
        if (show) {
            mOpenInView.setVisibility(View.GONE);
            if (!mPdfManager.isInitialized()) {
                mPrintPreviewView.setVisibility(View.GONE);
                mPageControls.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.VISIBLE);
                showPrintSettingsButton(v, false);
            } else {
                mPrintPreviewView.setVisibility(View.VISIBLE);
                mPageControls.setVisibility(View.VISIBLE);
                mPrintPreviewView.reconfigureCurlView();
                mPrintPreviewView.refreshView();
                updateSeekBar();
                updatePageLabel();
                showPrintSettingsButton(v, true);
                setTitle(v, mPdfManager.getFileName());
            }
        } else {
            mOpenInView.setVisibility(View.VISIBLE);
            mPrintPreviewView.setVisibility(View.GONE);
            mPageControls.setVisibility(View.INVISIBLE);
            showPrintSettingsButton(v, false);
        }
    }
    
    /**
     * @brief Sets the default tile of the view (Home)
     * 
     * @param v Root view which contains the action bar view
     */
    public void setDefaultTitle(View v) {
        setTitle(v, getResources().getString(R.string.ids_lbl_home));
    }
    
    /**
     * @brief Sets the default tile of the view
     * 
     * @param v Root view which contains the action bar view
     * @param title String to be displayed in the title bar
     */
    public void setTitle(View v, String title) {
        TextView textView = (TextView) v.findViewById(R.id.actionBarTitle);
        textView.setText(title);
    }
    
    /**
     * @brief Shows/Hide the Print Settings button
     * 
     * @param v View which contains the Print Settings button
     * @param show Show or hide the button
     */
    public void showPrintSettingsButton(View v, boolean show) {
        if (v.findViewById(R.id.view_id_print_button) != null) {
            if (show) {
                v.findViewById(R.id.view_id_print_button).setVisibility(View.VISIBLE);
            } else {
                v.findViewById(R.id.view_id_print_button).setVisibility(View.GONE);
            }
        }
    }
    
    // ================================================================================
    // Private functions
    // ================================================================================
    
    /**
     * @brief Get the PDF error message associated with the status
     * 
     * @param status PDF open status
     * 
     * @retval ids_err_msg_pdf_encrypted Encrypted status
     * @retval ids_err_msg_pdf_printing_not_allowed Print restricted status
     * @retval ids_err_msg_open_failed Open failed status
     * @retval Empty No error status
     */
    private String getPdfErrorMessage(int status) {
        switch (status) {
            case PDFFileManager.PDF_ENCRYPTED:
                return getResources().getString(R.string.ids_err_msg_pdf_encrypted);
            case PDFFileManager.PDF_PRINT_RESTRICTED:
                return getResources().getString(R.string.ids_err_msg_pdf_printing_not_allowed);
            case PDFFileManager.PDF_OPEN_FAILED:
                return getResources().getString(R.string.ids_err_msg_open_failed);
            case PDFFileManager.PDF_NOT_ENOUGH_FREE_SPACE:
                return getResources().getString(R.string.ids_err_msg_not_enough_space);
        }
        
        return "";
    }
    
    // ================================================================================
    // Page Control functions
    // ================================================================================
    
    /**
     * @brief Update the Seek Bar to match the Preview displayed
     */
    private void updateSeekBar() {
        int currentPage = mPrintPreviewView.getCurrentPage();
        int pageCount = mPrintPreviewView.getPageCount();
        
        if (!mPrintPreviewView.getAllowLastPageCurl()) {
            pageCount--;
        }
        
        mSeekBar.setMax(0);
        mSeekBar.setMax(pageCount);
        updateSeekBarProgress(currentPage);
    }

    /**
     * @brief Sets the current seek bar progress to the specified value
     * 
     * @param index Value to be set on the seek bar
     */
    private void updateSeekBarProgress(int index) {
        mSeekBar.setProgress(index);
    }

    /**
     * @brief Updates the displayed page label to match the Preview displayed
     */
    private void updatePageLabel() {
        mPageLabel.setText(mPrintPreviewView.getPageString());
    }
    
    // ================================================================================
    // INTERFACE - View.OnLayoutChangeListener
    // ================================================================================
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.view_id_print_button:
                Message msg = Message.obtain(mPauseableHandler, R.id.view_id_print_button);
                msg.obj = v;
                mPauseableHandler.sendMessage(msg);
                break;
            case R.id.menu_id_action_button:
                mPauseableHandler.sendEmptyMessage(R.id.menu_id_action_button);
                break;
        }
    }
    
    // ================================================================================
    // INTERFACE - PDFFileManagerInterface
    // ================================================================================
    
    @Override
    public void onFileInitialized(int status) {
        if (!isDetached() && getView() != null) {
            setPrintPreviewViewDisplayed(getView(), false);

            switch (status) {
                case PDFFileManager.PDF_OK:
                    setPrintPreviewViewDisplayed(getView(), true);

                    if(shouldResetToFirstPageOnInitialize) {
                        shouldResetToFirstPageOnInitialize = false;
                        mPrintPreviewView.setCurrentPage(0);
                        onIndexChanged(0);
                    }
                    break;
                default:
                    String message = getPdfErrorMessage(status);
                    String button = getResources().getString(R.string.ids_lbl_ok);
                    DialogUtils.displayDialog(getActivity(), FRAGMENT_TAG_DIALOG, InfoDialogFragment.newInstance(message, button));
                    break;
            }
        }
    }
    
    // ================================================================================
    // INTERFACE - PreviewControlsListener
    // ================================================================================
    
    @Override
    public void onIndexChanged(int index) {
        updateSeekBarProgress(index);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updatePageLabel();
            }
        });
    }
    
    @Override
    public int getControlsHeight() {
        return 0;
    }
    
    @Override
    public void panChanged(float panX, float panY) {
        mPanX = panX;
        mPanY = panY;
    }
    
    @Override
    public void zoomLevelChanged(float zoomLevel) {
        mZoomLevel = zoomLevel;
    }
    
    @Override
    public void setControlsEnabled(boolean enable) {
    }
    
    // ================================================================================
    // INTERFACE - OnSeekBarChangeListener
    // ================================================================================
    
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            mPageLabel.setText(mPrintPreviewView.getPageString(progress));
        }
    }
    
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }
    
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        mPrintPreviewView.setCurrentPage(progress);
        updatePageLabel();
    }
    
    // ================================================================================
    // INTERFACE - Callback
    // ================================================================================
    
    @Override
    public boolean handleMessage(Message msg) {
        mPrintPreviewView.reconfigureCurlView();
        mPrintPreviewView.setVisibility(msg.arg1);
        return true;
    }
    
    // ================================================================================
    // INTERFACE - PauseableHandlerCallback
    // ================================================================================
    
    @Override
    public boolean storeMessage(Message msg) {
        return false;
    }
    
    @Override
    public void processMessage(Message msg) {
        switch (msg.what) {
            case R.id.view_id_print_button:
                mPauseableHandler.pause();
                PrinterManager printerManager = PrinterManager.getInstance(SmartDeviceApp.getAppContext());

                if (printerManager.getDefaultPrinter() == PrinterManager.EMPTY_ID) {
                    String titleMsg = getResources().getString(R.string.ids_lbl_print_settings);
                    String strMsg = getString(R.string.ids_err_msg_no_selected_printer);
                    String btnMsg = getString(R.string.ids_lbl_ok);
                    InfoDialogFragment fragment = InfoDialogFragment.newInstance(titleMsg, strMsg, btnMsg);
                    DialogUtils.displayDialog(getActivity(), TAG_MESSAGE_DIALOG, fragment);
                    mPauseableHandler.resume();
                    return;
                }
                if (getActivity() != null && getActivity() instanceof MainActivity) {
                    MainActivity activity = (MainActivity) getActivity();
                    View v = (View) msg.obj;
                    if (!activity.isDrawerOpen(Gravity.RIGHT)) {
                        FragmentManager fm = getFragmentManager();

                        setIconState(v.getId(), true);

                        // Always make new
                        PrintSettingsFragment fragment = null;// (PrintSettingsFragment)
                                                              // fm.findFragmentByTag(FRAGMENT_TAG_PRINTSETTINGS);
                        if (fragment == null) {
                            FragmentTransaction ft = fm.beginTransaction();
                            fragment = new PrintSettingsFragment();
                            ft.replace(R.id.rightLayout, fragment, FRAGMENT_TAG_PRINTSETTINGS);
                            ft.commit();
                        }

                        fragment.setPrinterId(mPrinterId);
                        fragment.setPdfPath(mPdfManager.getPath());
                        fragment.setPDFisLandscape(mPdfManager.isPDFLandscape());
                        fragment.setPrintSettings(mPrintSettings);
                        fragment.setFragmentForPrinting(true);
                        fragment.setTargetFragment(this, 0);

                        activity.openDrawer(Gravity.RIGHT, isTablet());
                    } else {
                        activity.closeDrawers();
                    }
                }
                break;
            case R.id.menu_id_action_button:
                mPauseableHandler.pause();
                MainActivity activity = (MainActivity) getActivity();
                activity.openDrawer(Gravity.LEFT);
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_EXTERNAL_STORAGE: {
                mIsPermissionDialogOpen = false; // the request returned a result hence dialog is closed
                if (grantResults.length > 0) {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // permission was granted, run PDF initializations
                        initializePdfManagerAndRunAsync();
                        shouldResetToFirstPageOnInitialize = true;
                    } else {
                        // permission was denied check if Print Settings is open
                        MainActivity activity = (MainActivity) getActivity();
                        if (activity.isDrawerOpen(Gravity.RIGHT)) {
                            activity.closeDrawers();
                        }
                    }
                }
                return;
            }
        }
    }

    @Override
    @TargetApi(23)
    public void onConfirm() {
        mConfirmDialogFragment = null;
        mIsPermissionDialogOpen = true;
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PrintPreviewFragment.REQUEST_WRITE_EXTERNAL_STORAGE);
    }

    @Override
    public void onCancel() {
        // cancel is disabled since no cancel button
    }
}
