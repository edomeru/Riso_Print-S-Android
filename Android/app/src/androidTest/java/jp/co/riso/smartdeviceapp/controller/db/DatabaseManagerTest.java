
package jp.co.riso.smartdeviceapp.controller.db;

import jp.co.riso.smartdeviceapp.model.PrintJob.JobResult;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

public class DatabaseManagerTest extends AndroidTestCase {
    private static final String KEY_SQL_PRINTER_ID = "prn_id";
    private static final String KEY_SQL_PRINTER_IP = "prn_ip_address";
    private static final String KEY_SQL_PRINTER_NAME = "prn_name";
    private static final String KEY_SQL_PRINTER_PORT = "prn_port_setting";
    private static final String KEY_SQL_PRINTER_LPR = "prn_enabled_lpr";
    private static final String KEY_SQL_PRINTER_RAW = "prn_enabled_raw";
    private static final String KEY_SQL_PRINTER_STAPLER = "prn_enabled_stapler";
    private static final String KEY_SQL_PRINTER_PUNCH3 = "prn_enabled_punch3";
    private static final String KEY_SQL_PRINTER_PUNCH4 = "prn_enabled_punch4";
    private static final String KEY_SQL_PRINTER_TRAYFACEDOWN = "prn_enabled_tray_facedown";
    private static final String KEY_SQL_PRINTER_TRAYTOP = "prn_enabled_tray_top";
    private static final String KEY_SQL_PRINTER_TRAYSTACK = "prn_enabled_tray_stack";
    private static final String KEY_SQL_PRINTER_TABLE = "Printer";
    private static final String KEY_SQL_DEFAULT_PRINTER_TABLE = "DefaultPrinter";
    private static final String KEY_SQL_PRINTJOB_TABLE = "PrintJob";
    private static final String KEY_SQL_PRINTJOB_NAME = "pjb_name";
    private static final String KEY_SQL_PRINTJOB_RESULT = "pjb_result";
    private static final String KEY_SQL_PRINTSETTING_TABLE = "PrintSetting";

    private DatabaseManager mDBManager = null;
    private String printerName = "Printer name1";
    private String printerName2 = "Printer name 2";
    private String printerIP = "192.168.1.1";
    private int printerId = 1;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Context context = new RenamingDelegatingContext(getContext(), "test_");
        mDBManager = new DatabaseManager(context);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testPreConditions() {
        assertNotNull(mDBManager);
        assertEquals(mDBManager.getDatabaseName(), "SmartDeviceAppDB.sqlite");
    }

