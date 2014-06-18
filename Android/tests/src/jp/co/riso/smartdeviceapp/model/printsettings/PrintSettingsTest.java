
package jp.co.riso.smartdeviceapp.model.printsettings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.view.MainActivity;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;

public class PrintSettingsTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public PrintSettingsTest() {
        super(MainActivity.class);
    }

    public PrintSettingsTest(Class<MainActivity> activityClass) {
        super(activityClass);
    }

    private static final String PRINTER_ID = "prn_id";
    private static final String PRINTER_IP = "prn_ip_address";
    private static final String PRINTER_NAME = "prn_name";
    private static final String PRINTER_TABLE = "Printer";
    private static final String PRINTSETTING_TABLE = "PrintSetting";
    private static final String PRINTSETTING_ID = "pst_id";
    private static final String PRINTSETTING_COLOR = "pst_color_mode";
    private static final String PRINTSETTING_ORIENTATION = "pst_orientation";
    private static final String PRINTSETTING_COPIES = "pst_copies";
    private static final String PRINTSETTING_DUPLEX = "pst_duplex";
    private static final String PRINTSETTING_PAPER_SIZE = "pst_paper_size";
    private static final String PRINTSETTING_SCALE_TO_FIT = "pst_scale_to_fit";
    private static final String PRINTSETTING_PAPER_TRAY = "pst_paper_type";
    private static final String PRINTSETTING_INPUT_TRAY = "pst_input_tray";
    private static final String PRINTSETTING_IMPOSITION = "pst_imposition";
    private static final String PRINTSETTING_IMPOSITION_ORDER = "pst_imposition_order";
    private static final String PRINTSETTING_SORT = "pst_sort";
    private static final String PRINTSETTING_BOOKLET = "pst_booklet";
    private static final String PRINTSETTING_BOOKLET_FINISH = "pst_booklet_finish";
    private static final String PRINTSETTING_BOOKLET_LAYOUT = "pst_booklet_layout";
    private static final String PRINTSETTING_FINISHING_SIDE = "pst_finishing_side";
    private static final String PRINTSETTING_STAPLE = "pst_staple";
    private static final String PRINTSETTING_PUNCH = "pst_punch";
    private static final String PRINTSETTING_OUTPUT_TRAY = "pst_output_tray";

    private static final String KEY_COLOR = "colorMode";
    private static final String KEY_ORIENTATION = "orientation";
    private static final String KEY_COPIES = "copies";
    private static final String KEY_DUPLEX = "duplex";
    private static final String KEY_PAPER_SIZE = "paperSize";
    private static final String KEY_SCALE_TO_FIT = "scaleToFit";
    private static final String KEY_PAPER_TRAY = "paperType";
    private static final String KEY_INPUT_TRAY = "inputTray";
    private static final String KEY_IMPOSITION = "imposition";
    private static final String KEY_IMPOSITION_ORDER = "impositionOrder";
    private static final String KEY_SORT = "sort";
    private static final String KEY_BOOKLET = "booklet";
    private static final String KEY_BOOKLET_FINISH = "bookletFinish";
    private static final String KEY_BOOKLET_LAYOUT = "bookletLayout";
    private static final String KEY_FINISHING_SIDE = "finishingSide";
    private static final String KEY_STAPLE = "staple";
    private static final String KEY_PUNCH = "punch";
    private static final String KEY_OUTPUT_TRAY = "outputTray";
    
    private static final int DEFAULT_KEY_COLOR = 0;
    private static final int DEFAULT_KEY_ORIENTATION = 0;
    private static final int DEFAULT_KEY_COPIES = 1;
    private static final int DEFAULT_KEY_DUPLEX = 0;
    private static final int DEFAULT_KEY_PAPER_SIZE = 2;
    private static final int DEFAULT_KEY_SCALE_TO_FIT = 1;
    private static final int DEFAULT_KEY_PAPER_TRAY = 0;
    private static final int DEFAULT_KEY_INPUT_TRAY = 0;
    private static final int DEFAULT_KEY_IMPOSITION = 0;
    private static final int DEFAULT_KEY_IMPOSITION_ORDER = 0;
    private static final int DEFAULT_KEY_SORT = 0;
    private static final int DEFAULT_KEY_BOOKLET = 0;
    private static final int DEFAULT_KEY_BOOKLET_FINISH = 0;
    private static final int DEFAULT_KEY_BOOKLET_LAYOUT = 0;
    private static final int DEFAULT_KEY_FINISHING_SIDE = 0;
    private static final int DEFAULT_KEY_STAPLE = 0;
    private static final int DEFAULT_KEY_PUNCH = 0;
    private static final int DEFAULT_KEY_OUTPUT_TRAY = 0;

    private PrintSettings mPrintSettings;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mPrintSettings = new PrintSettings(); //default values
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mPrintSettings = null;
    }

    public void testPreConditions() {
        assertNotNull(mPrintSettings);
    }

    public void testConstructor() {
        PrintSettings settings = new PrintSettings();
        assertNotNull(settings);
        HashMap<String, Integer> settingValues = settings.getSettingValues();
        assertNotNull(settingValues);

        assertEquals(DEFAULT_KEY_COLOR, (int) settingValues.get(KEY_COLOR));
        assertEquals(DEFAULT_KEY_ORIENTATION, (int) settingValues.get(KEY_ORIENTATION));
        assertEquals(DEFAULT_KEY_COPIES, (int) settingValues.get(KEY_COPIES));
        assertEquals(DEFAULT_KEY_DUPLEX, (int) settingValues.get(KEY_DUPLEX));
        assertEquals(DEFAULT_KEY_PAPER_SIZE, (int) settingValues.get(KEY_PAPER_SIZE));
        assertEquals(DEFAULT_KEY_SCALE_TO_FIT, (int) settingValues.get(KEY_SCALE_TO_FIT));
        assertEquals(DEFAULT_KEY_PAPER_TRAY, (int) settingValues.get(KEY_PAPER_TRAY));
        assertEquals(DEFAULT_KEY_INPUT_TRAY, (int) settingValues.get(KEY_INPUT_TRAY));
        assertEquals(DEFAULT_KEY_IMPOSITION, (int) settingValues.get(KEY_IMPOSITION));
        assertEquals(DEFAULT_KEY_IMPOSITION_ORDER, (int) settingValues.get(KEY_IMPOSITION_ORDER));
        assertEquals(DEFAULT_KEY_SORT, (int) settingValues.get(KEY_SORT));
        assertEquals(DEFAULT_KEY_BOOKLET, (int) settingValues.get(KEY_BOOKLET));
        assertEquals(DEFAULT_KEY_BOOKLET_FINISH, (int) settingValues.get(KEY_BOOKLET_FINISH));
        assertEquals(DEFAULT_KEY_BOOKLET_LAYOUT, (int) settingValues.get(KEY_BOOKLET_LAYOUT));
        assertEquals(DEFAULT_KEY_FINISHING_SIDE, (int) settingValues.get(KEY_FINISHING_SIDE));
        assertEquals(DEFAULT_KEY_STAPLE, (int) settingValues.get(KEY_STAPLE));
        assertEquals(DEFAULT_KEY_PUNCH, (int) settingValues.get(KEY_PUNCH));
        assertEquals(DEFAULT_KEY_OUTPUT_TRAY, (int) settingValues.get(KEY_OUTPUT_TRAY));
    }

    public void testConstructor_PrintSettings() {

        mPrintSettings.setValue(KEY_COLOR, 2);
        mPrintSettings.setValue(KEY_COPIES, 10);
        mPrintSettings.setValue(KEY_SCALE_TO_FIT, 0);

        PrintSettings settings = new PrintSettings(mPrintSettings);
        assertNotNull(settings);

        HashMap<String, Integer> settingValues = settings.getSettingValues();
        assertNotNull(settingValues);
        
        assertEquals(2, (int) settingValues.get(KEY_COLOR)); // from 0 to 2
        assertEquals(DEFAULT_KEY_ORIENTATION, (int) settingValues.get(KEY_ORIENTATION));
        assertEquals(10, (int) settingValues.get(KEY_COPIES)); // from 1 to 10
        assertEquals(DEFAULT_KEY_DUPLEX, (int) settingValues.get(KEY_DUPLEX));
        assertEquals(DEFAULT_KEY_PAPER_SIZE, (int) settingValues.get(KEY_PAPER_SIZE));
        assertEquals(0, (int) settingValues.get(KEY_SCALE_TO_FIT)); // from 1 to 0
        assertEquals(DEFAULT_KEY_PAPER_TRAY, (int) settingValues.get(KEY_PAPER_TRAY));
        assertEquals(DEFAULT_KEY_INPUT_TRAY, (int) settingValues.get(KEY_INPUT_TRAY));
        assertEquals(DEFAULT_KEY_IMPOSITION, (int) settingValues.get(KEY_IMPOSITION));
        assertEquals(DEFAULT_KEY_IMPOSITION_ORDER, (int) settingValues.get(KEY_IMPOSITION_ORDER));
        assertEquals(DEFAULT_KEY_SORT, (int) settingValues.get(KEY_SORT));
        assertEquals(DEFAULT_KEY_BOOKLET, (int) settingValues.get(KEY_BOOKLET));
        assertEquals(DEFAULT_KEY_BOOKLET_FINISH, (int) settingValues.get(KEY_BOOKLET_FINISH));
        assertEquals(DEFAULT_KEY_BOOKLET_LAYOUT, (int) settingValues.get(KEY_BOOKLET_LAYOUT));
        assertEquals(DEFAULT_KEY_FINISHING_SIDE, (int) settingValues.get(KEY_FINISHING_SIDE));
        assertEquals(DEFAULT_KEY_STAPLE, (int) settingValues.get(KEY_STAPLE));
        assertEquals(DEFAULT_KEY_PUNCH, (int) settingValues.get(KEY_PUNCH));
        assertEquals(DEFAULT_KEY_OUTPUT_TRAY, (int) settingValues.get(KEY_OUTPUT_TRAY));
    }

    // must have default values
    public void testConstructor_PrinterIdInvalid() {
        PrintSettings settings = new PrintSettings(PrinterManager.EMPTY_ID);
        assertNotNull(settings);
        HashMap<String, Integer> settingValues = settings.getSettingValues();
        assertNotNull(settingValues);

        assertEquals(DEFAULT_KEY_COLOR, (int) settingValues.get(KEY_COLOR));
        assertEquals(DEFAULT_KEY_ORIENTATION, (int) settingValues.get(KEY_ORIENTATION));
        assertEquals(DEFAULT_KEY_COPIES, (int) settingValues.get(KEY_COPIES));
        assertEquals(DEFAULT_KEY_DUPLEX, (int) settingValues.get(KEY_DUPLEX));
        assertEquals(DEFAULT_KEY_PAPER_SIZE, (int) settingValues.get(KEY_PAPER_SIZE));
        assertEquals(DEFAULT_KEY_SCALE_TO_FIT, (int) settingValues.get(KEY_SCALE_TO_FIT));
        assertEquals(DEFAULT_KEY_PAPER_TRAY, (int) settingValues.get(KEY_PAPER_TRAY));
        assertEquals(DEFAULT_KEY_INPUT_TRAY, (int) settingValues.get(KEY_INPUT_TRAY));
        assertEquals(DEFAULT_KEY_IMPOSITION, (int) settingValues.get(KEY_IMPOSITION));
        assertEquals(DEFAULT_KEY_IMPOSITION_ORDER, (int) settingValues.get(KEY_IMPOSITION_ORDER));
        assertEquals(DEFAULT_KEY_SORT, (int) settingValues.get(KEY_SORT));
        assertEquals(DEFAULT_KEY_BOOKLET, (int) settingValues.get(KEY_BOOKLET));
        assertEquals(DEFAULT_KEY_BOOKLET_FINISH, (int) settingValues.get(KEY_BOOKLET_FINISH));
        assertEquals(DEFAULT_KEY_BOOKLET_LAYOUT, (int) settingValues.get(KEY_BOOKLET_LAYOUT));
        assertEquals(DEFAULT_KEY_FINISHING_SIDE, (int) settingValues.get(KEY_FINISHING_SIDE));
        assertEquals(DEFAULT_KEY_STAPLE, (int) settingValues.get(KEY_STAPLE));
        assertEquals(DEFAULT_KEY_PUNCH, (int) settingValues.get(KEY_PUNCH));
        assertEquals(DEFAULT_KEY_OUTPUT_TRAY, (int) settingValues.get(KEY_OUTPUT_TRAY));
    }

    // must have default values
    public void testConstructor_PrinterIdValid() {
        int printerId = -1;

        DatabaseManager mManager = new DatabaseManager(SmartDeviceApp.getAppContext());

        SQLiteDatabase db = mManager.getWritableDatabase();

        Cursor c = db.query(PRINTSETTING_TABLE, null, null, null, null, null, null);

        if (c.getCount() > 0) {
            // if from database, values must be from database
            c.moveToFirst();
            printerId = c.getInt(c.getColumnIndex(PRINTER_ID));
            PrintSettings settings = new PrintSettings(printerId);
            assertNotNull(settings);
            HashMap<String, Integer> settingValues = settings.getSettingValues();
            assertNotNull(settingValues);

            assertEquals(c.getInt(c.getColumnIndex(PRINTSETTING_COLOR)),
                    (int) settingValues.get(KEY_COLOR));
            assertEquals(c.getInt(c.getColumnIndex(PRINTSETTING_ORIENTATION)),
                    (int) settingValues.get(KEY_ORIENTATION));
            assertEquals(c.getInt(c.getColumnIndex(PRINTSETTING_COPIES)),
                    (int) settingValues.get(KEY_COPIES));
            assertEquals(c.getInt(c.getColumnIndex(PRINTSETTING_DUPLEX)),
                    (int) settingValues.get(KEY_DUPLEX));
            assertEquals(c.getInt(c.getColumnIndex(PRINTSETTING_PAPER_SIZE)),
                    (int) settingValues.get(KEY_PAPER_SIZE));
            assertEquals(c.getInt(c.getColumnIndex(PRINTSETTING_SCALE_TO_FIT)),
                    (int) settingValues.get(KEY_SCALE_TO_FIT));
            assertEquals(c.getInt(c.getColumnIndex(PRINTSETTING_PAPER_TRAY)),
                    (int) settingValues.get(KEY_PAPER_TRAY));
            assertEquals(c.getInt(c.getColumnIndex(PRINTSETTING_INPUT_TRAY)),
                    (int) settingValues.get(KEY_INPUT_TRAY));
            assertEquals(c.getInt(c.getColumnIndex(PRINTSETTING_IMPOSITION)),
                    (int) settingValues.get(KEY_IMPOSITION));
            assertEquals(c.getInt(c.getColumnIndex(PRINTSETTING_IMPOSITION_ORDER)),
                    (int) settingValues.get(KEY_IMPOSITION_ORDER));
            assertEquals(c.getInt(c.getColumnIndex(PRINTSETTING_SORT)),
                    (int) settingValues.get(KEY_SORT));
            assertEquals(c.getInt(c.getColumnIndex(PRINTSETTING_BOOKLET)),
                    (int) settingValues.get(KEY_BOOKLET));
            assertEquals(c.getInt(c.getColumnIndex(PRINTSETTING_BOOKLET_FINISH)),
                    (int) settingValues.get(KEY_BOOKLET_FINISH));
            assertEquals(c.getInt(c.getColumnIndex(PRINTSETTING_BOOKLET_LAYOUT)),
                    (int) settingValues.get(KEY_BOOKLET_LAYOUT));
            assertEquals(c.getInt(c.getColumnIndex(PRINTSETTING_FINISHING_SIDE)),
                    (int) settingValues.get(KEY_FINISHING_SIDE));
            assertEquals(c.getInt(c.getColumnIndex(PRINTSETTING_STAPLE)),
                    (int) settingValues.get(KEY_STAPLE));
            assertEquals(c.getInt(c.getColumnIndex(PRINTSETTING_PUNCH)),
                    (int) settingValues.get(KEY_PUNCH));
            assertEquals(c.getInt(c.getColumnIndex(PRINTSETTING_OUTPUT_TRAY)),
                    (int) settingValues.get(KEY_OUTPUT_TRAY));
            c.close();
        } else {
            // if not yet existing in database must be default values
            printerId = 1;
            PrintSettings settings = new PrintSettings(printerId);
            assertNotNull(settings);
            HashMap<String, Integer> settingValues = settings.getSettingValues();
            assertNotNull(settingValues);

            assertEquals(DEFAULT_KEY_COLOR, (int) settingValues.get(KEY_COLOR));
            assertEquals(DEFAULT_KEY_ORIENTATION, (int) settingValues.get(KEY_ORIENTATION));
            assertEquals(DEFAULT_KEY_COPIES, (int) settingValues.get(KEY_COPIES));
            assertEquals(DEFAULT_KEY_DUPLEX, (int) settingValues.get(KEY_DUPLEX));
            assertEquals(DEFAULT_KEY_PAPER_SIZE, (int) settingValues.get(KEY_PAPER_SIZE));
            assertEquals(DEFAULT_KEY_SCALE_TO_FIT, (int) settingValues.get(KEY_SCALE_TO_FIT));
            assertEquals(DEFAULT_KEY_PAPER_TRAY, (int) settingValues.get(KEY_PAPER_TRAY));
            assertEquals(DEFAULT_KEY_INPUT_TRAY, (int) settingValues.get(KEY_INPUT_TRAY));
            assertEquals(DEFAULT_KEY_IMPOSITION, (int) settingValues.get(KEY_IMPOSITION));
            assertEquals(DEFAULT_KEY_IMPOSITION_ORDER, (int) settingValues.get(KEY_IMPOSITION_ORDER));
            assertEquals(DEFAULT_KEY_SORT, (int) settingValues.get(KEY_SORT));
            assertEquals(DEFAULT_KEY_BOOKLET, (int) settingValues.get(KEY_BOOKLET));
            assertEquals(DEFAULT_KEY_BOOKLET_FINISH, (int) settingValues.get(KEY_BOOKLET_FINISH));
            assertEquals(DEFAULT_KEY_BOOKLET_LAYOUT, (int) settingValues.get(KEY_BOOKLET_LAYOUT));
            assertEquals(DEFAULT_KEY_FINISHING_SIDE, (int) settingValues.get(KEY_FINISHING_SIDE));
            assertEquals(DEFAULT_KEY_STAPLE, (int) settingValues.get(KEY_STAPLE));
            assertEquals(DEFAULT_KEY_PUNCH, (int) settingValues.get(KEY_PUNCH));
            assertEquals(DEFAULT_KEY_OUTPUT_TRAY, (int) settingValues.get(KEY_OUTPUT_TRAY));
        }
        db.close();
    }
    
    public void testFormattedString_Portrait() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.getAppContext());
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean(AppConstants.PREF_KEY_AUTH_SECURE_PRINT, false);
        editor.putString(AppConstants.PREF_KEY_LOGIN_ID, "test");
        editor.putString(AppConstants.PREF_KEY_AUTH_PIN_CODE, "1234");

        editor.apply();
        
        String formattedString = mPrintSettings.formattedString(false);

        assertNotNull(formattedString);
        assertFalse(formattedString.isEmpty());
        assertTrue(formattedString.contains("orientation=0\n"));
        assertTrue(formattedString.contains("securePrint=0\n"));
        assertTrue(formattedString.contains("loginId=test\n"));
        assertTrue(formattedString.contains("pinCode=1234\n"));

        mPrintSettings.setValue(KEY_ORIENTATION, 1);
        editor.putBoolean(AppConstants.PREF_KEY_AUTH_SECURE_PRINT, true); // secure print ON
        editor.apply();
        
        String formattedString2 = mPrintSettings.formattedString(false);

        assertNotNull(formattedString2);
        assertFalse(formattedString2.isEmpty());
        assertFalse(formattedString2.equals(formattedString));
        assertTrue(formattedString2.contains("orientation=0\n"));
        assertTrue(formattedString2.contains("securePrint=1\n"));
        assertTrue(formattedString2.contains("loginId=test\n"));
        assertTrue(formattedString2.contains("pinCode=1234\n"));
    }
    
    public void testFormattedString_Landscape() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SmartDeviceApp.getAppContext());
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean(AppConstants.PREF_KEY_AUTH_SECURE_PRINT, false);
        editor.putString(AppConstants.PREF_KEY_LOGIN_ID, "test");
        editor.putString(AppConstants.PREF_KEY_AUTH_PIN_CODE, "1234");

        editor.apply();
        
        String formattedString = mPrintSettings.formattedString(true);

        assertNotNull(formattedString);
        assertFalse(formattedString.isEmpty());
        assertTrue(formattedString.contains("orientation=1\n"));
        assertTrue(formattedString.contains("securePrint=0\n"));
        assertTrue(formattedString.contains("loginId=test\n"));
        assertTrue(formattedString.contains("pinCode=1234\n"));

        mPrintSettings.setValue(KEY_ORIENTATION, 1);
        editor.putBoolean(AppConstants.PREF_KEY_AUTH_SECURE_PRINT, true); // secure print ON
        editor.apply();
        
        String formattedString2 = mPrintSettings.formattedString(true);

        assertNotNull(formattedString2);
        assertFalse(formattedString2.isEmpty());
        assertFalse(formattedString2.equals(formattedString));
        assertTrue(formattedString2.contains("orientation=1\n"));
        assertTrue(formattedString2.contains("securePrint=1\n"));
        assertTrue(formattedString2.contains("loginId=test\n"));
        assertTrue(formattedString2.contains("pinCode=1234\n"));
    }

    public void testGetSettingValues() {
        assertNotNull(mPrintSettings.getSettingValues());
        assertEquals(18, mPrintSettings.getSettingValues().size());
    }

    public void testGetValue() {
        assertEquals(0, mPrintSettings.getValue("colorMode"));
    }

    public void testSetValue() {
        mPrintSettings.setValue("colorMode", 2);
        assertEquals(2, mPrintSettings.getValue("colorMode"));
    }

    public void testGetColorMode() {
        assertEquals(Preview.ColorMode.AUTO, mPrintSettings.getColorMode());
    }

    public void testGetOrientation() {
        assertEquals(Preview.Orientation.PORTRAIT, mPrintSettings.getOrientation());
    }

    public void testGetDuplex() {
        assertEquals(Preview.Duplex.OFF, mPrintSettings.getDuplex());
    }

    public void testGetPaperSize() {
        assertEquals(Preview.PaperSize.A4, mPrintSettings.getPaperSize());
    }

    public void testIsScaleToFit() {
        assertEquals(false, mPrintSettings.isScaleToFit());
        mPrintSettings.setValue(KEY_SCALE_TO_FIT, 1);
        assertEquals(true, mPrintSettings.isScaleToFit());
    }

    public void testGetImposition() {
        assertEquals(Preview.Imposition.OFF, mPrintSettings.getImposition());
    }

    public void testGetImpositionOrder() {
        assertEquals(Preview.ImpositionOrder.L_R, mPrintSettings.getImpositionOrder());
    }

    public void testGetSort() {
        assertEquals(Preview.Sort.PER_PAGE, mPrintSettings.getSort());
    }

    public void testIsBooklet() {
        assertEquals(false, mPrintSettings.isBooklet());
        mPrintSettings.setValue(KEY_BOOKLET, 1);
        assertEquals(true, mPrintSettings.isBooklet());
    }

    public void testGetBookletFinish() {
        assertEquals(Preview.BookletFinish.OFF, mPrintSettings.getBookletFinish());
    }

    public void testGetBookletLayout() {
        assertEquals(Preview.BookletLayout.FORWARD, mPrintSettings.getBookletLayout());
    }

    public void testGetFinishingSide() {
        assertEquals(Preview.FinishingSide.LEFT, mPrintSettings.getFinishingSide());
    }

    public void testGetStaple() {
        assertEquals(Preview.Staple.OFF, mPrintSettings.getStaple());
    }

    public void testGetPunch() {
        assertEquals(Preview.Punch.OFF, mPrintSettings.getPunch());
    }

    public void testSavePrintSettingToDb_Invalid() {
        assertFalse(mPrintSettings.savePrintSettingToDB(PrinterManager.EMPTY_ID));
    }

    public void testSavePrintSettingToDb() {
        int printerId = -1;

        DatabaseManager mManager = new DatabaseManager(SmartDeviceApp.getAppContext());

        SQLiteDatabase db = mManager.getWritableDatabase();

        Cursor c = db.query(PRINTER_TABLE, null, null, null, null, null, null);
        if (c.moveToFirst()) {
            printerId = c.getInt(c.getColumnIndex(PRINTER_ID));
        } else { // create data
            printerId = 1000;
            ContentValues cv = new ContentValues();
            cv.put(PRINTER_ID, printerId);
            cv.put(PRINTER_NAME, "test printers");
            cv.put(PRINTER_IP, "192.168.1.2");

            db.insert("Printer", null, cv);

            db.close();
        }

        c.close();
        db.close();

        HashMap<String, Integer> settingValues = mPrintSettings.getSettingValues();
        assertNotNull(settingValues);

        assertTrue(mPrintSettings.savePrintSettingToDB(printerId));

        db = mManager.getReadableDatabase();
        c = db.query(PRINTER_TABLE, null, "prn_id=?", new String[] {
                String.valueOf(printerId)
        }, null, null, null);
        assertEquals(1, c.getCount());

        c = db.query(PRINTSETTING_TABLE, null, "prn_id=?", new String[] {
                String.valueOf(printerId)
        }, null, null, null);

        assertEquals(1, c.getCount());
        c.moveToFirst();
        assertTrue(c.getInt(c.getColumnIndex(PRINTSETTING_ID)) != -1);

        int settingId = c.getInt(c.getColumnIndex(PRINTSETTING_ID));

        assertEquals((int) settingValues.get(KEY_COLOR),
                c.getInt(c.getColumnIndex(PRINTSETTING_COLOR)));
        assertEquals((int) settingValues.get(KEY_ORIENTATION),
                c.getInt(c.getColumnIndex(PRINTSETTING_ORIENTATION)));
        assertEquals((int) settingValues.get(KEY_COPIES),
                c.getInt(c.getColumnIndex(PRINTSETTING_COPIES)));
        assertEquals((int) settingValues.get(KEY_DUPLEX),
                c.getInt(c.getColumnIndex(PRINTSETTING_DUPLEX)));
        assertEquals((int) settingValues.get(KEY_PAPER_SIZE),
                c.getInt(c.getColumnIndex(PRINTSETTING_PAPER_SIZE)));
        assertEquals((int) settingValues.get(KEY_SCALE_TO_FIT),
                c.getInt(c.getColumnIndex(PRINTSETTING_SCALE_TO_FIT)));
        assertEquals((int) settingValues.get(KEY_PAPER_TRAY),
                c.getInt(c.getColumnIndex(PRINTSETTING_PAPER_TRAY)));
        assertEquals((int) settingValues.get(KEY_INPUT_TRAY),
                c.getInt(c.getColumnIndex(PRINTSETTING_INPUT_TRAY)));
        assertEquals((int) settingValues.get(KEY_IMPOSITION),
                c.getInt(c.getColumnIndex(PRINTSETTING_IMPOSITION)));
        assertEquals((int) settingValues.get(KEY_IMPOSITION_ORDER),
                c.getInt(c.getColumnIndex(PRINTSETTING_IMPOSITION_ORDER)));
        assertEquals((int) settingValues.get(KEY_SORT),
                c.getInt(c.getColumnIndex(PRINTSETTING_SORT)));
        assertEquals((int) settingValues.get(KEY_BOOKLET),
                c.getInt(c.getColumnIndex(PRINTSETTING_BOOKLET)));
        assertEquals((int) settingValues.get(KEY_BOOKLET_FINISH),
                c.getInt(c.getColumnIndex(PRINTSETTING_BOOKLET_FINISH)));
        assertEquals((int) settingValues.get(KEY_BOOKLET_LAYOUT),
                c.getInt(c.getColumnIndex(PRINTSETTING_BOOKLET_LAYOUT)));
        assertEquals((int) settingValues.get(KEY_FINISHING_SIDE),
                c.getInt(c.getColumnIndex(PRINTSETTING_FINISHING_SIDE)));
        assertEquals((int) settingValues.get(KEY_STAPLE),
                c.getInt(c.getColumnIndex(PRINTSETTING_STAPLE)));
        assertEquals((int) settingValues.get(KEY_PUNCH),
                c.getInt(c.getColumnIndex(PRINTSETTING_PUNCH)));
        assertEquals((int) settingValues.get(KEY_OUTPUT_TRAY),
                c.getInt(c.getColumnIndex(PRINTSETTING_OUTPUT_TRAY)));

        c = db.query(PRINTER_TABLE, null, "prn_id=?", new String[] {
                String.valueOf(printerId)
        }, null, null, null);

        assertEquals(1, c.getCount());

        c.moveToFirst();

        assertEquals(settingId, c.getInt(c.getColumnIndex(PRINTSETTING_ID)));

        c.close();
        db.close();
    }

    public void testInitializeStaticObjects() {
        assertEquals(18, PrintSettings.sSettingMap.size());
        assertEquals(3, PrintSettings.sGroupList.size());

        try {
            PrintSettings.initializeStaticObjects(null);
        } catch (NullPointerException e) {
            fail("NullPointerException");
        }
        // blank content
        PrintSettings.initializeStaticObjects("");
        // without group end tag
        PrintSettings.initializeStaticObjects(getFileContentsFromAssets("invalid_missingEndTag.xml"));
        // without setting start tag
        PrintSettings.initializeStaticObjects(getFileContentsFromAssets("invalid_missingStartTag.xml"));
        // starting " does not have a corresponding "
        PrintSettings.initializeStaticObjects(getFileContentsFromAssets("invalid_missingEndString.xml"));
        // value is not enclosed in ""
        PrintSettings.initializeStaticObjects(getFileContentsFromAssets("invalid_missingString.xml"));

        // w/ values since initially loaded - must still be equal to initial size
        assertEquals(18, PrintSettings.sSettingMap.size());
        assertEquals(3, PrintSettings.sGroupList.size());
    }

    public void testInitializeStaticObjects_Duplicate() {
        // w/ values since initially loaded
        assertEquals(18, PrintSettings.sSettingMap.size());
        PrintSettings.initializeStaticObjects(getFileContentsFromAssets("printsettings.xml"));
        assertEquals(18, PrintSettings.sSettingMap.size());
        assertEquals(6, PrintSettings.sGroupList.size());
        int count = 0;
        for (String key : PrintSettings.sSettingMap.keySet()) {
            Setting s = PrintSettings.sSettingMap.get(key);
            if (s.getAttributeValue("name").equals("colorMode")) {
                count++;
            }
        }
        assertEquals(1, count);
    }

    public void testSSettingMap_ExistInDb() {
        DatabaseManager mgr = new DatabaseManager(SmartDeviceApp.getAppContext());
        SQLiteDatabase db = mgr.getReadableDatabase();
        Cursor c = db.query("PrintSetting", null, null, null, null, null, null);
        String[] columnNames = c.getColumnNames();
        c.close();
        db.close();

        List<String> columns = Arrays.asList(columnNames);

        for (String key : PrintSettings.sSettingMap.keySet()) {
            Setting s = PrintSettings.sSettingMap.get(key);
            assertTrue(columns.contains(s.getDbKey()));
        }
    }

    // ================================================================================
    // Private methods
    // ================================================================================

    private String getFileContentsFromAssets(String assetFile) {
        AssetManager assetManager = getInstrumentation().getContext().getAssets();

        StringBuilder buf = new StringBuilder();
        InputStream stream;
        try {
            stream = assetManager.open(assetFile);
            BufferedReader in = new BufferedReader(new InputStreamReader(stream));
            String str;

            while ((str = in.readLine()) != null) {
                buf.append(str);
            }

            in.close();
        } catch (IOException e) {
            return null;
        }

        return buf.toString();
    }


}
