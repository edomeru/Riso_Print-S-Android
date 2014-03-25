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
import android.util.Log;

public class SNMPManager {
    private static final String TAG = "SNMPManager";
    private WeakReference<SNMPSearchCallback> mSearchCallbackRef;
    
    // ================================================================================
    // Public Methods
    // ================================================================================
    
    public void setSNMPSearchCallback(SNMPSearchCallback snmpSearchCallback) {
        mSearchCallbackRef = new WeakReference<SNMPSearchCallback>(snmpSearchCallback);
    }
    
    public void startSNMP() {
        new SNMPTask().execute();
    }
    
    public void searchPrinter(String ipAddress) {
        new ManualSearchTask().execute(ipAddress);
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
    // AsyncTask
    // ================================================================================
    
    class SNMPTask extends AsyncTask<Void, Void, Void> {
        
        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                startSNMPDeviceDiscovery();
            } catch (Exception e) {
                Log.w(TAG, "snmp end");
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
                if (mSearchCallbackRef != null) {
                    mSearchCallbackRef.get().onSearchEnd();
                }
            }
            return null;
        }
    }
    
    // ================================================================================
    // Interface
    // ================================================================================
    
    public interface SNMPSearchCallback {
        public void onSearchedPrinterAdd(String printerName, String ipAddress);
        
        public void onSearchEnd();
    }
    
    // ================================================================================
    // SNMP NDK
    // ================================================================================
    
    private native void startSNMPDeviceDiscovery();
    
    private native void snmpManualSearch(String ipAddress);
    
    static {
        System.loadLibrary("snmp");
        System.loadLibrary("snmpAPI");
    }
}
