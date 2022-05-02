package jp.co.riso.smartdeviceapp.controller.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.test.AndroidTestCase
import android.test.RenamingDelegatingContext
import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager.Companion.getBooleanFromCursor
import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager.Companion.getIntFromCursor
import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager.Companion.getStringFromCursor
import jp.co.riso.smartdeviceapp.model.PrintJob.JobResult
import junit.framework.TestCase

class DatabaseManagerTest : AndroidTestCase() {
    private var mDBManager: DatabaseManager? = null
    private val printerName = "Printer name1"
    private val printerName2 = "Printer name 2"
    private val printerIP = "192.168.1.1"
    private val printerId = 1
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        val context: Context = RenamingDelegatingContext(context, "test_")
        mDBManager = DatabaseManager(context)
    }

    @Throws(Exception::class)
    override fun tearDown() {
        super.tearDown()
    }

    fun testPreConditions() {
        TestCase.assertNotNull(mDBManager)
        TestCase.assertEquals(mDBManager!!.databaseName, "SmartDeviceAppDB.sqlite")
    }

    fun testCreate() {
        val db = SQLiteDatabase.create(null)
        mDBManager!!.onCreate(db)
        val cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type = ?", arrayOf(
                "table"
            )
        )
        TestCase.assertEquals(cursor.count, 6) // 4 tables + 2 default
        cursor.close()
        db.close()
    }

    fun testInsert() {
        var db = mDBManager!!.writableDatabase
        val initialCount: Int
        var cursor: Cursor
        val result: Boolean
        cursor = db.query(KEY_SQL_PRINTER_TABLE, null, null, null, null, null, null)
        initialCount = cursor.count
        val values = ContentValues()
        values.put(KEY_SQL_PRINTER_NAME, "Printer name")
        result = mDBManager!!.insert(KEY_SQL_PRINTER_TABLE, null, values)
        TestCase.assertTrue(result)
        db = mDBManager!!.readableDatabase
        cursor = db.query(KEY_SQL_PRINTER_TABLE, null, null, null, null, null, null)
        TestCase.assertNotNull(cursor)
        TestCase.assertEquals(initialCount + 1, cursor.count)
        cursor.close()
        db.close()
    }

    fun testInsert_Fail() {
        var db = mDBManager!!.writableDatabase
        val cursor: Cursor
        val result: Boolean
        db.delete(KEY_SQL_PRINTER_TABLE, null, null)
        db.close()
        val values = ContentValues()
        values.put(KEY_SQL_PRINTER_ID, 1)
        values.put(KEY_SQL_PRINTJOB_NAME, "job name")
        values.put(KEY_SQL_PRINTJOB_RESULT, JobResult.SUCCESSFUL.ordinal)

        //will fail due to foreign key constraints
        result = mDBManager!!.insert(KEY_SQL_PRINTJOB_TABLE, null, values)
        TestCase.assertFalse(result)
        db = mDBManager!!.readableDatabase
        cursor = db.query(KEY_SQL_PRINTJOB_TABLE, null, null, null, null, null, null)
        TestCase.assertNotNull(cursor)
        TestCase.assertEquals(0, cursor.count)
        cursor.close()
        db.close()
    }

    fun testInsertOrReplace() {
        var db = mDBManager!!.writableDatabase
        var cursor: Cursor
        var row: Long
        db.delete(KEY_SQL_PRINTER_TABLE, null, null)
        db.close()
        val values = ContentValues()
        values.put(KEY_SQL_PRINTER_ID, printerId)
        values.put(KEY_SQL_PRINTER_NAME, printerName)

        //will insert the row
        row = mDBManager!!.insertOrReplace(KEY_SQL_PRINTER_TABLE, null, values)
        TestCase.assertTrue(row > -1)
        db = mDBManager!!.readableDatabase
        cursor = db.query(KEY_SQL_PRINTER_TABLE, null, null, null, null, null, null)
        TestCase.assertNotNull(cursor)
        TestCase.assertEquals(1, cursor.count)
        cursor.moveToFirst()
        TestCase.assertEquals(printerId, cursor.getInt(cursor.getColumnIndex(KEY_SQL_PRINTER_ID)))
        TestCase.assertEquals(
            printerName, cursor.getString(
                cursor.getColumnIndex(
                    KEY_SQL_PRINTER_NAME
                )
            )
        )
        cursor.close()
        db.close()
        values.put(KEY_SQL_PRINTER_NAME, printerName2)

        //will replace the row
        row = mDBManager!!.insertOrReplace(KEY_SQL_PRINTER_TABLE, null, values)
        TestCase.assertTrue(row > -1)
        db = mDBManager!!.readableDatabase
        cursor = db.query(KEY_SQL_PRINTER_TABLE, null, null, null, null, null, null)
        TestCase.assertNotNull(cursor)
        TestCase.assertEquals(1, cursor.count)
        cursor.moveToFirst()
        TestCase.assertEquals(printerId, cursor.getInt(cursor.getColumnIndex(KEY_SQL_PRINTER_ID)))
        TestCase.assertEquals(
            printerName2, cursor.getString(
                cursor.getColumnIndex(
                    KEY_SQL_PRINTER_NAME
                )
            )
        )
        cursor.close()
        db.close()
    }

    fun testInsertOrReplace_Fail() {
        var db = mDBManager!!.writableDatabase
        val cursor: Cursor
        val row: Long
        db.delete(KEY_SQL_PRINTER_TABLE, null, null)
        db.close()
        val values = ContentValues()
        values.put(KEY_SQL_PRINTER_ID, 1)
        values.put(KEY_SQL_PRINTJOB_NAME, "job name")
        values.put(KEY_SQL_PRINTJOB_RESULT, JobResult.SUCCESSFUL.ordinal)

        //will fail due to foreign key constraints
        row = mDBManager!!.insertOrReplace(KEY_SQL_PRINTJOB_TABLE, null, values)
        TestCase.assertEquals(-1, row)
        db = mDBManager!!.readableDatabase
        cursor = db.query(KEY_SQL_PRINTJOB_TABLE, null, null, null, null, null, null)
        TestCase.assertNotNull(cursor)
        TestCase.assertEquals(0, cursor.count)
        cursor.close()
        db.close()
    }

    fun testUpdate() {
        var db = mDBManager!!.writableDatabase
        val cursor: Cursor
        db.delete(KEY_SQL_PRINTER_TABLE, null, null)
        val values = ContentValues()
        values.put(KEY_SQL_PRINTER_ID, printerId)
        values.put(KEY_SQL_PRINTER_NAME, printerName)
        val row = db.insert(KEY_SQL_PRINTER_TABLE, null, values)
        TestCase.assertTrue(row > -1)
        db.close()
        values.put(KEY_SQL_PRINTER_NAME, printerName2)
        val result = mDBManager!!.update(
            KEY_SQL_PRINTER_TABLE, values,
            KeyConstants.KEY_SQL_PRINTER_ID + "=?", printerId.toString()
        )
        TestCase.assertTrue(result)
        db = mDBManager!!.readableDatabase
        cursor = db.query(KEY_SQL_PRINTER_TABLE, null, null, null, null, null, null)
        TestCase.assertNotNull(cursor)
        TestCase.assertEquals(1, cursor.count)
        cursor.moveToFirst()
        TestCase.assertEquals(printerId, cursor.getInt(cursor.getColumnIndex(KEY_SQL_PRINTER_ID)))
        TestCase.assertEquals(
            printerName2, cursor.getString(
                cursor.getColumnIndex(
                    KEY_SQL_PRINTER_NAME
                )
            )
        )
        cursor.close()
        db.close()
    }

    fun testUpdate_Fail() {
        var db = mDBManager!!.writableDatabase
        val cursor: Cursor
        db.delete(KEY_SQL_PRINTER_TABLE, null, null)
        db.close()
        val values = ContentValues()
        values.put(KEY_SQL_PRINTER_NAME, "new printer name")

        //will fail since not existing
        val result = mDBManager!!.update(
            KEY_SQL_PRINTER_TABLE, values,
            KeyConstants.KEY_SQL_PRINTER_ID + "=?", printerId.toString()
        )
        TestCase.assertFalse(result)
        db = mDBManager!!.readableDatabase
        cursor = db.query(KEY_SQL_PRINTJOB_TABLE, null, null, null, null, null, null)
        TestCase.assertNotNull(cursor)
        TestCase.assertEquals(0, cursor.count)
        cursor.close()
        db.close()
    }

    fun testQuery() {
        val db = mDBManager!!.writableDatabase
        var c1: Cursor? = null
        var c2: Cursor? = null
        TestCase.assertNotNull(db)
        try {
            c1 = db.query(KEY_SQL_PRINTER_TABLE, null, null, null, null, null, null)
            c2 = mDBManager!!
                .query(KEY_SQL_PRINTER_TABLE, null, null, null, null, null, null)
            TestCase.assertTrue(c1.count == c2!!.count)
            c1 = db.query(KEY_SQL_PRINTJOB_TABLE, null, null, null, null, null, null)
            c2 = mDBManager!!.query(
                KEY_SQL_PRINTJOB_TABLE, null, null, null, null, null,
                null
            )
            TestCase.assertTrue(c1.count == c2!!.count)
            c1 = db.query(KEY_SQL_PRINTSETTING_TABLE, null, null, null, null, null, null)
            c2 = mDBManager!!.query(
                KEY_SQL_PRINTSETTING_TABLE, null, null, null, null, null,
                null
            )
            TestCase.assertTrue(c1.count == c2!!.count)
            c1 = db.query(KEY_SQL_DEFAULT_PRINTER_TABLE, null, null, null, null, null, null)
            c2 = mDBManager!!.query(
                KEY_SQL_DEFAULT_PRINTER_TABLE, null, null, null, null,
                null, null
            )
            TestCase.assertTrue(c1.count == c2!!.count)
        } catch (e: SQLiteException) {
            TestCase.fail("table not exist!")
        }
        c1!!.close()
        c2!!.close()
        db.delete(KEY_SQL_PRINTER_TABLE, null, null)

        //query empty table
        c1 = mDBManager!!.query(
            KEY_SQL_PRINTER_TABLE, null, null, null, null,
            null, null
        )
        TestCase.assertEquals(0, c1!!.count)
        c1.close()
        val values = ContentValues()
        values.put(KEY_SQL_PRINTER_ID, printerId)
        values.put(KEY_SQL_PRINTER_NAME, printerName)
        values.put(KEY_SQL_PRINTER_IP, printerIP)

        //insert new value
        val row = db.insert(KEY_SQL_PRINTER_TABLE, null, values)
        TestCase.assertTrue(row > -1)

        //query with selection
        c1 = mDBManager!!.query(
            KEY_SQL_PRINTER_TABLE, null, "prn_id=?", arrayOf(printerId.toString()), null,
            null, null
        )
        TestCase.assertEquals(1, c1!!.count)
        c1.moveToFirst()
        TestCase.assertEquals(printerId, c1.getInt(c1.getColumnIndex(KEY_SQL_PRINTER_ID)))
        TestCase.assertEquals(printerName, c1.getString(c1.getColumnIndex(KEY_SQL_PRINTER_NAME)))
        TestCase.assertEquals(printerIP, c1.getString(c1.getColumnIndex(KEY_SQL_PRINTER_IP)))

        //default values
        TestCase.assertEquals(0, c1.getInt(c1.getColumnIndex(KEY_SQL_PRINTER_PORT)))
        TestCase.assertEquals(1, c1.getInt(c1.getColumnIndex(KEY_SQL_PRINTER_LPR)))
        TestCase.assertEquals(1, c1.getInt(c1.getColumnIndex(KEY_SQL_PRINTER_RAW)))
        TestCase.assertEquals(1, c1.getInt(c1.getColumnIndex(KEY_SQL_PRINTER_STAPLER)))
        TestCase.assertEquals(0, c1.getInt(c1.getColumnIndex(KEY_SQL_PRINTER_PUNCH3)))
        TestCase.assertEquals(1, c1.getInt(c1.getColumnIndex(KEY_SQL_PRINTER_PUNCH4)))
        TestCase.assertEquals(1, c1.getInt(c1.getColumnIndex(KEY_SQL_PRINTER_TRAYFACEDOWN)))
        TestCase.assertEquals(1, c1.getInt(c1.getColumnIndex(KEY_SQL_PRINTER_TRAYSTACK)))
        TestCase.assertEquals(1, c1.getInt(c1.getColumnIndex(KEY_SQL_PRINTER_TRAYTOP)))
        c1.close()
        db.close()
    }

    fun testDelete_All() {
        val initialCount: Int
        var cursor: Cursor
        var result: Boolean

        //initialize value
        val values = ContentValues()
        values.put(KEY_SQL_PRINTER_NAME, printerName)
        result = mDBManager!!.insert(KEY_SQL_PRINTER_TABLE, null, values)
        TestCase.assertTrue(result)
        var db = mDBManager!!.writableDatabase
        cursor = db.query(KEY_SQL_PRINTER_TABLE, null, null, null, null, null, null)
        initialCount = cursor.count
        TestCase.assertTrue(initialCount > 0)
        cursor.close()
        db.close()
        result = mDBManager!!.delete(KEY_SQL_PRINTER_TABLE, null, null)
        TestCase.assertTrue(result)
        db = mDBManager!!.readableDatabase
        cursor = db.query(KEY_SQL_PRINTER_TABLE, null, null, null, null, null, null)
        TestCase.assertNotNull(cursor)
        TestCase.assertEquals(0, cursor.count)
        cursor.close()
        db.close()
    }

    fun testDelete_WithSelection() {
        val initialCount: Int
        var cursor: Cursor
        var result: Boolean
        val values = ContentValues()
        values.put(KEY_SQL_PRINTER_ID, 1000)
        values.put(KEY_SQL_PRINTER_NAME, printerName)

        //initialize data
        result = mDBManager!!.insert(KEY_SQL_PRINTER_TABLE, null, values)
        TestCase.assertTrue(result)
        var db = mDBManager!!.writableDatabase
        cursor =
            db.query(KEY_SQL_PRINTER_TABLE, null, "prn_id=?", arrayOf("1000"), null, null, null)
        initialCount = cursor.count
        TestCase.assertEquals(1, initialCount)
        cursor.close()
        db.close()

        // delete data
        result = mDBManager!!.delete(KEY_SQL_PRINTER_TABLE, "prn_id=?", "1000")
        TestCase.assertTrue(result)
        db = mDBManager!!.readableDatabase
        cursor =
            db.query(KEY_SQL_PRINTER_TABLE, null, "prn_id=?", arrayOf("1000"), null, null, null)
        TestCase.assertNotNull(cursor)
        TestCase.assertEquals(0, cursor.count)
        cursor.close()
        db.close()
    }

    fun testGetString() {
        val cursor: Cursor

        //initialize data
        val db = mDBManager!!.writableDatabase
        db.delete(KEY_SQL_PRINTER_TABLE, null, null)
        val values = ContentValues()
        values.put(KEY_SQL_PRINTER_ID, printerId)
        values.put(KEY_SQL_PRINTER_NAME, printerName)
        val row = db.insert(KEY_SQL_PRINTER_TABLE, null, values)
        TestCase.assertTrue(row > -1)
        cursor = db.query(KEY_SQL_PRINTER_TABLE, null, null, null, null, null, null)
        TestCase.assertNotNull(cursor)
        TestCase.assertEquals(1, cursor.count)
        cursor.moveToFirst()

        // test
        TestCase.assertEquals(printerName, getStringFromCursor(cursor, KEY_SQL_PRINTER_NAME))
        TestCase.assertEquals("", getStringFromCursor(cursor, KEY_SQL_INVALID))
        cursor.close()
        db.close()
    }

    fun testGetInt() {
        val cursor: Cursor
        //initialize data
        val db = mDBManager!!.writableDatabase
        db.delete(KEY_SQL_PRINTER_TABLE, null, null)
        val values = ContentValues()
        values.put(KEY_SQL_PRINTER_ID, printerId)
        values.put(KEY_SQL_PRINTER_NAME, printerName)
        val row = db.insert(KEY_SQL_PRINTER_TABLE, null, values)
        TestCase.assertTrue(row > -1)
        cursor = db.query(KEY_SQL_PRINTER_TABLE, null, null, null, null, null, null)
        TestCase.assertNotNull(cursor)
        TestCase.assertEquals(1, cursor.count)
        cursor.moveToFirst()

        //test
        TestCase.assertEquals(printerId, getIntFromCursor(cursor, KEY_SQL_PRINTER_ID))
        TestCase.assertEquals(0, getIntFromCursor(cursor, KEY_SQL_INVALID))
        cursor.close()
        db.close()
    }

    fun testGetBoolean() {
        val cursor: Cursor
        //initialize data
        val db = mDBManager!!.writableDatabase
        db.delete(KEY_SQL_PRINTER_TABLE, null, null)
        val values = ContentValues()
        values.put(KEY_SQL_PRINTER_ID, printerId)
        values.put(KEY_SQL_PRINTER_NAME, printerName)
        values.put(KEY_SQL_PRINTER_RAW, 0)
        val row = db.insert(KEY_SQL_PRINTER_TABLE, null, values)
        TestCase.assertTrue(row > -1)
        cursor = db.query(KEY_SQL_PRINTER_TABLE, null, null, null, null, null, null)
        TestCase.assertNotNull(cursor)
        TestCase.assertEquals(1, cursor.count)
        cursor.moveToFirst()

        //test (default true)
        TestCase.assertEquals(true, getBooleanFromCursor(cursor, KEY_SQL_PRINTER_LPR))
        TestCase.assertEquals(false, getBooleanFromCursor(cursor, KEY_SQL_PRINTER_RAW))
        TestCase.assertEquals(false, getBooleanFromCursor(cursor, KEY_SQL_INVALID))
        cursor.close()
        db.close()
    }

    companion object {
        private const val KEY_SQL_INVALID = "invalid_key"
        private const val KEY_SQL_PRINTER_ID = "prn_id"
        private const val KEY_SQL_PRINTER_IP = "prn_ip_address"
        private const val KEY_SQL_PRINTER_NAME = "prn_name"
        private const val KEY_SQL_PRINTER_PORT = "prn_port_setting"
        private const val KEY_SQL_PRINTER_LPR = "prn_enabled_lpr"
        private const val KEY_SQL_PRINTER_RAW = "prn_enabled_raw"
        private const val KEY_SQL_PRINTER_STAPLER = "prn_enabled_stapler"
        private const val KEY_SQL_PRINTER_PUNCH3 = "prn_enabled_punch3"
        private const val KEY_SQL_PRINTER_PUNCH4 = "prn_enabled_punch4"
        private const val KEY_SQL_PRINTER_TRAYFACEDOWN = "prn_enabled_tray_facedown"
        private const val KEY_SQL_PRINTER_TRAYTOP = "prn_enabled_tray_top"
        private const val KEY_SQL_PRINTER_TRAYSTACK = "prn_enabled_tray_stack"
        private const val KEY_SQL_PRINTER_TABLE = "Printer"
        private const val KEY_SQL_DEFAULT_PRINTER_TABLE = "DefaultPrinter"
        private const val KEY_SQL_PRINTJOB_TABLE = "PrintJob"
        private const val KEY_SQL_PRINTJOB_NAME = "pjb_name"
        private const val KEY_SQL_PRINTJOB_RESULT = "pjb_result"
        private const val KEY_SQL_PRINTSETTING_TABLE = "PrintSetting"
    }
}