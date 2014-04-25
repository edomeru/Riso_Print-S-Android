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
    private static final String TAG = "DirectPrintManager";
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
    
    public native void initializeDirectPrint(String jobName, String fileName, String printSetting, String ipAddress);
    public native void finalizeDirectPrint();
    public native void lprPrint();
    public native void cancel();
    
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
            mManager.finalizeDirectPrint();
            return null;
        }

        /** {@inheritDoc} */
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }
}
