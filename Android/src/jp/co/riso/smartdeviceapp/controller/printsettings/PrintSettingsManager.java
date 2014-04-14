package jp.co.riso.smartdeviceapp.controller.printsettings;

import java.util.HashMap;

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
     * @return hashmap of the settings values retrieved
     */
    public HashMap<String, Integer> getPrintSetting(int printerId) {
        HashMap<String, Integer> settingValues = new HashMap<String, Integer>();
        Cursor c = mManager.query(KeyConstants.KEY_SQL_PRINTSETTING_TABLE, null, KeyConstants.KEY_SQL_PRINTER_ID + "=?",
                new String[] { String.valueOf(printerId) }, null, null, null);
        // overwrite values if there is an entry retrieved from database
        if (c.moveToFirst()) {
            for (String key : PrintSettings.sSettingMap.keySet()) {
                Setting setting = PrintSettings.sSettingMap.get(key);
                int col = c.getColumnIndex(setting.getDbKey());
                
                switch (setting.getType()) {
                    case Setting.TYPE_LIST:
                    case Setting.TYPE_NUMERIC:
                        settingValues.put(key, c.getInt(col));
                        break;
                    case Setting.TYPE_BOOLEAN:
                        settingValues.put(key, Boolean.parseBoolean(c.getString(col)) ? 1 : 0);
                        break;
                }
            }
        }
        c.close();
        mManager.close();
        return settingValues;
    }
    
    /**
     * This method inserts a PrintSetting entry in the database or replaces the entry if the
     * PrintSetting is already existing; and updates the Print Setting ID in the Printer table.
     * 
     * @param printerId
     *            current printer ID selected
     * @param settingValues
     *            values of the settings to be saved
     * @return boolean result of insert/replace to DB, returns true if successful.
     */
    public boolean saveToDB(int printerId, HashMap<String, Integer> settingValues) {
        boolean result = false;
        // save to PrintSetting table
        long rowid = mManager.insertOrReplace(KeyConstants.KEY_SQL_PRINTSETTING_TABLE, null,
                createContentValues(printerId, settingValues));
        
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
     * @param settingValues
     *            values of the settings to be saved
     * @return content value containing the print settings
     */
    private ContentValues createContentValues(int printerId, HashMap<String, Integer> settingValues) {
        ContentValues cv = new ContentValues();
        cv.put(KeyConstants.KEY_SQL_PRINTER_ID, printerId);
        
        for (String key : PrintSettings.sSettingMap.keySet()) {
            Setting setting = PrintSettings.sSettingMap.get(key);
            String dbKey = setting.getDbKey();
            
            switch (setting.getType()) {
                case Setting.TYPE_LIST:
                case Setting.TYPE_NUMERIC:
                    cv.put(dbKey, settingValues.get(key));
                    break;
                case Setting.TYPE_BOOLEAN:
                    cv.put(dbKey, settingValues.get(key) == 1 ? true : false);
                    break;
            }
        }
        
        // get pst_id of the current printer
        Cursor c = mManager.query(KeyConstants.KEY_SQL_PRINTER_TABLE, new String[] { KeyConstants.KEY_SQL_PRINTSETTING_ID },
                KeyConstants.KEY_SQL_PRINTER_ID + "=?", new String[] { String.valueOf(printerId) }, null, null, null);
        
        if (c.moveToFirst()) {
            if (!c.isNull(c.getColumnIndex(KeyConstants.KEY_SQL_PRINTSETTING_ID))) {
                int pst_id = c.getInt(c.getColumnIndex(KeyConstants.KEY_SQL_PRINTSETTING_ID));
                cv.put(KeyConstants.KEY_SQL_PRINTSETTING_ID, pst_id);
            }
        }
        
        c.close();
        return cv;
    }
}
