/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * SNMPManger.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.common;

import java.lang.ref.WeakReference;

/**
 * @class SNMPManager
 * 
 * @brief Manager responsible for SNMP operations. 
 */
public class SNMPManager {
    private WeakReference<SNMPManagerCallback> mCallbackRef = null;
    
    @SuppressWarnings("JniMissingFunction")
    public native void initializeSNMPManager(String communityName);
    @SuppressWarnings("JniMissingFunction")
    public native void finalizeSNMPManager();
    @SuppressWarnings("JniMissingFunction")
    public native void deviceDiscovery();
    @SuppressWarnings("JniMissingFunction")
    public native void manualDiscovery(String ipAddress);
    @SuppressWarnings("JniMissingFunction")
    public native void cancel();
    
    /// Booklet finishing capability
    public static final int SNMP_CAPABILITY_BOOKLET_FINISHING = 0;
    /// Staple capability
    public static final int SNMP_CAPABILITY_STAPLER = 1;
    /// Punch 3 holes capability for Japan printers
    public static final int SNMP_CAPABILITY_FINISH_2_3 = 2;
    /// Punch 4 holes capability
    public static final int SNMP_CAPABILITY_FINISH_2_4 = 3;
    /// Punch 0 holes capability
    public static final int SNMP_CAPABILITY_FINISH_0 = 4;
    /// Tray face down capability
    public static final int SNMP_CAPABILITY_TRAY_FACE_DOWN = 5;
    /// Tray top capability
    public static final int SNMP_CAPABILITY_TRAY_TOP = 6;
    /// Tray stack capability
    public static final int SNMP_CAPABILITY_TRAY_STACK = 7;
    /// LPR print capability
    public static final int SNMP_CAPABILITY_LPR = 8;
    /// Raw print capability 
    public static final int SNMP_CAPABILITY_RAW = 9;
    /// External feeder capability
    public static final int SNMP_CAPABILITY_EXTERNAL_FEEDER = 10;
    
    /**
     * @brief Sets the callback function for the SNMP Manager.
     * 
     * @param callback Callback function
     */
    public void setCallback(SNMPManagerCallback callback) {
        mCallbackRef = new WeakReference<>(callback);
    }
    
    /**
     * @brief Callback called at the end of device discovery
     * 
     * @param result Result of device discovery
     */
    private void onEndDiscovery(int result) {
        if (mCallbackRef != null && mCallbackRef.get() != null) {
            mCallbackRef.get().onEndDiscovery(this, result);
        }
    }
    
    /**
     * @brief Callback called when a device is found during device discovery
     * 
     * @param ipAddress Device IP Address
     * @param name Device Name
     * @param capabilities Device capabilities
     */
    private void onFoundDevice(String ipAddress, String name, boolean[] capabilities) {
        if (mCallbackRef != null && mCallbackRef.get() != null) {
            mCallbackRef.get().onFoundDevice(this, ipAddress, name, capabilities);
        }
    }
    
    /**
     * @interface SNMPManagerCallback
     * 
     * @brief SNMP Manager Interface
     */
    public interface SNMPManagerCallback {
        /**
         * @brief Callback called at the end of device discovery
         * 
         * @param manager SNMP Manager
         * @param result Result of device discovery
         */
        public void onEndDiscovery(SNMPManager manager, int result);
        
        /**
         * @brief Callback called when a device is found during device discovery
         * 
         * @param manager SNMP Manager
         * @param ipAddress Device IP Address 
         * @param name Device Name 
         * @param capabilities Device capabilities
         */
        public void onFoundDevice(SNMPManager manager, String ipAddress, String name, boolean[] capabilities);
    }
    
    static {
        System.loadLibrary("common");
    }
}
