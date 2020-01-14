/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * DirectPrintManager.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.common;

import java.lang.ref.WeakReference;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;

/**
 * @class DirectPrintManager
 * 
 * @brief Helper class for printing PDF files with PJL settings.
 */
public class DirectPrintManager {
    private long mJob = 0;
    private WeakReference<DirectPrintCallback> mCallbackRef = null;
    
    public static final int PRINT_STATUS_ERROR_CONNECTING = -4; ///< Error while connecting to printer
    public static final int PRINT_STATUS_ERROR_SENDING = -3; ///< Error while sending file
    public static final int PRINT_STATUS_ERROR_FILE = -2; ///< Error while opening file
    public static final int PRINT_STATUS_ERROR = -1; ///< Error while starting print
    public static final int PRINT_STATUS_STARTED = 0; ///< Print has started
    public static final int PRINT_STATUS_CONNECTING = 1; ///< Connecting to the printer
    public static final int PRINT_STATUS_CONNECTED = 2; ///< Connected to the printer
    public static final int PRINT_STATUS_SENDING = 3; ///< Sending file to the printer
    public static final int PRINT_STATUS_SENT = 4; ///< File is successfully sent to the printer
    public static final int PRINT_STATUS_JOB_NUM_UPDATE = 5; ///< Update job number - LPR print retry

    /**
     * @brief Initializes Direct Print.
     * 
     * @param userName To be sent as OwnerName in the PJL command.
     * @param jobName Name of the Print Job.
     * @param fileName File name of the PDF.
     * @param printSetting Formatted string of the print settings.
     * @param ipAddress IP address of the printer.
     */
    @SuppressWarnings("JniMissingFunction")
    // Ver.2.0.4.2 Start
    //private native void initializeDirectPrint(String printerName, String appName, String appVersion, String userName, String jobName, String fileName, String printSetting, String ipAddress);
    //private native void initializeDirectPrint(String printerName, String appName, String appVersion, String userName, String jobName, String fileName, String printSetting, String ipAddress, String hostName);
    // Ver.2.0.4.2 End
    // Ver.2.2.0.0 Start
    private native void initializeDirectPrint(String printerName, String appName, String appVersion, String userName, String jobName, String fileName, String printSetting, String ipAddress, String hostName, int jobNumber);
    // Ver.2.2.0.0 End

    /**
     * @brief Finalizes Direct Print.
     */
    @SuppressWarnings("JniMissingFunction")
    private native void finalizeDirectPrint();

    /**
     * @brief Executes an LPR Print.
     */
    @SuppressWarnings("JniMissingFunction")
    private native void lprPrint();

    /**
     * @brief Executes a RAW Print.
     */
    @SuppressWarnings("JniMissingFunction")
    private native void rawPrint();

    /**
     * @brief Cancels Direct Print.
     */
    @SuppressWarnings("JniMissingFunction")
    private native void cancel();
    
    
    /**
     * @brief Sets the callback for the DirectPrint Manager.
     * 
     * @param callback Callback function
     */
    public void setCallback(DirectPrintCallback callback) {
        mCallbackRef = new WeakReference<DirectPrintCallback>(callback);
    }
    
