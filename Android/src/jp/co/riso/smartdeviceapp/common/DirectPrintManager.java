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
    
    public void setCallback(DirectPrintCallback callback) {
        mCallbackRef = new WeakReference<DirectPrintCallback>(callback);
    }
    
    public void sendCancelCommand() {
        setCallback(null);
        (new DirectPrintCancelTask(this)).execute();
    }
    
    static {
        System.loadLibrary("common");
    }
    
    private void onNotifyProgress(int status, float progress) {
        if (mCallbackRef != null && mCallbackRef.get() != null) {
            mCallbackRef.get().onNotifyProgress(this, status, progress);
        }
    }
    
    public interface DirectPrintCallback {
        public void onNotifyProgress(DirectPrintManager manager, int status, float progress);
    }

    
    // ================================================================================
    // Internal classes
    // ================================================================================
    
    public class DirectPrintCancelTask extends AsyncTask<Void, Void, Void> {
        private DirectPrintManager mManager;
        
        public DirectPrintCancelTask(DirectPrintManager manager) {
            mManager = manager;
        }

        @Override
        protected Void doInBackground(Void... params) {
            mManager.cancel();
            mManager.finalizeDirectPrint();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }
}
