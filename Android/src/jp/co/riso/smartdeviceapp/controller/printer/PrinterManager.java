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
import jp.co.riso.smartdeviceapp.controller.snmp.SNMPManager;
import jp.co.riso.smartdeviceapp.controller.snmp.SNMPManager.SNMPSearchCallback;
import jp.co.riso.smartdeviceapp.model.Printer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class PrinterManager implements SNMPSearchCallback {
    
    public static final int EMPTY_ID = -1;
    
    private static SNMPManager sSNMPManager = null;
    private static PrinterManager sSharedMngr = null;
    private List<Printer> mPrinterList;
    private Context mContext;
    private boolean mDefaultExists = false;
    private boolean mIsSearching = false;
    private WeakReference<OnPrinterSearchCallback> mOnPrinterAddCallback = null;
    private WeakReference<OnPrintersListChangeCallback> mOnPrintersListRefreshCallback = null;
    
    private PrinterManager(Context context) {
        mContext = context;
        mPrinterList = new ArrayList<Printer>();
        sSNMPManager = new SNMPManager();
        sSNMPManager.setSNMPSearchCallback(this);
    }
    
    public static PrinterManager sharedManager(Context context) {
        if (sSharedMngr == null) {
            sSharedMngr = new PrinterManager(context);
        }
        return sSharedMngr;
    }
    
    // ================================================================================
    // DataBase
    // ================================================================================
    
    public boolean savePrinterToDB(Printer printer) {
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
        
        if (mDefaultExists == false) {
            setDefaultPrinter(printer);
            mDefaultExists = true;
        }
        dbManager.close();
        
        if (mOnPrintersListRefreshCallback != null && mOnPrintersListRefreshCallback.get() != null) {
            mOnPrintersListRefreshCallback.get().onAddedNewPrinter(printer);
        }
        return true;
    }
    
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
        Cursor cursor = dbManager.query(KeyConstants.KEY_SQL_PRINTER_TABLE, new String[] { KeyConstants.KEY_SQL_PRINTER_ID, KeyConstants.KEY_SQL_PRINTER_NAME,
                KeyConstants.KEY_SQL_PRINTER_IP }, null, null, null, null, null);
        
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
                        .getColumnIndexOrThrow(KeyConstants.KEY_SQL_PRINTER_IP)), false, null);
                printer.setId(cursor.getInt(cursor.getColumnIndexOrThrow(KeyConstants.KEY_SQL_PRINTER_ID)));
                mPrinterList.add(printer);
            } while (cursor.moveToNext());
            
        }
        mDefaultExists = true;
        dbManager.close();
        cursor.close();
        return mPrinterList;
    }
    
    public void setDefaultPrinter(Printer printer) {
        
        if (printer == null) {
            return;
        }
        
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
        
        dbManager.delete(KeyConstants.KEY_SQL_DEFAULT_PRINTER_TABLE, null, null);
        
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
            printer = cursor.getInt(cursor.getColumnIndexOrThrow(KeyConstants.KEY_SQL_PRINTER_ID));
        }
        
        cursor.close();
        dbManager.close();
        return printer;
    }
    
    public void startPrinterSearch() {
        mIsSearching = true;
        sSNMPManager.startSNMP();
    }
    
    public void searchPrinter(String ipAddress) {
        
        if (ipAddress == null) {
            return;
        }
        
        mIsSearching = true;
        sSNMPManager.searchPrinter(ipAddress);
    }
    
    public void cancelPrinterSearch() {
        mIsSearching = false;
        // TODO: Call SNMP cancel - sSNMPManager.stopSNMP();
    }
    
    public boolean isSearching() {
        return mIsSearching;
    }
    
    public void setOnPrinterSearchCallback(OnPrinterSearchCallback onPrinterSearchCallback) {
        mOnPrinterAddCallback = new WeakReference<OnPrinterSearchCallback>(onPrinterSearchCallback);
    }
    
    public void setOnPrintersListRefreshCallback(OnPrintersListChangeCallback onPrintersListRefreshCallback) {
        mOnPrintersListRefreshCallback = new WeakReference<OnPrintersListChangeCallback>(onPrintersListRefreshCallback);
    }
    
    // ================================================================================
    // Interface - OnSNMPSearch (SNMP Callback)
    // ================================================================================
    
    @Override
    public void onSearchedPrinterAdd(String printerName, String ipAddress) {
        Printer printer = new Printer(printerName, ipAddress, false, null);
        if (isSearching()) {
            if (mOnPrinterAddCallback != null && mOnPrinterAddCallback.get() != null) {
                mOnPrinterAddCallback.get().onPrinterAdd(printer);
            }
        }
    }
    
    @Override
    public void onSearchEnd() {
        mIsSearching = false;
        if (mOnPrinterAddCallback != null && mOnPrinterAddCallback.get() != null) {
            mOnPrinterAddCallback.get().onSearchEnd();
        }
    }
    
    // ================================================================================
    // Interface - OnPrinterSearch (SNMP)
    // ================================================================================
    
    public interface OnPrinterSearchCallback {
        public void onPrinterAdd(Printer printer);
        
        public void onSearchEnd();
    }
    
    // ================================================================================
    // Interface - OnPrintersListChange (Tablet View)
    // ================================================================================
    
    public interface OnPrintersListChangeCallback {
        public void onAddedNewPrinter(Printer printer);
    }
}
