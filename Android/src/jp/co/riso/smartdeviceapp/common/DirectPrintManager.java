/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * DirectPrintManager.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.common;

import java.lang.ref.WeakReference;

public class DirectPrintManager {
    private static final String TAG = "DirectPrintManager";
    private long mJob = 0;
    private WeakReference<DirectPrintCallback>mCallbackRef;
    
    public native void initializeDirectPrint(String jobName, String fileName, String printSetting, String ipAddress);
    public native void finalizeDirectPrint();
    public native void lprPrint();
    public native void cancel();

    public void setCallback(DirectPrintCallback callback)
    {
        mCallbackRef = new WeakReference<DirectPrintCallback>(callback);
    }
    
    static {
        //System.loadLibrary("common");
    }
    
    private void onNotifyProgress(int status, float progress)
    {
        if (mCallbackRef != null && mCallbackRef.get() != null)
        {
            mCallbackRef.get().onNotifyProgress(this, status, progress);
        }
    }
    
    public interface DirectPrintCallback
    {
        public void onNotifyProgress(DirectPrintManager manager, int status, float progress);
    }
}
