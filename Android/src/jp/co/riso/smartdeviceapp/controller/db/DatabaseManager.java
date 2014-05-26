/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * DatabaseManager.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.controller.db;

import jp.co.riso.android.util.AppUtils;
import jp.co.riso.android.util.Logger;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseManager extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    
    private static final String DATABASE_NAME = "SmartDeviceAppDB.sqlite";
    private static final String DATABASE_SQL = "db/SmartDeviceAppDB.sql";
    
    private static final String INITIALIZE_SQL = "db/initializeDB.sql"; // for testing only
    private static final boolean INITIALIZE_DATA = false; // set to true for testing
    
    private Context mContext;
    
    /**
     * Constructor
     * 
     * @param context
     */
    public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }
    
    /** {@inheritDoc} */
    @Override
    public void onOpen(SQLiteDatabase db) {
        // http://stackoverflow.com/questions/13641250/sqlite-delete-cascade-not-working
        db.execSQL("PRAGMA foreign_keys = ON;");
    }
    
    /** {@inheritDoc} */
    @Override
    public void onCreate(SQLiteDatabase db) {
        Logger.logInfo(DatabaseManager.class, "onCreate - Begin");
        
        String sqlString = AppUtils.getFileContentsFromAssets(mContext, DATABASE_SQL);
        String[] separated = sqlString.split(";");
        
        for (int i = 0; i < separated.length; i++) {
            db.execSQL(separated[i]);
        }
        
        /* for testing only */
        if (INITIALIZE_DATA) {
            sqlString = AppUtils.getFileContentsFromAssets(mContext, INITIALIZE_SQL);
            separated = sqlString.split(";");
            
            for (int i = 0; i < separated.length; i++) {
                db.execSQL(separated[i]);
            }
        }
        /* end of for testing only */

        Logger.logInfo(DatabaseManager.class, "onCreate - End");
    }
    
    /** {@inheritDoc} */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logger.logInfo(DatabaseManager.class, "onUpgrade - Begin (" + oldVersion + "=>" + newVersion + ")");
        // Should not happen for now
        Logger.logInfo(DatabaseManager.class, "onUpgrade - End");
    }
    
    /**
     * Get the value of the requested column as a String.
     * 
     * @param cursor
     *            cursor
     * @param columnName
     *            column name
     * @return Returns the value of the requested column as a String.
     */
    public static String getStringFromCursor(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndex(columnName));
    }
    
    /**
     * Get the value of the requested column as an integer.
     * 
     * @param cursor
     *            cursor
     * @param columnName
     *            column name
     * @return Returns the value of the requested column as an integer.
     */
    public static int getIntFromCursor(Cursor cursor, String columnName) {
        return cursor.getInt(cursor.getColumnIndex(columnName));
    }
    
    /**
     * Get the value of the requested column as a boolean.
     * <p>
     * Converts integer value retrieved from database to boolean
     * http://www.sqlite.org/datatype3.html
     * http://stackoverflow.com/questions/2510652/is-there-a-boolean-literal-in-sqlite
     * 
     * @param cursor
     *            cursor
     * @param columnName
     *            column name
     * @return Returns the value of the requested column as a boolean.
     */
    public static boolean getBooleanFromCursor(Cursor cursor, String columnName) {
        return (cursor.getInt(cursor.getColumnIndex(columnName)) == 1);
    }
    
    /**
     * Convenience method for inserting a row into the database.
     * 
     * @param table
     *            the table to insert the row into
     * @param nullColumnHack
     *            optional; may be null. SQL doesn't allow inserting a completely empty row without naming at least one
     *            column name. If your provided values is empty, no column names are known and an empty row can't be
     *            inserted. If not set to null, the nullColumnHack parameter provides the name of nullable column name
     *            to explicitly insert a NULL into in the case where your values is empty.
     * @param values
     *            this map contains the initial column values for the row. The keys should be the column names and the
     *            values the column values
     * @return insert is successful
     */
    public boolean insert(String table, String nullColumnHack, ContentValues values) {
        long rowId = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        
        try {
            rowId = db.insertOrThrow(table, nullColumnHack, values);
        } catch (SQLException e) {
            Logger.logError(DatabaseManager.class, "failed insert to " + table + ". Error: " + e.getMessage());
        }
        
        db.close();
        
        return (rowId > -1);
    }
    
    /**
     * General method for inserting a row into the database.
     * When a UNIQUE constraint violation occurs, the pre-existing rows that are causing the
     * constraint violation are removed prior to inserting or updating the current row.
     * 
     * @param table
     *           the table to insert / replace the row into
     * @param nullColumnHack
     *            optional; may be null. SQL doesn't allow inserting a completely empty row without naming at least one
     *            column name. If your provided values is empty, no column names are known and an empty row can't be
     *            inserted. If not set to null, the nullColumnHack parameter provides the name of nullable column name
     *            to explicitly insert a NULL into in the case where your values is empty.
     * @param values
     *            this map contains the initial column values for the row. The keys should be the column names and the
     *            values the column values
     * @return rowId of the inserted row if successful else -1
     */
    public long insertOrReplace(String table, String nullColumnHack, ContentValues values) {
        long rowId = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        
        try {
            rowId = db.insertWithOnConflict(table, nullColumnHack, values, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (SQLException e) {
            Logger.logError(DatabaseManager.class, "failed insert to " + table + ". Error: " + e.getMessage());
        }
        
        db.close();
        
        return rowId;
    }
    
    /**
     * Convenience method for updating rows in the database.
     * 
     * @param table
     *            the table to update in
     * @param whereClause
     *            the optional WHERE clause to apply when updating. Passing null will update all rows.
     * @param whereArg
     *            You may include ?s in the where clause, which will be replaced by the value from whereArg.
     * @return update is successful
     */
    public boolean update(String table, ContentValues values, String whereClause, String whereArg) {
        int rowsNum = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        String whereArgs[] = null;
        
        if (whereArg != null && !whereArg.isEmpty()) {
            whereArgs = new String[] { whereArg };
        }
        
        rowsNum = db.update(table, values, whereClause, whereArgs);
        db.close();
        
        return (rowsNum > 0);
    }
    
    /**
     * Query the given table, returning a Cursor over the result set.
     * 
     * @param table
     *            The table name to compile the query against.
     * @param columns
     *            A list of which columns to return.
     * @param selection
     *            A filter declaring which rows to return, formatted as an SQL WHERE clause (excluding the WHERE
     *            itself).
     * @param selectionArgs
     *            You may include ?s in selection, which will be replaced by the values from selectionArgs, in order
     *            that they appear in the selection.
     * @param groupBy
     *            A filter declaring how to group rows, formatted as an SQL GROUP BY clause (excluding the GROUP BY
     *            itself).
     * @param having
     *            A filter declare which row groups to include in the cursor, if row grouping is being used, formatted
     *            as an SQL HAVING clause (excluding the HAVING itself). Passing null will cause all row groups to be
     *            included, and is required when row grouping is not being used.
     * @param orderBy
     *            How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself).
     * @return cursor
     */
    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
        return cur;
    }
    
    /**
     * Delete items from database
     * 
     * @param table
     *            the table to delete from
     * 
     * @param whereClause
     *            the optional WHERE clause to apply when deleting. Passing null will delete all rows.
     * @param whereArg
     *            You may include ?s in the where clause, which will be replaced by the value from whereArg.
     * @return delete is successful
     */
    public boolean delete(String table, String whereClause, String whereArg) {
        String whereArgs[] = null;
        if (whereArg != null && !whereArg.isEmpty()) {
            whereArgs = new String[] { whereArg };
        }
        
        return delete(table, whereClause, whereArgs);
    }
    
    /**
     * Delete items from database
     * 
     * @param table
     *            the table to delete from
     * 
     * @param whereClause
     *            the optional WHERE clause to apply when deleting. Passing null will delete all rows.
     * @param whereArgs
     *            You may include ?s in the where clause, which will be replaced by the values from whereArgs. The
     *            values will be bound as Strings.
     * @return delete is successful
     */
    private boolean delete(String table, String whereClause, String[] whereArgs) {
        int rowsNum = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        rowsNum = db.delete(table, whereClause, whereArgs);
        db.close();
        
        return (rowsNum > 0);
    }
}
