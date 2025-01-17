/*
 * Copyright (c) 2022 RISO, Inc. All rights reserved.
 *
 * DatabaseManager.kt
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.controller.db

import android.annotation.SuppressLint
import jp.co.riso.android.util.Logger.logInfo
import jp.co.riso.android.util.AppUtils.getFileContentsFromAssets
import jp.co.riso.android.util.Logger.logError
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.controller.jobs.PrintJobManager
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.util.Log
import jp.co.riso.smartdeviceapp.controller.db.KeyConstants.KEY_SQL_CONTENT_FILE_ID
import jp.co.riso.smartdeviceapp.controller.db.KeyConstants.KEY_SQL_CONTENT_FILE_NAME
import jp.co.riso.smartdeviceapp.controller.db.KeyConstants.KEY_SQL_CONTENT_PRINT_TABLE
import jp.co.riso.smartdeviceapp.controller.db.KeyConstants.KEY_SQL_CONTENT_USER_CURRENT_EMAIL
import jp.co.riso.smartdeviceapp.controller.print.ContentPrintManager.Companion.TAG
import java.util.*

/**
 * @class DatabaseManager
 *
 * @brief Helper class for opening, creating and managing the database.
 * @brief Creates a DatabaseManager instance.
 *
 * @param _context Context to use to open or create the database.
 */
