package jp.co.riso.smartdeviceapp.model

import jp.co.riso.smartdeviceapp.model.PrintJob.JobResult
import junit.framework.TestCase
import org.junit.Before
import org.junit.Test
import java.util.*

class PrintJobTest {
    private var _pj: PrintJob? = null
    private var _date: Date? = null

    @Before
    fun setUp() {
        _date = Date()
        _pj = PrintJob(1, 1, "print job name.pdf", _date!!, JobResult.SUCCESSFUL)
    }

    @Test
    fun testGetId() {
        TestCase.assertEquals(1, _pj!!.id)
    }

    @Test
    fun testSetId() {
        _pj!!.id = 2
        TestCase.assertEquals(2, _pj!!.id)
    }

    @Test
    fun testGetPrinterId() {
        TestCase.assertEquals(1, _pj!!.printerId)
    }

    @Test
    fun testSetPrinterId() {
        _pj!!.printerId = 3
        TestCase.assertEquals(3, _pj!!.printerId)
    }

    @Test
    fun testGetName() {
        TestCase.assertEquals("print job name.pdf", _pj!!.name)
    }

    @Test
    fun testSetName() {
        _pj!!.name = "new name.pdf"
        TestCase.assertEquals("new name.pdf", _pj!!.name)
    }

    @Test
    fun testSetName_Null() {
        _pj!!.name = null
        TestCase.assertNull(_pj!!.name)
    }

    @Test
    fun testGetDate() {
        TestCase.assertEquals(_date, _pj!!.date)
    }

    @Test
    fun testSetDate() {
        val dateNew = Date()
        _pj!!.date = dateNew
        TestCase.assertEquals(dateNew, _pj!!.date)
    }

    @Test
    fun testSetDate_Null() {
        _pj!!.date = null
        TestCase.assertNull(_pj!!.date)
    }

    @Test
    fun testGetResult() {
        TestCase.assertEquals(JobResult.SUCCESSFUL, _pj!!.result)
    }

    @Test
    fun testSetResult() {
        _pj!!.result = JobResult.ERROR
        TestCase.assertEquals(JobResult.ERROR, _pj!!.result)
    }

    @Test
    fun testJobResult() {
        TestCase.assertEquals(JobResult.ERROR, JobResult.valueOf("ERROR"))
        TestCase.assertEquals(JobResult.SUCCESSFUL, JobResult.valueOf("SUCCESSFUL"))
    }
}