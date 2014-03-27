/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintPreviewFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.fragment;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.LruCache;
import android.view.Gravity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import jp.co.riso.android.dialog.DialogUtils;
import jp.co.riso.android.dialog.InfoDialogFragment;
import jp.co.riso.android.util.AppUtils;
import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManager;
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManagerInterface;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.model.PrintSettings;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartdeviceapp.view.preview.PrintPreviewView;

public class PrintPreviewFragment extends BaseFragment implements PDFFileManagerInterface {
    public static final String TAG = "PrintPreviewFragment";
    
    public static final String KEY_CURRENT_PAGE = "current_page";
    public static final int ID_PRINT_BUTTON = 0x11000002;
    
    public static final String FRAGMENT_TAG_DIALOG = "pdf_error_dialog";
    public static final String FRAGMENT_TAG_PRINTSETTINGS = "fragment_printsettings";
    
    private PDFFileManager mPdfManager = null;
    
    private int mPrinterId = PrinterManager.EMPTY_ID;
    private PrintSettings mPrintSettings;
    
    private PrintPreviewView mPrintPreviewView = null;
    private ProgressBar mProgressBar = null;
    private View mOpenInView = null;
    
    private int mCurrentPage = 0;
    
    private LruCache<String, Bitmap> mBmpCache;
    
    public PrintPreviewFragment() {
        
    }
    
    @Override
    public int getViewLayout() {
        return R.layout.fragment_printpreview;
    }
    
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
        setRetainInstance(true);
        
        // Initialize PDF File Manager if it has not been previously initialized yet
        if (mPdfManager == null) {
            Uri data = null;
            
            if (getActivity().getIntent().getData() != null) {
                data = getActivity().getIntent().getData();
            }

            /*
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean(PDFFileManager.KEY_NEW_PDF_DATA, true);
            edit.commit();
            data = Uri.parse(getActivity().getExternalFilesDir("pdfs")+"/PDF-270MB_134pages.pdf");
            //data = Uri.parse(getActivity().getExternalFilesDir("pdfs")+"/PDF-squarish.pdf");
            */
            mPdfManager = new PDFFileManager(this);
            
            if (data != null) {
                mPdfManager.setPDF(data.getPath());
                
                // Automatically open asynchronously
                mPdfManager.initializeAsync();
            }
        }
        
        if (mPrinterId == PrinterManager.EMPTY_ID) {
            mPrinterId = PrinterManager.sharedManager(SmartDeviceApp.getAppContext()).getDefaultPrinter();
            
            if (mPrinterId != -1) {
                mPrintSettings = new PrintSettings(mPrinterId);
            }
        }
        
        // Initialize Print Settings if it has not been previously initialized yet
        if (mPrintSettings == null) {
            mPrintSettings = new PrintSettings();
        }
        
