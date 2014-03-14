package jp.co.riso.smartdeviceapp.controller.jobs;

import java.util.Date;
import java.util.List;

import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager;
import jp.co.riso.smartdeviceapp.model.PrintJob;
import jp.co.riso.smartdeviceapp.model.PrintJob.JobResult;
import android.content.ContentValues;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

public class PrintJobManagerTest extends AndroidTestCase {
	private PrintJobManager mPrintJobManager;
	private DatabaseManager mManager;

	private static final String TABLE = "PrintJob";
	private static final String C_PJB_ID = "pjb_id";
	private static final String C_PRN_ID = "prn_id";
	private static final String C_PJB_NAME = "pjb_name";
	private static final String C_PJB_DATE = "pjb_date";
	private static final String C_PJB_RESULT = "pjb_result";
	private static final String WHERE_PJB_ID = C_PJB_ID + "=?";
	private static final String WHERE_PRN_ID = C_PRN_ID + "=?";
	private static final String TABLE_PRINTER = "Printer";
	private static final String C_PRN_NAME = "prn_name";

	public PrintJobManagerTest() {
		super();
	}

	protected void setUp() throws Exception {
		super.setUp();
		RenamingDelegatingContext context = new RenamingDelegatingContext(
				getContext(), "test_");
		
		//clear database tables first
		//then initialize database printer and printerjob tables
		PrintJobManager.initializeInstance(context);
		mPrintJobManager = PrintJobManager.getInstance();
		mManager = new DatabaseManager(context);
		
		ContentValues pvalues = new ContentValues();

		pvalues.put("prn_name", "printer with job");
		pvalues.put("prn_port_setting", 0);

		mManager.insert("Printer", "true", pvalues);

		pvalues.put("prn_name", "printer without job");
		pvalues.put("prn_port_setting", 0);
		mManager.insert("Printer", "true", pvalues);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		//delete database -- no need to clear database tables in setup of db
	}

	public void testGetInstance() {
		assertEquals(mPrintJobManager, PrintJobManager.getInstance());
	}

	public void testInitializeInstance() {
		PrintJobManager.initializeInstance(getContext());
		assertEquals(mPrintJobManager, PrintJobManager.getInstance());
	}

	public void testGetPrintJobs() {
		List<PrintJob> pj = PrintJobManager.getPrintJobs();
		assertNotNull(pj);
		assertTrue(pj.size() == 1);
	}

	public void testGetPrintersWithJobs() {

		
	}

	public void testCreatePrintJob() {
		boolean result = PrintJobManager.createPrintJob(1, "printjob.pdf",
				new Date(), JobResult.ERROR);
		assertTrue(result);
	}

	public void testFormatSQLDateTime() {
		//how?
	}


	public void testDeleteWithPrinterIdInvalid() {
		boolean result = PrintJobManager.deleteWithPrinterId(-1);
		assertFalse(result);
	}

	public void testDeleteWithJobIdInvalid() {
		boolean result = PrintJobManager.deleteWithJobId(-1);
		assertFalse(result);
	}

	
	public void testGetPrintJobsEmpty() {
		//delete all here
		List<PrintJob> pj = PrintJobManager.getPrintJobs();
		assertNotNull(pj);
		assertTrue(pj.size() == 0);
	}
	
	//same as invalid?
	public void testDeleteWithPrinterIdEmpty() {
		boolean result = PrintJobManager.deleteWithPrinterId(1);
		assertFalse(result);
	}

	public void testDeleteWithJobIdEmpty() {
		boolean result = PrintJobManager.deleteWithJobId(1);
		assertFalse(result);
	}



}
