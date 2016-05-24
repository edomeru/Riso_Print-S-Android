/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintSettingsManager.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.controller.printsettings;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.HashMap;

import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager;
import jp.co.riso.smartdeviceapp.controller.db.KeyConstants;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings;
import jp.co.riso.smartdeviceapp.model.printsettings.Setting;

/**
 * @class PrintSettingsManager
 * 
 * @brief Helper class for managing the database transactions of Print Settings.
 */
public class PrintSettingsManager {
    private static PrintSettingsManager sInstance;
    
    private DatabaseManager mManager;
    
    /**
     * @brief Creates a PrintSettingsManager instance.
     * 
     * @param context Context object to use to manage the database.
     */
    private PrintSettingsManager(Context context) {
        mManager = new DatabaseManager(context);
    }
    
    /**
     * @brief Gets a PrintSettingsManager instance.
     * 
     * @param context Context object to use to manage the database.
     * 
     * @return PrintSettingsManager instance
     */
    public static PrintSettingsManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PrintSettingsManager(context);
        }
        return sInstance;
    }
    
    /**
     * @brief Retrieves the Printer Settings from the database using printer ID.
     * 
     * @param printerId Current printer ID selected
     * 
     * @return PrintSettings object containing the values from the database
     */
    public PrintSettings getPrintSetting(int printerId, String printerType) {
        PrintSettings printSettings = new PrintSettings(printerType);
        
        Cursor c = mManager.query(KeyConstants.KEY_SQL_PRINTSETTING_TABLE, null, KeyConstants.KEY_SQL_PRINTER_ID + "=?",
                new String[]{String.valueOf(printerId)}, null, null, null);
        // overwrite values if there is an entry retrieved from database
        if (c != null && c.moveToFirst()) {
            HashMap<String, Setting> currentPrintSettings = PrintSettings.sSettingsMaps.get(printerType);

            for (String key : currentPrintSettings.keySet()) {
                Setting setting =  currentPrintSettings.get(key);
                
                switch (setting.getType()) {
                    case Setting.TYPE_LIST:
                    case Setting.TYPE_NUMERIC:
                        printSettings.setValue(key, DatabaseManager.getIntFromCursor(c, setting.getDbKey()));
                        break;
                    case Setting.TYPE_BOOLEAN:
                        printSettings.setValue(key, DatabaseManager.getIntFromCursor(c, setting.getDbKey()));
                        break;
                }
            }
            c.close();
            mManager.close();
        }
        
        return printSettings;
    }
    
    /**
     * @brief Inserts a PrintSetting entry in the database or replaces the entry if the
     * PrintSetting is already existing; and updates the Print Setting ID in the Printer table.
     * 
     * @param printerId Current printer ID selected
     * @param printSettings Values of the settings to be saved
     * 
     * @retval true Insert/Replace in DB is successful.
     * @retval false Insert/Replace in DB has failed.
     */
    public boolean saveToDB(int printerId, PrintSettings printSettings) {
        if (printerId == PrinterManager.EMPTY_ID || printSettings == null) {
            return false;
        }
        
        boolean result = false;
        // save to PrintSetting table
        long rowid = mManager.insertOrReplace(KeyConstants.KEY_SQL_PRINTSETTING_TABLE, null,
                createContentValues(printerId, printSettings));
        // update pst_id of Printer table
        if (rowid != -1) {
            ContentValues cv = new ContentValues();
            cv.put(KeyConstants.KEY_SQL_PRINTSETTING_ID, rowid);
            result = mManager.update(KeyConstants.KEY_SQL_PRINTER_TABLE, cv,
                    KeyConstants.KEY_SQL_PRINTER_ID + "=?", String.valueOf(printerId));
        }
        
        mManager.close();
        return result;
    }
    
    /**
     * @brief Converts the Print Settings Values into a ContentValues object.
     * 
     * @param printerId Current printer ID selected
     * @param printSettings Values of the settings to be saved
     * 
     * @return ContentValues object containing the print settings
     */
    private ContentValues createContentValues(int printerId, PrintSettings printSettings) {
        ContentValues cv = new ContentValues();
        cv.put(KeyConstants.KEY_SQL_PRINTER_ID, printerId);
        if(PrintSettings.sSettingsMaps.get(printSettings.getSettingMapKey()) == null){
            return null;
        }

        HashMap<String, Setting> printerSettings = PrintSettings.sSettingsMaps.get(printSettings.getSettingMapKey());

        for (String key : printerSettings.keySet()) {
            Setting setting =  printerSettings.get(key);
            String dbKey = setting.getDbKey();
            
            // no need to convert since BOOL is also stored as integer in SQLite DB
            // http://stackoverflow.com/questions/2510652/is-there-a-boolean-literal-in-sqlite
            cv.put(dbKey, printSettings.getValue(key));
        }
        
        // get pst_id of the current printer
        Cursor c = mManager.query(KeyConstants.KEY_SQL_PRINTER_TABLE, new String[] { KeyConstants.KEY_SQL_PRINTSETTING_ID },
                KeyConstants.KEY_SQL_PRINTER_ID + "=?", new String[] { String.valueOf(printerId) }, null, null, null);
        
        if (c != null && c.moveToFirst()) {
            if (!c.isNull(c.getColumnIndex(KeyConstants.KEY_SQL_PRINTSETTING_ID))) {
                int pstId = DatabaseManager.getIntFromCursor(c, KeyConstants.KEY_SQL_PRINTSETTING_ID);
                cv.put(KeyConstants.KEY_SQL_PRINTSETTING_ID, pstId);
            }
            c.close();
        }
        
        return cv;
    }
}
