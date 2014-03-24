/*
 * Copyright (c) 2014 All rights reserved.
 *
 * SNMPManager.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.controller.snmp;

import android.os.AsyncTask;

public class SNMPManager {
    
    // ================================================================================
    // Interface
    // ================================================================================
    
    private OnSNMPSearch mOnPrinterAdd;
    
    public interface OnSNMPSearch {
        public void onSearchedPrinterAdd(String printerName, String ipAddress);
        
        public void onSearchEnd();
    }
    
    public void setOnPrinterSearchListener(OnSNMPSearch onSNMPSearch) {
        mOnPrinterAdd = onSNMPSearch;
    }
    
    // ================================================================================
    // Public Methods
    // ================================================================================
    
    public void startSNMP() {
        new SNMPTask().execute();
    }
    
    public void searchPrinter(String ipAddress) {
        new manualSearchTask().execute(ipAddress);
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
    
    // ================================================================================
    // SNMP NDK Callback
    // ================================================================================
    
    private void printerAdded(String printerName, String ipAddress) {
        mOnPrinterAdd.onSearchedPrinterAdd(printerName, ipAddress);
    }
    
    private void searchPrinterEnd() {
        mOnPrinterAdd.onSearchEnd();
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
                mOnPrinterAdd.onSearchEnd();
            }
            return null;
        }
    }
    
    class manualSearchTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... arg0) {
            try {
                snmpManualSearch(arg0[0]);
            } catch (Exception e) {
                mOnPrinterAdd.onSearchEnd();
            }
            return null;
        }
    }
}
