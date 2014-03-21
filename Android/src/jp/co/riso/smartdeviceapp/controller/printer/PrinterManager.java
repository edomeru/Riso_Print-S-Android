/*
 * Copyright (c) 2014 All rights reserved.
 *
 * PrinterManager.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.controller.printer;

import java.util.ArrayList;
import java.util.List;

import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager;
import jp.co.riso.smartdeviceapp.controller.db.KeyConstants;
import jp.co.riso.smartdeviceapp.controller.snmp.SNMPManager;
import jp.co.riso.smartdeviceapp.controller.snmp.SNMPManager.OnSNMPSearch;
import jp.co.riso.smartdeviceapp.model.Printer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class PrinterManager implements OnSNMPSearch {
    private List<Printer> mPrinterList;
    private Context mContext;
    private boolean mDefaultExists = false;
    private boolean mIsSearching = false;
    private static SNMPManager mSNMPManager = null;
    private static PrinterManager mSharedMngr = null;
    
    private PrinterManager(Context context) {
        mContext = context;
        mPrinterList = new ArrayList<Printer>();
        mSNMPManager = new SNMPManager();
        mSNMPManager.setOnPrinterSearchListener(this);
    }
    
    public static PrinterManager sharedManager(Context context) {
        if (mSharedMngr == null) {
            mSharedMngr = new PrinterManager(context);
        }
        return mSharedMngr;
    }
    
    // ================================================================================
    // DataBase
    // ================================================================================
    
    public int savePrinterToDB(Printer printer) {
        if (printer == null || isExists(printer)) {
            return -1;
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
        
        // Save Printer Information to Database
        DatabaseManager manager = new DatabaseManager(mContext);
        SQLiteDatabase db = manager.getWritableDatabase();
        if (db == null) {
            return -1;
        }
        if (db.insert(KeyConstants.KEY_SQL_PRINTER_TABLE, null, newPrinter) == -1) {
            manager.close();
            return -1;
        }
        
        if (mDefaultExists == false) {
            setDefaultPrinter(printer);
            mDefaultExists = true;
        }
        manager.close();
        
        return 0;
    }
    
    public boolean isExists(Printer printer) {
        if (printer == null) {
            return false;
        }
        
        DatabaseManager manager = new DatabaseManager(mContext);
        SQLiteDatabase db = manager.getWritableDatabase();
        if (db == null) {
            return false;
        }
        
        Cursor cursor = db.query(KeyConstants.KEY_SQL_PRINTER_TABLE, null, KeyConstants.KEY_SQL_PRINTER_NAME + "=? and " + KeyConstants.KEY_SQL_PRINTER_IP
                + "=?", new String[] { printer.getName(), printer.getIpAddress() }, null, null, null);
        if (cursor.getCount() > 0) {
            manager.close();
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }
    
    public List<Printer> getSavedPrintersList() {
        DatabaseManager manager = new DatabaseManager(mContext);
        SQLiteDatabase db = manager.getWritableDatabase();
        
        if (db == null) {
            return null;
        }
        
        Cursor cursor = db.query(KeyConstants.KEY_SQL_PRINTER_TABLE, new String[] { KeyConstants.KEY_SQL_PRINTER_ID, KeyConstants.KEY_SQL_PRINTER_NAME,
                KeyConstants.KEY_SQL_PRINTER_IP }, null, null, null, null, null);
        
        mPrinterList.clear();
        if (cursor.getCount() < 1) {
            manager.close();
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
        manager.close();
        cursor.close();
        return mPrinterList;
    }
    
    public int setDefaultPrinter(Printer printer) {
        
        if (printer == null) {
            return -1;
        }
        
        DatabaseManager manager = new DatabaseManager(mContext);
        SQLiteDatabase db = manager.getWritableDatabase();
        if (db == null) {
            return -1;
        }
        
        Cursor cursor = db.query(KeyConstants.KEY_SQL_PRINTER_TABLE, null, KeyConstants.KEY_SQL_PRINTER_NAME + "=? and " + KeyConstants.KEY_SQL_PRINTER_IP
                + "=?", new String[] { printer.getName(), printer.getIpAddress() }, null, null, null);
        
        if (cursor.getCount() != 1) {
            manager.close();
            cursor.close();
            return -1;
        }
        
        ContentValues newDefaultPrinter = new ContentValues();
        
        if (cursor.moveToFirst()) {
            newDefaultPrinter.put(KeyConstants.KEY_SQL_PRINTER_ID, cursor.getInt(cursor.getColumnIndexOrThrow(KeyConstants.KEY_SQL_PRINTER_ID)));
            cursor.close();
        } else {
            manager.close();
            return -1;
        }
        
        if (db.delete(KeyConstants.KEY_SQL_DEFAULT_PRINTER_TABLE, null, null) == -1) {
            manager.close();
            return -1;
        }
        if (db.insert(KeyConstants.KEY_SQL_DEFAULT_PRINTER_TABLE, null, newDefaultPrinter) == -1) {
            manager.close();
            return -1;
        }
        manager.close();
        return 0;
    }
    
    public int clearDefaultPrinter() {
        DatabaseManager manager = new DatabaseManager(mContext);
        SQLiteDatabase db = manager.getWritableDatabase();
        if (db == null) {
            return -1;
        }
        
        if (db.delete(KeyConstants.KEY_SQL_DEFAULT_PRINTER_TABLE, null, null) == -1) {
            manager.close();
            return -1;
        }
        
        manager.close();
        return 0;
    }
    
    public boolean removePrinter(Printer printer) {
        DatabaseManager manager = new DatabaseManager(mContext);
        SQLiteDatabase db = manager.getWritableDatabase();
        if (db == null) {
            return false;
        }
        db.delete(KeyConstants.KEY_SQL_PRINTER_TABLE, KeyConstants.KEY_SQL_PRINTER_ID + "=?", new String[] { "" + printer.getId() });
        manager.close();
        return true;
    }
    
    public int getDefaultPrinter() {
        int printer = -1;
        DatabaseManager manager = new DatabaseManager(mContext);
        SQLiteDatabase db = manager.getWritableDatabase();
        if (db == null) {
            return -1;
        }
        
        Cursor cursor = db.query(KeyConstants.KEY_SQL_DEFAULT_PRINTER_TABLE, null, KeyConstants.KEY_SQL_PRINTER_ID, null, null, null, null);
        
        if (cursor.getCount() != 1) {
            manager.close();
            cursor.close();
            return -1;
        }
        
        if (cursor.moveToFirst()) {
            printer = cursor.getInt(cursor.getColumnIndexOrThrow(KeyConstants.KEY_SQL_PRINTER_ID));
        }
        
        cursor.close();
        manager.close();
        return printer;
    }
    
    public void startPrinterSearch() {
        mIsSearching = true;
        mSNMPManager.startSNMP();
    }
    
    public void searchPrinter(String ipAddress) {
        mIsSearching = true;
        mSNMPManager.searchPrinter(ipAddress);
    }
    
    public boolean isSearching() {
        return mIsSearching;
    }
    
    // ================================================================================
    // Interface - OnSNMPSearch (SNMP Callback)
    // ================================================================================
    
    @Override
    public void onSearchedPrinterAdd(String printerName, String ipAddress) {
        mOnPrinterAdd.onPrinterAdd(new Printer(printerName, ipAddress, false, null));
        if (mOnPrintersListRefresh != null) {
            mOnPrintersListRefresh.onPrintersListRefresh();
        }
    }
    
    @Override
    public void onSearchEnd() {
        mIsSearching = false;
        mOnPrinterAdd.onSearchEnd();
    }
    
    // ================================================================================
    // Interface - OnPrinterSearch (SNMP)
    // ================================================================================
    
    private OnPrinterSearch mOnPrinterAdd = null;
    
    public interface OnPrinterSearch {
        public void onPrinterAdd(Printer printer);
        
        public void onSearchEnd();
    }
    
    public void setOnPrinterSearchListener(OnPrinterSearch onPrinterSearch) {
        mOnPrinterAdd = onPrinterSearch;
    }
    
    // ================================================================================
    // Interface - OnPrintersListRefresh (Tablet View)
    // ================================================================================
    
    private OnPrintersListRefresh mOnPrintersListRefresh = null;
    
    public interface OnPrintersListRefresh {
        public void onPrintersListRefresh();
    }
    
    public void setOnPrintersListRefresh(OnPrintersListRefresh onPrintersListRefresh) {
        mOnPrintersListRefresh = onPrintersListRefresh;
    }
}
