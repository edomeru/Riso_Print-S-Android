/*
 * Copyright (c) 2014 All rights reserved.
 *
 * HomePreviewFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.view.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import jp.co.riso.android.smartdeviceap.controller.pdf.PDFFileManager;
import jp.co.riso.android.smartdeviceap.controller.pdf.PDFFileManagerInterface;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartdeviceapp.view.preview.PrintPreviewView;

public class HomePreviewFragment extends BaseFragment implements PDFFileManagerInterface{

    public static final String KEY_CURRENT_PAGE = "current_page";
    
    PDFFileManager mPdfManager = null;
    PrintPreviewView mPrintPreviewView = null;
    View mOpenInView = null;
    
    public HomePreviewFragment() {
        setRetainInstance(true);
    }
    
    @Override
    public int getViewLayout() {
        return R.layout.fragment_homepreview;
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        
        Uri data = null;

        if (getActivity().getIntent().getData() != null) {
            data = getActivity().getIntent().getData();
        }
        
        data = Uri.parse(Environment.getExternalStorageDirectory()+"/TestPDFs/PDF-270MB_134pages.pdf");
        
        // Initialize PDF File Manager if it has not been previously initialized yet
        if (mPdfManager == null) {
            mPdfManager = new PDFFileManager(this);
            
            if (data != null) {
                mPdfManager.setPath(data.getPath());
                
                // Automatically open asynchronously
                mPdfManager.openAsync();
            }
        }
        
        mPrintPreviewView = (PrintPreviewView)view.findViewById(R.id.printPreviewView);
        mPrintPreviewView.setPdfManager(mPdfManager);
        
        mOpenInView = view.findViewById(R.id.openInView);

        // Hide appropriate views
        if (data == null) {
            mPrintPreviewView.setVisibility(View.GONE);
        } else {
            mOpenInView.setVisibility(View.GONE);
            if (!mPdfManager.isOpenable()) {
                mPrintPreviewView.setVisibility(View.GONE);
            }
        }
        
        if (savedInstanceState != null) {
            // Restore pages.. state etc.
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
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
    }
    
    @Override
    public void onResume() {
        // The activity must call the GL surface view's onResume() on activity onResume().
        super.onResume();
        mPrintPreviewView.getCurlView().onResume();
    }

    @Override
    public void onPause()
    {
        // The activity must call the GL surface view's onPause() on activity onPause().
        super.onPause();
        mPrintPreviewView.getCurlView().onPause();
    }

    // ================================================================================
    // INTERFACE - PDFFileManagerInterface
    // ================================================================================

    @Override
    public void onFileOpenedResult(int status) {
        mPdfManager.preBuffer();
        
        if (getActivity() != null) {
            Toast.makeText(getActivity(), "file open status: " + status + "\n page count: " + mPdfManager.getPageCount(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPreBufferFinished() {
        if (mPrintPreviewView != null) {
            mPrintPreviewView.setPdfManager(mPdfManager);
            mPrintPreviewView.setVisibility(View.VISIBLE);
        }
    }
    
    
}
