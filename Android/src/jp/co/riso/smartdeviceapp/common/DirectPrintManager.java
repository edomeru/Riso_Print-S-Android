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

public class DirectPrintManager {
    private long mJob = 0;
    private WeakReference<DirectPrintCallback> mCallbackRef = null;
    
    public static final int PRINT_STATUS_ERROR_CONNECTING = -4;
    public static final int PRINT_STATUS_ERROR_SENDING = -3;
    public static final int PRINT_STATUS_ERROR_FILE = -2;
    public static final int PRINT_STATUS_ERROR = -1;
    public static final int PRINT_STATUS_STARTED = 0;
    public static final int PRINT_STATUS_CONNECTING = 1;
    public static final int PRINT_STATUS_CONNECTED = 2;
    public static final int PRINT_STATUS_SENDING = 3;
    public static final int PRINT_STATUS_SENT = 4;
    
    private native void initializeDirectPrint(String userName, String jobName, String fileName, String printSetting, String ipAddress);
    private native void finalizeDirectPrint();
    private native void lprPrint();
    private native void rawPrint();
    private native void cancel();
    
    
    /**
     * Set Callback.
     * <p>
     * Sets the callback for the DirectPrint Manager.
     * 
     * @param callback
     *            Callback function
     */
    public void setCallback(DirectPrintCallback callback) {
        mCallbackRef = new WeakReference<DirectPrintCallback>(callback);
    }
    
    /**
     * Executes an LPR Print
     * 
     * @param userName
     * @param jobName
     * @param fileName
     * @param printSetting
     * @param ipAddress
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
     * Executes an RAW Print
     * 
     * @param userName
     * @param jobName
     * @param fileName
     * @param printSetting
     * @param ipAddress
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
     * Check if print is ongoing
     * 
     * @return true if print is ongoing
     */
    public boolean isPrinting() {
        return (mJob != 0);
    }
    
    /**
     * Send cancel command
     * <p>
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
     * Notify progress
     * 
     * @param status
     * @param progress
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
    
    public interface DirectPrintCallback {
        /**
         * Notify progress callback
         * 
         * @param manager
         *            DirectPrint Manager
         * @param status
         *            Print status
         * @param progress
         *            Printing progress
         */
        public void onNotifyProgress(DirectPrintManager manager, int status, float progress);
    }

    
    // ================================================================================
    // Internal classes
    // ================================================================================
    
    /**
     * Async Task for Canceling Direct Print
     */
    public class DirectPrintCancelTask extends AsyncTask<Void, Void, Void> {
        private DirectPrintManager mManager;
        
        /**
         * Constructor
         * 
         * @param manager
         *            DirectPrint Manager
         */
        public DirectPrintCancelTask(DirectPrintManager manager) {
            mManager = manager;
        }

        /** {@inheritDoc} */
        @Override
        protected Void doInBackground(Void... params) {
            mManager.cancel();
            finalizeDirectPrint();
            return null;
        }

        /** {@inheritDoc} */
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }
}
