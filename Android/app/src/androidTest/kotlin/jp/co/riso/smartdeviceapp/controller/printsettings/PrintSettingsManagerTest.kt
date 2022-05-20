package jp.co.riso.smartdeviceapp.controller.printsettings

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager
import jp.co.riso.smartdeviceapp.controller.db.KeyConstants
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import jp.co.riso.smartdeviceapp.model.Printer
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Test

class PrintSettingsManagerTest {

    private var _printSettingsMgr: PrintSettingsManager? = null
    private var _manager: DatabaseManager? = null
    private var _context: Context? = null
    private var _printerId = PrinterManager.EMPTY_ID
    private val _printerType = AppConstants.PRINTER_MODEL_IS
    private var _settingId = 1
    private val _intValue = 1

    @Before
    fun setUp() {
        val printerManager = PrinterManager.getInstance(SmartDeviceApp.appContext!!)
        val printersList = printerManager!!.savedPrintersList
        val printer: Printer
        if (printersList!!.isEmpty()) {
            printer = Printer("", IPV4_OFFLINE_PRINTER_ADDRESS)
            printerManager.savePrinterToDB(printer, false)
        } else {
            printer = printersList[0]!!
        }
        _context = SmartDeviceApp.appContext!!
        _manager = DatabaseManager(_context)
        _printerId = printer.id
        val c = _manager!!.query(
            KeyConstants.KEY_SQL_PRINTSETTING_TABLE,
            null,
            KeyConstants.KEY_SQL_PRINTER_ID + "=?",
            arrayOf(_printerId.toString()),
            null,
            null,
            null
        )
        if (c!!.moveToFirst()) {
            _settingId = DatabaseManager.getIntFromCursor(c, KeyConstants.KEY_SQL_PRINTSETTING_ID)
        }
        _printSettingsMgr = PrintSettingsManager.getInstance(_context!!)
        val db = _manager!!.writableDatabase
        val cv = ContentValues()
        cv.put(PRINTER_ID, _printerId)
        db.insertWithOnConflict(PRINTER_TABLE, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
        cv.put(PRINTSETTING_ID, _settingId)
        cv.put(PRINTSETTING_BOOKLET, _intValue)
        cv.put(PRINTSETTING_BOOKLET_FINISH, _intValue)
        cv.put(PRINTSETTING_BOOKLET_LAYOUT, _intValue)
        cv.put(PRINTSETTING_COLOR, _intValue)
        cv.put(PRINTSETTING_COPIES, _intValue)
        cv.put(PRINTSETTING_DUPLEX, _intValue)
        cv.put(PRINTSETTING_FINISHING_SIDE, _intValue)
        cv.put(PRINTSETTING_IMPOSITION, _intValue)
        cv.put(PRINTSETTING_IMPOSITION_ORDER, _intValue)
        cv.put(PRINTSETTING_INPUT_TRAY, _intValue)
        cv.put(PRINTSETTING_ORIENTATION, _intValue)
        cv.put(PRINTSETTING_OUTPUT_TRAY, _intValue)
        cv.put(PRINTSETTING_PAPER_SIZE, _intValue)
        cv.put(PRINTSETTING_PAPER_TRAY, _intValue)
        cv.put(PRINTSETTING_PUNCH, _intValue)
        cv.put(PRINTSETTING_SCALE_TO_FIT, _intValue)
        cv.put(PRINTSETTING_SORT, _intValue)
        cv.put(PRINTSETTING_STAPLE, _intValue)
        db.insertWithOnConflict(PRINTSETTING_TABLE, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
        cv.clear()
        cv.put(PRINTER_ID, _printerId)
        cv.put(PRINTSETTING_ID, _settingId)
        db.update(PRINTER_TABLE, cv, "prn_id=?", arrayOf(_printerId.toString()))
        db.close()
    }

    @After
    fun tearDown() {
        val db = _manager!!.writableDatabase
        db.delete(PRINTER_TABLE, null, null)
        db.delete(PRINTSETTING_TABLE, null, null)
        db.close()
    }

    // ================================================================================
    // Tests - precondition
    // ================================================================================
    @Test
    fun testPreConditions() {
        var db = _manager!!.readableDatabase
        var c = db.query(
            PRINTSETTING_TABLE,
            null,
            "prn_id=?",
            arrayOf(_printerId.toString()),
            null,
            null,
            null
        )
        TestCase.assertEquals(1, c.count)
        db = _manager!!.readableDatabase
        c = db.query(
            PRINTER_TABLE,
            null,
            "prn_id=?",
            arrayOf(_printerId.toString()),
            null,
            null,
            null
        )
        TestCase.assertEquals(1, c.count)
    }

    // ================================================================================
    // Tests - getInstance
    // ================================================================================
    @Test
    fun testGetInstance() {
        TestCase.assertEquals(_printSettingsMgr, PrintSettingsManager.getInstance(_context!!))
    }

    // ================================================================================
    // Tests - getPrintSetting
    // ================================================================================
    @Test
    fun testGetPrintSetting() {
        val settings = _printSettingsMgr!!.getPrintSetting(_printerId, _printerType)
        TestCase.assertNotNull(settings)
        val settingValues = settings.settingValues
        TestCase.assertNotNull(settingValues)
        TestCase.assertEquals(1, settingValues[KEY_COLOR] as Int)
        TestCase.assertEquals(1, settingValues[KEY_ORIENTATION] as Int)
        TestCase.assertEquals(1, settingValues[KEY_COPIES] as Int)
        TestCase.assertEquals(1, settingValues[KEY_DUPLEX] as Int)
        TestCase.assertEquals(1, settingValues[KEY_PAPER_SIZE] as Int)
        TestCase.assertEquals(1, settingValues[KEY_SCALE_TO_FIT] as Int)
        TestCase.assertEquals(1, settingValues[KEY_PAPER_TRAY] as Int)
        TestCase.assertEquals(1, settingValues[KEY_INPUT_TRAY] as Int)
        TestCase.assertEquals(1, settingValues[KEY_IMPOSITION] as Int)
        TestCase.assertEquals(1, settingValues[KEY_IMPOSITION_ORDER] as Int)
        TestCase.assertEquals(1, settingValues[KEY_SORT] as Int)
        TestCase.assertEquals(1, settingValues[KEY_BOOKLET] as Int)
        TestCase.assertEquals(1, settingValues[KEY_BOOKLET_FINISH] as Int)
        TestCase.assertEquals(1, settingValues[KEY_BOOKLET_LAYOUT] as Int)
        TestCase.assertEquals(1, settingValues[KEY_FINISHING_SIDE] as Int)
        TestCase.assertEquals(1, settingValues[KEY_STAPLE] as Int)
        TestCase.assertEquals(1, settingValues[KEY_PUNCH] as Int)
        TestCase.assertEquals(1, settingValues[KEY_OUTPUT_TRAY] as Int)
    }

    // ================================================================================
    // Tests - saveToDB
    // ================================================================================
    @Test
    fun testSaveToDB_DefaultValues() {
        val settings = PrintSettings()
        TestCase.assertNotNull(settings)
        val settingValues = settings.settingValues
        TestCase.assertNotNull(settingValues)
        var result = _printSettingsMgr!!.saveToDB(_printerId, settings)
        TestCase.assertTrue(result)
        val db = _manager!!.readableDatabase
        var c = db.query(
            PRINTSETTING_TABLE,
            null,
            "prn_id=?",
            arrayOf(_printerId.toString()),
            null,
            null,
            null
        )
        TestCase.assertEquals(1, c.count)
        c.moveToFirst()
        TestCase.assertEquals(_settingId, c.getInt(c.getColumnIndex(PRINTSETTING_ID)))
        TestCase.assertEquals(
            settingValues[KEY_COLOR] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_COLOR))
        )
        TestCase.assertEquals(
            settingValues[KEY_ORIENTATION] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_ORIENTATION))
        )
        TestCase.assertEquals(
            settingValues[KEY_COPIES] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_COPIES))
        )
        TestCase.assertEquals(
            settingValues[KEY_DUPLEX] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_DUPLEX))
        )
        TestCase.assertEquals(
            settingValues[KEY_PAPER_SIZE] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_PAPER_SIZE))
        )
        TestCase.assertEquals(
            settingValues[KEY_SCALE_TO_FIT] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_SCALE_TO_FIT))
        )
        TestCase.assertEquals(
            settingValues[KEY_PAPER_TRAY] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_PAPER_TRAY))
        )
        TestCase.assertEquals(
            settingValues[KEY_INPUT_TRAY] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_INPUT_TRAY))
        )
        TestCase.assertEquals(
            settingValues[KEY_IMPOSITION] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_IMPOSITION))
        )
        TestCase.assertEquals(
            settingValues[KEY_IMPOSITION_ORDER] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_IMPOSITION_ORDER))
        )
        TestCase.assertEquals(
            settingValues[KEY_SORT] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_SORT))
        )
        TestCase.assertEquals(
            settingValues[KEY_BOOKLET] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_BOOKLET))
        )
        TestCase.assertEquals(
            settingValues[KEY_BOOKLET_FINISH] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_BOOKLET_FINISH))
        )
        TestCase.assertEquals(
            settingValues[KEY_BOOKLET_LAYOUT] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_BOOKLET_LAYOUT))
        )
        TestCase.assertEquals(
            settingValues[KEY_FINISHING_SIDE] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_FINISHING_SIDE))
        )
        TestCase.assertEquals(
            settingValues[KEY_STAPLE] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_STAPLE))
        )
        TestCase.assertEquals(
            settingValues[KEY_PUNCH] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_PUNCH))
        )
        TestCase.assertEquals(
            settingValues[KEY_OUTPUT_TRAY] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_OUTPUT_TRAY))
        )
        c.close()
        c = db.query(
            PRINTER_TABLE,
            null,
            "prn_id=?",
            arrayOf(_printerId.toString()),
            null,
            null,
            null
        )
        TestCase.assertEquals(1, c.count)
        c.moveToFirst()
        TestCase.assertEquals(_settingId, c.getInt(c.getColumnIndex(PRINTSETTING_ID)))
        c.close()
        db.close()

        // Retry save
        result = _printSettingsMgr!!.saveToDB(_printerId, settings)
        TestCase.assertTrue(result)
    }

    @Test
    fun testSaveToDB_NullValues() {
        val settings = PrintSettings()
        TestCase.assertNotNull(settings)
        val settingValues = settings.settingValues
        TestCase.assertNotNull(settingValues)
        val result = _printSettingsMgr!!.saveToDB(999, null)
        TestCase.assertFalse(result)
        val db = _manager!!.readableDatabase
        val c = db.query(
            PRINTSETTING_TABLE,
            null,
            "prn_id=?",
            arrayOf(999.toString()),
            null,
            null,
            null
        )
        TestCase.assertEquals(0, c.count)
        c.close()
        db.close()
    }

    @Test
    fun testSaveToDB_InitialSave() {
        val settings = PrintSettings()
        TestCase.assertNotNull(settings)
        val settingValues = settings.settingValues
        TestCase.assertNotNull(settingValues)
        var db = _manager!!.writableDatabase
        db.delete(PRINTER_TABLE, null, null)
        val cv = ContentValues()
        cv.put(PRINTER_ID, _printerId)
        db.insertWithOnConflict(PRINTER_TABLE, null, cv, SQLiteDatabase.CONFLICT_REPLACE)
        db.close()
        val result = _printSettingsMgr!!.saveToDB(_printerId, settings)
        TestCase.assertTrue(result)
        db = _manager!!.readableDatabase
        var c = db.query(
            PRINTSETTING_TABLE,
            null,
            "prn_id=?",
            arrayOf(_printerId.toString()),
            null,
            null,
            null
        )
        TestCase.assertEquals(1, c.count)
        c.moveToFirst()
        TestCase.assertEquals(
            settingValues[KEY_COLOR] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_COLOR))
        )
        TestCase.assertEquals(
            settingValues[KEY_ORIENTATION] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_ORIENTATION))
        )
        TestCase.assertEquals(
            settingValues[KEY_COPIES] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_COPIES))
        )
        TestCase.assertEquals(
            settingValues[KEY_DUPLEX] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_DUPLEX))
        )
        TestCase.assertEquals(
            settingValues[KEY_PAPER_SIZE] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_PAPER_SIZE))
        )
        TestCase.assertEquals(
            settingValues[KEY_SCALE_TO_FIT] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_SCALE_TO_FIT))
        )
        TestCase.assertEquals(
            settingValues[KEY_PAPER_TRAY] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_PAPER_TRAY))
        )
        TestCase.assertEquals(
            settingValues[KEY_INPUT_TRAY] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_INPUT_TRAY))
        )
        TestCase.assertEquals(
            settingValues[KEY_IMPOSITION] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_IMPOSITION))
        )
        TestCase.assertEquals(
            settingValues[KEY_IMPOSITION_ORDER] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_IMPOSITION_ORDER))
        )
        TestCase.assertEquals(
            settingValues[KEY_SORT] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_SORT))
        )
        TestCase.assertEquals(
            settingValues[KEY_BOOKLET] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_BOOKLET))
        )
        TestCase.assertEquals(
            settingValues[KEY_BOOKLET_FINISH] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_BOOKLET_FINISH))
        )
        TestCase.assertEquals(
            settingValues[KEY_BOOKLET_LAYOUT] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_BOOKLET_LAYOUT))
        )
        TestCase.assertEquals(
            settingValues[KEY_FINISHING_SIDE] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_FINISHING_SIDE))
        )
        TestCase.assertEquals(
            settingValues[KEY_STAPLE] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_STAPLE))
        )
        TestCase.assertEquals(
            settingValues[KEY_PUNCH] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_PUNCH))
        )
        TestCase.assertEquals(
            settingValues[KEY_OUTPUT_TRAY] as Int,
            c.getInt(c.getColumnIndex(PRINTSETTING_OUTPUT_TRAY))
        )
        val newSettingId = c.getInt(c.getColumnIndex(PRINTSETTING_ID))
        c.close()
        c = db.query(
            PRINTER_TABLE,
            null,
            "prn_id=?",
            arrayOf(_printerId.toString()),
            null,
            null,
            null
        )
        TestCase.assertEquals(1, c.count)
        c.moveToFirst()
        TestCase.assertEquals(newSettingId, c.getInt(c.getColumnIndex(PRINTSETTING_ID)))
        c.close()
        db.close()
    }

    companion object {
        private const val IPV4_OFFLINE_PRINTER_ADDRESS = "192.168.0.206"
        private const val PRINTER_ID = "prn_id"
        private const val PRINTER_TABLE = "Printer"
        private const val PRINTSETTING_TABLE = "PrintSetting"
        private const val PRINTSETTING_ID = "pst_id"
        private const val PRINTSETTING_COLOR = "pst_color_mode"
        private const val PRINTSETTING_ORIENTATION = "pst_orientation"
        private const val PRINTSETTING_COPIES = "pst_copies"
        private const val PRINTSETTING_DUPLEX = "pst_duplex"
        private const val PRINTSETTING_PAPER_SIZE = "pst_paper_size"
        private const val PRINTSETTING_SCALE_TO_FIT = "pst_scale_to_fit"
        private const val PRINTSETTING_PAPER_TRAY = "pst_paper_type"
        private const val PRINTSETTING_INPUT_TRAY = "pst_input_tray"
        private const val PRINTSETTING_IMPOSITION = "pst_imposition"
        private const val PRINTSETTING_IMPOSITION_ORDER = "pst_imposition_order"
        private const val PRINTSETTING_SORT = "pst_sort"
        private const val PRINTSETTING_BOOKLET = "pst_booklet"
        private const val PRINTSETTING_BOOKLET_FINISH = "pst_booklet_finish"
        private const val PRINTSETTING_BOOKLET_LAYOUT = "pst_booklet_layout"
        private const val PRINTSETTING_FINISHING_SIDE = "pst_finishing_side"
        private const val PRINTSETTING_STAPLE = "pst_staple"
        private const val PRINTSETTING_PUNCH = "pst_punch"
        private const val PRINTSETTING_OUTPUT_TRAY = "pst_output_tray"
        private const val KEY_COLOR = "colorMode"
        private const val KEY_ORIENTATION = "orientation"
        private const val KEY_COPIES = "copies"
        private const val KEY_DUPLEX = "duplex"
        private const val KEY_PAPER_SIZE = "paperSize"
        private const val KEY_SCALE_TO_FIT = "scaleToFit"
        private const val KEY_PAPER_TRAY = "paperType"
        private const val KEY_INPUT_TRAY = "inputTray"
        private const val KEY_IMPOSITION = "imposition"
        private const val KEY_IMPOSITION_ORDER = "impositionOrder"
        private const val KEY_SORT = "sort"
        private const val KEY_BOOKLET = "booklet"
        private const val KEY_BOOKLET_FINISH = "bookletFinish"
        private const val KEY_BOOKLET_LAYOUT = "bookletLayout"
        private const val KEY_FINISHING_SIDE = "finishingSide"
        private const val KEY_STAPLE = "staple"
        private const val KEY_PUNCH = "punch"
        private const val KEY_OUTPUT_TRAY = "outputTray"
    }
}