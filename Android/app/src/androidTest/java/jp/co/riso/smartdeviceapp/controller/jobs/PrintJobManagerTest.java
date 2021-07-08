package jp.co.riso.smartdeviceapp.controller.jobs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager;
import jp.co.riso.smartdeviceapp.model.PrintJob;
import jp.co.riso.smartdeviceapp.model.PrintJob.JobResult;
import jp.co.riso.smartdeviceapp.model.Printer;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.util.Log;

public class PrintJobManagerTest extends AndroidTestCase {

    private static final String TABLE = "PrintJob";
    private static final String C_PRN_ID = "prn_id";
    private static final String C_PJB_NAME = "pjb_name";
    private static final String C_PJB_DATE = "pjb_date";
    private static final String C_PJB_RESULT = "pjb_result";
    private static final String TABLE_PRINTER = "Printer";
    private static final String C_PRN_NAME = "prn_name";
    private static final String C_SQL_DATEFORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String C_TIMEZONE ="UTC";
    private SimpleDateFormat mSdf;
    private RenamingDelegatingContext mContext;
    private PrintJobManager mPrintJobManager;
    private DatabaseManager mManager;
    private int mPrinterId=-1;
    private int mPrinterId2=-1;
    private boolean mInitialFlag = false;
    private String mPrinterName1 = "printer with job";
    private String mPrinterName2 = "printer without job";

    public PrintJobManagerTest() {
        super();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = new RenamingDelegatingContext(getContext(), "test_");

        mSdf = new SimpleDateFormat(C_SQL_DATEFORMAT, Locale.getDefault());
        mSdf.setTimeZone(TimeZone.getTimeZone(C_TIMEZONE));

        mPrintJobManager = PrintJobManager.getInstance(mContext);
        mPrintJobManager.setRefreshFlag(mInitialFlag);

        mManager = new DatabaseManager(mContext);
        SQLiteDatabase db = mManager.getWritableDatabase();
        // set printers
        db.delete(TABLE_PRINTER, null, null);
        ContentValues pvalues = new ContentValues();
        pvalues.put(C_PRN_ID, 1);
        pvalues.put(C_PRN_NAME, mPrinterName1);
        db.insert(TABLE_PRINTER, "true", pvalues);
        pvalues.put(C_PRN_ID, 2);
        pvalues.put(C_PRN_NAME, mPrinterName2);
        db.insert(TABLE_PRINTER, "true", pvalues);
        Cursor c = db.query(TABLE_PRINTER, null, null, null, null, null, null);
        c.moveToFirst();
        mPrinterId = c.getInt(c.getColumnIndex(C_PRN_ID));
        c.moveToNext();
        mPrinterId2 = c.getInt(c.getColumnIndex(C_PRN_ID));
        c.close();
        db.close();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        // clear data
        SQLiteDatabase db = mManager.getWritableDatabase();
        db.delete(TABLE_PRINTER, null, null);
        db.delete(TABLE, null, null);
        db.close();

    }


    public void testPreConditions() {
        assertNotNull(mPrintJobManager);
        SQLiteDatabase db = mManager.getReadableDatabase();
        Cursor c = db.query(TABLE_PRINTER, null, null, null, null, null, null);
        assertEquals(2, c.getCount());

        c.moveToFirst();
        assertEquals(c.getInt(c.getColumnIndex(C_PRN_ID)), mPrinterId);

        c.moveToNext();
        assertEquals(c.getInt(c.getColumnIndex(C_PRN_ID)), mPrinterId2);

        assertFalse(mPrintJobManager.isRefreshFlag());

        c.close();
        db.close();
    }


    public void testGetInstance() {
        assertEquals(mPrintJobManager, PrintJobManager.getInstance(mContext));
    }

    public void testGetPrintJobs_WithData() {
        SQLiteDatabase db = mManager.getWritableDatabase();
        db.delete(TABLE, null, null);

        ContentValues pvalues = new ContentValues();

        pvalues.put(C_PRN_ID, mPrinterId);
        pvalues.put(C_PJB_NAME, "Print Job Name");
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal());
        pvalues.put(C_PJB_DATE, "2014-03-17 13:12:11");

        db.insert(TABLE, null, pvalues);
        db.close();

