/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * SNMPManger.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.common;

import java.lang.ref.WeakReference;

import android.util.Log;

public class SNMPManager {
    private static final String TAG = "SNMPManager";
    
    public long mContext = 0;
    private WeakReference<SNMPManagerCallback> mCallbackRef;
    
    public native void initializeSNMPManager();
    public native void finalizeSNMPManager();
    public native void deviceDiscovery();
    public native void manualDiscovery(String ipAddress);
    public native void cancel();

    public static final int SNMP_CAPABILITY_BOOKLET = 0;
    public static final int SNMP_CAPABILITY_STAPLER = 1;
    public static final int SNMP_CAPABILITY_FINISH_2_3 = 2;
    public static final int SNMP_CAPABILITY_FINISH_2_4 = 3;
    public static final int SNMP_CAPABILITY_TRAY_FACE_DOWN = 4;
    public static final int SNMP_CAPABILITY_TRAY_AUTO_STACK = 5;
    public static final int SNMP_CAPABILITY_TRAY_TOP = 6;
    public static final int SNMP_CAPABILITY_TRAY_STACK = 7;
    
    public void setCallback(SNMPManagerCallback callback) {
        mCallbackRef = new WeakReference<SNMPManagerCallback>(callback);
    }
    
    private void onEndDiscovery(int result) {
        Log.d(TAG, "onEndDiscovery");
        if (mCallbackRef != null && mCallbackRef.get() != null) {
            mCallbackRef.get().onEndDiscovery(this, result);
        }
    }
    
    private void onFoundDevice(String ipAddress, String name, boolean[] capabilities) {
        Log.d(TAG, "onFoundDevice");
        if (mCallbackRef != null && mCallbackRef.get() != null) {
            mCallbackRef.get().onFoundDevice(this, ipAddress, name, capabilities);
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
