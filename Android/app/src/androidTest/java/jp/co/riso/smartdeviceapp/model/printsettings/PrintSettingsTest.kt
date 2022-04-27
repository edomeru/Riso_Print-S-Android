package jp.co.riso.smartdeviceapp.model.printsettings

import android.content.ContentValues
import androidx.preference.PreferenceManager
import androidx.test.platform.app.InstrumentationRegistry
import jp.co.riso.smartdeviceapp.AppConstants
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

class PrintSettingsTest {

    private var _printSettings: PrintSettings? = null

    @Before
    fun setUp() {
        _printSettings = PrintSettings() //default values
    }

    @After
    fun tearDown() {
        _printSettings = null
    }

    @Test
    fun testPreConditions() {
        TestCase.assertNotNull(_printSettings)
    }

    @Test
    fun testConstructor() {
        val settings = PrintSettings()
        TestCase.assertNotNull(settings)
        val settingValues = settings.settingValues
        TestCase.assertNotNull(settingValues)
        TestCase.assertEquals(DEFAULT_KEY_COLOR, settingValues[KEY_COLOR] as Int)
        TestCase.assertEquals(DEFAULT_KEY_ORIENTATION, settingValues[KEY_ORIENTATION] as Int)
        TestCase.assertEquals(DEFAULT_KEY_COPIES, settingValues[KEY_COPIES] as Int)
        TestCase.assertEquals(DEFAULT_KEY_DUPLEX, settingValues[KEY_DUPLEX] as Int)
        TestCase.assertEquals(DEFAULT_KEY_PAPER_SIZE, settingValues[KEY_PAPER_SIZE] as Int)
        TestCase.assertEquals(DEFAULT_KEY_SCALE_TO_FIT, settingValues[KEY_SCALE_TO_FIT] as Int)
        TestCase.assertEquals(DEFAULT_KEY_PAPER_TRAY, settingValues[KEY_PAPER_TRAY] as Int)
        TestCase.assertEquals(DEFAULT_KEY_INPUT_TRAY, settingValues[KEY_INPUT_TRAY] as Int)
        TestCase.assertEquals(DEFAULT_KEY_IMPOSITION, settingValues[KEY_IMPOSITION] as Int)
        TestCase.assertEquals(
            DEFAULT_KEY_IMPOSITION_ORDER,
            settingValues[KEY_IMPOSITION_ORDER] as Int
        )
        TestCase.assertEquals(DEFAULT_KEY_SORT, settingValues[KEY_SORT] as Int)
        TestCase.assertEquals(DEFAULT_KEY_BOOKLET, settingValues[KEY_BOOKLET] as Int)
        TestCase.assertEquals(DEFAULT_KEY_BOOKLET_FINISH, settingValues[KEY_BOOKLET_FINISH] as Int)
        TestCase.assertEquals(DEFAULT_KEY_BOOKLET_LAYOUT, settingValues[KEY_BOOKLET_LAYOUT] as Int)
        TestCase.assertEquals(DEFAULT_KEY_FINISHING_SIDE, settingValues[KEY_FINISHING_SIDE] as Int)
        TestCase.assertEquals(DEFAULT_KEY_STAPLE, settingValues[KEY_STAPLE] as Int)
        TestCase.assertEquals(DEFAULT_KEY_PUNCH, settingValues[KEY_PUNCH] as Int)
        TestCase.assertEquals(DEFAULT_KEY_OUTPUT_TRAY, settingValues[KEY_OUTPUT_TRAY] as Int)
    }

