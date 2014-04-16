/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintSettingsFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.fragment;

import java.util.Date;

import jp.co.riso.android.dialog.DialogUtils;
import jp.co.riso.android.dialog.WaitingDialogFragment;
import jp.co.riso.android.dialog.WaitingDialogFragment.WaitingDialogListener;
import jp.co.riso.android.os.pauseablehandler.PauseableHandler;
import jp.co.riso.android.os.pauseablehandler.PauseableHandlerCallback;
import jp.co.riso.smartdeviceapp.R;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.common.DirectPrintManager;
import jp.co.riso.smartdeviceapp.common.DirectPrintManager.DirectPrintCallback;
import jp.co.riso.smartdeviceapp.controller.jobs.PrintJobManager;
import jp.co.riso.smartdeviceapp.model.PrintJob.JobResult;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartdeviceapp.view.printsettings.PrintSettingsView;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

public class PrintSettingsFragment extends BaseFragment implements PrintSettingsView.PrintSettingsViewInterface, PauseableHandlerCallback, DirectPrintCallback, WaitingDialogListener {
    public static final String TAG = "PrintSettingsFragment";
    
    private DirectPrintManager mDirectPrintManager = null;
    
    private static final int MSG_PRINT = 0;
    
    private static final int PRINT_STATUS_ERROR_CONNECTING =-4;
    private static final int PRINT_STATUS_ERROR_SENDING = -3;
    private static final int PRINT_STATUS_ERROR_FILE = -2;
    private static final int PRINT_STATUS_ERROR = -1;
    private static final int PRINT_STATUS_SENT = 4;
    
    private boolean mFragmentForPrinting = false;
    
    private int mPrinterId;
    private PrintSettings mPrintSettings;
    private PrintSettingsView mPrintSettingsView;
    private Bundle mPrintSettingsBundle = null;
    
    private String mPdfPath;
    private PauseableHandler mPauseableHandler;
    private WaitingDialogFragment mWaitingDialog;
    
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
        if (mPauseableHandler == null) {
            mPauseableHandler = new PauseableHandler(this);
        }
        
        if (mDirectPrintManager  == null) {
            mDirectPrintManager = new DirectPrintManager();
            mDirectPrintManager.setCallback(this);
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
    
    @Override
    public void onPause() {
        super.onPause();
        mPauseableHandler.pause();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        mPauseableHandler.resume();
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
    
    /**
     * This method sets the value of mPdfPath
     * 
     * @param pdfPath
     *            the PDF sandbox path
     */
    public void setPdfPath(String pdfPath) {
        mPdfPath = pdfPath;
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
        
        if (!mFragmentForPrinting) {
            printSettings.savePrintSettingToDB(mPrinterId);
        }
        
        if (getTargetFragment() instanceof PrintPreviewFragment) {
            PrintPreviewFragment fragment = (PrintPreviewFragment) getTargetFragment();
            fragment.setPrintSettings(printSettings);
        }
    }
    
    /**
     * This method is triggered when the print button is pressed and displays the waiting dialog.
     */
    @Override
    public void onPrint(Printer printer, PrintSettings printSettings) {
        if (printer == null || printSettings == null){
            return;
        }
        
        mWaitingDialog = WaitingDialogFragment.newInstance(null, getResources().getString(R.string.ids_lbl_printing), true);
        mWaitingDialog.setTargetFragment(this, 0);
        DialogUtils.displayDialog(getActivity(), TAG, mWaitingDialog);
        
        String jobname = mPdfPath.substring(mPdfPath.lastIndexOf("/") + 1);
        
        mDirectPrintManager.initializeDirectPrint(jobname, mPdfPath, printSettings.formattedString(), printer.getIpAddress());
        mDirectPrintManager.lprPrint();
    }
    
    // ================================================================================
    // INTERFACE - PauseableHandlerCallback
    // ================================================================================
    
    @Override
    public boolean storeMessage(Message message) {
        return message.what == MSG_PRINT;
    }
    
    @Override
    public void processMessage(Message message) {
        switch (message.what) {
            case MSG_PRINT:
                PrintJobManager pm = PrintJobManager.getInstance(SmartDeviceApp.getAppContext());
                String filename = mPdfPath.substring(mPdfPath.lastIndexOf("/") + 1);
                
                if (message.arg1 == PRINT_STATUS_SENT) {
                    pm.createPrintJob(mPrinterId, filename, new Date(), JobResult.SUCCESSFUL);
                }
                else {
                    pm.createPrintJob(mPrinterId, filename, new Date(), JobResult.ERROR);
                }
                
                mWaitingDialog.dismiss();
                ((HomeFragment) getFragmentManager().findFragmentById(R.id.leftLayout)).goToJobsFragment();
        }
    }
    
    // ================================================================================
    // INTERFACE - DirectPrintCallback
    // ================================================================================
    
    @Override
    public void onNotifyProgress(DirectPrintManager manager, int status, float progress) {
        switch (status) {
            case PRINT_STATUS_ERROR_CONNECTING:
            case PRINT_STATUS_ERROR_SENDING:
            case PRINT_STATUS_ERROR_FILE:
            case PRINT_STATUS_ERROR:
            case PRINT_STATUS_SENT :
                Message newMessage = Message.obtain(mPauseableHandler, MSG_PRINT);
                newMessage.arg1 = status;
                mPauseableHandler.sendMessage(newMessage);
                manager.finalizeDirectPrint();
                break;
        }
    }
    
    // ================================================================================
    // INTERFACE - WaitingDialogListener
    // ================================================================================
    
    @Override
    public void onCancel() {
        mDirectPrintManager.cancel();
    }
    
    
}
