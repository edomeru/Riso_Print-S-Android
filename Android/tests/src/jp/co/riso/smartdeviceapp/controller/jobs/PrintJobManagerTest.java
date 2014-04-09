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
    private int mPrinterid=-1;
    private int mPrinterid2=-1;

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
        mManager = new DatabaseManager(mContext);

        // set printers
        mManager.delete(TABLE_PRINTER, null, null);
        ContentValues pvalues = new ContentValues();
        pvalues.put(C_PRN_NAME, "printer with job");
        mManager.insert(TABLE_PRINTER, "true", pvalues);
        pvalues.put(C_PRN_NAME, "printer without job");
        mManager.insert(TABLE_PRINTER, "true", pvalues);
        Cursor c = mManager.query(TABLE_PRINTER, null, null, null, null, null, null);
        c.moveToFirst();
        mPrinterid = c.getInt(c.getColumnIndex(C_PRN_ID));
        c.moveToNext();
        mPrinterid2 = c.getInt(c.getColumnIndex(C_PRN_ID));
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mManager.getWritableDatabase();
        SQLiteDatabase db = mManager.getWritableDatabase();
        db.delete(TABLE_PRINTER, null, null);
        db.delete(TABLE, null, null);
        db.close();

    }

    public void testGetInstance() {
        assertEquals(mPrintJobManager, PrintJobManager.getInstance(mContext));
    }

    public void testGetPrintJobs() {
        mManager.getWritableDatabase();
        SQLiteDatabase db = mManager.getWritableDatabase();
        db.delete(TABLE, null, null);

        ContentValues pvalues = new ContentValues();

        pvalues.put(C_PRN_ID, mPrinterid);
        pvalues.put(C_PJB_NAME, "Print Job Name");
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal());
        pvalues.put(C_PJB_DATE, "2014-03-17 13:12:11");

        db.insert(TABLE, null, pvalues);

        db.close();

        List<PrintJob> pj = mPrintJobManager.getPrintJobs();
        assertNotNull(pj);
        assertEquals(1, pj.size());
        assertEquals(mPrinterid, pj.get(0).getPrinterId());
        assertEquals("Print Job Name", pj.get(0).getName());
        assertEquals(JobResult.SUCCESSFUL, pj.get(0).getResult());

        assertTrue(mSdf.format(pj.get(0).getDate()).equals(
                "2014-03-17 13:12:11"));

    }

    public void testGetPrintJobsOrder() {
        mManager.getWritableDatabase();
        SQLiteDatabase db = mManager.getWritableDatabase();
        db.delete(TABLE, null, null);

        ContentValues pvalues = new ContentValues();

        pvalues.put(C_PRN_ID, mPrinterid);
        pvalues.put(C_PJB_NAME, "Print Job Name");
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal());
        pvalues.put(C_PJB_DATE, "2014-03-17 13:12:11");

        db.insert(TABLE, null, pvalues);

        pvalues.clear();

        pvalues.put(C_PRN_ID, mPrinterid);
        pvalues.put(C_PJB_NAME, "Print Job Name1");
        pvalues.put(C_PJB_RESULT, JobResult.ERROR.ordinal());
        pvalues.put(C_PJB_DATE, "2014-03-18 08:12:11");

        db.insert(TABLE, null, pvalues);

        pvalues.clear();

        pvalues.put(C_PRN_ID, mPrinterid);
        pvalues.put(C_PJB_NAME, "Print Job Name2");
        pvalues.put(C_PJB_RESULT, JobResult.ERROR.ordinal());
        pvalues.put(C_PJB_DATE, "2012-03-18 08:12:11");

        db.insert(TABLE, null, pvalues);

        pvalues.clear();

        pvalues.put(C_PRN_ID, mPrinterid2);
        pvalues.put(C_PJB_NAME, "Print Job Name3");
        pvalues.put(C_PJB_RESULT, JobResult.ERROR.ordinal());
        pvalues.put(C_PJB_DATE, "2014-01-18 08:12:11");

        db.insert(TABLE, null, pvalues);

        pvalues.clear();

        pvalues.put(C_PRN_ID, mPrinterid);
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
            if (pj.get(i).getPrinterId() == pj.get(i + 1).getPrinterId())
                assertTrue(pj.get(i).getDate().after(pj.get(i + 1).getDate()));
        }

    }

    public void testGetPrintersWithJobs() {

        SQLiteDatabase db = mManager.getWritableDatabase();
        db.delete(TABLE, null, null);

        ContentValues pvalues = new ContentValues();

        pvalues.put(C_PRN_ID, mPrinterid);
        pvalues.put(C_PJB_NAME, "Print Job Name");
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal());
        pvalues.put(C_PJB_DATE, "2014-03-17 13:12:11");

        db.insert(TABLE, null, pvalues);

        db.close();

        List<Printer> printers = mPrintJobManager.getPrintersWithJobs();

        assertNotNull(printers);
        assertEquals(1, printers.size());
        assertEquals(mPrinterid,printers.get(0).getId());
        assertEquals("printer with job",printers.get(0).getName());
    }

    public void testGetPrintersCount() {

        SQLiteDatabase db = mManager.getWritableDatabase();
        db.delete(TABLE, null, null);

        ContentValues pvalues = new ContentValues();

        pvalues.put(C_PRN_ID, mPrinterid);
        pvalues.put(C_PJB_NAME, "New Print Job Name");
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal());
        pvalues.put(C_PJB_DATE, "2014-03-17 13:12:11");

        db.insert(TABLE, null, pvalues);

        pvalues.put(C_PRN_ID, mPrinterid2);
        pvalues.put(C_PJB_NAME, "A Print Job");
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal());
        pvalues.put(C_PJB_DATE, "2014-03-17 13:12:11");

        db.insert(TABLE, null, pvalues);

        db.close();

        List<Printer> printers = mPrintJobManager.getPrintersWithJobs();

        assertNotNull(printers);

    }

    public void testCreatePrintJob() {
        boolean result = false;

        SQLiteDatabase db = mManager.getWritableDatabase();
        db.delete(TABLE, null, null);
        Cursor c = db.query(TABLE, null, null, null, null, null, null);
        assertNotNull("not null"+c.getCount(), c);
        db.close();
        mManager.close();
        Date date = null;

        try {
            date = mSdf.parse("2014-03-17 13:12:11");
        } catch (ParseException e) {
            Log.e("PrintJobManagerTest", "convertSQLToDate parsing error.");
        }

        result = mPrintJobManager.createPrintJob(mPrinterid, "printjob.pdf", date,
                JobResult.ERROR);
        assertTrue(result);

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
    }

    public void testDeleteWithPrinterIdInvalid() {
        boolean result = mPrintJobManager.deleteWithPrinterId(-1);
        assertFalse(result);
    }

    public void testDeleteWithJobIdInvalid() {
        boolean result = mPrintJobManager.deleteWithJobId(-1);
        assertFalse(result);
    }

    public void testDeleteWithPrinterIdEmpty() {
        SQLiteDatabase db = mManager.getWritableDatabase();
        db.delete(TABLE, null, null);
        db.close();
        boolean result = mPrintJobManager.deleteWithPrinterId(mPrinterid);
        assertFalse(result);
    }

    public void testDeleteWithJobIdEmpty() {
        SQLiteDatabase db = mManager.getWritableDatabase();
        db.delete(TABLE, null, null);
        db.close();
        boolean result = mPrintJobManager.deleteWithJobId(1);
        assertFalse(result);
    }

}