    @Test
    fun testConstructor_PrintSettings() {
        _printSettings!!.setValue(KEY_COLOR, 2)
        _printSettings!!.setValue(KEY_COPIES, 10)
        _printSettings!!.setValue(KEY_SCALE_TO_FIT, 0)
        val settings = PrintSettings(_printSettings)
        TestCase.assertNotNull(settings)
        val settingValues = settings.settingValues
        TestCase.assertNotNull(settingValues)
        TestCase.assertEquals(2, settingValues[KEY_COLOR] as Int) // from 0 to 2
        TestCase.assertEquals(DEFAULT_KEY_ORIENTATION, settingValues[KEY_ORIENTATION] as Int)
        TestCase.assertEquals(10, settingValues[KEY_COPIES] as Int) // from 1 to 10
        TestCase.assertEquals(DEFAULT_KEY_DUPLEX, settingValues[KEY_DUPLEX] as Int)
        TestCase.assertEquals(DEFAULT_KEY_PAPER_SIZE, settingValues[KEY_PAPER_SIZE] as Int)
        TestCase.assertEquals(0, settingValues[KEY_SCALE_TO_FIT] as Int) // from 1 to 0
        TestCase.assertEquals(DEFAULT_KEY_PAPER_TRAY, settingValues[KEY_PAPER_TRAY] as Int)
        TestCase.assertEquals(DEFAULT_KEY_INPUT_TRAY, settingValues[KEY_INPUT_TRAY] as Int)
        TestCase.assertEquals(DEFAULT_KEY_IMPOSITION, settingValues[KEY_IMPOSITION] as Int)
        TestCase.assertEquals(
            DEFAULT_KEY_IMPOSITION_ORDER,
            settingValues[KEY_IMPOSITION_ORDER] as Int
        )
        TestCase.assertEquals(DEFAULT_KEY_SORT, settingValues[KEY_SORT] as Int)
        TestCase.assertEquals(DEFAULT_KEY_BOOKLET, settingValues[KEY_BOOKLET] as Int)
        TestCase.assertEquals(DEFAULT_KEY_BOOKLET_FINISH, settingValues[KEY_BOOKLET_FINISH] as Int)
        TestCase.assertEquals(DEFAULT_KEY_BOOKLET_LAYOUT, settingValues[KEY_BOOKLET_LAYOUT] as Int)
        TestCase.assertEquals(DEFAULT_KEY_FINISHING_SIDE, settingValues[KEY_FINISHING_SIDE] as Int)
        TestCase.assertEquals(DEFAULT_KEY_STAPLE, settingValues[KEY_STAPLE] as Int)
        TestCase.assertEquals(DEFAULT_KEY_PUNCH, settingValues[KEY_PUNCH] as Int)
        TestCase.assertEquals(DEFAULT_KEY_OUTPUT_TRAY, settingValues[KEY_OUTPUT_TRAY] as Int)
    }

    // must have default values
    @Test
    fun testConstructor_PrinterIdInvalid() {
        val settings = PrintSettings(PrinterManager.EMPTY_ID, AppConstants.PRINTER_MODEL_IS)
        TestCase.assertNotNull(settings)
        val settingValues = settings.settingValues
        TestCase.assertNotNull(settingValues)
        TestCase.assertEquals(DEFAULT_KEY_COLOR, settingValues[KEY_COLOR] as Int)
        TestCase.assertEquals(DEFAULT_KEY_ORIENTATION, settingValues[KEY_ORIENTATION] as Int)
        TestCase.assertEquals(DEFAULT_KEY_COPIES, settingValues[KEY_COPIES] as Int)
        TestCase.assertEquals(DEFAULT_KEY_DUPLEX, settingValues[KEY_DUPLEX] as Int)
        TestCase.assertEquals(DEFAULT_KEY_PAPER_SIZE, settingValues[KEY_PAPER_SIZE] as Int)
        TestCase.assertEquals(DEFAULT_KEY_SCALE_TO_FIT, settingValues[KEY_SCALE_TO_FIT] as Int)
        TestCase.assertEquals(DEFAULT_KEY_PAPER_TRAY, settingValues[KEY_PAPER_TRAY] as Int)
        TestCase.assertEquals(DEFAULT_KEY_INPUT_TRAY, settingValues[KEY_INPUT_TRAY] as Int)
        TestCase.assertEquals(DEFAULT_KEY_IMPOSITION, settingValues[KEY_IMPOSITION] as Int)
        TestCase.assertEquals(
            DEFAULT_KEY_IMPOSITION_ORDER,
            settingValues[KEY_IMPOSITION_ORDER] as Int
        )
        TestCase.assertEquals(DEFAULT_KEY_SORT, settingValues[KEY_SORT] as Int)
        TestCase.assertEquals(DEFAULT_KEY_BOOKLET, settingValues[KEY_BOOKLET] as Int)
        TestCase.assertEquals(DEFAULT_KEY_BOOKLET_FINISH, settingValues[KEY_BOOKLET_FINISH] as Int)
        TestCase.assertEquals(DEFAULT_KEY_BOOKLET_LAYOUT, settingValues[KEY_BOOKLET_LAYOUT] as Int)
        TestCase.assertEquals(DEFAULT_KEY_FINISHING_SIDE, settingValues[KEY_FINISHING_SIDE] as Int)
        TestCase.assertEquals(DEFAULT_KEY_STAPLE, settingValues[KEY_STAPLE] as Int)
        TestCase.assertEquals(DEFAULT_KEY_PUNCH, settingValues[KEY_PUNCH] as Int)
        TestCase.assertEquals(DEFAULT_KEY_OUTPUT_TRAY, settingValues[KEY_OUTPUT_TRAY] as Int)
    }

