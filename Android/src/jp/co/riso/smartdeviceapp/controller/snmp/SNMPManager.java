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
    
    // ================================================================================
    // SNMP NDK
    // ================================================================================
    private native void startSNMPDeviceDiscovery();
    
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
            try{
                startSNMPDeviceDiscovery();
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            return null;
        }      
    }

}
