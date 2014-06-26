/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintPreviewFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.fragment;

import java.util.List;

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
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.LruCache;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

/**
 * @class PrintPreviewFragment
 * 
 * @brief Fragment which contains the Print Preview Screen
 */
public class PrintPreviewFragment extends BaseFragment implements Callback, PDFFileManagerInterface,
        PreviewControlsListener, OnSeekBarChangeListener, PauseableHandlerCallback {

    /// Tag used to identify the error dialog
    public static final String FRAGMENT_TAG_DIALOG = "pdf_error_dialog";
    /// Tag used to identify the print settings fragment
    public static final String FRAGMENT_TAG_PRINTSETTINGS = "fragment_printsettings";
    
    /// Key used to store current page
    private static final String KEY_CURRENT_PAGE = "current_page";
    
    /// Print button view ID
    private static final int ID_PRINT_BUTTON = 0x11000002;

    /// Tag used to identify the dialog message
    private static final String TAG_MESSAGE_DIALOG = "dialog_message";
    
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
    
    @Override
    public int getViewLayout() {
        return R.layout.fragment_printpreview;
    }
    
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
        setRetainInstance(true);
        
        mHandler = new Handler(this);
        if (mPauseableHandler == null) {
            mPauseableHandler = new PauseableHandler(this);
        }

        // Initialize PDF File Manager if it has not been previously initialized yet
        if (mPdfManager == null) {
            Uri data = null;

            data = getActivity().getIntent().getData();
            
            mPdfManager = new PDFFileManager(this);
            
            String pdfInSandbox = PDFFileManager.getSandboxPDFName(SmartDeviceApp.getAppContext());
            if (pdfInSandbox != null) {
                mPdfManager.setSandboxPDF();
                
                // Automatically open asynchronously
                mPdfManager.initializeAsync();
            } else if (data != null) {
                mPdfManager.setPDF(data.getPath());
                
                // Automatically open asynchronously
                mPdfManager.initializeAsync();
            }
        }
        
        if (mPrinterId == PrinterManager.EMPTY_ID) {
            mPrinterId = PrinterManager.getInstance(SmartDeviceApp.getAppContext()).getDefaultPrinter();
            
            if (mPrinterId != -1) {
                mPrintSettings = new PrintSettings(mPrinterId);
            }
        }
        
        // Initialize Print Settings if it has not been previously initialized yet
        if (mPrintSettings == null) {
            mPrintSettings = new PrintSettings();
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
        
        if (savedInstanceState != null) {
            mCurrentPage = savedInstanceState.getInt(KEY_CURRENT_PAGE, 0);
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
    }
    
    @Override
    public void clearIconStates() {
        super.clearIconStates();
        setIconState(ID_PRINT_BUTTON, false);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        PrinterManager printerManager = PrinterManager.getInstance(SmartDeviceApp.getAppContext());
        
        if (!printerManager.isExists(mPrinterId)) {
            setPrintId(printerManager.getDefaultPrinter());
            setPrintSettings(new PrintSettings(mPrinterId));
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
        addMenuButton(v, R.id.rightActionLayout, ID_PRINT_BUTTON, R.drawable.selector_actionbar_printsettings, this);
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
        if (v.findViewById(ID_PRINT_BUTTON) != null) {
            if (show) {
                v.findViewById(ID_PRINT_BUTTON).setVisibility(View.VISIBLE);
            } else {
                v.findViewById(ID_PRINT_BUTTON).setVisibility(View.GONE);
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
            case ID_PRINT_BUTTON:
                Message msg = Message.obtain(mPauseableHandler, ID_PRINT_BUTTON);
                msg.obj = v;
                mPauseableHandler.sendMessage(msg);
                break;
            case ID_MENU_ACTION_BUTTON:
                mPauseableHandler.sendEmptyMessage(ID_MENU_ACTION_BUTTON);
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
            case ID_PRINT_BUTTON:
                mPauseableHandler.pause();
                PrinterManager printerManager = PrinterManager.getInstance(SmartDeviceApp.getAppContext());
                
                if (printerManager.getDefaultPrinter() == PrinterManager.EMPTY_ID) {
                    String strMsg = getString(R.string.ids_err_msg_no_selected_printer);
                    String btnMsg = getString(R.string.ids_lbl_ok);
                    InfoDialogFragment fragment = InfoDialogFragment.newInstance(strMsg, btnMsg);
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
            case ID_MENU_ACTION_BUTTON:
                mPauseableHandler.pause();
                MainActivity activity = (MainActivity) getActivity();
                activity.openDrawer(Gravity.LEFT);
                break;
        }
    }
}
