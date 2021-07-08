
package jp.co.riso.smartdeviceapp.controller.printsettings;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashMap;
import java.util.List;

import jp.co.riso.smartdeviceapp.AppConstants;
import jp.co.riso.smartdeviceapp.SmartDeviceApp;
import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager;
import jp.co.riso.smartdeviceapp.controller.db.KeyConstants;
import jp.co.riso.smartdeviceapp.controller.printer.PrinterManager;
import jp.co.riso.smartdeviceapp.model.Printer;
import jp.co.riso.smartdeviceapp.model.printsettings.PrintSettings;

public class PrintSettingsManagerTest extends AndroidTestCase {

    private static final String IPV4_OFFLINE_PRINTER_ADDRESS = "192.168.0.206";
    private static final String PRINTER_ID = "prn_id";
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

    private PrintSettingsManager mPrintSettingsMgr;
    private DatabaseManager mManager;
    private Context mContext;
    private int mPrinterId = PrinterManager.EMPTY_ID;
    private final String mPrinterType = AppConstants.PRINTER_MODEL_IS;
    private int mSettingId = 1;
    private final int mIntValue = 1;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        PrinterManager printerManager = PrinterManager.getInstance(SmartDeviceApp.getAppContext());
        List<Printer> printersList = printerManager.getSavedPrintersList();
        Printer printer;
        
        if(printersList.isEmpty()) {
            printer = new Printer("", IPV4_OFFLINE_PRINTER_ADDRESS);
            printerManager.savePrinterToDB(printer, false);
        } else {
            printer = printersList.get(0);
        }
        mContext = SmartDeviceApp.getAppContext();
        mManager = new DatabaseManager(mContext);
        mPrinterId = printer.getId();
        
        Cursor c = mManager.query(KeyConstants.KEY_SQL_PRINTSETTING_TABLE, null, KeyConstants.KEY_SQL_PRINTER_ID + "=?",
                new String[] { String.valueOf(mPrinterId) }, null, null, null);
        if (c.moveToFirst()) {
            mSettingId = DatabaseManager.getIntFromCursor(c, KeyConstants.KEY_SQL_PRINTSETTING_ID);
        }
        
        mPrintSettingsMgr = PrintSettingsManager.getInstance(mContext);

        SQLiteDatabase db = mManager.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(PRINTER_ID, mPrinterId);

        db.insertWithOnConflict(PRINTER_TABLE, null, cv, SQLiteDatabase.CONFLICT_REPLACE);

        cv.put(PRINTSETTING_ID, mSettingId);
        cv.put(PRINTSETTING_BOOKLET, mIntValue);
        cv.put(PRINTSETTING_BOOKLET_FINISH, mIntValue);
        cv.put(PRINTSETTING_BOOKLET_LAYOUT, mIntValue);
        cv.put(PRINTSETTING_COLOR, mIntValue);
        cv.put(PRINTSETTING_COPIES, mIntValue);
        cv.put(PRINTSETTING_DUPLEX, mIntValue);
        cv.put(PRINTSETTING_FINISHING_SIDE, mIntValue);
        cv.put(PRINTSETTING_IMPOSITION, mIntValue);
        cv.put(PRINTSETTING_IMPOSITION_ORDER, mIntValue);
        cv.put(PRINTSETTING_INPUT_TRAY, mIntValue);
        cv.put(PRINTSETTING_ORIENTATION, mIntValue);
        cv.put(PRINTSETTING_OUTPUT_TRAY, mIntValue);
        cv.put(PRINTSETTING_PAPER_SIZE, mIntValue);
        cv.put(PRINTSETTING_PAPER_TRAY, mIntValue);
        cv.put(PRINTSETTING_PUNCH, mIntValue);
        cv.put(PRINTSETTING_SCALE_TO_FIT, mIntValue);
        cv.put(PRINTSETTING_SORT, mIntValue);
        cv.put(PRINTSETTING_STAPLE, mIntValue);

        db.insertWithOnConflict(PRINTSETTING_TABLE, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        cv.clear();
        cv.put(PRINTER_ID, mPrinterId);
        cv.put(PRINTSETTING_ID, mSettingId);
        db.update(PRINTER_TABLE, cv, "prn_id=?", new String[] {
                String.valueOf(mPrinterId)
        });

        db.close();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        SQLiteDatabase db = mManager.getWritableDatabase();
        db.delete(PRINTER_TABLE, null, null);
        db.delete(PRINTSETTING_TABLE, null, null);
        db.close();
    }

    // ================================================================================
    // Tests - precondition
    // ================================================================================
    
    public void testPreConditions() {
        SQLiteDatabase db = mManager.getReadableDatabase();
        Cursor c = db.query(PRINTSETTING_TABLE, null, "prn_id=?", new String[] {
                String.valueOf(mPrinterId)
        }, null, null, null);
        assertEquals(1, c.getCount());

        db = mManager.getReadableDatabase();
        c = db.query(PRINTER_TABLE, null, "prn_id=?", new String[] {
                String.valueOf(mPrinterId)
        }, null, null, null);
        assertEquals(1, c.getCount());
    }

    // ================================================================================
    // Tests - getInstance
    // ================================================================================
    
    public void testGetInstance() {
        assertEquals(mPrintSettingsMgr, PrintSettingsManager.getInstance(mContext));
    }

