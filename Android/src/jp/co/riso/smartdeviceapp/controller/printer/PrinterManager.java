/*
 * Copyright (c) 2013 alink-group. All rights reserved.
 *
 * PrinterManager.java
 * SmartDevice
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.controller.printer;

import java.util.ArrayList;
import java.util.List;

import jp.co.riso.smartdeviceapp.controller.KeyConstants;
import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager;
import jp.co.riso.smartdeviceapp.model.Printer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class PrinterManager {
    private List<Printer> mPrinterList;
    private Context mContext;
    private boolean mDefaultExists = false;
    
    private static PrinterManager mSharedMngr;
    
    private PrinterManager(Context context) {
        mContext = context;
        mPrinterList = new ArrayList<Printer>();
    }
      
    public static PrinterManager sharedManager(Context context) {
        if (mSharedMngr == null) {
            mSharedMngr = new PrinterManager(context);
        }
        return mSharedMngr;
    }
        
    
    // ================================================================================
    // DataBase()
    // ================================================================================
    public long savePrinterToDB(Printer printer) {
        long rowId = -1;
        if (printer == null || isExists(printer)) {
            return -1;
        }
        
        //Create Content
        ContentValues newPrinter = new ContentValues(); 
        
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_IP, printer.getIpAddress());
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_NAME, printer.getName()); 
        //TODO update values
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_PORT, 0);
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_EN_LPR, true); 
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_EN_RAW, true);
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_EN_PAGINATION, true); 
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_EN_DUPLEX, true);
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_EN_BOOKLET_BINDING, true); 
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_EN_STAPLE, true);
        newPrinter.put(KeyConstants.KEY_SQL_PRINTER_EN_BIND, true);
        
        //Save Printer Information to Database
        DatabaseManager manager = new DatabaseManager(mContext);
        SQLiteDatabase db = manager.getWritableDatabase();
        if(db == null) {
            return -1;
        }
        if((rowId = db.insert(KeyConstants.KEY_SQL_PRINTER_TABLE, null, newPrinter)) == -1) {   
            manager.close();
            return -1;
        }
        manager.close();

        
        if(mDefaultExists == false) {
            setDefaultPrinter(printer);
            mDefaultExists = true;
        }
            
        return rowId;
    }
    
    public boolean isExists(Printer printer) {
        if(printer == null){
            return false;
        }
        
        //Check database
        DatabaseManager manager = new DatabaseManager(mContext);
        SQLiteDatabase db = manager.getWritableDatabase();
        
        if(db == null){
            return false;
        }
        
        Cursor cursor = db.query(KeyConstants.KEY_SQL_PRINTER_TABLE, null, 
                KeyConstants.KEY_SQL_PRINTER_NAME + "=? and " + KeyConstants.KEY_SQL_PRINTER_IP + "=?", 
                new String[]{printer.getName(), printer.getIpAddress()}, 
                null, null, null);
        if(cursor.getCount() > 0)  
        {
            manager.close();
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }
    
    public List<Printer> getSavedPrintersList() {
        //Check database
        DatabaseManager manager = new DatabaseManager(mContext);
        SQLiteDatabase db = manager.getWritableDatabase();
        
        if(db == null) {
            return null;
        }        
        
        Cursor cursor = db.query(KeyConstants.KEY_SQL_PRINTER_TABLE, 
                new String[]{KeyConstants.KEY_SQL_PRINTER_ID, 
                KeyConstants.KEY_SQL_PRINTER_NAME, KeyConstants.KEY_SQL_PRINTER_IP }, 
                null, null, null, null, null);
        
        mPrinterList.clear();
        if(cursor.getCount() < 1) {
            manager.close();
            cursor.close();
            mDefaultExists = false;
            return mPrinterList;
        }
        if (cursor.moveToFirst()) { 
            do {
                Printer printer = new Printer(cursor.getString(cursor.getColumnIndexOrThrow(KeyConstants.KEY_SQL_PRINTER_NAME)), 
                        cursor.getString(cursor.getColumnIndexOrThrow(KeyConstants.KEY_SQL_PRINTER_IP)), 
                        false, null);
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
        if(printer == null) {
            return -1;
        }
        
        //Check database
        DatabaseManager manager = new DatabaseManager(mContext);
        SQLiteDatabase db = manager.getWritableDatabase();
        if(db == null) {
            return -1;
        }
        
        Cursor cursor = db.query(KeyConstants.KEY_SQL_PRINTER_TABLE, 
                null, 
                KeyConstants.KEY_SQL_PRINTER_NAME + "=? and " + KeyConstants.KEY_SQL_PRINTER_IP + "=?", 
                new String[]{printer.getName(), printer.getIpAddress()}, 
                null, null, null);
        
        if(cursor.getCount() != 1) {
            manager.close();
            cursor.close();
            return -1;
        }

        ContentValues newDefaultPrinter = new ContentValues();

        if (cursor.moveToFirst()) { 
                newDefaultPrinter.put(KeyConstants.KEY_SQL_PRINTER_ID, cursor.getInt(cursor.getColumnIndexOrThrow(KeyConstants.KEY_SQL_PRINTER_ID)));
                cursor.close();
        }
        else {
            manager.close();
            return -1;
        }


        if(db.delete(KeyConstants.KEY_SQL_DEFAULT_PRINTER_TABLE, null, null) == -1) {
            manager.close();
            return -1;
        }
        if(db.insert(KeyConstants.KEY_SQL_DEFAULT_PRINTER_TABLE, null, newDefaultPrinter) == -1) {
            manager.close();
            return -1;
        }
        manager.close();
        return 0;
    }

    public boolean removePrinter(Printer printer) {
        
        //Check database
        DatabaseManager manager = new DatabaseManager(mContext);
        SQLiteDatabase db = manager.getWritableDatabase();
        if(db == null) {
            return false;
        }
        db.delete(KeyConstants.KEY_SQL_PRINTER_TABLE, KeyConstants.KEY_SQL_PRINTER_ID + "=?", 
                new String[]{"" + printer.getId()}); 
        manager.close();
        return true;
    }
    
   public int getDefaultPrinter() {
        int printer = -1;
        //Check database
        DatabaseManager manager = new DatabaseManager(mContext);
        SQLiteDatabase db = manager.getWritableDatabase();
        if(db == null) {
            return -1;
        }
        
        Cursor cursor = db.query(KeyConstants.KEY_SQL_DEFAULT_PRINTER_TABLE, null, 
                KeyConstants.KEY_SQL_PRINTER_ID, 
                null, 
                null, null, null);
        
        if(cursor.getCount() != 1) {
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
}
