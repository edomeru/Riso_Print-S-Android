package jp.co.riso.smartdeviceapp.model

import android.test.AndroidTestCase
import jp.co.riso.smartdeviceapp.model.PrintJob.JobResult
import junit.framework.TestCase
import java.util.*

class PrintJobTest : AndroidTestCase() {
    private var pj: PrintJob? = null
    private var date: Date? = null
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        date = Date()
        pj = PrintJob(1, 1, "print job name.pdf", date!!, JobResult.SUCCESSFUL)
    }

    @Throws(Exception::class)
    override fun tearDown() {
        super.tearDown()
    }

    fun testGetId() {
        TestCase.assertEquals(1, pj!!.id)
    }

    fun testSetId() {
        pj!!.id = 2
        TestCase.assertEquals(2, pj!!.id)
    }

    fun testGetPrinterId() {
        TestCase.assertEquals(1, pj!!.printerId)
    }

    fun testSetPrinterId() {
        pj!!.printerId = 3
        TestCase.assertEquals(3, pj!!.printerId)
    }

    fun testGetName() {
        TestCase.assertEquals("print job name.pdf", pj!!.name)
    }

    fun testSetName() {
        pj!!.name = "new name.pdf"
        TestCase.assertEquals("new name.pdf", pj!!.name)
    }

    fun testSetName_Null() {
        pj!!.name = null
        TestCase.assertNull(pj!!.name)
    }

    fun testGetDate() {
        TestCase.assertEquals(date, pj!!.date)
    }

    fun testSetDate() {
        val dateNew = Date()
        pj!!.date = dateNew
        TestCase.assertEquals(dateNew, pj!!.date)
    }

    fun testSetDate_Null() {
        pj!!.date = null
        TestCase.assertNull(pj!!.date)
    }

    fun testGetResult() {
        TestCase.assertEquals(JobResult.SUCCESSFUL, pj!!.result)
    }

    fun testSetResult() {
        pj!!.result = JobResult.ERROR
        TestCase.assertEquals(JobResult.ERROR, pj!!.result)
    }

    fun testJobResult() {
        TestCase.assertEquals(JobResult.ERROR, JobResult.valueOf("ERROR"))
        TestCase.assertEquals(JobResult.SUCCESSFUL, JobResult.valueOf("SUCCESSFUL"))
    }
}