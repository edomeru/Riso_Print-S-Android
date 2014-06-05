/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintSettingsFragment.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.view.fragment;

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
import jp.co.riso.smartprint.R;
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
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

public class PrintSettingsFragment extends BaseFragment implements PrintSettingsView.PrintSettingsViewInterface, PauseableHandlerCallback, DirectPrintCallback, WaitingDialogListener {
    
    private static final String TAG_WAITING_DIALOG = "dialog_printing";
    private static final String TAG_MESSAGE_DIALOG = "dialog_message";
    
    private DirectPrintManager mDirectPrintManager = null;
    
    private static final int MSG_PRINT = 0;
    
    private boolean mFragmentForPrinting = false;
    
    private int mPrinterId = PrinterManager.EMPTY_ID;
    private PrintSettings mPrintSettings;
    private PrintSettingsView mPrintSettingsView;
    private Bundle mPrintSettingsBundle = null;
    
    private String mPdfPath;
    private PauseableHandler mPauseableHandler;
    private WaitingDialogFragment mWaitingDialog;
    private String mPrintMsg = "";
    
    /** {@inheritDoc} */
    @Override
    public int getViewLayout() {
        return R.layout.fragment_printsettings;
    }
    
    /** {@inheritDoc} */
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
    
    /** {@inheritDoc} */
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
    
    /** {@inheritDoc} */
    @Override
    public void initializeCustomActionBar(View view, Bundle savedInstanceState) {
    }
    
    /** {@inheritDoc} */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        if (mPrintSettingsView != null) {
            mPrintSettingsBundle = new Bundle();
            mPrintSettingsView.saveState(mPrintSettingsBundle);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void onPause() {
        super.onPause();
        
        PrinterManager.getInstance(SmartDeviceApp.getAppContext()).cancelUpdateStatusThread();
        mPauseableHandler.pause();
    }
    
    /** {@inheritDoc} */
    @Override
    public void onResume() {
        super.onResume();
        mPauseableHandler.resume();
    }
    
    // ================================================================================
    // Public functions
    // ================================================================================
    
    /**
     * Set fragment for printing
     * 
     * @param fragmentForPrinting
     */
    public void setFragmentForPrinting(boolean fragmentForPrinting) {
        mFragmentForPrinting = fragmentForPrinting;
    }
    
    /**
     * Set printer ID
     * 
     * @param printerId
     */
    public void setPrinterId(int printerId) {
        mPrinterId = printerId;
    }
    
    /**
     * Set print settings
     * 
     * @param printSettings
     */
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
    
    /** {@inheritDoc} */
    @Override
    public void onPrinterIdSelectedChanged(int printerId) {
        setPrinterId(printerId);
        
        if (getTargetFragment() instanceof PrintPreviewFragment) {
            PrintPreviewFragment fragment = (PrintPreviewFragment) getTargetFragment();
            fragment.setPrintId(printerId);
        }
    }
    
    /** {@inheritDoc} */
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
        
        if (!NetUtils.isNetworkAvailable(getActivity())) {
            String strMsg = getString(R.string.ids_err_msg_network_error);
            String btnMsg = getString(R.string.ids_lbl_ok);
            InfoDialogFragment fragment = InfoDialogFragment.newInstance(strMsg, btnMsg);
            DialogUtils.displayDialog(getActivity(), TAG_MESSAGE_DIALOG, fragment);
            return;
        }
        
        String btnMsg = getResources().getString(R.string.ids_lbl_cancel);
        mWaitingDialog = WaitingDialogFragment.newInstance(null, mPrintMsg, true, btnMsg);
        mWaitingDialog.setTargetFragment(this, 0);
        DialogUtils.displayDialog(getActivity(), TAG_WAITING_DIALOG, mWaitingDialog);
        
        String jobname = PDFFileManager.getSandboxPDFName(SmartDeviceApp.getAppContext());
        
        mDirectPrintManager = new DirectPrintManager();
        mDirectPrintManager.setCallback(this);
        
        
        String userName = AppUtils.getOwnerName();

        if (printer.getPortSetting() == PortSetting.LPR) {
            mDirectPrintManager.executeLPRPrint(userName, jobname, mPdfPath, printSettings.formattedString(), printer.getIpAddress());
        } else {
            mDirectPrintManager.executeRAWPrint(userName, jobname, mPdfPath, printSettings.formattedString(), printer.getIpAddress());
        }
    }
    
    // ================================================================================
    // INTERFACE - PauseableHandlerCallback
    // ================================================================================
    
    /** {@inheritDoc} */
    @Override
    public boolean storeMessage(Message message) {
        return message.what == MSG_PRINT;
    }
    
    /** {@inheritDoc} */
    @Override
    public void processMessage(Message message) {
        switch (message.what) {
            case MSG_PRINT:
                DialogUtils.dismissDialog(getActivity(), TAG_WAITING_DIALOG);
                
                PrintJobManager pm = PrintJobManager.getInstance(SmartDeviceApp.getAppContext());
                String filename = PDFFileManager.getSandboxPDFName(SmartDeviceApp.getAppContext());
                
                if (message.arg1 == DirectPrintManager.PRINT_STATUS_SENT) {
                    pm.createPrintJob(mPrinterId, filename, new Date(), JobResult.SUCCESSFUL);
                    ((PrintPreviewFragment) getFragmentManager().findFragmentById(R.id.mainLayout)).clearIconStates();
                    ((HomeFragment) getFragmentManager().findFragmentById(R.id.leftLayout)).goToJobsFragment();
                    
                    // Show dialog
                    String strMsg = getString(R.string.ids_info_msg_print_job_successful);
                    String btnMsg = getString(R.string.ids_lbl_ok);
                    InfoDialogFragment fragment = InfoDialogFragment.newInstance(strMsg, btnMsg);
                    DialogUtils.displayDialog(getActivity(), TAG_MESSAGE_DIALOG, fragment);
                }
                else {
                    pm.createPrintJob(mPrinterId, filename, new Date(), JobResult.ERROR);
                    
                    // Show dialog
                    String strMsg = getString(R.string.ids_info_msg_print_job_failed);
                    String btnMsg = getString(R.string.ids_lbl_ok);
                    InfoDialogFragment fragment = InfoDialogFragment.newInstance(strMsg, btnMsg);
                    DialogUtils.displayDialog(getActivity(), TAG_MESSAGE_DIALOG, fragment);
                }
                break;
        }
    }
    
    // ================================================================================
    // INTERFACE - DirectPrintCallback
    // ================================================================================
    
    /** {@inheritDoc} */
    @Override
    public void onNotifyProgress(DirectPrintManager manager, int status, float progress) {
        if (NetUtils.isNetworkAvailable(SmartDeviceApp.getAppContext())) {
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
                        String msg = String.format(Locale.getDefault(), "%s %.2f%%", mPrintMsg, progress);
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
    
    /** {@inheritDoc} */
    @Override
    public void onCancel() {
        if (mDirectPrintManager != null) {
            mDirectPrintManager.sendCancelCommand();
            mDirectPrintManager = null;
        }
    }
}
