/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * PrintSettingsManager.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.controller.printsettings

import android.content.ContentValues
import android.content.Context
import jp.co.riso.android.util.Logger
import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager
import jp.co.riso.smartdeviceapp.controller.db.KeyConstants
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings
import jp.co.riso.smartdeviceapp.model.printsettings.Setting

/**
 * @class PrintSettingsManager
 *
 * @brief Helper class for managing the database transactions of Print Settings.
 */
class PrintSettingsManager private constructor(context: Context) {
    private val _databaseManager: DatabaseManager = DatabaseManager(context)

    /**
     * @brief Retrieves the Printer Settings from the database using printer ID.
     *
     * @param printerId Current printer ID selected
     *
     * @return PrintSettings object containing the values from the database
     */
    fun getPrintSetting(printerId: Int, printerType: String?): PrintSettings {
        val printSettings = PrintSettings(printerType!!)
        val c = _databaseManager.query(
            KeyConstants.KEY_SQL_PRINTSETTING_TABLE,
            null,
            KeyConstants.KEY_SQL_PRINTER_ID + "=?",
            arrayOf(printerId.toString()),
            null,
            null,
            null
        )
        // overwrite values if there is an entry retrieved from database
        if (c != null && c.moveToFirst()) {
            val currentPrintSettings = PrintSettings.sSettingsMaps!![printerType]!!
            for (key in currentPrintSettings.keys) {
                val setting = currentPrintSettings[key]
                when (setting!!.type) {
                    Setting.TYPE_LIST, Setting.TYPE_NUMERIC, Setting.TYPE_BOOLEAN -> printSettings.setValue(
                        key,
                        DatabaseManager.getIntFromCursor(c, setting.dbKey)
                    )
                }
            }
            c.close()
            _databaseManager.close()
        }
        return printSettings
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
    fun saveToDB(printerId: Int, printSettings: PrintSettings?): Boolean {
        if (printerId == PrinterManager.EMPTY_ID || printSettings == null) {
            return false
        }
        var result = false
        // save to PrintSetting table
        val rowId = _databaseManager.insertOrReplace(
            KeyConstants.KEY_SQL_PRINTSETTING_TABLE, null,
            createContentValues(printerId, printSettings)
        )
        // update pst_id of Printer table
        if (rowId != -1L) {
            val cv = ContentValues()
            cv.put(KeyConstants.KEY_SQL_PRINTSETTING_ID, rowId)
            result = _databaseManager.update(
                KeyConstants.KEY_SQL_PRINTER_TABLE, cv,
                KeyConstants.KEY_SQL_PRINTER_ID + "=?", printerId.toString()
            )
        }
        _databaseManager.close()
        return result
    }

    /**
     * @brief Converts the Print Settings Values into a ContentValues object.
     *
     * @param printerId Current printer ID selected
     * @param printSettings Values of the settings to be saved
     *
     * @return ContentValues object containing the print settings
     */
    private fun createContentValues(printerId: Int, printSettings: PrintSettings): ContentValues? {
        val cv = ContentValues()
        cv.put(KeyConstants.KEY_SQL_PRINTER_ID, printerId)
        if (PrintSettings.sSettingsMaps!![printSettings.settingMapKey] == null) {
            return null
        }
        val printerSettings = PrintSettings.sSettingsMaps!![printSettings.settingMapKey]!!
        for (key in printerSettings.keys) {
            val setting = printerSettings[key]
            val dbKey = setting!!.dbKey

            // no need to convert since BOOL is also stored as integer in SQLite DB
            // http://stackoverflow.com/questions/2510652/is-there-a-boolean-literal-in-sqlite
            cv.put(dbKey, printSettings.getValue(key))
        }

        // get pst_id of the current printer
        val c = _databaseManager.query(
            KeyConstants.KEY_SQL_PRINTER_TABLE, arrayOf(KeyConstants.KEY_SQL_PRINTSETTING_ID),
            KeyConstants.KEY_SQL_PRINTER_ID + "=?", arrayOf(printerId.toString()), null, null, null
        )
        if (c != null && c.moveToFirst()) {
            val columnIndex = c.getColumnIndex(KeyConstants.KEY_SQL_PRINTSETTING_ID)
            if (columnIndex >= 0) {
                if (!c.isNull(columnIndex)) {
                    val pstId =
                        DatabaseManager.getIntFromCursor(c, KeyConstants.KEY_SQL_PRINTSETTING_ID)
                    cv.put(KeyConstants.KEY_SQL_PRINTSETTING_ID, pstId)
                }
            } else {
                Logger.logError(
                    PrintSettingsManager::class.java,
                    "columnName:" + KeyConstants.KEY_SQL_PRINTSETTING_ID + " not found"
                )
            }
            c.close()
        }
        return cv
    }

    companion object {
        private var printSettingsManager: PrintSettingsManager? = null

        /**
         * @brief Gets a PrintSettingsManager instance.
         *
         * @param context Context object to use to manage the database.
         *
         * @return PrintSettingsManager instance
         */
        @JvmStatic
        fun getInstance(context: Context): PrintSettingsManager? {
            if (printSettingsManager == null) {
                printSettingsManager = PrintSettingsManager(context)
            }
            return printSettingsManager
        }
    }

}