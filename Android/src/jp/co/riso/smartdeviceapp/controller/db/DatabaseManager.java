
package jp.co.riso.smartdeviceapp.controller.db;

import jp.co.riso.android.util.AppUtils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseManager extends SQLiteOpenHelper {
    public static final String TAG = "DatabaseManager";

    private static final String DATABASE_SQL = "db/SmartDeviceAppDB.sql";
    public static final String DATABASE_NAME = "SmartDeviceAppDB.sqlite";
    
    private static final int DATABASE_VERSION = 1;
    private Context mContext;
    
    public DatabaseManager(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate - Begin");
        
        String sqlString = AppUtils.getFileContentsFromAssets(mContext, DATABASE_SQL);
        String[] separated = sqlString.split(";");
        
        for (int i = 0; i < separated.length; i++) {
            db.execSQL(separated[i]);
        }
        
        Log.d(TAG, "onCreate - End");
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade - Begin ("+oldVersion+"=>"+newVersion+")");
        // Should not happen for now
        Log.d(TAG, "onUpgrade - End");
    }
}
