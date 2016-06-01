/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintSettingsFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.fragment;

import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import java.util.Date;
import java.util.Locale;

import jp.co.riso.android.dialog.DialogUtils;
import jp.co.riso.android.dialog.InfoDialogFragment;
import jp.co.riso.android.dialog.WaitingDialogFragment;
import jp.co.riso.android.dialog.WaitingDialogFragment.WaitingDialogListener;
import jp.co.riso.android.os.pauseablehandler.PauseableHandler;
import jp.co.riso.android.os.pauseablehandler.PauseableHandlerCallback;
import jp.co.riso.android.util.AppUtils;
import jp.co.riso.android.util.NetUtils;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.common.DirectPrintManager;
import jp.co.riso.smartdeviceapp.common.DirectPrintManager.DirectPrintCallback;
import jp.co.riso.smartdeviceapp.controller.jobs.PrintJobManager;
import jp.co.riso.smartdeviceapp.controller.pdf.PDFFileManager;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.model.PrintJob.JobResult;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.model.Printer.PortSetting;
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings;
import jp.co.riso.smartdeviceapp.view.base.BaseFragment;
import jp.co.riso.smartdeviceapp.view.printsettings.PrintSettingsView;
import jp.co.riso.smartprint.R;

/**
 * @class PrintSettingsFragment
 * 
 * @brief Fragment which contains the Print Settings Screen
 */
public class PrintSettingsFragment extends BaseFragment implements PrintSettingsView.PrintSettingsViewInterface, PauseableHandlerCallback, DirectPrintCallback, WaitingDialogListener {
    
    private static final String TAG_WAITING_DIALOG = "dialog_printing";
    private static final String TAG_MESSAGE_DIALOG = "dialog_message";
    
    private DirectPrintManager mDirectPrintManager = null;
    
    private static final int MSG_PRINT = 0;
    private static final int REQUEST_CODE_CANCEL = 0;
    
    private boolean mFragmentForPrinting = false;
    
    private int mPrinterId = PrinterManager.EMPTY_ID;
    private PrintSettings mPrintSettings = null;
    private PrintSettingsView mPrintSettingsView = null;
    private Bundle mPrintSettingsBundle = null;
    
    private String mPdfPath = null;
    private boolean mPDFisLandscape = false;
    private PauseableHandler mPauseableHandler = null;
    private WaitingDialogFragment mWaitingDialog = null;
    private String mPrintMsg = "";
    
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
        
