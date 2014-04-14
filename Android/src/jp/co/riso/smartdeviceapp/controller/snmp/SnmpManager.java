/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * SNMPManager.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.controller.snmp;

import java.lang.ref.WeakReference;

import android.os.AsyncTask;

public class SnmpManager {
    private static final String TAG = "SNMPManager";
    private WeakReference<SnmpSearchCallback> mSearchCallbackRef;
    
    // ================================================================================
    // Public Methods
    // ================================================================================
    
    public void setSnmpSearchCallback(SnmpSearchCallback snmpSearchCallback) {
        mSearchCallbackRef = new WeakReference<SnmpSearchCallback>(snmpSearchCallback);
    }
    
    public void startSnmp() {
        new DeviceDiscoveryTask().execute();
    }
    
    public void searchPrinter(String ipAddress) {
        new ManualSearchTask().execute(ipAddress);
    }
    
    public boolean isOnline(String ipAddress) {
        try {
            return (snmpCheckDeviceStatus(ipAddress) > 0);
        } catch (Exception e) {
            return false;
        }
    }
    
    public void stopSnmpSearch() {
        snmpDeviceDiscoveryCancel();
    }
    
    // ================================================================================
    // SNMP NDK Callback
    // ================================================================================
    
    private void printerAdded(String printerName, String ipAddress) {
        if (mSearchCallbackRef != null && mSearchCallbackRef.get() != null) {
            mSearchCallbackRef.get().onSearchedPrinterAdd(printerName, ipAddress);
        }
    }
    
    private void searchPrinterEnd() {
        if (mSearchCallbackRef != null && mSearchCallbackRef.get() != null) {
            mSearchCallbackRef.get().onSearchEnd();
        }
    }
    
    // ================================================================================
    // Internal Classes
    // ================================================================================
    
    class DeviceDiscoveryTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                startSnmpDeviceDiscovery();
            } catch (Exception e) {
                if (mSearchCallbackRef != null && mSearchCallbackRef.get() != null) {
                    mSearchCallbackRef.get().onSearchEnd();
                }
            }
            return null;
        }
    }
    
    class ManualSearchTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... arg0) {
            try {
                snmpManualSearch(arg0[0]);
            } catch (Exception e) {
                if (mSearchCallbackRef != null && mSearchCallbackRef.get() != null) {
                    mSearchCallbackRef.get().onSearchEnd();
                }
            }
            return null;
        }
    }
    
    // ================================================================================
    // Interface
    // ================================================================================
    
    public interface SnmpSearchCallback {
        public void onSearchedPrinterAdd(String printerName, String ipAddress);
        
        public void onSearchEnd();
    }
    
    // ================================================================================
    // SNMP NDK
    // ================================================================================
    
    private native void startSnmpDeviceDiscovery();
    
    private native void snmpManualSearch(String ipAddress);
    
    private native void snmpDeviceDiscoveryCancel();
    
    private native int snmpCheckDeviceStatus(String ipAddress);
    
    static {
        System.loadLibrary("snmp");
        System.loadLibrary("snmpAPI");
    }
}