open class DatabaseManager(private val _context: Context?) :
    SQLiteOpenHelper(_context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onOpen(db: SQLiteDatabase) {
        // http://stackoverflow.com/questions/13641250/sqlite-delete-cascade-not-working
        try {
            db.execSQL("PRAGMA foreign_keys = ON;")
        } catch (e: SQLException) {
            // Do nothing
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        logInfo(DatabaseManager::class.java, "onCreate - Begin")

        db.execSQL(SQL_CREATE_CONTENT_PRINT_TABLE)
        // initial database structure
        executeSqlCommandFromScript(db, DATABASE_SQL)
        if (DATABASE_VERSION > DATABASE_VERSION_01) {   // For database v2
            executeSqlCommandFromScript(db, DATABASE_SQLv2)
        }
        if (DATABASE_VERSION > DATABASE_VERSION_02) { // For database v3
            executeSqlCommandFromScript(db, DATABASE_SQLv3)
        }
        if (DATABASE_VERSION > DATABASE_VERSION_03) { // For database v4
            executeSqlCommandFromScript(db, DATABASE_SQLv4)
        }

        /* for testing only */if (AppConstants.INITIAL_DB) {
            executeSqlCommandFromScript(db, INITIALIZE_SQL)
            if (AppConstants.FOR_PERF_LOGS) {
                for (i in 1..10) {
                    for (j in 1..100) {
                        val sql = String.format(
                            Locale.getDefault(), "INSERT INTO PrintJob" +
                                    "(prn_id, pjb_name, pjb_date, pjb_result) VALUES ('%d', '%s', '%s', '%d')",
                            i, "Print Job $j", PrintJobManager.convertDateToString(null), j % 2
                        )
                        try {
                            db.execSQL(sql)
                        } catch (e: SQLException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
        /* end of for testing only */logInfo(DatabaseManager::class.java, "onCreate - End")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        logInfo(DatabaseManager::class.java, "onUpgrade - Begin ($oldVersion=>$newVersion)")

        // For database v2
        if (oldVersion < DATABASE_VERSION_03) {
            executeSqlCommandFromScript(db, DATABASE_SQLv3)
        }
        // For database v3
        if (oldVersion < DATABASE_VERSION_04) {
            executeSqlCommandFromScript(db, DATABASE_SQLv4)
        }
        // Should not happen for now
        logInfo(DatabaseManager::class.java, "onUpgrade - End")
    }

    @SuppressLint("Range")
    fun getColumnTypes(tableName: String): Map<String, String>? {
        val columns = mutableMapOf<String, String>()
        val db = readableDatabase
        val cursor = db.rawQuery("PRAGMA table_info($tableName)", null)
        try {
            if (cursor.moveToFirst()) {
                do {
                    val name = cursor.getString(cursor.getColumnIndex("name"))
                    val type = cursor.getString(cursor.getColumnIndex("type"))
                    columns[name] = type
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error retrieving column types", e)
        } finally {
            cursor.close()
            db.close()
        }
        return columns
    }


    @SuppressLint("Range")
    fun getAllViewedFiles(emailAdd: String?): List<ViewedFiles> {
        val dataList = mutableListOf<ViewedFiles>()
        val db = readableDatabase
        var cursor: Cursor? = null
        try {
            if (emailAdd == null) {
                Log.e(TAG, "Email address is null")
                return dataList
            }

            val query =
                "SELECT * FROM $KEY_SQL_CONTENT_PRINT_TABLE WHERE $KEY_SQL_CONTENT_USER_CURRENT_EMAIL  = ?"
            cursor = db.rawQuery(query, arrayOf(emailAdd))
            if (cursor.moveToFirst()) {
                do {
                    val fileId = cursor.getString(cursor.getColumnIndex(KEY_SQL_CONTENT_FILE_ID))
                    val fileName =
                        cursor.getString(cursor.getColumnIndex(KEY_SQL_CONTENT_FILE_NAME))
                    val email = cursor.getString(
                        cursor.getColumnIndex(
                            KEY_SQL_CONTENT_USER_CURRENT_EMAIL
                        )
                    )
                    val data = ViewedFiles(fileId, fileName, email)
                    dataList.add(data)
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching all data from $KEY_SQL_CONTENT_PRINT_TABLE", e)
        } finally {
            cursor?.close()
            db.close()
        }
        return dataList
    }

    data class ViewedFiles(
        val fileId: String,
        val fileName: String,
        val email: String
    )


    /**
     * @brief Executes the SQL script from the given path to SQL script file
     *
     * @param db SQLiteDatabase object
     * @param sqlScript Path to SQL Script file
     */
    private fun executeSqlCommandFromScript(db: SQLiteDatabase, sqlScript: String?) {
        val sqlString = getFileContentsFromAssets(_context, sqlScript)
        val separated = sqlString!!.split(";").toTypedArray()
        for (s in separated) {
            try {
                db.execSQL(s)
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * @brief Convenience method for inserting a row into the database.
     *
     * @param table The table to insert the row into.
     * @param nullColumnHack Optional; may be null. SQL doesn't allow inserting a completely empty row without naming at least one
     * column name. If your provided values is empty, no column names are known and an empty row can't be
     * inserted. If not set to null, the nullColumnHack parameter provides the name of nullable column name
     * to explicitly insert a NULL into in the case where your values is empty.
     * @param values This map contains the initial column values for the row. The keys should be the column names and the
     * values the column values.
     *
     * @retval true Insert is successful.
     * @retval false Insert has failed.
     */
    open fun insert(table: String, nullColumnHack: String?, values: ContentValues?): Boolean {
        var rowId: Long = -1
        try {
            val db = this.writableDatabase
            rowId = db.insertOrThrow(table, nullColumnHack, values)
            db.close()
        } catch (e: SQLException) {
            logError(
                DatabaseManager::class.java,
                "failed insert to " + table + ". Error: " + e.message
            )
        }
        return rowId > -1
    }

    /**
     * @brief General method for inserting a row into the database.
     *
     * When a UNIQUE constraint violation occurs, the pre-existing rows that are causing the
     * constraint violation are removed prior to inserting or updating the current row.
     *
     * @param table The table to insert / replace the row into
     * @param nullColumnHack Optional; may be null. SQL doesn't allow inserting a completely empty row without naming at least one
     * column name. If your provided values is empty, no column names are known and an empty row can't be
     * inserted. If not set to null, the nullColumnHack parameter provides the name of nullable column name
     * to explicitly insert a NULL into in the case where your values is empty.
     * @param values This map contains the initial column values for the row. The keys should be the column names and the
     * values the column values
     *
     * @return Row id of the inserted row.
     * @retval -1 Insert has failed.
     */
    fun insertOrReplace(table: String, nullColumnHack: String?, values: ContentValues?): Long {
        var rowId: Long = -1
        try {
            val db = this.writableDatabase
            rowId = db.insertWithOnConflict(
                table,
                nullColumnHack,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
            )
            db.close()
        } catch (e: SQLException) {
            logError(
                DatabaseManager::class.java,
                "failed insert to " + table + ". Error: " + e.message
            )
        }
        return rowId
    }

    /**
     * @brief Convenience method for updating rows in the database.
     *
     * @param table The table to update in.
     * @param values This map contains the column values for the row. The keys should be the column names and the
     * values the column values
     * @param whereClause The optional WHERE clause to apply when updating. Passing null will update all rows.
     * @param whereArg You may include ?s in the where clause, which will be replaced by the value from whereArg.
     *
     * @retval true Update is successful.
     * @retval false Update has failed.
     */
    fun update(
        table: String,
        values: ContentValues?,
        whereClause: String?,
        whereArg: String?
    ): Boolean {
        var rowsNum = 0
        try {
            val db = this.writableDatabase
            var whereArgs: Array<String>? = null
            if (whereArg != null && whereArg.isNotEmpty()) {
                whereArgs = arrayOf(whereArg)
            }
            rowsNum = db.update(table, values, whereClause, whereArgs)
            db.close()
        } catch (e: SQLException) {
            logError(
                DatabaseManager::class.java,
                "failed update to " + table + ". Error: " + e.message
            )
        }
        return rowsNum > 0
    }

    /**
     * @brief Convenience method for querying, returning a Cursor over the result set.
     *
     * @param table The table name to compile the query against.
     * @param columns A list of which columns to return.
     * @param selection A filter declaring which rows to return, formatted as an SQL WHERE clause (excluding the WHERE
     * itself).
     * @param selectionArgs You may include ?s in selection, which will be replaced by the values from selectionArgs, in order
     * that they appear in the selection.
     * @param groupBy A filter declaring how to group rows, formatted as an SQL GROUP BY clause (excluding the GROUP BY
     * itself).
     * @param having A filter declare which row groups to include in the cursor, if row grouping is being used, formatted
     * as an SQL HAVING clause (excluding the HAVING itself). Passing null will cause all row groups to be
     * included, and is required when row grouping is not being used.
     * @param orderBy How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself).
     *
     * @return Cursor containing the data retrieved from the database.
     * @retval null Query has failed.
     */
    open fun query(
        table: String,
        columns: Array<String?>?,
        selection: String?,
        selectionArgs: Array<String?>?,
        groupBy: String?,
        having: String?,
        orderBy: String?
    ): Cursor? {
        var cur: Cursor? = null
        try {
            val db = this.readableDatabase
            cur = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy)
        } catch (e: SQLException) {
            logError(
                DatabaseManager::class.java,
                "failed query to " + table + ". Error: " + e.message
            )
        }
        return cur
    }

    /**
     * @brief Convenience method for deleting items from database.
     *
     * @param table The table to delete from
     * @param whereClause The optional WHERE clause to apply when deleting. Passing null will delete all rows.
     * @param whereArg You may include ?s in the where clause, which will be replaced by the value from whereArg.
     *
     * @retval true Delete is successful.
     * @retval false Delete has failed.
     */
    fun delete(table: String, whereClause: String?, whereArg: String?): Boolean {
        var whereArgs: Array<String>? = null
        if (whereArg != null && whereArg.isNotEmpty()) {
            whereArgs = arrayOf(whereArg)
        }
        return delete(table, whereClause, whereArgs)
    }

    /**
     * @brief Deletes items from database.
     *
     * @param table The table to delete from
     * @param whereClause the optional WHERE clause to apply when deleting. Passing null will delete all rows.
     * @param whereArgs You may include ?s in the where clause, which will be replaced by the values from whereArgs. The
     * values will be bound as Strings.
     *
     * @retval true Delete is successful.
     * @retval false Delete has failed.
     */
    private fun delete(table: String, whereClause: String?, whereArgs: Array<String>?): Boolean {
        var rowsNum = 0
        try {
            val db = this.writableDatabase
            rowsNum = db.delete(table, whereClause, whereArgs)
            db.close()
        } catch (e: SQLException) {
            logError(
                DatabaseManager::class.java,
                "failed delete to " + table + ". Error: " + e.message
            )
        }
        return rowsNum > 0
    }

    companion object {
        private const val DATABASE_VERSION_01 = 1
        private const val DATABASE_VERSION_02 = 2
        private const val DATABASE_VERSION_03 = 3
        private const val DATABASE_VERSION_04 = 4 ///< current database version of the application

        @JvmField
        val DATABASE_VERSION =
            if (AppConstants.DEBUG_LOWER_DB_VERSION) DATABASE_VERSION_03 else DATABASE_VERSION_04
        private const val DATABASE_NAME = "SmartDeviceAppDB.sqlite"
        private const val DATABASE_SQL = "db/SmartDeviceAppDB.sql"
        private const val DATABASE_SQLv2 = "db/SmartDeviceAppDBv2.sql"
        private const val DATABASE_SQLv3 = "db/SmartDeviceAppDBv3.sql"
        private const val DATABASE_SQLv4 = "db/SmartDeviceAppDBv4.sql"
        private const val INITIALIZE_SQL = "db/initializeDB.sql" // for testing only
        private const val SQL_CREATE_CONTENT_PRINT_TABLE =
            "CREATE TABLE IF NOT EXISTS ContentPrint (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "fileId TEXT NOT NULL," +
                    "fileName TEXT NOT NULL," +
                    "email TEXT NOT NULL" +
                    ");"

        /**
         * @brief Gets the value of the requested column as a String.
         *
         * @param cursor Cursor object
         * @param columnName Name of the column in the database
         *
         * @return Value of the requested column as a String.
         */
        @JvmStatic
        fun getStringFromCursor(cursor: Cursor, columnName: String): String? {
            val columnIndex = cursor.getColumnIndex(columnName)
            return if (columnIndex >= 0) {
                cursor.getString(columnIndex)
            } else {
                logError(
                    DatabaseManager::class.java,
                    "columnName:$columnName not found"
                )
                ""
            }
        }

        /**
         * @brief Gets the value of the requested column as an integer.
         *
         * @param cursor Cursor object
         * @param columnName Name of the column in the database
         *
         * @return Value of the requested column as an integer.
         */
        @JvmStatic
        fun getIntFromCursor(cursor: Cursor, columnName: String): Int {
            val columnIndex = cursor.getColumnIndex(columnName)
            return if (columnIndex >= 0) {
                cursor.getInt(columnIndex)
            } else {
                logError(
                    DatabaseManager::class.java,
                    "columnName:$columnName not found"
                )
                0
            }
        }

        /**
         * @brief Gets the value of the requested column as a boolean (i.e.
         * converts integer value retrieved from database to boolean).
         *
         * Based on http://stackoverflow.com/questions/2510652/is-there-a-boolean-literal-in-sqlite
         *
         * @param cursor Cursor object
         * @param columnName Name of the column in the database
         *
         * @retval true Value is 1.
         * @retval false Value is not 1.
         */
        @JvmStatic
        fun getBooleanFromCursor(cursor: Cursor, columnName: String): Boolean {
            val columnIndex = cursor.getColumnIndex(columnName)
            return if (columnIndex >= 0) {
                cursor.getInt(columnIndex) == 1
            } else {
                logError(
                    DatabaseManager::class.java,
                    "columnName:$columnName not found"
                )
                false
            }
        }
    }
}