    public void testCreate() {
        SQLiteDatabase db = SQLiteDatabase.create(null);
        mDBManager.onCreate(db);

        Cursor cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type = ?",
                new String[] {
                        "table"
                });
        assertEquals(cursor.getCount(), 6); // 4 tables + 2 default
        cursor.close();
        db.close();
    }

    public void testInsert() {
        SQLiteDatabase db = mDBManager.getWritableDatabase();
        int initialCount = 0;
        Cursor cursor = null;
        boolean result = false;

        cursor = db.query(KEY_SQL_PRINTER_TABLE, null, null, null, null, null, null);
        initialCount = cursor.getCount();

        ContentValues values = new ContentValues();
        values.put(KEY_SQL_PRINTER_NAME, "Printer name");

        result = mDBManager.insert(KEY_SQL_PRINTER_TABLE, null, values);
        assertTrue(result);

        db = mDBManager.getReadableDatabase();
        cursor = db.query(KEY_SQL_PRINTER_TABLE, null, null, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(initialCount + 1, cursor.getCount());
        cursor.close();
        db.close();
    }

    public void testInsert_Fail() {
        SQLiteDatabase db = mDBManager.getWritableDatabase();
        Cursor cursor = null;
        boolean result = false;

        db.delete(KEY_SQL_PRINTER_TABLE, null, null);
        db.close();

        ContentValues values = new ContentValues();
        values.put(KEY_SQL_PRINTER_ID, 1);
        values.put(KEY_SQL_PRINTJOB_NAME, "job name");
        values.put(KEY_SQL_PRINTJOB_RESULT, JobResult.SUCCESSFUL.ordinal());

        //will fail due to foreign key constraints
        result = mDBManager.insert(KEY_SQL_PRINTJOB_TABLE, null, values);
        assertFalse(result);

        db = mDBManager.getReadableDatabase();
        cursor = db.query(KEY_SQL_PRINTJOB_TABLE, null, null, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());
        cursor.close();
        db.close();
    }

    public void testInsertOrReplace() {
        SQLiteDatabase db = mDBManager.getWritableDatabase();
        Cursor cursor = null;

        long row = -1;

        db.delete(KEY_SQL_PRINTER_TABLE, null, null);
        db.close();

        ContentValues values = new ContentValues();
        values.put(KEY_SQL_PRINTER_ID, printerId);
        values.put(KEY_SQL_PRINTER_NAME, printerName);

        //will insert the row
        row = mDBManager.insertOrReplace(KEY_SQL_PRINTER_TABLE, null, values);
        assertTrue(row > -1);

        db = mDBManager.getReadableDatabase();
        cursor = db.query(KEY_SQL_PRINTER_TABLE, null, null, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        assertEquals(printerId, cursor.getInt(cursor.getColumnIndex(KEY_SQL_PRINTER_ID)));
        assertEquals(printerName, cursor.getString(cursor.getColumnIndex(KEY_SQL_PRINTER_NAME)));
        cursor.close();
        db.close();

        values.put(KEY_SQL_PRINTER_NAME, printerName2);

        //will replace the row
        row = mDBManager.insertOrReplace(KEY_SQL_PRINTER_TABLE, null, values);
        assertTrue(row > -1);

        db = mDBManager.getReadableDatabase();
        cursor = db.query(KEY_SQL_PRINTER_TABLE, null, null, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        assertEquals(printerId, cursor.getInt(cursor.getColumnIndex(KEY_SQL_PRINTER_ID)));
        assertEquals(printerName2, cursor.getString(cursor.getColumnIndex(KEY_SQL_PRINTER_NAME)));
        cursor.close();
        db.close();
    }

    public void testInsertOrReplace_Fail() {
        SQLiteDatabase db = mDBManager.getWritableDatabase();
        Cursor cursor = null;
        long row = -1;

        db.delete(KEY_SQL_PRINTER_TABLE, null, null);
        db.close();

        ContentValues values = new ContentValues();
        values.put(KEY_SQL_PRINTER_ID, 1);
        values.put(KEY_SQL_PRINTJOB_NAME, "job name");
        values.put(KEY_SQL_PRINTJOB_RESULT, JobResult.SUCCESSFUL.ordinal());

        //will fail due to foreign key constraints
        row = mDBManager.insertOrReplace(KEY_SQL_PRINTJOB_TABLE, null, values);
        assertEquals(-1, row);

        db = mDBManager.getReadableDatabase();
        cursor = db.query(KEY_SQL_PRINTJOB_TABLE, null, null, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());
        cursor.close();
        db.close();
    }


    public void testUpdate() {
        SQLiteDatabase db = mDBManager.getWritableDatabase();
        Cursor cursor = null;

        db.delete(KEY_SQL_PRINTER_TABLE, null, null);

        ContentValues values = new ContentValues();
        values.put(KEY_SQL_PRINTER_ID, printerId);
        values.put(KEY_SQL_PRINTER_NAME, printerName);

        long row = db.insert(KEY_SQL_PRINTER_TABLE, null, values);
        assertTrue(row > -1);

        db.close();

        values.put(KEY_SQL_PRINTER_NAME, printerName2);

        boolean result = mDBManager.update(KEY_SQL_PRINTER_TABLE, values,
                KeyConstants.KEY_SQL_PRINTER_ID + "=?",
                String.valueOf(printerId));
        assertTrue(result);

        db = mDBManager.getReadableDatabase();
        cursor = db.query(KEY_SQL_PRINTER_TABLE, null, null, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();
        assertEquals(printerId, cursor.getInt(cursor.getColumnIndex(KEY_SQL_PRINTER_ID)));
        assertEquals(printerName2, cursor.getString(cursor.getColumnIndex(KEY_SQL_PRINTER_NAME)));
        cursor.close();
        db.close();
    }

    public void testUpdate_Fail() {
        SQLiteDatabase db = mDBManager.getWritableDatabase();
        Cursor cursor = null;

        db.delete(KEY_SQL_PRINTER_TABLE, null, null);
        db.close();

        ContentValues values = new ContentValues();
        values.put(KEY_SQL_PRINTER_NAME, "new printer name");

        //will fail since not existing
        boolean result = mDBManager.update(KEY_SQL_PRINTER_TABLE, values,
                KeyConstants.KEY_SQL_PRINTER_ID + "=?", String.valueOf(printerId));
        assertFalse(result);

        db = mDBManager.getReadableDatabase();
        cursor = db.query(KEY_SQL_PRINTJOB_TABLE, null, null, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());
        cursor.close();
        db.close();
    }

    public void testQuery() {
        SQLiteDatabase db = mDBManager.getWritableDatabase();
        Cursor c1 = null;
        Cursor c2 = null;

        assertNotNull(db);
        try {
            c1 = db.query(KEY_SQL_PRINTER_TABLE, null, null, null, null, null, null);
            c2 = mDBManager
                    .query(KEY_SQL_PRINTER_TABLE, null, null, null, null, null, null);
            assertTrue(c1.getCount() == c2.getCount());

            c1 = db.query(KEY_SQL_PRINTJOB_TABLE, null, null, null, null, null, null);
            c2 = mDBManager.query(KEY_SQL_PRINTJOB_TABLE, null, null, null, null, null,
                    null);
            assertTrue(c1.getCount() == c2.getCount());
            c1 = db.query(KEY_SQL_PRINTSETTING_TABLE, null, null, null, null, null, null);
            c2 = mDBManager.query(KEY_SQL_PRINTSETTING_TABLE, null, null, null, null, null,
                    null);
            assertTrue(c1.getCount() == c2.getCount());

            c1 = db.query(KEY_SQL_DEFAULT_PRINTER_TABLE, null, null, null, null, null, null);
            c2 = mDBManager.query(KEY_SQL_DEFAULT_PRINTER_TABLE, null, null, null, null,
                    null, null);
            assertTrue(c1.getCount() == c2.getCount());

        } catch (SQLiteException e) {
            fail("table not exist!");
        }

        c1.close();
        c2.close();

        db.delete(KEY_SQL_PRINTER_TABLE, null, null);

        //query empty table
        c1 = mDBManager.query(KEY_SQL_PRINTER_TABLE, null, null, null, null,
                null, null);
        assertEquals(0, c1.getCount());
        c1.close();

        ContentValues values = new ContentValues();
        values.put(KEY_SQL_PRINTER_ID, printerId);
        values.put(KEY_SQL_PRINTER_NAME, printerName);
        values.put(KEY_SQL_PRINTER_IP, printerIP);

        //insert new value
        long row = db.insert(KEY_SQL_PRINTER_TABLE, null, values);
        assertTrue(row > -1);

        //query with selection
        c1 = mDBManager.query(KEY_SQL_PRINTER_TABLE, null, "prn_id=?", new String[] { String.valueOf(printerId) }, null,
                null, null);

        assertEquals(1, c1.getCount());

        c1.moveToFirst();
        assertEquals(printerId, c1.getInt(c1.getColumnIndex(KEY_SQL_PRINTER_ID)));
        assertEquals(printerName, c1.getString(c1.getColumnIndex(KEY_SQL_PRINTER_NAME)));
        assertEquals(printerIP, c1.getString(c1.getColumnIndex(KEY_SQL_PRINTER_IP)));

        //default values
        assertEquals(0, c1.getInt(c1.getColumnIndex(KEY_SQL_PRINTER_PORT)));
        assertEquals(1, c1.getInt(c1.getColumnIndex(KEY_SQL_PRINTER_LPR)));
        assertEquals(1, c1.getInt(c1.getColumnIndex(KEY_SQL_PRINTER_RAW)));
        assertEquals(1, c1.getInt(c1.getColumnIndex(KEY_SQL_PRINTER_STAPLER)));
        assertEquals(0, c1.getInt(c1.getColumnIndex(KEY_SQL_PRINTER_PUNCH3)));
        assertEquals(1, c1.getInt(c1.getColumnIndex(KEY_SQL_PRINTER_PUNCH4)));
        assertEquals(1, c1.getInt(c1.getColumnIndex(KEY_SQL_PRINTER_TRAYFACEDOWN)));
        assertEquals(1, c1.getInt(c1.getColumnIndex(KEY_SQL_PRINTER_TRAYSTACK)));
        assertEquals(1, c1.getInt(c1.getColumnIndex(KEY_SQL_PRINTER_TRAYTOP)));

        c1.close();

        db.close();
    }

    public void testDelete_All() {
        int initialCount = 0;
        Cursor cursor = null;
        boolean result = false;

        //initialize value
        ContentValues values = new ContentValues();
        values.put(KEY_SQL_PRINTER_NAME, printerName);

        result = mDBManager.insert(KEY_SQL_PRINTER_TABLE, null, values);
        assertTrue(result);

        SQLiteDatabase db = mDBManager.getWritableDatabase();
        cursor = db.query(KEY_SQL_PRINTER_TABLE, null, null, null, null, null, null);
        initialCount = cursor.getCount();
        assertTrue(initialCount > 0);

        cursor.close();
        db.close();

        result = mDBManager.delete(KEY_SQL_PRINTER_TABLE, null, null);
        assertTrue(result);

        db = mDBManager.getReadableDatabase();
        cursor = db.query(KEY_SQL_PRINTER_TABLE, null, null, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());

        cursor.close();
        db.close();
    }

    public void testDelete_WithSelection() {
        int initialCount = 0;
        Cursor cursor = null;
        boolean result = false;

        ContentValues values = new ContentValues();
        values.put(KEY_SQL_PRINTER_ID, 1000);
        values.put(KEY_SQL_PRINTER_NAME, printerName);

        //initialize data
        result = mDBManager.insert(KEY_SQL_PRINTER_TABLE, null, values);
        assertTrue(result);

        SQLiteDatabase db = mDBManager.getWritableDatabase();
        cursor = db.query(KEY_SQL_PRINTER_TABLE, null, "prn_id=?", new String[] { "1000" }, null, null, null);
        initialCount = cursor.getCount();
        assertEquals(1, initialCount);

        cursor.close();
        db.close();

        // delete data
        result = mDBManager.delete(KEY_SQL_PRINTER_TABLE, "prn_id=?", "1000");
        assertTrue(result);

        db = mDBManager.getReadableDatabase();
        cursor = db.query(KEY_SQL_PRINTER_TABLE, null, "prn_id=?", new String[] { "1000" }, null, null, null);
        assertNotNull(cursor);
        assertEquals(0, cursor.getCount());
        cursor.close();
        db.close();
    }

    public void testGetString() {
        Cursor cursor = null;

        //initialize data
        SQLiteDatabase db = mDBManager.getWritableDatabase();
        db.delete(KEY_SQL_PRINTER_TABLE, null, null);

        ContentValues values = new ContentValues();
        values.put(KEY_SQL_PRINTER_ID, printerId);
        values.put(KEY_SQL_PRINTER_NAME, printerName);

        long row = db.insert(KEY_SQL_PRINTER_TABLE, null, values);
        assertTrue(row > -1);

        cursor = db.query(KEY_SQL_PRINTER_TABLE, null, null, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();

        // test
        assertEquals(printerName, DatabaseManager.getStringFromCursor(cursor, KEY_SQL_PRINTER_NAME));

        cursor.close();
        db.close();
    }

    public void testGetInt() {
        Cursor cursor = null;
        //initialize data
        SQLiteDatabase db = mDBManager.getWritableDatabase();
        db.delete(KEY_SQL_PRINTER_TABLE, null, null);

        ContentValues values = new ContentValues();
        values.put(KEY_SQL_PRINTER_ID, printerId);
        values.put(KEY_SQL_PRINTER_NAME, printerName);

        long row = db.insert(KEY_SQL_PRINTER_TABLE, null, values);
        assertTrue(row > -1);

        cursor = db.query(KEY_SQL_PRINTER_TABLE, null, null, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();

        //test
        assertEquals(printerId, DatabaseManager.getIntFromCursor(cursor, KEY_SQL_PRINTER_ID));

        cursor.close();
        db.close();
    }

    public void testGetBoolean() {
        Cursor cursor = null;
        //initialize data
        SQLiteDatabase db = mDBManager.getWritableDatabase();
        db.delete(KEY_SQL_PRINTER_TABLE, null, null);

        ContentValues values = new ContentValues();
        values.put(KEY_SQL_PRINTER_ID, printerId);
        values.put(KEY_SQL_PRINTER_NAME, printerName);
        values.put(KEY_SQL_PRINTER_RAW, 0);

        long row = db.insert(KEY_SQL_PRINTER_TABLE, null, values);
        assertTrue(row > -1);

        cursor = db.query(KEY_SQL_PRINTER_TABLE, null, null, null, null, null, null);
        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());
        cursor.moveToFirst();

        //test (default true)
        assertEquals(true, DatabaseManager.getBooleanFromCursor(cursor, KEY_SQL_PRINTER_LPR));
        assertEquals(false, DatabaseManager.getBooleanFromCursor(cursor, KEY_SQL_PRINTER_RAW));

        cursor.close();
        db.close();
    }

}
