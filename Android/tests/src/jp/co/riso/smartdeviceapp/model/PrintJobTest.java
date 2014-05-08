
package jp.co.riso.smartdeviceapp.model;

import java.util.Date;

import jp.co.riso.smartdeviceapp.model.PrintJob.JobResult;
import android.test.AndroidTestCase;

public class PrintJobTest extends AndroidTestCase {
    private PrintJob pj;
    private Date date;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        date =new Date();
        pj = new PrintJob(1, 1, "print job name.pdf",date, JobResult.SUCCESSFUL);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetId() {
        assertEquals(1,pj.getId());
    }

    public void testSetId() {
        pj.setId(2);
        assertEquals(2,pj.getId());
    }

    public void testGetPrinterId() {
        assertEquals(1,pj.getPrinterId());
    }

    public void testSetPrinterId() {
        pj.setPrinterId(3);
        assertEquals(3,pj.getPrinterId());
    }

    public void testGetName() {
        assertEquals("print job name.pdf",pj.getName());
    }

    public void testSetName() {
        pj.setName("new name.pdf");
        assertEquals("new name.pdf", pj.getName());
    }

    public void testGetDate() {
        assertEquals(date, pj.getDate());
    }

    public void testSetDate() {
        Date dateNew = new Date();
        pj.setDate(dateNew);
        assertEquals(dateNew, pj.getDate());
    }

    public void testGetResult() {
        assertEquals(JobResult.SUCCESSFUL, pj.getResult());
    }

    public void testSetResult() {
        pj.setResult(JobResult.ERROR);
        assertEquals(JobResult.ERROR, pj.getResult());
    }
}
