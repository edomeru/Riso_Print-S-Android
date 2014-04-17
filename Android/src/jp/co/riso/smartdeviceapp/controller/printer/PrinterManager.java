/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrinterManager.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.controller.printer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import jp.co.riso.smartdeviceapp.common.SNMPManager;
import jp.co.riso.smartdeviceapp.common.SNMPManager.SNMPManagerCallback;
import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager;
import jp.co.riso.smartdeviceapp.controller.db.KeyConstants;
import jp.co.riso.smartdeviceapp.model.Printer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

public class PrinterManager implements SNMPManagerCallback {
    public static final int EMPTY_ID = -1;
    public static final int MAX_PRINTER_COUNT = 10;
    private static PrinterManager sSharedMngr = null;
    private List<Printer> mPrinterList = null;
    private Context mContext = null;
    private boolean mDefaultExists = false;
    private boolean mIsSearching = false;
    private SNMPManager mSNMPManager = null;
    private WeakReference<PrinterSearchCallback> mPrinterSearchCallback = null;
    private WeakReference<PrintersCallback> mPrintersCallback = null;
    
    private PrinterManager(Context context) {
        mContext = context;
        mPrinterList = new ArrayList<Printer>();
        mSNMPManager = new SNMPManager();
        mSNMPManager.setCallback(this);
    }
    
    public static PrinterManager getInstance(Context context) {
        if (sSharedMngr == null) {
            sSharedMngr = new PrinterManager(context);
        }
        return sSharedMngr;
    }
    
    // ================================================================================
    // Public Methods
    // ================================================================================
    
    /**
     * Save Printer to the Database
     * <p>
     * Saves the Printer to the Database
     * 
     * @param printer
     *            Printer object containing the Printer Information such as Print Settings
     */
    public boolean savePrinterToDB(Printer printer) {
        if (printer == null || isExists(printer)) {
            return false;
        }
        
        if (!savePrinterInfo(printer)) {
            return false;
        }
        
        if (mDefaultExists == false) {
            setDefaultPrinter(printer);
            mDefaultExists = true;
        }
        
        if (mPrintersCallback != null && mPrintersCallback.get() != null) {
            mPrintersCallback.get().onAddedNewPrinter(printer);
        }
        return true;
    }
    
    /**
     * Check Printer Existence
     * <p>
     * Determines if the Printer exists in the Saved Printer List
     * 
     * @param printer
     *            Printer object that must contain the IP address and Printer Name.
     */
    public boolean isExists(Printer printer) {
        if (printer == null) {
            return false;
        }
        DatabaseManager dbManager = new DatabaseManager(mContext);
        Cursor cursor = dbManager.query(KeyConstants.KEY_SQL_PRINTER_TABLE, null, KeyConstants.KEY_SQL_PRINTER_NAME + "=? and "
                + KeyConstants.KEY_SQL_PRINTER_IP + "=?", new String[] { printer.getName(), printer.getIpAddress() }, null, null, null);
        
        if (cursor.getCount() > 0) {
            dbManager.close();
            cursor.close();
            return true;
        }
        dbManager.close();
        cursor.close();
        return false;
    }
    
    /**
     * Check Printer Existence
     * <p>
     * Determines if the Printer exists in the Saved Printer List
     * 
     * @param ipAddress
     *            IP address of the Printer.
     */
    public boolean isExists(String ipAddress) {
        if (ipAddress == null) {
            return false;
        }
        DatabaseManager dbManager = new DatabaseManager(mContext);
        Cursor cursor = dbManager.query(KeyConstants.KEY_SQL_PRINTER_TABLE, null, KeyConstants.KEY_SQL_PRINTER_IP + "=?", new String[] { ipAddress }, null,
                null, null);
        
        if (cursor.getCount() > 0) {
            dbManager.close();
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }
    
    /**
     * Retrieves the Printer objects from the Database
     * 
     * @return list of Printer objects
     */
    public List<Printer> getSavedPrintersList() {
        DatabaseManager dbManager = new DatabaseManager(mContext);
        Cursor cursor = dbManager.query(KeyConstants.KEY_SQL_PRINTER_TABLE, null, null, null, null, null, null);
        
        mPrinterList.clear();
        if (cursor.getCount() < 1) {
            dbManager.close();
            cursor.close();
            mDefaultExists = false;
            return mPrinterList;
        }
        if (cursor.moveToFirst()) {
            do {
                Printer printer = new Printer(DatabaseManager.getStringFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_NAME),
                        DatabaseManager.getStringFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_IP));
                printer.setId(DatabaseManager.getIntFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_ID));
                printer.setPortSetting(DatabaseManager.getIntFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_PORT));
                