        mPrintMsg = getResources().getString(R.string.ids_info_msg_printing);
    }
    
    @Override
    public void initializeView(View view, Bundle savedInstanceState) {
        mPrintSettingsView = (PrintSettingsView) view.findViewById(R.id.rootView);
        
        mPrintSettingsView.setValueChangedListener(this);
        
        mPrintSettingsView.setInitialValues(mPrinterId, mPrintSettings);
        mPrintSettingsView.setShowPrintControls(mFragmentForPrinting);
        
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
        
        PrinterManager.getInstance(SmartDeviceApp.getAppContext()).cancelUpdateStatusThread();
        mPauseableHandler.pause();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        mPauseableHandler.resume();
        
        //update strings in case locale has changed         
        mPrintMsg = getString(R.string.ids_info_msg_printing);
        if (mWaitingDialog != null){
            mWaitingDialog.setButtonText(getString(R.string.ids_lbl_cancel));
        }
    }
    
    // ================================================================================
    // Public functions
    // ================================================================================
    
    /**
     * @brief Sets fragment for printing
     * 
     * @param fragmentForPrinting Whether the fragment is for printing or for default print settings
     */
    public void setFragmentForPrinting(boolean fragmentForPrinting) {
        mFragmentForPrinting = fragmentForPrinting;
    }
    
    /**
     * @brief Sets the printer ID of the view
     * 
     * @param printerId Printer ID
     */
    public void setPrinterId(int printerId) {
        mPrinterId = printerId;
    }

    /**
     * @brief Creates a copy of the print settings to be applied as print settings values
     * 
     * @param printSettings Print settings to be applied
     */
    public void setPrintSettings(PrintSettings printSettings) {
        mPrintSettings = new PrintSettings(printSettings);
    }
    
    /**
     * @brief This method sets the value of mPdfPath
     * 
     * @param pdfPath The path of the PDF to be printed
     */
    public void setPdfPath(String pdfPath) {
        mPdfPath = pdfPath;
    }
    
    /**
     * @brief This method sets whether the PDF is landscape or not
     * 
     * @note Will be passed in the PJL
     * 
     * @param pdfIsLandscape Whether PDF is landscape or not
     */
    public void setPDFisLandscape(boolean pdfIsLandscape) {
        mPDFisLandscape = pdfIsLandscape;
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
    
    @Override
    public void onPrint(Printer printer, PrintSettings printSettings) {
        // do not print if mPdfPath is not set
        if (mPdfPath != null && mPdfPath.isEmpty()) {
            return;
        }
        
        if (printer == null || printSettings == null) {
            String strMsg = getString(R.string.ids_err_msg_no_selected_printer);
            String btnMsg = getString(R.string.ids_lbl_ok);
            InfoDialogFragment fragment = InfoDialogFragment.newInstance(strMsg, btnMsg);
            DialogUtils.displayDialog(getActivity(), TAG_MESSAGE_DIALOG, fragment);
            return;
        }
        
        if (!NetUtils.isWifiAvailable(SmartDeviceApp.getAppContext())) {
            String strMsg = getString(R.string.ids_err_msg_network_error);
            String btnMsg = getString(R.string.ids_lbl_ok);
            InfoDialogFragment fragment = InfoDialogFragment.newInstance(strMsg, btnMsg);
            DialogUtils.displayDialog(getActivity(), TAG_MESSAGE_DIALOG, fragment);
            return;
        }
        
        String btnMsg = null;
        String jobname = PDFFileManager.getSandboxPDFName(SmartDeviceApp.getAppContext());
        
        mDirectPrintManager = new DirectPrintManager();
        mDirectPrintManager.setCallback(this);

        String appName = getResources().getString(R.string.ids_app_name);
        String appVersion = AppUtils.getApplicationVersion(SmartDeviceApp.getAppContext());
        String userName = AppUtils.getOwnerName();
        boolean ret = false;
        
        String formattedString = printSettings.formattedString(mPDFisLandscape);
        if (printer.getPortSetting() == PortSetting.LPR) {
            ret = mDirectPrintManager.executeLPRPrint(printer.getName(), appName, appVersion, userName, jobname, mPdfPath, formattedString, printer.getIpAddress());
        } else {
            ret = mDirectPrintManager.executeRAWPrint(printer.getName(), appName, appVersion, userName, jobname, mPdfPath, formattedString, printer.getIpAddress());
        }
        if (ret) {
            btnMsg = getResources().getString(R.string.ids_lbl_cancel);
            mWaitingDialog = WaitingDialogFragment.newInstance(null, mPrintMsg, true, btnMsg);
            mWaitingDialog.setTargetFragment(this, REQUEST_CODE_CANCEL);
            DialogUtils.displayDialog(getActivity(), TAG_WAITING_DIALOG, mWaitingDialog);
        } else {
            String strMsg = getString(R.string.ids_info_msg_print_job_failed);
            btnMsg = getString(R.string.ids_lbl_ok);
            InfoDialogFragment fragment = InfoDialogFragment.newInstance(strMsg, btnMsg);
            DialogUtils.displayDialog(getActivity(), TAG_MESSAGE_DIALOG, fragment);
        }
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
                DialogUtils.dismissDialog(getActivity(), TAG_WAITING_DIALOG);
                
                PrintJobManager pm = PrintJobManager.getInstance(SmartDeviceApp.getAppContext());
                String filename = PDFFileManager.getSandboxPDFName(SmartDeviceApp.getAppContext());
                InfoDialogFragment fragment = null;
                String strMsg = null;
                String btnMsg = null;
                
                if (message.arg1 == DirectPrintManager.PRINT_STATUS_SENT) {
                    pm.createPrintJob(mPrinterId, filename, new Date(), JobResult.SUCCESSFUL);
                    
                    strMsg = getString(R.string.ids_info_msg_print_job_successful);
                    btnMsg = getString(R.string.ids_lbl_ok);
                } else {
                    pm.createPrintJob(mPrinterId, filename, new Date(), JobResult.ERROR);
                    
                    strMsg = getString(R.string.ids_info_msg_print_job_failed);
                    btnMsg = getString(R.string.ids_lbl_ok);
                }
                // Show dialog
                fragment = InfoDialogFragment.newInstance(strMsg, btnMsg);
                DialogUtils.displayDialog(getActivity(), TAG_MESSAGE_DIALOG, fragment);
                break;
        }
    }
    
    // ================================================================================
    // INTERFACE - DirectPrintCallback
    // ================================================================================
    @Override
    public void onNotifyProgress(DirectPrintManager manager, int status, float progress) {
        if (NetUtils.isWifiAvailable(SmartDeviceApp.getAppContext())) {
            switch (status) {
                case DirectPrintManager.PRINT_STATUS_ERROR_CONNECTING:
                case DirectPrintManager.PRINT_STATUS_ERROR_SENDING:
                case DirectPrintManager.PRINT_STATUS_ERROR_FILE:
                case DirectPrintManager.PRINT_STATUS_ERROR:
                case DirectPrintManager.PRINT_STATUS_SENT:
                    Message newMessage = Message.obtain(mPauseableHandler, MSG_PRINT);
                    newMessage.arg1 = status;
                    mPauseableHandler.sendMessage(newMessage);
                    break;
                case DirectPrintManager.PRINT_STATUS_SENDING:
                    if (mWaitingDialog != null) {
                        String msg = String.format(Locale.getDefault(), "%s %.2f%%", mPrintMsg, Math.min((progress),100.0f));
                        mWaitingDialog.setMessage(msg);
                    }
                    break;
                case DirectPrintManager.PRINT_STATUS_STARTED:
                case DirectPrintManager.PRINT_STATUS_CONNECTING:
                case DirectPrintManager.PRINT_STATUS_CONNECTED:
                    break;
            }
        } else {
            // cancel Direct Print but considered as failed job
            onCancel();
            Message newMessage = Message.obtain(mPauseableHandler, MSG_PRINT);
            mPauseableHandler.sendMessage(newMessage);
        }
    }
    
    // ================================================================================
    // INTERFACE - WaitingDialogListener
    // ================================================================================
    
    @Override
    public void onCancel() {
        if (mDirectPrintManager != null) {
            mDirectPrintManager.sendCancelCommand();
            mDirectPrintManager = null;
        }
    }
}
