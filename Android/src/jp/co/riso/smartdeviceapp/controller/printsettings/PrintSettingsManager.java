/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintSettingsManager.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.controller.printsettings;

import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager;
import jp.co.riso.smartdeviceapp.controller.db.KeyConstants;
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings;
import jp.co.riso.smartdeviceapp.model.printsettings.Setting;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class PrintSettingsManager {
    private static PrintSettingsManager sInstance;
    
    private DatabaseManager mManager;
    
    private PrintSettingsManager(Context context) {
        mManager = new DatabaseManager(context);
    }
    
    public static PrintSettingsManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PrintSettingsManager(context);
        }
        return sInstance;
    }
    
    /**
     * This method retrieves the Printer Settings from the database using printer ID.
     * 
     * @param printerId
     *            current printer ID selected
     * @return PrintSettings object containing the values from the database
     */
    public PrintSettings getPrintSetting(int printerId) {
        PrintSettings printSettings = new PrintSettings();
        
        Cursor c = mManager.query(KeyConstants.KEY_SQL_PRINTSETTING_TABLE, null, KeyConstants.KEY_SQL_PRINTER_ID + "=?",
                new String[] { String.valueOf(printerId) }, null, null, null);
        // overwrite values if there is an entry retrieved from database
        if (c.moveToFirst()) {
            for (String key : PrintSettings.sSettingMap.keySet()) {
                Setting setting = PrintSettings.sSettingMap.get(key);
                
                switch (setting.getType()) {
                    case Setting.TYPE_LIST:
                    case Setting.TYPE_NUMERIC:
                        printSettings.setValue(key, DatabaseManager.getIntFromCursor(c, setting.getDbKey()));
                        break;
                    case Setting.TYPE_BOOLEAN:
                        printSettings.setValue(key, Boolean.parseBoolean(DatabaseManager.getStringFromCursor(c, setting.getDbKey())) ? 1 : 0);
                        break;
                }
            }
        }
        c.close();
        mManager.close();
        return printSettings;
    }
    
    /**
     * This method inserts a PrintSetting entry in the database or replaces the entry if the
     * PrintSetting is already existing; and updates the Print Setting ID in the Printer table.
     * 
     * @param printerId
     *            current printer ID selected
     * @param printSettings
     *            values of the settings to be saved
     * @return boolean result of insert/replace to DB, returns true if successful.
     */
    public boolean saveToDB(int printerId, PrintSettings printSettings) {
        boolean result = false;
        // save to PrintSetting table
        long rowid = mManager.insertOrReplace(KeyConstants.KEY_SQL_PRINTSETTING_TABLE, null,
                createContentValues(printerId, printSettings));
        
        // update pst_id of Printer table
        if (rowid != -1) {
            ContentValues cv = new ContentValues();
            cv.put(KeyConstants.KEY_SQL_PRINTSETTING_ID, rowid);
            result = mManager.update(KeyConstants.KEY_SQL_PRINTER_TABLE, cv,
                    KeyConstants.KEY_SQL_PRINTER_ID + "=?", new String[] { String.valueOf(printerId) });
        }
        
        mManager.close();
        return result;
    }
    
    /**
     * This method converts the Print Settings Values into a ContentValues object.
     * 
     * @param printerId
     *            current printer ID selected
     * @param printSettings
     *            values of the settings to be saved
     * @return content value containing the print settings
     */
    private ContentValues createContentValues(int printerId, PrintSettings printSettings) {
        ContentValues cv = new ContentValues();
        cv.put(KeyConstants.KEY_SQL_PRINTER_ID, printerId);
        
        for (String key : PrintSettings.sSettingMap.keySet()) {
            Setting setting = PrintSettings.sSettingMap.get(key);
            String dbKey = setting.getDbKey();
            
            switch (setting.getType()) {
                case Setting.TYPE_LIST:
                case Setting.TYPE_NUMERIC:
                    cv.put(dbKey, printSettings.getValue(key));
                    break;
                case Setting.TYPE_BOOLEAN:
                    cv.put(dbKey, printSettings.getValue(key) == 1 ? true : false);
                    break;
            }
        }
        
        // get pst_id of the current printer
        Cursor c = mManager.query(KeyConstants.KEY_SQL_PRINTER_TABLE, new String[] { KeyConstants.KEY_SQL_PRINTSETTING_ID },
                KeyConstants.KEY_SQL_PRINTER_ID + "=?", new String[] { String.valueOf(printerId) }, null, null, null);
        
        if (c.moveToFirst()) {
            if (!c.isNull(c.getColumnIndex(KeyConstants.KEY_SQL_PRINTSETTING_ID))) {
                int pst_id = DatabaseManager.getIntFromCursor(c, KeyConstants.KEY_SQL_PRINTSETTING_ID);
                cv.put(KeyConstants.KEY_SQL_PRINTSETTING_ID, pst_id);
            }
        }
        
        c.close();
        return cv;
    }
}