                boolean lprAvailable = Boolean.parseBoolean(DatabaseManager.getStringFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_LPR));
                boolean rawAvailable = Boolean.parseBoolean(DatabaseManager.getStringFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_RAW));
                boolean bookletAvailable = Boolean.parseBoolean(DatabaseManager.getStringFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_BOOKLET));
                boolean staplerAvailable = Boolean.parseBoolean(DatabaseManager.getStringFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_STAPLER));
                boolean punch4Available = Boolean.parseBoolean(DatabaseManager.getStringFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_PUNCH4));
                boolean trayFaceDownAvailable = Boolean.parseBoolean(DatabaseManager.getStringFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_TRAYFACEDOWN));
                boolean trayAutoStackAvailable = Boolean.parseBoolean(DatabaseManager.getStringFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_TRAYAUTOSTACK));
                boolean trayTopAvailable = Boolean.parseBoolean(DatabaseManager.getStringFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_TRAYTOP));
                boolean trayStackAvailable = Boolean.parseBoolean(DatabaseManager.getStringFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_TRAYSTACK));
                
                printer.getConfig().setLprAvailable(lprAvailable);
                printer.getConfig().setRawAvailable(rawAvailable);
                printer.getConfig().setBookletAvailable(bookletAvailable);
                printer.getConfig().setStaplerAvailable(staplerAvailable);
                printer.getConfig().setPunch4Available(punch4Available);
                printer.getConfig().setTrayFaceDownAvailable(trayFaceDownAvailable);
                printer.getConfig().setTrayAutoStackAvailable(trayAutoStackAvailable);
                printer.getConfig().setTrayTopAvailable(trayTopAvailable);
                printer.getConfig().setTrayStackAvailable(trayStackAvailable);
                mPrinterList.add(printer);
            } while (cursor.moveToNext());
            
        }
        mDefaultExists = true;
        dbManager.close();
        cursor.close();
        
        return mPrinterList;
    }
    
    /**
     * Sets the Default Printer by clearing and inserting an entry in DefaultPrinter table
     * 
     * @param printer
     *            The Printer object selected
     */
    public void setDefaultPrinter(Printer printer) {
        
        if (printer == null) {
            return;
        }
        clearDefaultPrinter();
        
        DatabaseManager dbManager = new DatabaseManager(mContext);
        Cursor cursor = dbManager.query(KeyConstants.KEY_SQL_PRINTER_TABLE, null, KeyConstants.KEY_SQL_PRINTER_NAME + "=? and "
                + KeyConstants.KEY_SQL_PRINTER_IP + "=?", new String[] { printer.getName(), printer.getIpAddress() }, null, null, null);
        
        if (cursor.getCount() != 1) {
            dbManager.close();
            cursor.close();
            return;
        }
        
        ContentValues newDefaultPrinter = new ContentValues();
        
        if (cursor.moveToFirst()) {
            printer.setId(DatabaseManager.getIntFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_ID));
            newDefaultPrinter.put(KeyConstants.KEY_SQL_PRINTER_ID, printer.getId());
            cursor.close();
        } else {
            dbManager.close();
            return;
        }
        
        if (!dbManager.insert(KeyConstants.KEY_SQL_DEFAULT_PRINTER_TABLE, null, newDefaultPrinter)) {
            dbManager.close();
            return;
        }
        dbManager.close();
    }
    
    /**
     * Clears the Default Printer table in the database.
     * 
     */
    public void clearDefaultPrinter() {
        DatabaseManager dbManager = new DatabaseManager(mContext);
        
        dbManager.delete(KeyConstants.KEY_SQL_DEFAULT_PRINTER_TABLE, null, null);
        dbManager.close();
    }
    
    /**
     * Removes the Printer from the database.
     * 
     * @param printer
     *            The Printer object selected for deletion
     */
    public void removePrinter(Printer printer) {
        
        if (printer == null) {
            return;
        }
        DatabaseManager dbManager = new DatabaseManager(mContext);
        
        dbManager.delete(KeyConstants.KEY_SQL_PRINTER_TABLE, KeyConstants.KEY_SQL_PRINTER_ID + "=?", String.valueOf(printer.getId()));
        
        dbManager.close();
    }
    
    /**
     * Get Default Printer
     * <p>
     * Obtains the printer ID of the default printer
     * 
     * @return Printer ID of the default printer
     */
    public int getDefaultPrinter() {
        int printer = EMPTY_ID;
        DatabaseManager dbManager = new DatabaseManager(mContext);
        
        Cursor cursor = dbManager.query(KeyConstants.KEY_SQL_DEFAULT_PRINTER_TABLE, null, KeyConstants.KEY_SQL_PRINTER_ID, null, null, null, null);
        
        if (cursor.getCount() != 1) {
            dbManager.close();
            cursor.close();
            return EMPTY_ID;
        }
        
        if (cursor.moveToFirst()) {
            printer = DatabaseManager.getIntFromCursor(cursor, KeyConstants.KEY_SQL_PRINTER_ID);
        }
        
        cursor.close();
        dbManager.close();
        return printer;
    }
    
    /**
     * Search for Printer Device
     * <p>
     * Search for the Printer Devices using Device Discovery/Auto Search.
     * 
     * @param ipAddress
     *            The IP Address of the Printer
     */
    public void startPrinterSearch() {
        mIsSearching = true;
        mSNMPManager.initializeSNMPManager();
        mSNMPManager.deviceDiscovery();
        Log.d("PrinterManager", "Auto");
    }
    
    /**
     * Search for Printer Device
     * <p>
     * Search for the Printer Device using Manual Search.
     * 
     * @param ipAddress
     *            The IP Address of the Printer
     */
    public void searchPrinter(String ipAddress) {
        
        if (ipAddress == null) {
            return;
        }
        
        mIsSearching = true;
        mSNMPManager.initializeSNMPManager();
        mSNMPManager.manualDiscovery(ipAddress);
        Log.d("PrinterManager", "Manual");
    }
    
    /**
     * Cancel Printer Search
     * <p>
     * Stops Device Discovery/Manual Search.
     */
    public void cancelPrinterSearch() {
        mIsSearching = false;
        mSNMPManager.cancel();
        Log.d("PrinterManager", "Cancel");
    }
    
    /**
     * <p>
     * Checks if there is an ongoing printer search.
     */
    public boolean isSearching() {
        return mIsSearching;
    }
    
    /**
     * <p>
     * Updates the Online Status of the specified printer view
     * 
     * @param ipAddress
     *            IP Address of the printer
     * @param view
     *            View of the online indicator
     */
    public void updateOnlineStatus(String ipAddress, View view) {
        // TODO: update implementation
        // new UpdateOnlineStatusTask().execute(ipAddress, view);
    }
    
    /**
     * Set Printer Search Screen Callback
     * <p>
     * Set the Callback for Adding Printer to the Searched Printer List during Device Discovery/Manual search.
     */
    public void setPrinterSearchCallback(PrinterSearchCallback printerSearchCallback) {
        mPrinterSearchCallback = new WeakReference<PrinterSearchCallback>(printerSearchCallback);
    }
    
    /**
     * Set Printer Screen Callback
     * <p>
     * Sets the Saved Printer List/View Callback for adding printers.
     */
    public void setPrintersCallback(PrintersCallback printersCallback) {
        mPrintersCallback = new WeakReference<PrintersCallback>(printersCallback);
    }
    
    public int getPrinterCount() {
        return mPrinterList.size();
    }
    
    // ================================================================================
    // Private Methods
    // ================================================================================
    
    private boolean savePrinterInfo(Printer printer) {
        if (printer == null || isExists(printer)) {
            return false;
        }
        
        // Create Content
        ContentValues newPrinter = new ContentValues();
        
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_IP, printer.getIpAddress());
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_NAME, printer.getName());
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_PORT, printer.getPortSetting());
        
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_LPR, printer.getConfig().isLprAvailable() );
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_RAW, printer.getConfig().isRawAvailable());
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_BOOKLET, printer.getConfig().isBookletAvailable());
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_STAPLER, printer.getConfig().isStaplerAvailable());
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_PUNCH4, printer.getConfig().isPunch4Available());
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_TRAYFACEDOWN, printer.getConfig().isTrayFaceDownAvailable());
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_TRAYAUTOSTACK, printer.getConfig().isTrayAutoStackAvailable());
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_TRAYTOP, printer.getConfig().isTrayTopAvailable());
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_TRAYAUTOSTACK, printer.getConfig().isTrayStackAvailable());
        
        DatabaseManager dbManager = new DatabaseManager(mContext);
        if (!dbManager.insert(KeyConstants.KEY_SQL_PRINTER_TABLE, null, newPrinter)) {
            dbManager.close();
            return false;
        }
        dbManager.close();
        return true;
    }
    
    private static void setupPrinterConfig(Printer printer, boolean[] capabilities) {
        printer.getConfig().setBookletAvailable(capabilities[SNMPManager.SNMP_CAPABILITY_BOOKLET]);
        printer.getConfig().setStaplerAvailable(capabilities[SNMPManager.SNMP_CAPABILITY_STAPLER]);
        printer.getConfig().setPunch4Available(capabilities[SNMPManager.SNMP_CAPABILITY_FINISH_2_4]);
        printer.getConfig().setTrayFaceDownAvailable(capabilities[SNMPManager.SNMP_CAPABILITY_TRAY_FACE_DOWN]);
        printer.getConfig().setTrayTopAvailable(capabilities[SNMPManager.SNMP_CAPABILITY_TRAY_TOP]);
        printer.getConfig().setTrayStackAvailable(capabilities[SNMPManager.SNMP_CAPABILITY_TRAY_STACK]);
        
    }
    
    // ================================================================================
    // Interface - SNMPManagerCallback
    // ================================================================================
    
    @Override
    public void onEndDiscovery(SNMPManager manager, int result) {
        mIsSearching = false;
        manager.finalizeSNMPManager();
        Log.d("PrinterManager", "Finalize");
        if (mPrinterSearchCallback != null && mPrinterSearchCallback.get() != null) {
            mPrinterSearchCallback.get().onSearchEnd();
        }
    }
    
    @Override
    public void onFoundDevice(SNMPManager manager, String ipAddress, String name, boolean[] capabilities) {
        Printer printer = new Printer(name, ipAddress);
        PrinterManager.setupPrinterConfig(printer, capabilities);
        Log.d("PrinterManager", "onFoundDevice");
        if (isSearching()) {
            if (mPrinterSearchCallback != null && mPrinterSearchCallback.get() != null) {
                mPrinterSearchCallback.get().onPrinterAdd(printer);
            }
        }
    }
    
    // ================================================================================
    // Interface - PrinterSearchCallback
    // ================================================================================
    
    public interface PrinterSearchCallback {
        
        public void onPrinterAdd(Printer printer);
        
        public void onSearchEnd();
    }
    
    // ================================================================================
    // Interface - PrintersCallback
    // ================================================================================
    
    public interface PrintersCallback {
        public void onAddedNewPrinter(Printer printer);
    }
    
    // ================================================================================
    // Internal Classes
    // ================================================================================
    
    class UpdateOnlineStatusTask extends AsyncTask<Object, View, Boolean> {
        private WeakReference<View> mViewRef = null;
        private String mIpAddress = null;
        
        public UpdateOnlineStatusTask(View view, String ipAddress) {
            mViewRef = new WeakReference<View>(view);
            mIpAddress = ipAddress;
        }
        
        @Override
        protected Boolean doInBackground(Object... arg) {
            if (mIpAddress.isEmpty()) {
                return false;
            }
            return true;
        }
        
        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            
            if (mViewRef != null && mViewRef.get() != null) {
                mViewRef.get().setActivated(result);
            }
            /*
            if (mView != null && mView.get() != null && mipAddress != null && mipAddress.get() != null) {
                if (mSNMPManager.isOnline(mipAddress.get())) {
                    mView.get().setActivated(true);
                } else {
                    mView.get().setActivated(false);
                }
            }
             */
        }
        
    }
    
    
}
