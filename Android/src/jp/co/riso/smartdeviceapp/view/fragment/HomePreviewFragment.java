/*
 * Copyright (c) 2014 All rights reserved.
 *
 * HomePreviewFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.fragment;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.LruCache;
import android.view.View;
import android.widget.TextView;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManager;
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManagerInterface;
import jp.co.riso.smartdeviceapp.model.PrintSettings;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartdeviceapp.view.preview.PrintPreviewView;

public class HomePreviewFragment extends BaseFragment implements PDFFileManagerInterface{

    public static final String KEY_CURRENT_PAGE = "current_page";
    
    PDFFileManager mPdfManager = null;
    PrintSettings mPrintSettings;
    
    PrintPreviewView mPrintPreviewView = null;
    View mOpenInView = null;
    
    int mCurrentPage = 0;
    
    private LruCache<String, Bitmap> mBmpCache;
    
    public HomePreviewFragment() {
    }
    
    @Override
    public int getViewLayout() {
        return R.layout.fragment_homepreview;
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
        
            mPdfManager = new PDFFileManager(this);
            
            if (data != null) {
                mPdfManager.setPath(data.getPath());
                
                // Automatically open asynchronously
                mPdfManager.openAsync();
            }
        }
        
        // Initialize Print Settings if it has not been previously initialized yet
        if (mPrintSettings == null) {
            mPrintSettings = new PrintSettings();
        }
        
        if (mBmpCache == null) {
            int cacheSize = 16 * 1024 * 1024; // 16MB
            mBmpCache = new LruCache<String, Bitmap>(cacheSize) {
                protected int sizeOf(String key, Bitmap value) {
                    return value.getByteCount();
                }
            };
        }

        if (savedInstanceState != null) {
            mCurrentPage = savedInstanceState.getInt(KEY_CURRENT_PAGE, 0);
        }
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {

        mPrintPreviewView = (PrintPreviewView)view.findViewById(R.id.printPreviewView);
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

        // Hide appropriate views
        if (mPdfManager.getPath() == null) {
            mPrintPreviewView.setVisibility(View.GONE);
        } else {
            mOpenInView.setVisibility(View.GONE);
            if (!mPdfManager.isInitialized()) {
                mPrintPreviewView.setVisibility(View.GONE);
            }
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mCurrentPage = mPrintPreviewView.getCurrentPage();
        mPrintPreviewView = null;
    }

    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
        TextView textView = (TextView) view.findViewById(R.id.actionBarTitle);
        textView.setText(R.string.ids_app_name);
        
        addActionMenuButton(view);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mCurrentPage = mPrintPreviewView.getCurrentPage();
        outState.putInt(KEY_CURRENT_PAGE, mCurrentPage);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        // The activity must call the GL surface view's onResume() on activity onResume().
        mPrintPreviewView.getCurlView().onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        
        // The activity must call the GL surface view's onPause() on activity onPause().
        mPrintPreviewView.getCurlView().onPause();
    }

    // ================================================================================
    // INTERFACE - PDFFileManagerInterface
    // ================================================================================

    @Override
    public void onFileInitialized(int status) {
        switch (status) {
            case PDFFileManager.PDF_OK:
                if (mPrintPreviewView != null) {
                    mPrintPreviewView.refreshView();
                    mPrintPreviewView.setVisibility(View.VISIBLE);
                }
                if (mOpenInView != null) {
                    mOpenInView.setVisibility(View.GONE);
                }
                
                break;
            case PDFFileManager.PDF_ENCRYPTED:
                break;
            case PDFFileManager.PDF_UNKNOWN_ENCRYPTION:
                break;
            case PDFFileManager.PDF_DAMAGED:
                break;
            case PDFFileManager.PDF_INVALID_PATH:
                break;
            default:
                break;
        }
        
    }
}
