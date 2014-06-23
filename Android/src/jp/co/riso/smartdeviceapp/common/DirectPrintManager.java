/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * DirectPrintManager.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.common;

import java.lang.ref.WeakReference;

import android.os.AsyncTask;

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
    
    /**
     * @brief Initializes Direct Print.
     * 
     * @param userName To be sent as OwnerName in the PJL command.
     * @param jobName Name of the Print Job.
     * @param fileName File name of the PDF.
     * @param printSetting Formatted string of the print settings.
     * @param ipAddress IP address of the printer.
     */
    private native void initializeDirectPrint(String userName, String jobName, String fileName, String printSetting, String ipAddress);
    /**
     * @brief Finalizes Direct Print.
     */
    private native void finalizeDirectPrint();
    /**
     * @brief Executes an LPR Print.
     */
    private native void lprPrint();
    /**
     * @brief Executes a RAW Print.
     */
    private native void rawPrint();
    /**
     * @brief Cancels Direct Print.
     */
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
     * 
     * @retval true Print execution is started
     * @retval false Print not executed
     */
    public boolean executeLPRPrint(String userName, String jobName, String fileName, String printSetting, String ipAddress) {
        if (userName == null || jobName == null || fileName == null || printSetting == null || ipAddress == null || jobName.isEmpty() 
                || fileName.isEmpty() || printSetting.isEmpty() || ipAddress.isEmpty()) {
            return false;
        }
        initializeDirectPrint(userName, jobName, fileName, printSetting, ipAddress);
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
     * 
     * @retval true Print execution is started
     * @retval false Print not executed
     */
    public boolean executeRAWPrint(String userName, String jobName, String fileName, String printSetting, String ipAddress) {
        if (userName == null || jobName == null || fileName == null || printSetting == null || ipAddress == null 
                || jobName.isEmpty() || fileName.isEmpty() || printSetting.isEmpty() || ipAddress.isEmpty()) {
            return false;
        }
        initializeDirectPrint(userName, jobName, fileName, printSetting, ipAddress);
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
        }
        
        if (mCallbackRef != null && mCallbackRef.get() != null) {
            mCallbackRef.get().onNotifyProgress(this, status, progress);
        }
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