        if (mBmpCache == null) {
            
            int cacheSize = AppUtils.getCacheSizeBasedOnMemoryClass(getActivity());
            cacheSize = cacheSize >> AppConstants.APP_BMP_CACHE_PART; // 1/8
            mBmpCache = new LruCache<String, Bitmap>(cacheSize) {
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
        mPrintPreviewView.setPrintSettings(mPrintSettings);
        mPrintPreviewView.setBmpCache(mBmpCache);
        
        if (mCurrentPage != 0) {
            mPrintPreviewView.setCurrentPage(mCurrentPage);
        }
        if (mPdfManager.isInitialized()) {
            mPrintPreviewView.refreshView();
        }
        
        mOpenInView = view.findViewById(R.id.openInView);
        mProgressBar = (ProgressBar) view.findViewById(R.id.pdfLoadIndicator);
        
        mProgressBar.setVisibility(View.GONE);
        
        // Hide appropriate views
        setPrintPreviewViewDisplayed(view, mPdfManager.getFileName() != null);
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
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_app_name);
        
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
    public void onResume() {
        super.onResume();
        
        // The activity must call the GL surface view's onResume() on activity onResume().
        mPrintPreviewView.onResume();
    }
    
    @Override
    public void onPause() {
        super.onPause();
        
        // The activity must call the GL surface view's onPause() on activity onPause().
        mPrintPreviewView.onPause();
    }
    
    // ================================================================================
    // Public functions
    // ================================================================================
    
    public void addPrintButton(View v) {
        addMenuButton(v, R.id.rightActionLayout, ID_PRINT_BUTTON, R.drawable.selector_actionbar_printsettings, this);
    }
    
    public void setPrintId(int printerId) {
        mPrinterId = printerId;
    }
    
    public void setPrintSettings(PrintSettings printSettings) {
        mPrintSettings = new PrintSettings(printSettings);
        if (mPrintPreviewView != null) {
            mPrintPreviewView.setPrintSettings(mPrintSettings);
            mPrintPreviewView.refreshView();
        }
    }
    
    public void setPrintPreviewViewDisplayed(View v, boolean show) {
        if (show) {
            mProgressBar.setVisibility(View.GONE);
            mOpenInView.setVisibility(View.GONE);
            if (!mPdfManager.isInitialized()) {
                mPrintPreviewView.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                showPrintSettingsButton(v, false);
            } else {
                mPrintPreviewView.setVisibility(View.VISIBLE);
                mPrintPreviewView.refreshView();
                showPrintSettingsButton(v, true);
            }
        } else {
            mPrintPreviewView.setVisibility(View.GONE);
            showPrintSettingsButton(v, false);
        }
    }
    
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
    
    private String getPdfErrorMessage(int status) {
        switch (status) {
            case PDFFileManager.PDF_ENCRYPTED:
                return getResources().getString(R.string.ids_err_msg_pdf_encrypted);
            case PDFFileManager.PDF_UNKNOWN_ENCRYPTION:
                return getResources().getString(R.string.ids_err_msg_open_failed);
            case PDFFileManager.PDF_DAMAGED:
                return getResources().getString(R.string.ids_err_msg_open_failed);
            case PDFFileManager.PDF_INVALID_PATH:
                return getResources().getString(R.string.ids_err_msg_open_failed);
        }
        return null;
    }
    
    // ================================================================================
    // INTERFACE - View.OnLayoutChangeListener
    // ================================================================================
    
    /** {@inheritDoc} */
    @Override
    public void onClick(View v) {
        super.onClick(v);
        
        switch (v.getId()) {
            case ID_PRINT_BUTTON:
                if (getActivity() != null && getActivity() instanceof MainActivity) {
                    MainActivity activity = (MainActivity) getActivity();
                    
                    if (!activity.isDrawerOpen(Gravity.RIGHT)) {
                        FragmentManager fm = getFragmentManager();
                        
                        // Always make new
                        PrintSettingsFragment fragment = null;// (PrintSettingsFragment) fm.findFragmentByTag(FRAGMENT_TAG_PRINTSETTINGS);
                        if (fragment == null) {
                            FragmentTransaction ft = fm.beginTransaction();
                            fragment = new PrintSettingsFragment();
                            ft.replace(R.id.rightLayout, fragment, FRAGMENT_TAG_PRINTSETTINGS);
                            ft.commit();
                        }
                        
                        fragment.setPrinterId(mPrinterId);
                        fragment.setPrintSettings(mPrintSettings);
                        fragment.setFragmentForPrinting(true);
                        fragment.setTargetFragment(this, 0);
                        
                        activity.openDrawer(Gravity.RIGHT, true);
                    } else {
                        activity.closeDrawers();
                    }
                }
                break;
        }
    }
    
    // ================================================================================
    // INTERFACE - PDFFileManagerInterface
    // ================================================================================
    
    @Override
    public void onFileInitialized(int status) {
        setPrintPreviewViewDisplayed(getView(), false);
        
        switch (status) {
            case PDFFileManager.PDF_OK:
                setPrintPreviewViewDisplayed(getView(), true);
                
                break;
            case PDFFileManager.PDF_ENCRYPTED:
            case PDFFileManager.PDF_UNKNOWN_ENCRYPTION:
            case PDFFileManager.PDF_DAMAGED:
            case PDFFileManager.PDF_INVALID_PATH:
                String message = getPdfErrorMessage(status);
                String button = getResources().getString(R.string.ids_lbl_ok);
                DialogUtils.displayDialog(getActivity(), FRAGMENT_TAG_DIALOG, InfoDialogFragment.newInstance(message, button));
                break;
            default:
                break;
        }
        
    }
}
