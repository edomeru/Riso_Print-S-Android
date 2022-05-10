package jp.co.riso.smartdeviceapp.model

import jp.co.riso.smartdeviceapp.model.PrintJob.JobResult
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import java.util.*

class PrintJobTest {
    private var pj: PrintJob? = null
    private var date: Date? = null

    @Before
    fun setUp() {
        date = Date()
        pj = PrintJob(1, 1, "print job name.pdf", date!!, JobResult.SUCCESSFUL)
    }

    @Test
    fun testGetId() {
        TestCase.assertEquals(1, pj!!.id)
    }

    @Test
    fun testSetId() {
        pj!!.id = 2
        TestCase.assertEquals(2, pj!!.id)
    }

    @Test
    fun testGetPrinterId() {
        TestCase.assertEquals(1, pj!!.printerId)
    }

    @Test
    fun testSetPrinterId() {
        pj!!.printerId = 3
        TestCase.assertEquals(3, pj!!.printerId)
    }

    @Test
    fun testGetName() {
        TestCase.assertEquals("print job name.pdf", pj!!.name)
    }

    @Test
    fun testSetName() {
        pj!!.name = "new name.pdf"
        TestCase.assertEquals("new name.pdf", pj!!.name)
    }

    @Test
    fun testSetName_Null() {
        pj!!.name = null
        TestCase.assertNull(pj!!.name)
    }

    @Test
    fun testGetDate() {
        TestCase.assertEquals(date, pj!!.date)
    }

    @Test
    fun testSetDate() {
        val dateNew = Date()
        pj!!.date = dateNew
        TestCase.assertEquals(dateNew, pj!!.date)
    }

    @Test
    fun testSetDate_Null() {
        pj!!.date = null
        TestCase.assertNull(pj!!.date)
    }

    @Test
    fun testGetResult() {
        TestCase.assertEquals(JobResult.SUCCESSFUL, pj!!.result)
    }

    @Test
    fun testSetResult() {
        pj!!.result = JobResult.ERROR
        TestCase.assertEquals(JobResult.ERROR, pj!!.result)
    }

    @Test
    fun testJobResult() {
        TestCase.assertEquals(JobResult.ERROR, JobResult.valueOf("ERROR"))
        TestCase.assertEquals(JobResult.SUCCESSFUL, JobResult.valueOf("SUCCESSFUL"))
    }
}