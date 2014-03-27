/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintSettingsFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.fragment;

import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.model.PrintSettings;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartdeviceapp.view.printsettings.PrintSettingsView;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class PrintSettingsFragment extends BaseFragment implements PrintSettingsView.ValueChangedListener {
    public static final String TAG = "PrintSettingsFragment";
    
    private boolean mFragmentForPrinting = false;
    
    private int mPrinterId;
    private PrintSettings mPrintSettings;
    private PrintSettingsView mPrintSettingsView;
    private Bundle mPrintSettingsBundle = null;
    
    @Override
    public int getViewLayout() {
        return R.layout.fragment_printsettings;
    }
    
    @Override
    public void initializeFragment(Bundle savedInstanceState) {
        setRetainInstance(true);
        
        if (mPrintSettings == null) {
            mPrintSettings = new PrintSettings();
        }
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        mPrintSettingsView = (PrintSettingsView) view.findViewById(R.id.rootView);
        
        mPrintSettingsView.setValueChangedListener(this);
        
        mPrintSettingsView.setPrintSettings(mPrintSettings);
        mPrintSettingsView.setShowPrintControls(mFragmentForPrinting);
        mPrintSettingsView.setPrinterId(mPrinterId);

        TextView textView = (TextView) view.findViewById(R.id.titleTextView);
        textView.setText(R.string.ids_lbl_print_settings);
        
        if (!mFragmentForPrinting) {
            textView.setText(R.string.ids_lbl_default_print_settings);
        }
        
        if (mPrintSettingsBundle != null) {
            mPrintSettingsView.restoreState(mPrintSettingsBundle);
            mPrintSettingsBundle = null;
        }
    }
    
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        if (mPrintSettingsView != null) {
            mPrintSettingsBundle = new Bundle();
            mPrintSettingsView.saveState(mPrintSettingsBundle);
        }
    }
    
    // ================================================================================
    // Public functions
    // ================================================================================
    
    public void setFragmentForPrinting(boolean fragmentForPrinting) {
        mFragmentForPrinting = fragmentForPrinting;
    }
    
    public void setPrinterId(int printerId) {
        mPrinterId = printerId;
    }
    
    public void setPrintSettings(PrintSettings printSettings) {
        mPrintSettings = new PrintSettings(printSettings);
    }
    
    // ================================================================================
    // INTERFACE - ValueChangedListener
    // ================================================================================
    
    @Override
    public void onPrinterIdSelectedChanged(int printerId) {
        setPrinterId(printerId);
        
        if (getTargetFragment() instanceof PrintPreviewFragment) {
            PrintPreviewFragment fragment = (PrintPreviewFragment) getTargetFragment();
            fragment.setPrintId(printerId);
        }
    }
    
    @Override
    public void onPrintSettingsValueChanged(PrintSettings printSettings) {
        setPrintSettings(printSettings);
        
        if (getTargetFragment() instanceof PrintPreviewFragment) {
            PrintPreviewFragment fragment = (PrintPreviewFragment) getTargetFragment();
            fragment.setPrintSettings(printSettings);
        }
    }
}