    // must have default values
    @Test
    fun testConstructor_PrinterIdValid() {
        val printerId: Int
        val mManager = DatabaseManager(SmartDeviceApp.getAppContext())
        val db = mManager.writableDatabase
        val c = db.query(PRINTSETTING_TABLE, null, null, null, null, null, null)
        if (c.count > 0) {
            // if from database, values must be from database
            c.moveToFirst()
            printerId = c.getInt(c.getColumnIndex(PRINTER_ID))
            val settings = PrintSettings(printerId, AppConstants.PRINTER_MODEL_IS)
            TestCase.assertNotNull(settings)
            val settingValues = settings.settingValues
            TestCase.assertNotNull(settingValues)
            TestCase.assertEquals(
                c.getInt(c.getColumnIndex(PRINTSETTING_COLOR)),
                settingValues[KEY_COLOR] as Int
            )
            TestCase.assertEquals(
                c.getInt(c.getColumnIndex(PRINTSETTING_ORIENTATION)),
                settingValues[KEY_ORIENTATION] as Int
            )
            TestCase.assertEquals(
                c.getInt(c.getColumnIndex(PRINTSETTING_COPIES)),
                settingValues[KEY_COPIES] as Int
            )
            TestCase.assertEquals(
                c.getInt(c.getColumnIndex(PRINTSETTING_DUPLEX)),
                settingValues[KEY_DUPLEX] as Int
            )
            TestCase.assertEquals(
                c.getInt(c.getColumnIndex(PRINTSETTING_PAPER_SIZE)),
                settingValues[KEY_PAPER_SIZE] as Int
            )
            TestCase.assertEquals(
                c.getInt(c.getColumnIndex(PRINTSETTING_SCALE_TO_FIT)),
                settingValues[KEY_SCALE_TO_FIT] as Int
            )
            TestCase.assertEquals(
                c.getInt(c.getColumnIndex(PRINTSETTING_PAPER_TRAY)),
                settingValues[KEY_PAPER_TRAY] as Int
            )
            TestCase.assertEquals(
                c.getInt(c.getColumnIndex(PRINTSETTING_INPUT_TRAY)),
                settingValues[KEY_INPUT_TRAY] as Int
            )
            TestCase.assertEquals(
                c.getInt(c.getColumnIndex(PRINTSETTING_IMPOSITION)),
                settingValues[KEY_IMPOSITION] as Int
            )
            TestCase.assertEquals(
                c.getInt(c.getColumnIndex(PRINTSETTING_IMPOSITION_ORDER)),
                settingValues[KEY_IMPOSITION_ORDER] as Int
            )
            TestCase.assertEquals(
                c.getInt(c.getColumnIndex(PRINTSETTING_SORT)),
                settingValues[KEY_SORT] as Int
            )
            TestCase.assertEquals(
                c.getInt(c.getColumnIndex(PRINTSETTING_BOOKLET)),
                settingValues[KEY_BOOKLET] as Int
            )
            TestCase.assertEquals(
                c.getInt(c.getColumnIndex(PRINTSETTING_BOOKLET_FINISH)),
                settingValues[KEY_BOOKLET_FINISH] as Int
            )
            TestCase.assertEquals(
                c.getInt(c.getColumnIndex(PRINTSETTING_BOOKLET_LAYOUT)),
                settingValues[KEY_BOOKLET_LAYOUT] as Int
            )
            TestCase.assertEquals(
                c.getInt(c.getColumnIndex(PRINTSETTING_FINISHING_SIDE)),
                settingValues[KEY_FINISHING_SIDE] as Int
            )
            TestCase.assertEquals(
                c.getInt(c.getColumnIndex(PRINTSETTING_STAPLE)),
                settingValues[KEY_STAPLE] as Int
            )
            TestCase.assertEquals(
                c.getInt(c.getColumnIndex(PRINTSETTING_PUNCH)),
                settingValues[KEY_PUNCH] as Int
            )
            TestCase.assertEquals(
                c.getInt(c.getColumnIndex(PRINTSETTING_OUTPUT_TRAY)),
                settingValues[KEY_OUTPUT_TRAY] as Int
            )
            c.close()
        } else {
            // if not yet existing in database must be default values
            printerId = 1
            val settings = PrintSettings(printerId, AppConstants.PRINTER_MODEL_IS)
            TestCase.assertNotNull(settings)
            val settingValues = settings.settingValues
            TestCase.assertNotNull(settingValues)
            TestCase.assertEquals(DEFAULT_KEY_COLOR, settingValues[KEY_COLOR] as Int)
            TestCase.assertEquals(DEFAULT_KEY_ORIENTATION, settingValues[KEY_ORIENTATION] as Int)
            TestCase.assertEquals(DEFAULT_KEY_COPIES, settingValues[KEY_COPIES] as Int)
            TestCase.assertEquals(DEFAULT_KEY_DUPLEX, settingValues[KEY_DUPLEX] as Int)
            TestCase.assertEquals(DEFAULT_KEY_PAPER_SIZE, settingValues[KEY_PAPER_SIZE] as Int)
            TestCase.assertEquals(DEFAULT_KEY_SCALE_TO_FIT, settingValues[KEY_SCALE_TO_FIT] as Int)
            TestCase.assertEquals(DEFAULT_KEY_PAPER_TRAY, settingValues[KEY_PAPER_TRAY] as Int)
            TestCase.assertEquals(DEFAULT_KEY_INPUT_TRAY, settingValues[KEY_INPUT_TRAY] as Int)
            TestCase.assertEquals(DEFAULT_KEY_IMPOSITION, settingValues[KEY_IMPOSITION] as Int)
            TestCase.assertEquals(
                DEFAULT_KEY_IMPOSITION_ORDER,
                settingValues[KEY_IMPOSITION_ORDER] as Int
            )
            TestCase.assertEquals(DEFAULT_KEY_SORT, settingValues[KEY_SORT] as Int)
            TestCase.assertEquals(DEFAULT_KEY_BOOKLET, settingValues[KEY_BOOKLET] as Int)
            TestCase.assertEquals(
                DEFAULT_KEY_BOOKLET_FINISH,
                settingValues[KEY_BOOKLET_FINISH] as Int
            )
            TestCase.assertEquals(
                DEFAULT_KEY_BOOKLET_LAYOUT,
                settingValues[KEY_BOOKLET_LAYOUT] as Int
            )
            TestCase.assertEquals(
                DEFAULT_KEY_FINISHING_SIDE,
                settingValues[KEY_FINISHING_SIDE] as Int
            )
            TestCase.assertEquals(DEFAULT_KEY_STAPLE, settingValues[KEY_STAPLE] as Int)
            TestCase.assertEquals(DEFAULT_KEY_PUNCH, settingValues[KEY_PUNCH] as Int)
            TestCase.assertEquals(DEFAULT_KEY_OUTPUT_TRAY, settingValues[KEY_OUTPUT_TRAY] as Int)
        }
        db.close()
    }