    // ================================================================================
    // Tests - getPrintSetting
    // ================================================================================
    
    public void testGetPrintSetting() {
        PrintSettings settings = mPrintSettingsMgr.getPrintSetting(mPrinterId, mPrinterType);
        assertNotNull(settings);
        HashMap<String, Integer> settingValues = settings.getSettingValues();
        assertNotNull(settingValues);

        assertEquals(1, (int) settingValues.get(KEY_COLOR));
        assertEquals(1, (int) settingValues.get(KEY_ORIENTATION));
        assertEquals(1, (int) settingValues.get(KEY_COPIES));
        assertEquals(1, (int) settingValues.get(KEY_DUPLEX));
        assertEquals(1, (int) settingValues.get(KEY_PAPER_SIZE));
        assertEquals(1, (int) settingValues.get(KEY_SCALE_TO_FIT));
        assertEquals(1, (int) settingValues.get(KEY_PAPER_TRAY));
        assertEquals(1, (int) settingValues.get(KEY_INPUT_TRAY));
        assertEquals(1, (int) settingValues.get(KEY_IMPOSITION));
        assertEquals(1, (int) settingValues.get(KEY_IMPOSITION_ORDER));
        assertEquals(1, (int) settingValues.get(KEY_SORT));
        assertEquals(1, (int) settingValues.get(KEY_BOOKLET));
        assertEquals(1, (int) settingValues.get(KEY_BOOKLET_FINISH));
        assertEquals(1, (int) settingValues.get(KEY_BOOKLET_LAYOUT));
        assertEquals(1, (int) settingValues.get(KEY_FINISHING_SIDE));
        assertEquals(1, (int) settingValues.get(KEY_STAPLE));
        assertEquals(1, (int) settingValues.get(KEY_PUNCH));
        assertEquals(1, (int) settingValues.get(KEY_OUTPUT_TRAY));
    }

    // ================================================================================
    // Tests - saveToDB
    // ================================================================================
    
    public void testSaveToDB_DefaultValues() {
        PrintSettings settings = new PrintSettings();
        assertNotNull(settings);
        HashMap<String, Integer> settingValues = settings.getSettingValues();
        assertNotNull(settingValues);

        boolean result = mPrintSettingsMgr.saveToDB(mPrinterId, settings);
        assertTrue(result);

        SQLiteDatabase db = mManager.getReadableDatabase();
        Cursor c = db.query(PRINTSETTING_TABLE, null, "prn_id=?", new String[] {
                String.valueOf(mPrinterId)
        }, null, null, null);
        assertEquals(1, c.getCount());
        c.moveToFirst();

        assertEquals(mSettingId,  c.getInt(c.getColumnIndex(PRINTSETTING_ID)));
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

        c.close();

        c = db.query(PRINTER_TABLE, null, "prn_id=?", new String[] {
                String.valueOf(mPrinterId)
        }, null, null, null);
        assertEquals(1, c.getCount());
        c.moveToFirst();
        assertEquals(mSettingId, c.getInt(c.getColumnIndex(PRINTSETTING_ID)));
        c.close();
        db.close();
        
        // Retry save
        result = mPrintSettingsMgr.saveToDB(mPrinterId, settings);
        assertTrue(result);
    }
    
    public void testSaveToDB_NullValues() {
        PrintSettings settings = new PrintSettings();
        assertNotNull(settings);
        HashMap<String, Integer> settingValues = settings.getSettingValues();
        assertNotNull(settingValues);

        boolean result = mPrintSettingsMgr.saveToDB(999, null);
        assertFalse(result);

        SQLiteDatabase db = mManager.getReadableDatabase();
        Cursor c = db.query(PRINTSETTING_TABLE, null, "prn_id=?", new String[] {
                String.valueOf(999)
        }, null, null, null);
        assertEquals(0, c.getCount());
       
        c.close();

        db.close();
    }


    public void testSaveToDB_InitialSave() {
        PrintSettings settings = new PrintSettings();
        assertNotNull(settings);
        HashMap<String, Integer> settingValues = settings.getSettingValues();
        assertNotNull(settingValues);

        SQLiteDatabase db = mManager.getWritableDatabase();

        db.delete(PRINTER_TABLE, null, null);


        ContentValues cv = new ContentValues();
        cv.put(PRINTER_ID, mPrinterId);

        db.insertWithOnConflict(PRINTER_TABLE, null, cv, SQLiteDatabase.CONFLICT_REPLACE);

        db.close();
        boolean result = mPrintSettingsMgr.saveToDB(mPrinterId, settings);
        assertTrue(result);
        db = mManager.getReadableDatabase();
        Cursor c = db.query(PRINTSETTING_TABLE, null, "prn_id=?", new String[] {
                String.valueOf(mPrinterId)
        }, null, null, null);
        assertEquals(1, c.getCount());
        c.moveToFirst();

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

        int newSettingId = c.getInt(c.getColumnIndex(PRINTSETTING_ID));

        c.close();

        c = db.query(PRINTER_TABLE, null, "prn_id=?", new String[] {
                String.valueOf(mPrinterId)
        }, null, null, null);
        assertEquals(1, c.getCount());
        c.moveToFirst();
        assertEquals(newSettingId,  c.getInt(c.getColumnIndex(PRINTSETTING_ID)));
        c.close();
        db.close();
    }
}
