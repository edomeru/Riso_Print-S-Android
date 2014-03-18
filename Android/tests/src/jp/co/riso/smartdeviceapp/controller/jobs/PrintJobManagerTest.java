package jp.co.riso.smartdeviceapp.controller.jobs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
	private static final String C_PJB_ID = "pjb_id";
	private static final String C_PRN_ID = "prn_id";
	private static final String C_PJB_NAME = "pjb_name";
	private static final String C_PJB_DATE = "pjb_date";
	private static final String C_PJB_RESULT = "pjb_result";
	private static final String TABLE_PRINTER = "Printer";
	private static final String C_PRN_NAME = "prn_name";
	private static final String C_SQL_DATEFORMAT = "yyyy-MM-dd HH:mm:ss";
	private SimpleDateFormat mSdf;
	private RenamingDelegatingContext mContext; 
	private PrintJobManager mPrintJobManager;
	private DatabaseManager mManager;
	
	public PrintJobManagerTest() {
		super();
	}

	protected void setUp() throws Exception {
		super.setUp();
		mContext = new RenamingDelegatingContext(
				getContext(), "test_");

		mSdf = new SimpleDateFormat(C_SQL_DATEFORMAT, Locale.getDefault());

		mPrintJobManager = PrintJobManager.getInstance(mContext);
		mManager = new DatabaseManager(mContext);

		// set printers
		ContentValues pvalues = new ContentValues();
		pvalues.put(C_PRN_NAME, "printer with job");
		mManager.insert(TABLE_PRINTER, "true", pvalues);
		pvalues.put(C_PRN_NAME, "printer without job");
		mManager.insert(TABLE_PRINTER, "true", pvalues);

	}

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

		pvalues.put(C_PRN_ID, 1);
		pvalues.put(C_PJB_NAME, "Print Job Name");
		pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal());
		pvalues.put(C_PJB_DATE, "2014-03-17 13:12:11");

		db.insert(TABLE, null, pvalues);

		db.close();

		List<PrintJob> pj = PrintJobManager.getPrintJobs();
		assertNotNull(pj);
		assertEquals(1, pj.size());
		assertEquals(1, pj.get(0).getId());
		assertEquals("Print Job Name", pj.get(0).getName());
		assertEquals(JobResult.SUCCESSFUL, pj.get(0).getResult());

		assertTrue(mSdf.format(pj.get(0).getDate())
				.equals("2014-03-17 13:12:11"));

	}

	// public void testGetPrintJobsEmpty() {
	// mManager.delete(TABLE, null, null);
	// List<PrintJob> pj = PrintJobManager.getPrintJobs();
	// assertNotNull(pj);
	// assertTrue(pj.size() == 0);
	// }

	public void testGetPrintersWithJobs() {

		SQLiteDatabase db = mManager.getWritableDatabase();
		db.delete(TABLE, null, null);

		ContentValues pvalues = new ContentValues();

		pvalues.put(C_PRN_ID, 1);
		pvalues.put(C_PJB_NAME, "Print Job Name");
		pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal());
		pvalues.put(C_PJB_DATE, "2014-03-17 13:12:11");

		db.insert(TABLE, null, pvalues);

		db.close();

		List<Printer> printers = PrintJobManager.getPrintersWithJobs();

		assertNotNull(printers);
		assertTrue(printers.size() == 1);
		assertTrue(printers.get(0).getPrinterId() == 1);
		assertTrue(printers.get(0).getPrinterName().equals("printer with job"));
	}

	public void testCreatePrintJob() {
		boolean result = false;

		SQLiteDatabase db = mManager.getWritableDatabase();
		db.delete(TABLE, null, null);
		db.close();
		Date date = null;

		try {
			date = mSdf.parse("2014-03-17 13:12:11");

		} catch (ParseException e) {
			Log.e("PrintJobManagerTest", "convertSQLToDate parsing error.");
		}

		result = PrintJobManager.createPrintJob(1, "printjob.pdf", date,
				JobResult.ERROR);
		assertTrue(result);

		db = mManager.getWritableDatabase();

		Cursor cur = db.query(TABLE, null, null, null, null, null, null);
		assertNotNull(cur);
		assertEquals(1, cur.getCount());
		cur.moveToFirst();
		assertEquals(1, cur.getInt(cur.getColumnIndex(C_PJB_ID)));
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
		boolean result = PrintJobManager.deleteWithPrinterId(-1);
		assertFalse(result);
	}

	public void testDeleteWithJobIdInvalid() {
		boolean result = PrintJobManager.deleteWithJobId(-1);
		assertFalse(result);
	}

	public void testDeleteWithPrinterIdEmpty() {
		SQLiteDatabase db = mManager.getWritableDatabase();
		db.delete(TABLE, null, null);
		db.close();
		boolean result = PrintJobManager.deleteWithPrinterId(1);
		assertFalse(result);
	}

	public void testDeleteWithJobIdEmpty() {
		SQLiteDatabase db = mManager.getWritableDatabase();
		db.delete(TABLE, null, null);
		db.close();
		boolean result = PrintJobManager.deleteWithJobId(1);
		assertFalse(result);
	}

}