    @Test
    fun testFormattedString_Portrait() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.getAppContext())
        val editor = prefs.edit()
        editor.putBoolean(AppConstants.PREF_KEY_AUTH_SECURE_PRINT, false)
        editor.putString(AppConstants.PREF_KEY_LOGIN_ID, "test")
        editor.putString(AppConstants.PREF_KEY_AUTH_PIN_CODE, "1234")
        editor.apply()
        val formattedString = _printSettings!!.formattedString(false)
        TestCase.assertNotNull(formattedString)
        TestCase.assertFalse(formattedString.isEmpty())
        TestCase.assertTrue(formattedString.contains("orientation=0\n"))
        TestCase.assertTrue(formattedString.contains("securePrint=0\n"))
        TestCase.assertTrue(formattedString.contains("loginId=test\n"))
        TestCase.assertTrue(formattedString.contains("pinCode=\n"))
        _printSettings!!.setValue(KEY_ORIENTATION, 1)
        editor.putBoolean(AppConstants.PREF_KEY_AUTH_SECURE_PRINT, true) // secure print ON
        editor.apply()
        val formattedString2 = _printSettings!!.formattedString(false)
        TestCase.assertNotNull(formattedString2)
        TestCase.assertFalse(formattedString2.isEmpty())
        TestCase.assertFalse(formattedString2 == formattedString)
        TestCase.assertTrue(formattedString2.contains("orientation=0\n"))
        TestCase.assertTrue(formattedString2.contains("securePrint=1\n"))
        TestCase.assertTrue(formattedString2.contains("loginId=test\n"))
        TestCase.assertTrue(formattedString2.contains("pinCode=1234\n"))
    }

    @Test
    fun testFormattedString_Landscape() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.getAppContext())
        val editor = prefs.edit()
        editor.putBoolean(AppConstants.PREF_KEY_AUTH_SECURE_PRINT, false)
        editor.putString(AppConstants.PREF_KEY_LOGIN_ID, "test")
        editor.putString(AppConstants.PREF_KEY_AUTH_PIN_CODE, "1234")
        editor.apply()
        val formattedString = _printSettings!!.formattedString(true)
        TestCase.assertNotNull(formattedString)
        TestCase.assertFalse(formattedString.isEmpty())
        TestCase.assertTrue(formattedString.contains("orientation=1\n"))
        TestCase.assertTrue(formattedString.contains("securePrint=0\n"))
        TestCase.assertTrue(formattedString.contains("loginId=test\n"))
        TestCase.assertTrue(formattedString.contains("pinCode=\n"))
        _printSettings!!.setValue(KEY_ORIENTATION, 1)
        editor.putBoolean(AppConstants.PREF_KEY_AUTH_SECURE_PRINT, true) // secure print ON
        editor.apply()
        val formattedString2 = _printSettings!!.formattedString(true)
        TestCase.assertNotNull(formattedString2)
        TestCase.assertFalse(formattedString2.isEmpty())
        TestCase.assertFalse(formattedString2 == formattedString)
        TestCase.assertTrue(formattedString2.contains("orientation=1\n"))
        TestCase.assertTrue(formattedString2.contains("securePrint=1\n"))
        TestCase.assertTrue(formattedString2.contains("loginId=test\n"))
        TestCase.assertTrue(formattedString2.contains("pinCode=1234\n"))
    }

    @Test
    fun testGetSettingValues() {
        TestCase.assertNotNull(_printSettings!!.settingValues)
        TestCase.assertEquals(18, _printSettings!!.settingValues.size)
    }

    @Test
    fun testGetValueNull() {
        TestCase.assertEquals(-1, _printSettings!!.getValue(KEY_MISSING_VALUE))
    }

    @Test
    fun testGetValue() {
        TestCase.assertEquals(0, _printSettings!!.getValue(KEY_COLOR))
    }

    @Test
    fun testSetValue() {
        _printSettings!!.setValue("colorMode", 2)
        TestCase.assertEquals(2, _printSettings!!.getValue(KEY_COLOR))
    }

    @Test
    fun testGetColorMode() {
        TestCase.assertEquals(Preview.ColorMode.AUTO, _printSettings!!.colorMode)
    }

    @Test
    fun testGetOrientation() {
        TestCase.assertEquals(Preview.Orientation.PORTRAIT, _printSettings!!.orientation)
    }

    @Test
    fun testGetDuplex() {
        TestCase.assertEquals(Preview.Duplex.OFF, _printSettings!!.duplex)
    }

    @Test
    fun testGetPaperSize() {
        TestCase.assertEquals(Preview.PaperSize.A4, _printSettings!!.paperSize)
    }

    @Test
    fun testIsScaleToFit() {
        TestCase.assertEquals(true, _printSettings!!.isScaleToFit)
        _printSettings!!.setValue(KEY_SCALE_TO_FIT, 0)
        TestCase.assertEquals(false, _printSettings!!.isScaleToFit)
    }

    @Test
    fun testGetImposition() {
        TestCase.assertEquals(Preview.Imposition.OFF, _printSettings!!.imposition)
    }

    @Test
    fun testGetImpositionOrder() {
        TestCase.assertEquals(Preview.ImpositionOrder.L_R, _printSettings!!.impositionOrder)
    }

    @Test
    fun testGetSort() {
        TestCase.assertEquals(Preview.Sort.PER_COPY, _printSettings!!.sort)
    }

    @Test
    fun testIsBooklet() {
        TestCase.assertEquals(false, _printSettings!!.isBooklet)
        _printSettings!!.setValue(KEY_BOOKLET, 1)
        TestCase.assertEquals(true, _printSettings!!.isBooklet)
    }

    @Test
    fun testGetBookletFinish() {
        TestCase.assertEquals(Preview.BookletFinish.OFF, _printSettings!!.bookletFinish)
    }

    @Test
    fun testGetBookletLayout() {
        TestCase.assertEquals(Preview.BookletLayout.FORWARD, _printSettings!!.bookletLayout)
    }

    @Test
    fun testGetFinishingSide() {
        TestCase.assertEquals(Preview.FinishingSide.LEFT, _printSettings!!.finishingSide)
    }

    @Test
    fun testGetStaple() {
        TestCase.assertEquals(Preview.Staple.OFF, _printSettings!!.staple)
    }

    @Test
    fun testGetPunch() {
        TestCase.assertEquals(Preview.Punch.OFF, _printSettings!!.punch)
    }

    @Test
    fun testSavePrintSettingToDb_Invalid() {
        TestCase.assertFalse(_printSettings!!.savePrintSettingToDB(PrinterManager.EMPTY_ID))
    }

    @Test
    fun testSavePrintSettingToDb() {
        val printerId: Int
        val mManager = DatabaseManager(SmartDeviceApp.getAppContext())
        var db = mManager.writableDatabase
        var c = db.query(PRINTER_TABLE, null, null, null, null, null, null)
        if (c.moveToFirst()) {
            printerId = c.getInt(c.getColumnIndex(PRINTER_ID))
        } else { // create data
            printerId = 1000
            val cv = ContentValues()
            cv.put(PRINTER_ID, printerId)
            cv.put(PRINTER_NAME, "test printers")
            cv.put(PRINTER_IP, "192.168.1.2")
            db.insert("Printer", null, cv)
            db.close()
        }
        c.close()
        db.close()
        val settingValues = _printSettings!!.settingValues
        TestCase.assertNotNull(settingValues)
        TestCase.assertTrue(_printSettings!!.savePrintSettingToDB(printerId))
        db = mManager.readableDatabase
        c = db.query(
            PRINTER_TABLE,
            null,
            "prn_id=?",
            arrayOf(printerId.toString()),
            null,
            null,
            null
        )
        TestCase.assertEquals(1, c.count)
        c = db.query(
            PRINTSETTING_TABLE,
            null,
            "prn_id=?",
            arrayOf(printerId.toString()),
            null,
            null,
            null
        )
        TestCase.assertEquals(1, c.count)
        c.moveToFirst()
        TestCase.assertTrue(c.getInt(c.getColumnIndex(PRINTSETTING_ID)) != -1)
        val settingId = c.getInt(c.getColumnIndex(PRINTSETTING_ID))
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
        c = db.query(
            PRINTER_TABLE,
            null,
            "prn_id=?",
            arrayOf(printerId.toString()),
            null,
            null,
            null
        )
        TestCase.assertEquals(1, c.count)
        c.moveToFirst()
        TestCase.assertEquals(settingId, c.getInt(c.getColumnIndex(PRINTSETTING_ID)))
        c.close()
        db.close()
    }

    @Test
    fun testInitializeStaticObjects() {
        TestCase.assertEquals(18, PrintSettings.sSettingsMaps[AppConstants.PRINTER_MODEL_IS]!!.size)
        TestCase.assertEquals(3, PrintSettings.sGroupListMap[AppConstants.PRINTER_MODEL_IS]!!.size)
        try {
            PrintSettings.initializeStaticObjects(null)
        } catch (e: NullPointerException) {
            TestCase.fail("NullPointerException")
        }
        // blank content
        PrintSettings.initializeStaticObjects("")
        // without group end tag
        PrintSettings.initializeStaticObjects(getFileContentsFromAssets("invalid_missingEndTag.xml"))
        // without setting start tag
        PrintSettings.initializeStaticObjects(getFileContentsFromAssets("invalid_missingStartTag.xml"))
        // starting " does not have a corresponding "
        PrintSettings.initializeStaticObjects(getFileContentsFromAssets("invalid_missingEndString.xml"))
        // value is not enclosed in ""
        PrintSettings.initializeStaticObjects(getFileContentsFromAssets("invalid_missingString.xml"))

        // w/ values since initially loaded - must still be equal to initial size
        TestCase.assertEquals(18, PrintSettings.sSettingsMaps[AppConstants.PRINTER_MODEL_IS]!!.size)
        TestCase.assertEquals(3, PrintSettings.sGroupListMap[AppConstants.PRINTER_MODEL_IS]!!.size)
    }

    @Test
    fun testInitializeStaticObjects_Duplicate() {
        // w/ values since initially loaded
        TestCase.assertEquals(18, PrintSettings.sSettingsMaps[AppConstants.PRINTER_MODEL_IS]!!.size)
        PrintSettings.initializeStaticObjects(getFileContentsFromAssets("printsettings.xml"))
        TestCase.assertEquals(18, PrintSettings.sSettingsMaps[AppConstants.PRINTER_MODEL_IS]!!.size)
        TestCase.assertEquals(3, PrintSettings.sGroupListMap[AppConstants.PRINTER_MODEL_IS]!!.size)
        var count = 0
        for (key in PrintSettings.sSettingsMaps[AppConstants.PRINTER_MODEL_IS]!!.keys) {
            val s = PrintSettings.sSettingsMaps[AppConstants.PRINTER_MODEL_IS]!![key]
            if (s!!.getAttributeValue("name") == "colorMode") {
                count++
            }
        }
        TestCase.assertEquals(1, count)
    }

    @Test
    fun testSSettingMap_ExistInDb() {
        val mgr = DatabaseManager(SmartDeviceApp.getAppContext())
        val db = mgr.readableDatabase
        val c = db.query("PrintSetting", null, null, null, null, null, null)
        val columnNames = c.columnNames
        c.close()
        db.close()
        val columns = listOf(*columnNames)
        for (key in PrintSettings.sSettingsMaps[AppConstants.PRINTER_MODEL_IS]!!.keys) {
            val s = PrintSettings.sSettingsMaps[AppConstants.PRINTER_MODEL_IS]!![key]
            TestCase.assertTrue(columns.contains(s!!.dbKey))
        }
    }

    // ================================================================================
    // Private methods
    // ================================================================================
    private fun getFileContentsFromAssets(assetFile: String): String? {
        val assetManager = InstrumentationRegistry.getInstrumentation().context.assets
        val buf = StringBuilder()
        val stream: InputStream
        try {
            stream = assetManager.open(assetFile)
            val `in` = BufferedReader(InputStreamReader(stream))
            var str: String?
            while (`in`.readLine().also { str = it } != null) {
                buf.append(str)
            }
            `in`.close()
        } catch (e: IOException) {
            return null
        }
        return buf.toString()
    }

    companion object {
        private const val PRINTER_ID = "prn_id"
        private const val PRINTER_IP = "prn_ip_address"
        private const val PRINTER_NAME = "prn_name"
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
        private const val KEY_MISSING_VALUE = "missingValue"
        private const val DEFAULT_KEY_COLOR = 0
        private const val DEFAULT_KEY_ORIENTATION = 0
        private const val DEFAULT_KEY_COPIES = 1
        private const val DEFAULT_KEY_DUPLEX = 0
        private const val DEFAULT_KEY_PAPER_SIZE = 2
        private const val DEFAULT_KEY_SCALE_TO_FIT = 1
        private const val DEFAULT_KEY_PAPER_TRAY = 0
        private const val DEFAULT_KEY_INPUT_TRAY = 0
        private const val DEFAULT_KEY_IMPOSITION = 0
        private const val DEFAULT_KEY_IMPOSITION_ORDER = 0
        private const val DEFAULT_KEY_SORT = 0
        private const val DEFAULT_KEY_BOOKLET = 0
        private const val DEFAULT_KEY_BOOKLET_FINISH = 0
        private const val DEFAULT_KEY_BOOKLET_LAYOUT = 0
        private const val DEFAULT_KEY_FINISHING_SIDE = 0
        private const val DEFAULT_KEY_STAPLE = 0
        private const val DEFAULT_KEY_PUNCH = 0
        private const val DEFAULT_KEY_OUTPUT_TRAY = 0
    }
}