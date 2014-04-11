/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * SNMPManger.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.common;

import java.lang.ref.WeakReference;

public class SNMPManager {
    private static final String TAG = "SNMPManager";
    public long mContext = 0;
    private WeakReference<SNMPManagerCallback> mCallback;
    
    public native void initializeSNMPManager();
    public native void finalizeSNMPManager();
    public native void deviceDiscovery();
    
    public void setCallback(SNMPManagerCallback callback) {
        mCallback = new WeakReference<SNMPManagerCallback>(callback);
    }
    
    private void onEndDiscovery(int result) {
        if (mCallback != null && mCallback.get() != null) {
            mCallback.get().onEndDiscovery(this, result);
        }
    }
    
    private void onFoundDevice(String ipAddress, String name, boolean[] capabilities) {
        if (mCallback != null && mCallback.get() != null) {
            mCallback.get().onFoundDevice(this, ipAddress, name, capabilities);
        }
    }
    
    public interface SNMPManagerCallback {
        public void onEndDiscovery(SNMPManager manager, int result);
        public void onFoundDevice(SNMPManager manager, String ipAddress, String name, boolean[] capabilities);
    }
    
    static {
        System.loadLibrary("common");
    }
}
