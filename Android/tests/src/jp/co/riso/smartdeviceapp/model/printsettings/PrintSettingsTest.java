
package jp.co.riso.smartdeviceapp.model.printsettings;

import java.util.HashMap;

import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager;
import jp.co.riso.smartdeviceapp.controller.printsettings.PrintSettingsManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

public class PrintSettingsTest extends AndroidTestCase {
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

    private PrintSettings mPrintSettings;
    private Context mContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mPrintSettings = new PrintSettings();
        //use "mock" context in order to not destroy the data
        mContext = new RenamingDelegatingContext(getContext(), "test_");
        //initialize the PrintSettingsManager and give the "mock" context
        PrintSettingsManager.getInstance(mContext);
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

        assertEquals(1, (int) settingValues.get(KEY_COLOR));
        assertEquals(0, (int) settingValues.get(KEY_ORIENTATION));
        assertEquals(1, (int) settingValues.get(KEY_COPIES));
        assertEquals(0, (int) settingValues.get(KEY_DUPLEX));
        assertEquals(2, (int) settingValues.get(KEY_PAPER_SIZE));
        assertEquals(1, (int) settingValues.get(KEY_SCALE_TO_FIT));
        assertEquals(0, (int) settingValues.get(KEY_PAPER_TRAY));
        assertEquals(0, (int) settingValues.get(KEY_INPUT_TRAY));
        assertEquals(0, (int) settingValues.get(KEY_IMPOSITION));
        assertEquals(0, (int) settingValues.get(KEY_IMPOSITION_ORDER));
        assertEquals(0, (int) settingValues.get(KEY_SORT));
        assertEquals(0, (int) settingValues.get(KEY_BOOKLET));
        assertEquals(0, (int) settingValues.get(KEY_BOOKLET_FINISH));
        assertEquals(0, (int) settingValues.get(KEY_BOOKLET_LAYOUT));
        assertEquals(0, (int) settingValues.get(KEY_FINISHING_SIDE));
        assertEquals(0, (int) settingValues.get(KEY_STAPLE));
        assertEquals(0, (int) settingValues.get(KEY_PUNCH));
        assertEquals(0, (int) settingValues.get(KEY_OUTPUT_TRAY));
    }

    public void testConstructor_PrintSettings() {

        mPrintSettings.setValue(KEY_COLOR, 2);
        mPrintSettings.setValue(KEY_COPIES, 10);
        mPrintSettings.setValue(KEY_SCALE_TO_FIT, 0);

        PrintSettings settings = new PrintSettings(mPrintSettings);
        assertNotNull(settings);

        HashMap<String, Integer> settingValues = settings.getSettingValues();
        assertNotNull(settingValues);

        assertEquals(2, (int) settingValues.get(KEY_COLOR)); // from 1 to 2
        assertEquals(0, (int) settingValues.get(KEY_ORIENTATION));
        assertEquals(10, (int) settingValues.get(KEY_COPIES)); // from 1 to 10
        assertEquals(0, (int) settingValues.get(KEY_DUPLEX));
        assertEquals(2, (int) settingValues.get(KEY_PAPER_SIZE));
        assertEquals(0, (int) settingValues.get(KEY_SCALE_TO_FIT)); // from 1 to
        // 0
        assertEquals(0, (int) settingValues.get(KEY_PAPER_TRAY));
        assertEquals(0, (int) settingValues.get(KEY_INPUT_TRAY));
        assertEquals(0, (int) settingValues.get(KEY_IMPOSITION));
        assertEquals(0, (int) settingValues.get(KEY_IMPOSITION_ORDER));
        assertEquals(0, (int) settingValues.get(KEY_SORT));
        assertEquals(0, (int) settingValues.get(KEY_BOOKLET));
        assertEquals(0, (int) settingValues.get(KEY_BOOKLET_FINISH));
        assertEquals(0, (int) settingValues.get(KEY_BOOKLET_LAYOUT));
        assertEquals(0, (int) settingValues.get(KEY_FINISHING_SIDE));
        assertEquals(0, (int) settingValues.get(KEY_STAPLE));
        assertEquals(0, (int) settingValues.get(KEY_PUNCH));
        assertEquals(0, (int) settingValues.get(KEY_OUTPUT_TRAY));
    }

    // must have default values
    public void testConstructor_PrinterIdInvalid() {
        PrintSettings settings = new PrintSettings(-1);
        assertNotNull(settings);
        HashMap<String, Integer> settingValues = settings.getSettingValues();
        assertNotNull(settingValues);

        assertEquals(1, (int) settingValues.get(KEY_COLOR));
        assertEquals(0, (int) settingValues.get(KEY_ORIENTATION));
        assertEquals(1, (int) settingValues.get(KEY_COPIES));
        assertEquals(0, (int) settingValues.get(KEY_DUPLEX));
        assertEquals(2, (int) settingValues.get(KEY_PAPER_SIZE));
        assertEquals(1, (int) settingValues.get(KEY_SCALE_TO_FIT));
        assertEquals(0, (int) settingValues.get(KEY_PAPER_TRAY));
        assertEquals(0, (int) settingValues.get(KEY_INPUT_TRAY));
        assertEquals(0, (int) settingValues.get(KEY_IMPOSITION));
        assertEquals(0, (int) settingValues.get(KEY_IMPOSITION_ORDER));
        assertEquals(0, (int) settingValues.get(KEY_SORT));
        assertEquals(0, (int) settingValues.get(KEY_BOOKLET));
        assertEquals(0, (int) settingValues.get(KEY_BOOKLET_FINISH));
        assertEquals(0, (int) settingValues.get(KEY_BOOKLET_LAYOUT));
        assertEquals(0, (int) settingValues.get(KEY_FINISHING_SIDE));
        assertEquals(0, (int) settingValues.get(KEY_STAPLE));
        assertEquals(0, (int) settingValues.get(KEY_PUNCH));
        assertEquals(0, (int) settingValues.get(KEY_OUTPUT_TRAY));
    }

    // must have default values
    public void testConstructor_PrinterIdValid() {
        int printerId = -1;

        DatabaseManager mManager = new DatabaseManager(mContext);

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

            assertEquals(1, (int) settingValues.get(KEY_COLOR));
            assertEquals(0, (int) settingValues.get(KEY_ORIENTATION));
            assertEquals(1, (int) settingValues.get(KEY_COPIES));
            assertEquals(0, (int) settingValues.get(KEY_DUPLEX));
            assertEquals(2, (int) settingValues.get(KEY_PAPER_SIZE));
            assertEquals(1, (int) settingValues.get(KEY_SCALE_TO_FIT));
            assertEquals(0, (int) settingValues.get(KEY_PAPER_TRAY));
            assertEquals(0, (int) settingValues.get(KEY_INPUT_TRAY));
            assertEquals(0, (int) settingValues.get(KEY_IMPOSITION));
            assertEquals(0, (int) settingValues.get(KEY_IMPOSITION_ORDER));
            assertEquals(0, (int) settingValues.get(KEY_SORT));
            assertEquals(0, (int) settingValues.get(KEY_BOOKLET));
            assertEquals(0, (int) settingValues.get(KEY_BOOKLET_FINISH));
            assertEquals(0, (int) settingValues.get(KEY_BOOKLET_LAYOUT));
            assertEquals(0, (int) settingValues.get(KEY_FINISHING_SIDE));
            assertEquals(0, (int) settingValues.get(KEY_STAPLE));
            assertEquals(0, (int) settingValues.get(KEY_PUNCH));
            assertEquals(0, (int) settingValues.get(KEY_OUTPUT_TRAY));
        }
        db.close();
    }

    public void testFormattedString() {
        assertNotNull(mPrintSettings.formattedString());
        assertFalse(mPrintSettings.formattedString().isEmpty());
    }

    public void testGetSettingValues() {
        assertNotNull(mPrintSettings.getSettingValues());
        assertEquals(18, mPrintSettings.getSettingValues().size());
    }

    public void testGetValue() {
        assertEquals(1, mPrintSettings.getValue("colorMode"));
    }

    public void testSetValue() {
        mPrintSettings.setValue("colorMode", 2);
        assertEquals(2, mPrintSettings.getValue("colorMode"));
    }

    public void testGetColorMode() {
        assertEquals(Preview.ColorMode.FULL_COLOR, mPrintSettings.getColorMode());
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
        assertEquals(true, mPrintSettings.isScaleToFit());
        mPrintSettings.setValue(KEY_SCALE_TO_FIT, 0);
        assertEquals(false, mPrintSettings.isScaleToFit());
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
        assertEquals(Preview.BookletFinish.PAPER_FOLDING, mPrintSettings.getBookletFinish());
    }

    public void testGetBookletLayout() {
        assertEquals(Preview.BookletLayout.L_R, mPrintSettings.getBookletLayout());
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
        assertFalse(mPrintSettings.savePrintSettingToDB(-1));
    }

    public void testSavePrintSettingToDb() {
        int printerId = -1;

        DatabaseManager mManager = new DatabaseManager(mContext);

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
        PrintSettings.initializeStaticObjects("invalidFile");
        
        PrintSettings.initializeStaticObjects("db/initializeDB.sql");
        // w/ values since initially loaded
        assertEquals(18, PrintSettings.sSettingMap.size());
        assertEquals(3, PrintSettings.sGroupList.size());
    }
    
    public void testInitializeStaticObjects_Duplicate() {
        PrintSettings.initializeStaticObjects("printsettings3.xml");
        assertEquals(18, PrintSettings.sSettingMap.size());
        int count = 0;
        for (String key : PrintSettings.sSettingMap.keySet()) {
            Setting s = PrintSettings.sSettingMap.get(key);
            if (s.getAttributeValue("name").equals("colorMode")) {
                count++;
            }
        }
        assertEquals(1, count);
    }
}
