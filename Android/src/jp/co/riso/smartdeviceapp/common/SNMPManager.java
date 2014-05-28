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
    public long mContext = 0;
    private WeakReference<SNMPManagerCallback> mCallbackRef = null;
    
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
    
    /**
     * Set Callback.
     * <p>
     * Sets the callback for the SNMP Manager.
     * 
     * @param callback
     *            Callback function
     */
    public void setCallback(SNMPManagerCallback callback) {
        mCallbackRef = new WeakReference<SNMPManagerCallback>(callback);
    }
    
    /**
     * On end discovery callback.
     * <p>
     * Callback called at the end of device discovery
     * 
     * @param result
     *            Result of device discovery
     */
    private void onEndDiscovery(int result) {
        if (mCallbackRef != null && mCallbackRef.get() != null) {
            mCallbackRef.get().onEndDiscovery(this, result);
        }
    }
    
    /**
     * On device found callback.
     * <p>
     * Callback called when a device is found during device discovery
     * 
     * @param ipAddress
     *            IP Address of the device
     * @param name
     *            Name of the device
     * @param capabilities
     *            Device capabilities
     */
    private void onFoundDevice(String ipAddress, String name, boolean[] capabilities) {
        if (mCallbackRef != null && mCallbackRef.get() != null) {
            mCallbackRef.get().onFoundDevice(this, ipAddress, name, capabilities);
        }
    }
    
    /**
     * SNMP Manager Interface
     */
    public interface SNMPManagerCallback {
        /**
         * On end discovery callback.
         * <p>
         * Callback called at the end of device discovery
         * 
         * @param manager
         *            SNMP Manager
         * @param result
         *            Result of device discovery
         */
        public void onEndDiscovery(SNMPManager manager, int result);
        
        /**
         * On device found callback.
         * <p>
         * Callback called when a device is found during device discovery
         * 
         * @param manager
         *            SNMP Manager
         * @param ipAddress
         *            IP Address of the device
         * @param name
         *            Name of the device
         * @param capabilities
         *            Device capabilities
         */
        public void onFoundDevice(SNMPManager manager, String ipAddress, String name, boolean[] capabilities);
    }
    
    static {
        System.loadLibrary("common");
    }
}