        List<PrintJob> pj = mPrintJobManager.getPrintJobs();
        assertNotNull(pj);
        assertEquals(1, pj.size());
        assertEquals(mPrinterId, pj.get(0).getPrinterId());
        assertEquals("Print Job Name", pj.get(0).getName());
        assertEquals(JobResult.SUCCESSFUL, pj.get(0).getResult());
        assertTrue(mSdf.format(pj.get(0).getDate()).equals(
                "2014-03-17 13:12:11"));

        db = mManager.getWritableDatabase();
        //insert invalid date
        pvalues = new ContentValues();

        pvalues.put(C_PRN_ID, mPrinterId);
        pvalues.put(C_PJB_NAME, "Print Job Name");
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal());
        pvalues.put(C_PJB_DATE, "invalid date");

        db.insert(TABLE, null, pvalues);
        db.close();

        pj = mPrintJobManager.getPrintJobs();
        assertNotNull(pj);
        assertEquals(2, pj.size());
        assertEquals(mPrinterId, pj.get(1).getPrinterId());
        assertEquals("Print Job Name", pj.get(1).getName());
        assertEquals(JobResult.SUCCESSFUL, pj.get(1).getResult());
        //invalid date uses value of Date(0) = January 1, 1970 00:00:00
        assertEquals("1970-01-01 00:00:00",
                mSdf.format(pj.get(0).getDate()));
    }

    public void testGetPrintJobs_WithoutData() {
        SQLiteDatabase db = mManager.getWritableDatabase();
        //test no data
        db.delete(TABLE, null, null);
        db.close();

        List<PrintJob> pj  = mPrintJobManager.getPrintJobs();
        assertNotNull(pj);
        assertEquals(0, pj.size());
    }

    public void testGetPrintJobs_Order() {
        mManager.getWritableDatabase();
        SQLiteDatabase db = mManager.getWritableDatabase();
        db.delete(TABLE, null, null);

        ContentValues pvalues = new ContentValues();

        pvalues.put(C_PRN_ID, mPrinterId);
        pvalues.put(C_PJB_NAME, "Print Job Name");
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal());
        pvalues.put(C_PJB_DATE, "2014-03-17 13:12:11");

        db.insert(TABLE, null, pvalues);

        pvalues.clear();

        pvalues.put(C_PRN_ID, mPrinterId);
        pvalues.put(C_PJB_NAME, "Print Job Name1");
        pvalues.put(C_PJB_RESULT, JobResult.ERROR.ordinal());
        pvalues.put(C_PJB_DATE, "2014-03-18 08:12:11");

        db.insert(TABLE, null, pvalues);

        pvalues.clear();

        pvalues.put(C_PRN_ID, mPrinterId);
        pvalues.put(C_PJB_NAME, "Print Job Name2");
        pvalues.put(C_PJB_RESULT, JobResult.ERROR.ordinal());
        pvalues.put(C_PJB_DATE, "2012-03-18 08:12:11");

        db.insert(TABLE, null, pvalues);

        pvalues.clear();

        pvalues.put(C_PRN_ID, mPrinterId2);
        pvalues.put(C_PJB_NAME, "Print Job Name3");
        pvalues.put(C_PJB_RESULT, JobResult.ERROR.ordinal());
        pvalues.put(C_PJB_DATE, "2014-01-18 08:12:11");

        db.insert(TABLE, null, pvalues);

        pvalues.clear();

        pvalues.put(C_PRN_ID, mPrinterId);
        pvalues.put(C_PJB_NAME, "Print Job Name4");
        pvalues.put(C_PJB_RESULT, JobResult.ERROR.ordinal());
        pvalues.put(C_PJB_DATE, "2014-01-18 08:12:11");

        db.insert(TABLE, null, pvalues);
        db.close();

        List<PrintJob> pj = mPrintJobManager.getPrintJobs();
        assertNotNull(pj);
        assertEquals(5, pj.size());

        for (int i = 0; i < pj.size() - 1; i++) {
            //printer id is sorted
            assertTrue(pj.get(i).getPrinterId() <= pj.get(i + 1).getPrinterId());
            //same printer group; sorted according to date
            if (pj.get(i).getPrinterId() == pj.get(i + 1).getPrinterId()) {
                assertTrue(pj.get(i).getDate().after(pj.get(i + 1).getDate()));
            }
        }

    }

    public void testGetPrintersWithJobs_EmptyJobs() {
        SQLiteDatabase db = mManager.getWritableDatabase();
        db.delete(TABLE, null, null);

        //empty jobs yet but w/ existing printers -> printers list is empty
        List<Printer> printers = mPrintJobManager.getPrintersWithJobs();

        assertNotNull(printers);
        assertEquals(0, printers.size());
    }

    public void testGetPrintersWithJobs_WithJobs() {
        int newPrinterId = mPrinterId2 + 1;

        SQLiteDatabase db = mManager.getWritableDatabase();
        db.delete(TABLE, null, null);

        ContentValues pvalues = new ContentValues();

        pvalues.put(C_PRN_ID, mPrinterId);
        pvalues.put(C_PJB_NAME, "Print Job Name");
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal());
        pvalues.put(C_PJB_DATE, "2014-03-17 13:12:11");
        //add a job to an existing printer -> will be added to the printers list
        db.insert(TABLE, null, pvalues);

        //add another job to the same printer -> same printers list
        pvalues.put(C_PRN_ID, mPrinterId);
        pvalues.put(C_PJB_NAME, "Another Print Job Name");
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal());
        pvalues.put(C_PJB_DATE, "2014-03-17 13:14:11");

        db = mManager.getWritableDatabase();
        db.insert(TABLE, null, pvalues);

        db.close();

        List<Printer> printers = mPrintJobManager.getPrintersWithJobs();

        assertNotNull(printers);
        assertEquals(1, printers.size());
        assertEquals(mPrinterId,printers.get(0).getId());
        assertEquals(mPrinterName1,printers.get(0).getName());

        //add another printer -> same printers list
        pvalues.clear();
        pvalues.put(C_PRN_ID, newPrinterId);
        pvalues.put(C_PRN_NAME, "another printer");

        db = mManager.getWritableDatabase();
        db.insert("Printer", null, pvalues);
        db.close();

        printers = mPrintJobManager.getPrintersWithJobs();

        assertNotNull(printers);
        assertEquals(1, printers.size());
        assertEquals(mPrinterId,printers.get(0).getId());
        assertEquals(mPrinterName1,printers.get(0).getName());

        // add another job on a different printer -> added in printers list
        pvalues.clear();
        pvalues.put(C_PRN_ID, newPrinterId);
        pvalues.put(C_PJB_NAME, "A Print Job in another printer");
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal());
        pvalues.put(C_PJB_DATE, "2014-03-17 13:22:11");

        db = mManager.getWritableDatabase();
        db.insert(TABLE, null, pvalues);
        db.close();

        printers = mPrintJobManager.getPrintersWithJobs();

        assertNotNull(printers);
        assertEquals(2, printers.size());
        assertEquals(mPrinterId,printers.get(0).getId());
        assertEquals(mPrinterName1,printers.get(0).getName());
        assertEquals(newPrinterId,printers.get(1).getId());
        assertEquals("another printer",printers.get(1).getName());
    }

    public void testGetPrintersWithJobs_NewPrinterAdded() {
        SQLiteDatabase db = mManager.getWritableDatabase();
        db.delete(TABLE, null, null);
        ContentValues pvalues = new ContentValues();

        pvalues.put(C_PRN_ID, mPrinterId);
        pvalues.put(C_PJB_NAME, "Print Job Name");
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal());
        pvalues.put(C_PJB_DATE, "2014-03-17 13:12:11");
        db.insert(TABLE, null, pvalues);

        pvalues.put(C_PRN_ID, mPrinterId);
        pvalues.put(C_PJB_NAME, "Another Print Job Name");
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal());
        pvalues.put(C_PJB_DATE, "2014-03-17 13:14:11");

        db = mManager.getWritableDatabase();
        db.insert(TABLE, null, pvalues);

        db.close();

        List<Printer> printers = mPrintJobManager.getPrintersWithJobs();

        assertNotNull(printers);
        assertEquals(1, printers.size());
        assertEquals(mPrinterId,printers.get(0).getId());
        assertEquals(mPrinterName1,printers.get(0).getName());

        //add another printer -> same printers list
        pvalues.clear();
        pvalues.put(C_PRN_NAME, "a new printer");

        db = mManager.getWritableDatabase();

        db.insert("Printer", null, pvalues);

        db.close();

        printers = mPrintJobManager.getPrintersWithJobs();

        assertNotNull(printers);
        assertEquals(1, printers.size());
        assertEquals(mPrinterId,printers.get(0).getId());
        assertEquals(mPrinterName1,printers.get(0).getName());
    }

    public void testGetPrintersWithJobs_WithDeletedPrinter() {
        int newPrinterId = mPrinterId2 + 1;

        SQLiteDatabase db = mManager.getWritableDatabase();
        db.delete(TABLE, null, null);

        ContentValues pvalues = new ContentValues();

        pvalues.put(C_PRN_ID, mPrinterId);
        pvalues.put(C_PJB_NAME, "Print Job Name");
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal());
        pvalues.put(C_PJB_DATE, "2014-03-17 13:12:11");
        //add a job to an existing printer -> will be added to the printers list
        db.insert(TABLE, null, pvalues);

        //add another job to the same printer -> same printers list
        pvalues.put(C_PRN_ID, mPrinterId);
        pvalues.put(C_PJB_NAME, "Another Print Job Name");
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal());
        pvalues.put(C_PJB_DATE, "2014-03-17 13:14:11");

        db = mManager.getWritableDatabase();
        db.insert(TABLE, null, pvalues);

        pvalues.clear();
        pvalues.put(C_PRN_ID, newPrinterId);
        pvalues.put(C_PRN_NAME, "another printer");

        db = mManager.getWritableDatabase();
        db.insert("Printer", null, pvalues);

        // add another job on a different printer -> added in printers list
        pvalues.clear();
        pvalues.put(C_PRN_ID, newPrinterId);
        pvalues.put(C_PJB_NAME, "A Print Job in another printer");
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal());
        pvalues.put(C_PJB_DATE, "2014-03-17 13:22:11");

        db = mManager.getWritableDatabase();
        db.insert(TABLE, null, pvalues);
        db.close();

        List<Printer> printers = mPrintJobManager.getPrintersWithJobs();

        assertNotNull(printers);
        assertEquals(2, printers.size());
        assertEquals(mPrinterId,printers.get(0).getId());
        assertEquals(mPrinterName1,printers.get(0).getName());
        assertEquals(newPrinterId,printers.get(1).getId());
        assertEquals("another printer",printers.get(1).getName());

        db = mManager.getWritableDatabase();
        db.delete("Printer", "prn_id=?", new String[] {String.valueOf(newPrinterId)});
        db.close();

        printers = mPrintJobManager.getPrintersWithJobs();
        assertNotNull(printers);
        assertEquals(1, printers.size());
        assertEquals(mPrinterId,printers.get(0).getId());
        assertEquals(mPrinterName1,printers.get(0).getName());
    }

    public void testGetPrintersWithJobs_WithDeletedEmptyPrinter() {
        SQLiteDatabase db = mManager.getWritableDatabase();
        db.delete(TABLE, null, null);
        ContentValues pvalues = new ContentValues();

        pvalues.put(C_PRN_ID, mPrinterId);
        pvalues.put(C_PJB_NAME, "Print Job Name");
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal());
        pvalues.put(C_PJB_DATE, "2014-03-17 13:12:11");
        db.insert(TABLE, null, pvalues);

        pvalues.put(C_PRN_ID, mPrinterId);
        pvalues.put(C_PJB_NAME, "Another Print Job Name");
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal());
        pvalues.put(C_PJB_DATE, "2014-03-17 13:14:11");

        db = mManager.getWritableDatabase();
        db.insert(TABLE, null, pvalues);

        db.close();

        List<Printer> printers = mPrintJobManager.getPrintersWithJobs();

        assertNotNull(printers);
        assertEquals(1, printers.size());
        assertEquals(mPrinterId,printers.get(0).getId());
        assertEquals(mPrinterName1,printers.get(0).getName());

        //delete existing printer w/o job -> same printers list
        db = mManager.getWritableDatabase();

        db.delete("Printer", "prn_id=?", new String[] {String.valueOf(mPrinterId2)});

        db.close();

        printers = mPrintJobManager.getPrintersWithJobs();

        assertNotNull(printers);
        assertEquals(1, printers.size());
        assertEquals(mPrinterId,printers.get(0).getId());
        assertEquals(mPrinterName1,printers.get(0).getName());

    }

    public void testCreatePrintJob() {
        boolean result;
        Date date = null;

        SQLiteDatabase db = mManager.getWritableDatabase();
        db.delete(TABLE, null, null);
        db.close();

        try {
            date = mSdf.parse("2014-03-17 13:12:11");
        } catch (ParseException e) {
            Log.e("PrintJobManagerTest", "convertSQLToDate parsing error.");
        }

        result = mPrintJobManager.createPrintJob(mPrinterId, "printjob.pdf", date,
                JobResult.ERROR);
        assertTrue(result);

        assertTrue(mPrintJobManager.isRefreshFlag());

        db = mManager.getReadableDatabase();

        Cursor cur = db.query(TABLE, null, null, null, null, null, null);
        assertNotNull(cur);
        assertEquals(1, cur.getCount());
        cur.moveToFirst();

        assertEquals("printjob.pdf",
                cur.getString(cur.getColumnIndex(C_PJB_NAME)));
        assertEquals("2014-03-17 13:12:11",
                cur.getString(cur.getColumnIndex(C_PJB_DATE)));
        assertEquals(JobResult.ERROR.ordinal(),
                cur.getInt(cur.getColumnIndex(C_PJB_RESULT)));

        cur.close();
        db.close();
    }

    public void testCreatePrintJob_Max() {
        boolean result;

        SQLiteDatabase db = mManager.getWritableDatabase();
        db.delete(TABLE, null, null);
  

        for (int j = 1; j <= 100; j++) {
            String sql = String.format(Locale.getDefault(), "INSERT INTO PrintJob" +
                    "(prn_id, pjb_name, pjb_date, pjb_result) VALUES ('%d', '%s', '%s', '%d')",
                    1, "Print Job " + j, PrintJobManager.convertDateToString(new Date()), j % 2);
            db.execSQL(sql);
        }
        db.close();
        

        result = mPrintJobManager.createPrintJob(1, "printjob.pdf", new Date(),
                JobResult.ERROR);
        assertTrue(result);

        assertTrue(mPrintJobManager.isRefreshFlag());

        db = mManager.getReadableDatabase();

        Cursor cur = db.query(TABLE, null, null, null, null, null, "pjb_id ASC");
        assertNotNull(cur);

        assertEquals(100, cur.getCount());
        
        // Print Job 1 will be deleted and Print Job 2 will be retrieved as first entry 
        cur.moveToFirst();
        assertEquals(cur.getString(cur.getColumnIndex("pjb_id")), "Print Job 2",
                cur.getString(cur.getColumnIndex(C_PJB_NAME)));
        assertEquals(JobResult.SUCCESSFUL.ordinal(),
                cur.getInt(cur.getColumnIndex(C_PJB_RESULT)));

        cur.moveToLast();

        assertEquals("printjob.pdf",
                cur.getString(cur.getColumnIndex(C_PJB_NAME)));
        assertEquals(JobResult.ERROR.ordinal(),
                cur.getInt(cur.getColumnIndex(C_PJB_RESULT)));

        cur.close();
        db.close();
    }

    public void testCreatePrintJob_Invalid() {
        boolean result;
        Date date = null;

        SQLiteDatabase db = mManager.getWritableDatabase();
        db.delete(TABLE, null, null);
        db.close();

        try {
            date = mSdf.parse("2014-03-17 13:12:11");
        } catch (ParseException e) {
            Log.e("PrintJobManagerTest", "convertSQLToDate parsing error.");
        }

        result = mPrintJobManager.createPrintJob(mPrinterId, "printjob.pdf", date,
                JobResult.ERROR);
        assertTrue(result);

        assertTrue(mPrintJobManager.isRefreshFlag());

        db = mManager.getWritableDatabase();

        Cursor cur = db.query(TABLE, null, null, null, null, null, null);
        assertNotNull(cur);
        assertEquals(1, cur.getCount());
        cur.moveToFirst();

        assertEquals("printjob.pdf",
                cur.getString(cur.getColumnIndex(C_PJB_NAME)));
        assertEquals("2014-03-17 13:12:11",
                cur.getString(cur.getColumnIndex(C_PJB_DATE)));
        assertEquals(JobResult.ERROR.ordinal(),
                cur.getInt(cur.getColumnIndex(C_PJB_RESULT)));
        
        cur.close();
        
        db.close();
        
        //not existing printer
        result = mPrintJobManager.createPrintJob(-1, "printjob.pdf", date,
                JobResult.ERROR);
        assertFalse(result);
        
        //not existing printer
        result = mPrintJobManager.createPrintJob(-1, null, null,
                JobResult.ERROR);
        assertFalse(result);
    }
    
    public void testCreatePrintJob_NullValues() {
        boolean result;
 
        SQLiteDatabase db = mManager.getWritableDatabase();
        db.delete(TABLE, null, null);
        db.close();

        result = mPrintJobManager.createPrintJob(mPrinterId, null, null,
                JobResult.ERROR);
        assertTrue(result);

        assertTrue(mPrintJobManager.isRefreshFlag());

        db = mManager.getWritableDatabase();

        Cursor cur = db.query(TABLE, null, null, null, null, null, null);
        assertNotNull(cur);
        assertEquals(1, cur.getCount());
        cur.moveToFirst();

        assertNull(cur.getString(cur.getColumnIndex(C_PJB_NAME)));
        //invalid date uses value of Date(0) = January 1, 1970 00:00:00
        assertEquals("1970-01-01 00:00:00",
                cur.getString(cur.getColumnIndex(C_PJB_DATE)));
        assertEquals(JobResult.ERROR.ordinal(),
                cur.getInt(cur.getColumnIndex(C_PJB_RESULT)));
        
        cur.close();
        
        db.close();
    }
    
    
    public void testDeleteWithPrinterId_Invalid() {
        boolean result = mPrintJobManager.deleteWithPrinterId(-1);
        assertFalse(result);
    }

    public void testDeleteWithJobId_Invalid() {
        boolean result = mPrintJobManager.deleteWithJobId(-1);
        assertFalse(result);
    }

    public void testDeleteWithPrinterId_Empty() {
        SQLiteDatabase db = mManager.getWritableDatabase();
        db.delete(TABLE, null, null);
        db.close();
        boolean result = mPrintJobManager.deleteWithPrinterId(mPrinterId);
        assertFalse(result);
    }

    public void testDeleteWithJobId_Empty() {
        SQLiteDatabase db = mManager.getWritableDatabase();
        db.delete(TABLE, null, null);
        db.close();
        boolean result = mPrintJobManager.deleteWithJobId(1);
        assertFalse(result);
    }

    public void testDeleteWithPrinterId() {

        SQLiteDatabase db = mManager.getWritableDatabase();
        db.delete(TABLE, null, null);

        ContentValues pvalues = new ContentValues();

        pvalues.put(C_PRN_ID, mPrinterId);
        pvalues.put(C_PJB_NAME, "Print Job Name");
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal());
        pvalues.put(C_PJB_DATE, "2014-03-17 13:12:11");

        db.insert(TABLE, null, pvalues);

        db.close();

        boolean result = mPrintJobManager.deleteWithPrinterId(mPrinterId);
        assertTrue(result);

        db = mManager.getReadableDatabase();

        Cursor cur = db.query(TABLE, null, null, null, null, null, null);
        assertNotNull(cur);
        assertEquals(0, cur.getCount());

        cur.close();
        db.close();
    }

    public void testDeleteWithJobId() {
        int jobId = 1;

        SQLiteDatabase db = mManager.getWritableDatabase();
        db.delete(TABLE, null, null);

        ContentValues pvalues = new ContentValues();

        pvalues.put("pjb_id", jobId);
        pvalues.put(C_PRN_ID, mPrinterId);
        pvalues.put(C_PJB_NAME, "Print Job Name");
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal());
        pvalues.put(C_PJB_DATE, "2014-03-17 13:12:11");

        db.insert(TABLE, null, pvalues);

        db.close();

        boolean result = mPrintJobManager.deleteWithJobId(jobId);
        assertTrue(result);

        db = mManager.getReadableDatabase();

        Cursor cur = db.query(TABLE, null, null, null, null, null, null);
        assertEquals(0, cur.getCount());

        cur.close();
        db.close();
    }


    public void testGetFlag() {
        assertEquals(mInitialFlag, mPrintJobManager.isRefreshFlag());
    }

    public void testSetFlag() {
        boolean flag = true;
        mPrintJobManager.setRefreshFlag(flag);
        assertEquals(flag, mPrintJobManager.isRefreshFlag());
    }

}
