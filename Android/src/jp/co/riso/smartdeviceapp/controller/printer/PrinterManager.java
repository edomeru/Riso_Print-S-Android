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

import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager;
import jp.co.riso.smartdeviceapp.controller.db.KeyConstants;
import jp.co.riso.smartdeviceapp.controller.snmp.SnmpManager;
import jp.co.riso.smartdeviceapp.controller.snmp.SnmpManager.SnmpSearchCallback;
import jp.co.riso.smartdeviceapp.model.Printer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.view.View;

public class PrinterManager implements SnmpSearchCallback {
    private static PrinterManager sSharedMngr = null;
    private List<Printer> mPrinterList = null;
    private Context mContext = null;
    private boolean mDefaultExists = false;
    private boolean mIsSearching = false;
    private SnmpManager mSNMPManager = null;
    private WeakReference<PrinterSearchCallback> mPrinterSearchCallback = null;
    private WeakReference<PrintersCallback> mPrintersCallback = null;
    
    private PrinterManager(Context context) {
        mContext = context;
        mPrinterList = new ArrayList<Printer>();
        mSNMPManager = new SnmpManager();
        mSNMPManager.setSnmpSearchCallback(this);
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
        
        if (!savePrintSettings(printer)) {
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
                Printer printer = new Printer(cursor.getString(cursor.getColumnIndexOrThrow(KeyConstants.KEY_SQL_PRINTER_NAME)), cursor.getString(cursor
                        .getColumnIndexOrThrow(KeyConstants.KEY_SQL_PRINTER_IP)), null);
                printer.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KeyConstants.KEY_SQL_PRINTER_ID)));
                printer.setPortSetting(cursor.getInt(cursor.getColumnIndexOrThrow(KeyConstants.KEY_SQL_PRINTER_PORT)));
                printer.setLpr(cursor.getInt(cursor.getColumnIndexOrThrow(KeyConstants.KEY_SQL_PRINTER_EN_LPR)) > 0);
                printer.setRaw(cursor.getInt(cursor.getColumnIndexOrThrow(KeyConstants.KEY_SQL_PRINTER_EN_RAW)) > 0);
                printer.setPagination(cursor.getInt(cursor.getColumnIndexOrThrow(KeyConstants.KEY_SQL_PRINTER_EN_PAGINATION)) > 0);
                printer.setDuplex(cursor.getInt(cursor.getColumnIndexOrThrow(KeyConstants.KEY_SQL_PRINTER_EN_DUPLEX)) > 0);
                printer.setBookletBinding(cursor.getInt(cursor.getColumnIndexOrThrow(KeyConstants.KEY_SQL_PRINTER_EN_BOOKLET_BINDING)) > 0);
                printer.setStaple(cursor.getInt(cursor.getColumnIndexOrThrow(KeyConstants.KEY_SQL_PRINTER_EN_STAPLE)) > 0);
                printer.setBind(cursor.getInt(cursor.getColumnIndexOrThrow(KeyConstants.KEY_SQL_PRINTER_EN_BIND)) > 0);
                mPrinterList.add(printer);
            } while (cursor.moveToNext());
            
        }
        mDefaultExists = true;
        dbManager.close();
        cursor.close();
        
        for (int i = 0; i < mPrinterList.size(); i++) {
            getPrintSettings(mPrinterList.get(i));
        }
        
        return mPrinterList;
    }
    
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
            printer.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KeyConstants.KEY_SQL_PRINTER_ID)));
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
    
    public void clearDefaultPrinter() {
        DatabaseManager dbManager = new DatabaseManager(mContext);
        
        dbManager.delete(KeyConstants.KEY_SQL_DEFAULT_PRINTER_TABLE, null, null);
        dbManager.close();
    }
    
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
        int printer = -1;
        DatabaseManager dbManager = new DatabaseManager(mContext);
        
        Cursor cursor = dbManager.query(KeyConstants.KEY_SQL_DEFAULT_PRINTER_TABLE, null, KeyConstants.KEY_SQL_PRINTER_ID, null, null, null, null);
        
        if (cursor.getCount() != 1) {
            dbManager.close();
            cursor.close();
            return -1;
        }
        
        if (cursor.moveToFirst()) {
            printer = cursor.getInt(cursor.getColumnIndexOrThrow(KeyConstants.KEY_SQL_PRINTER_ID));
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
        mSNMPManager.startSnmp();
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
        mSNMPManager.searchPrinter(ipAddress);
    }
    
    /**
     * Cancel Printer Search
     * <p>
     * Stops Device Discovery/Manual Search.
     */
    public void cancelPrinterSearch() {
        mIsSearching = false;
        mSNMPManager.stopSnmpSearch();
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
        new UpdateOnlineStatusTask().execute(ipAddress, view);
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
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_EN_LPR, printer.getLpr());
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_EN_RAW, printer.getRaw());
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_EN_PAGINATION, printer.getPagination());
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_EN_DUPLEX, printer.getDuplex());
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_EN_BOOKLET_BINDING, printer.getBookletBinding());
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_EN_STAPLE, printer.getStaple());
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_EN_BIND, printer.getBind());
        
        DatabaseManager dbManager = new DatabaseManager(mContext);
        if (!dbManager.insert(KeyConstants.KEY_SQL_PRINTER_TABLE, null, newPrinter)) {
            dbManager.close();
            return false;
        }
        dbManager.close();
        return true;
    }
    
    private boolean savePrintSettings(Printer printer) {
        if (printer == null || printer.getPrintSettings() == null) {
            return false;
        }
        return true;
    }
    
    private void getPrintSettings(Printer printer) {
        // TODO: Get Print Settings
    }
    
    // ================================================================================
    // Interface - SnmpSearchCallback
    // ================================================================================
    
    @Override
    public void onSearchedPrinterAdd(String printerName, String ipAddress) {
        Printer printer = new Printer(printerName, ipAddress, null);
        if (isSearching()) {
            if (mPrinterSearchCallback != null && mPrinterSearchCallback.get() != null) {
                mPrinterSearchCallback.get().onPrinterAdd(printer);
            }
        }
    }
    
    @Override
    public void onSearchEnd() {
        mIsSearching = false;
        if (mPrinterSearchCallback != null && mPrinterSearchCallback.get() != null) {
            mPrinterSearchCallback.get().onSearchEnd();
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
    
    class UpdateOnlineStatusTask extends AsyncTask<Object, View, Void> {
        private WeakReference<View> mView = null;
        private WeakReference<String> mipAddress = null;
        
        @Override
        protected Void doInBackground(Object... arg) {
            mipAddress = new WeakReference<String>((String) arg[0]);
            mView = new WeakReference<View>((View) arg[1]);
            return null;
        }
        
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (mView != null && mView.get() != null && mipAddress != null && mipAddress.get() != null) {
                if (mSNMPManager.isOnline(mipAddress.get())) {
                    mView.get().setActivated(true);
                } else {
                    mView.get().setActivated(false);
                }
            }
        }
        
    }
}
