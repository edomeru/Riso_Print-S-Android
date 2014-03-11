package jp.co.riso.smartdeviceapp.controller.snmp;


public class SNMPManager {
    private OnSNMPSearch mOnPrinterAdd;
    public interface OnSNMPSearch {
        public void onSearchedPrinterAdd(String printerName, String ipAddress);
        public void onSearchEnd();
    }

    public void printerAdded(String printerName, String ipAddress) {
        mOnPrinterAdd.onSearchedPrinterAdd(printerName, ipAddress);
    }
    
    public void searchPrinterEnd() {
        mOnPrinterAdd.onSearchEnd();
    }
    
    public void setOnPrinterSearchListener(OnSNMPSearch onSNMPSearch) {
        mOnPrinterAdd = onSNMPSearch;
    }
    
    public void snmpStart() {
        try {
            startSNMPDeviceDiscovery();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
    // ================================================================================
    // SNMP NDK
    // ================================================================================
    public native void startSNMPDeviceDiscovery();
    
    static {
        System.loadLibrary("snmp");
        System.loadLibrary("snmpAPI");
    }

}
