package jp.co.riso.smartdeviceapp.controller.jobs;

import java.util.Date;
import java.util.List;

import jp.co.riso.smartdeviceapp.model.PrintJob;
import jp.co.riso.smartdeviceapp.model.PrintJob.JobResult;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

public class PrintJobManagerTest extends AndroidTestCase {
	PrintJobManager printJobManager;
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

		PrintJobManager.initializeInstance(context);
		printJobManager = PrintJobManager.getInstance();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testGetInstance() {
		assertEquals(printJobManager, PrintJobManager.getInstance());
	}

	public void testInitializeInstance() {
		PrintJobManager.initializeInstance(getContext());
		assertEquals(printJobManager, PrintJobManager.getInstance());
	}

	public void testGetPrintJobsEmpty() {
		List<PrintJob> pj = PrintJobManager.getPrintJobs();
		assertNotNull(pj);
		assertTrue(pj.size() == 0);
	}

	public void testDeleteWithPrinterIdEmpty() {
		boolean result = PrintJobManager.deleteWithPrinterId(-1);
		assertFalse(result);
	}

	public void testDeleteWithJobIdEmpty() {
		boolean result = PrintJobManager.deleteWithJobId(-1);
		assertFalse(result);
	}

	public void testCreatePrintJob() {		
		boolean result = PrintJobManager.createPrintJob(1,"printjob.pdf",new Date(), JobResult.ERROR);
		assertTrue(result);
	}

	public void testFormatSQLDateTime() {
	}

	public void testGetPrintJobs() {
		List<PrintJob> pj = PrintJobManager.getPrintJobs();
		assertNotNull(pj);
		assertTrue(pj.size() == 1);
	}


}
