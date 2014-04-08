/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * DatabaseManager.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.controller.db;

import jp.co.riso.android.util.AppUtils;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseManager extends SQLiteOpenHelper {
    public static final String TAG = "DatabaseManager";
    public static final String DATABASE_NAME = "SmartDeviceAppDB.sqlite";
    
    private static final String DATABASE_SQL = "db/SmartDeviceAppDB.sql";
    
    private static final String INITIALIZE_SQL = "db/initializeDB.sql"; // for testing only
    private static final boolean INITIALIZE_DATA = true; // set to true for testing
    
    private static final int DATABASE_VERSION = 1;
    
    private Context mContext;
    
    public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }
    
    @Override
    public void onOpen(SQLiteDatabase db) {
        // http://stackoverflow.com/questions/13641250/sqlite-delete-cascade-not-working
        db.execSQL("PRAGMA foreign_keys = ON;");
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate - Begin");
        
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
        
        Log.d(TAG, "onCreate - End");
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade - Begin (" + oldVersion + "=>" + newVersion + ")");
        // Should not happen for now
        Log.d(TAG, "onUpgrade - End");
    }
    
    public boolean insert(String table, String nullColumnHack, ContentValues values) {
        long rowId = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        
        try {
            rowId = db.insertOrThrow(table, nullColumnHack, values);
        } catch (SQLException e) {
            Log.e(TAG, "failed insert to " + table);
        }
        
        db.close();
        
        return (rowId > -1);
    }
    
    public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cur = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
        return cur;
    }
    
    public boolean delete(String table, String whereClause, String whereArg) {
        String whereArgs[] = null;
        if (whereArg != null && !whereArg.isEmpty()) {
            whereArgs = new String[] { whereArg };
        }
        
        return delete(table, whereClause, whereArgs);
    }
    
    private boolean delete(String table, String whereClause, String[] whereArgs) {
        int rowsNum = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        rowsNum = db.delete(table, whereClause, whereArgs);
        db.close();
        
        return (rowsNum > 0);
    }
}