    /**
     * @brief Executes an LPR Print.
     * 
     * @param userName To be sent as OwnerName in the PJL command.
     * @param jobName Name of the Print Job.
     * @param fileName File name of the PDF.
     * @param printSetting Formatted string of the print settings.
     * @param ipAddress IP address of the printer.
     * @param hostName The name of the industrial design.
     * 
     * @retval true Print execution is started
     * @retval false Print not executed
     */
    // Ver.2..0.4.2 Start
    /*
    public boolean executeLPRPrint(String printerName, String appName, String appVersion, String userName, String jobName, String fileName, String printSetting, String ipAddress) {
        if (printerName == null || appName == null || appVersion == null || userName == null || jobName == null || fileName == null || printSetting == null || ipAddress == null || jobName.isEmpty()
                || printerName.isEmpty() || appName.isEmpty() || appVersion.isEmpty() || fileName.isEmpty() || printSetting.isEmpty() || ipAddress.isEmpty()) {
            return false;
        }
      initializeDirectPrint(printerName, appName, appVersion, userName, jobName, fileName, printSetting, ipAddress);
    */
    public boolean executeLPRPrint(String printerName, String appName, String appVersion, String userName, String jobName, String fileName, String printSetting, String ipAddress, String hostName) {
        if (printerName == null || appName == null || appVersion == null || userName == null || jobName == null || fileName == null || printSetting == null || ipAddress == null || hostName == null || jobName.isEmpty()
                || printerName.isEmpty() || appName.isEmpty() || appVersion.isEmpty() || fileName.isEmpty() || printSetting.isEmpty() || ipAddress.isEmpty() || hostName.isEmpty()) {
            return false;
        }
        //initializeDirectPrint(printerName, appName, appVersion, userName, jobName, fileName, printSetting, ipAddress, hostName);
        // Ver.2.0.4.2 End
        // Ver.2.2.0.0 Start

        // LPR Cancel Fix: Set a unique job number for print job (0-999)
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.getAppContext());
        int jobNumber = preferences.getInt(AppConstants.PREF_KEY_JOB_NUMBER_COUNTER, AppConstants.PREF_DEFAULT_JOB_NUMBER_COUNTER);
        updateJobNumber();
        initializeDirectPrint(printerName, appName, appVersion, userName, jobName, fileName, printSetting, ipAddress, hostName, jobNumber);
        // Ver.2.2.0.0 End
        if (isPrinting()) {
            lprPrint();
            return true;
        }
        return false;
    }
    
    /**
     * @brief Executes a RAW Print.
     * 
     * @param userName To be sent as OwnerName in the PJL command.
     * @param jobName Name of the Print Job.
     * @param fileName File name of the PDF.
     * @param printSetting Formatted string of the print settings.
     * @param ipAddress IP address of the printer.
     * @param hostName The name of the industrial design.
     *
     * @retval true Print execution is started
     * @retval false Print not executed
     */
    // Ver.2.0.4.2 Start
    /*
    public boolean executeRAWPrint(String printerName, String appName, String appVersion, String userName, String jobName, String fileName, String printSetting, String ipAddress) {
        if (printerName == null || appName == null || appVersion == null || userName == null || jobName == null || fileName == null || printSetting == null || ipAddress == null
                || printerName.isEmpty() || appName.isEmpty() || appVersion.isEmpty() || jobName.isEmpty() || fileName.isEmpty() || printSetting.isEmpty() || ipAddress.isEmpty()) {
            return false;
        }

        initializeDirectPrint(printerName, appName, appVersion, userName, jobName, fileName, printSetting, ipAddress);
    */

    public boolean executeRAWPrint(String printerName, String appName, String appVersion, String userName, String jobName, String fileName, String printSetting, String ipAddress, String hostName) {
        if (printerName == null || appName == null || appVersion == null || userName == null || jobName == null || fileName == null || printSetting == null || ipAddress == null || hostName == null
                || printerName.isEmpty() || appName.isEmpty() || appVersion.isEmpty() || jobName.isEmpty() || fileName.isEmpty() || printSetting.isEmpty() || ipAddress.isEmpty() || hostName.isEmpty()) {
            return false;
        }

        // initializeDirectPrint(printerName, appName, appVersion, userName, jobName, fileName, printSetting, ipAddress, hostName);
        // Ver.2.0.4.2 End
        // Ver.2.2.0.0 Start
        // Set job number to default (1)
        initializeDirectPrint(printerName, appName, appVersion, userName, jobName, fileName, printSetting, ipAddress, hostName, 1);
        // Ver.2.2.0.0 End
        if (isPrinting()) {
            rawPrint();
            return true;
        }
        return false;
    }
    
    /**
     * @brief Checks if print is ongoing.
     * 
     * @retval true Print is ongoing
     * @retval false No ongoing print job
     */
    public boolean isPrinting() {
        return (mJob != 0);
    }
    
    /**
     * @brief Sends cancel command.
     * 
     * Cancel Print task.
     */
    public void sendCancelCommand() {
        setCallback(null);
        (new DirectPrintCancelTask(this)).execute();
    }
    
    static {
        System.loadLibrary("common");
    }
    
    /**
     * @brief Notify progress. Called when printing status and progress is updated.
     * 
     * @param status Printing status
     * @param progress Printing progress percentage 
     */
    private void onNotifyProgress(int status, float progress) {
        switch (status) {
            case DirectPrintManager.PRINT_STATUS_ERROR_CONNECTING:
            case DirectPrintManager.PRINT_STATUS_ERROR_SENDING:
            case DirectPrintManager.PRINT_STATUS_ERROR_FILE:
            case DirectPrintManager.PRINT_STATUS_ERROR:
            case DirectPrintManager.PRINT_STATUS_SENT:
                finalizeDirectPrint();
                break;
            case DirectPrintManager.PRINT_STATUS_JOB_NUM_UPDATE:    // Status only returned on LPR print
                updateJobNumber();
                break;
        }
        
        if (mCallbackRef != null && mCallbackRef.get() != null) {
            mCallbackRef.get().onNotifyProgress(this, status, progress);
        }
    }

    /**
     * @brief Notify progress. Called when printing status and progress is updated.
     *
     */
    private void updateJobNumber(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.getAppContext());
        SharedPreferences.Editor editor = preferences.edit();
        int jobNumber = preferences.getInt(AppConstants.PREF_KEY_JOB_NUMBER_COUNTER, AppConstants.PREF_DEFAULT_JOB_NUMBER_COUNTER);     // current job number
        int nextJobNumber = (jobNumber + 1) % (AppConstants.CONST_MAX_JOB_NUMBER + 1);      // increment job number (0-999)
        editor.putInt(AppConstants.PREF_KEY_JOB_NUMBER_COUNTER, nextJobNumber);
        editor.commit();
    }


    // ================================================================================
    // Internal classes
    // ================================================================================
    /**
     * @interface DirectPrintCallback
     * 
     * @brief Interface for DirectPrintCallback Events
     */
    public interface DirectPrintCallback {
        /**
         * @brief Notify progress callback. Called when printing status and progress is updated.
         * 
         * @param manager DirectPrint Manager
         * @param status Print status
         * @param progress Printing progress percentage
         */
        public void onNotifyProgress(DirectPrintManager manager, int status, float progress);
    }
    
    /**
     * @class DirectPrintCancelTask
     * 
     * @brief Async Task for Canceling Direct Print
     */
    public class DirectPrintCancelTask extends AsyncTask<Void, Void, Void> {
        private DirectPrintManager mManager;
        
        /**
         * @brief Creates DirectPrintCancelTask instance.
         * 
         * @param manager DirectPrint Manager
         */
        public DirectPrintCancelTask(DirectPrintManager manager) {
            mManager = manager;
        }

        @Override
        protected Void doInBackground(Void... params) {
            mManager.cancel();
            finalizeDirectPrint();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }
}
