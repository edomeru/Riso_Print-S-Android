package jp.co.riso.smartdeviceapp.controller.jobs

import android.content.ContentValues
import android.test.RenamingDelegatingContext
import android.util.Log
import jp.co.riso.smartdeviceapp.SmartDeviceApp
import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager
import jp.co.riso.smartdeviceapp.controller.jobs.PrintJobManager.Companion.convertDateToString
import jp.co.riso.smartdeviceapp.controller.jobs.PrintJobManager.Companion.getInstance
import jp.co.riso.smartdeviceapp.model.PrintJob.JobResult
import junit.framework.TestCase
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class PrintJobManagerTest {
    private var _sdf: SimpleDateFormat? = null
    private var _context: RenamingDelegatingContext? = null
    private var _printJobManager: PrintJobManager? = null
    private var _manager: DatabaseManager? = null
    private var _printerId = -1
    private var _printerId2 = -1
    private val _initialFlag = false
    private val _printerName1 = "printer with job"
    private val _printerName2 = "printer without job"

    @Before
    fun setUp() {
        _context = RenamingDelegatingContext(SmartDeviceApp.appContext, "test_")
        _sdf = SimpleDateFormat(C_SQL_DATEFORMAT, Locale.getDefault())
        _sdf!!.timeZone = TimeZone.getTimeZone(C_TIMEZONE)
        _printJobManager = getInstance(_context!!)
        _printJobManager!!.isRefreshFlag = _initialFlag
        _manager = DatabaseManager(_context)
        val db = _manager!!.writableDatabase
        // set printers
        db.delete(TABLE_PRINTER, null, null)
        val pvalues = ContentValues()
        pvalues.put(C_PRN_ID, 1)
        pvalues.put(C_PRN_NAME, _printerName1)
        db.insert(TABLE_PRINTER, "true", pvalues)
        pvalues.put(C_PRN_ID, 2)
        pvalues.put(C_PRN_NAME, _printerName2)
        db.insert(TABLE_PRINTER, "true", pvalues)
        val c = db.query(TABLE_PRINTER, null, null, null, null, null, null)
        c.moveToFirst()
        _printerId = c.getInt(c.getColumnIndex(C_PRN_ID))
        c.moveToNext()
        _printerId2 = c.getInt(c.getColumnIndex(C_PRN_ID))
        c.close()
        db.close()
    }

    @After
    fun tearDown() {
        // clear data
        val db = _manager!!.writableDatabase
        db.delete(TABLE_PRINTER, null, null)
        db.delete(TABLE, null, null)
        db.close()
    }

    @Test
    fun testPreConditions() {
        TestCase.assertNotNull(_printJobManager)
        val db = _manager!!.readableDatabase
        val c = db.query(TABLE_PRINTER, null, null, null, null, null, null)
        TestCase.assertEquals(2, c.count)
        c.moveToFirst()
        TestCase.assertEquals(c.getInt(c.getColumnIndex(C_PRN_ID)), _printerId)
        c.moveToNext()
        TestCase.assertEquals(c.getInt(c.getColumnIndex(C_PRN_ID)), _printerId2)
        TestCase.assertFalse(_printJobManager!!.isRefreshFlag)
        c.close()
        db.close()
    }

    @Test
    fun testGetInstance() {
        TestCase.assertEquals(_printJobManager, _context?.let { getInstance(it) })
    }

    @Test
    fun testGetPrintJobs_WithData() {
        var db = _manager!!.writableDatabase
        db.delete(TABLE, null, null)
        var pvalues = ContentValues()
        pvalues.put(C_PRN_ID, _printerId)
        pvalues.put(C_PJB_NAME, "Print Job Name")
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal)
        pvalues.put(C_PJB_DATE, "2014-03-17 13:12:11")
        db.insert(TABLE, null, pvalues)
        db.close()
        var pj = _printJobManager!!.printJobs
        TestCase.assertNotNull(pj)
        TestCase.assertEquals(1, pj.size)
        TestCase.assertEquals(_printerId, pj[0].printerId)
        TestCase.assertEquals("Print Job Name", pj[0].name)
        TestCase.assertEquals(JobResult.SUCCESSFUL, pj[0].result)
        TestCase.assertTrue(
            _sdf!!.format(pj[0].date as Date) ==
                    "2014-03-17 13:12:11"
        )
        db = _manager!!.writableDatabase
        //insert invalid date
        pvalues = ContentValues()
        pvalues.put(C_PRN_ID, _printerId)
        pvalues.put(C_PJB_NAME, "Print Job Name")
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal)
        pvalues.put(C_PJB_DATE, "invalid date")
        db.insert(TABLE, null, pvalues)
        db.close()
        pj = _printJobManager!!.printJobs
        TestCase.assertNotNull(pj)
        TestCase.assertEquals(2, pj.size)
        TestCase.assertEquals(_printerId, pj[1].printerId)
        TestCase.assertEquals("Print Job Name", pj[1].name)
        TestCase.assertEquals(JobResult.SUCCESSFUL, pj[1].result)
        //invalid date uses value of Date(0) = January 1, 1970 00:00:00
        TestCase.assertEquals(
            "1970-01-01 00:00:00",
            _sdf!!.format(pj[0].date as Date)
        )
    }

    @Test
    fun testGetPrintJobs_WithoutData() {
        val db = _manager!!.writableDatabase
        //test no data
        db.delete(TABLE, null, null)
        db.close()
        val pj = _printJobManager!!.printJobs
        TestCase.assertNotNull(pj)
        TestCase.assertEquals(0, pj.size)
    }

    @Test
    fun testGetPrintJobs_Order() {
        _manager!!.writableDatabase
        val db = _manager!!.writableDatabase
        db.delete(TABLE, null, null)
        val pvalues = ContentValues()
        pvalues.put(C_PRN_ID, _printerId)
        pvalues.put(C_PJB_NAME, "Print Job Name")
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal)
        pvalues.put(C_PJB_DATE, "2014-03-17 13:12:11")
        db.insert(TABLE, null, pvalues)
        pvalues.clear()
        pvalues.put(C_PRN_ID, _printerId)
        pvalues.put(C_PJB_NAME, "Print Job Name1")
        pvalues.put(C_PJB_RESULT, JobResult.ERROR.ordinal)
        pvalues.put(C_PJB_DATE, "2014-03-18 08:12:11")
        db.insert(TABLE, null, pvalues)
        pvalues.clear()
        pvalues.put(C_PRN_ID, _printerId)
        pvalues.put(C_PJB_NAME, "Print Job Name2")
        pvalues.put(C_PJB_RESULT, JobResult.ERROR.ordinal)
        pvalues.put(C_PJB_DATE, "2012-03-18 08:12:11")
        db.insert(TABLE, null, pvalues)
        pvalues.clear()
        pvalues.put(C_PRN_ID, _printerId2)
        pvalues.put(C_PJB_NAME, "Print Job Name3")
        pvalues.put(C_PJB_RESULT, JobResult.ERROR.ordinal)
        pvalues.put(C_PJB_DATE, "2014-01-18 08:12:11")
        db.insert(TABLE, null, pvalues)
        pvalues.clear()
        pvalues.put(C_PRN_ID, _printerId)
        pvalues.put(C_PJB_NAME, "Print Job Name4")
        pvalues.put(C_PJB_RESULT, JobResult.ERROR.ordinal)
        pvalues.put(C_PJB_DATE, "2014-01-18 08:12:11")
        db.insert(TABLE, null, pvalues)
        db.close()
        val pj = _printJobManager!!.printJobs
        TestCase.assertNotNull(pj)
        TestCase.assertEquals(5, pj.size)
        for (i in 0 until pj.size - 1) {
            //printer id is sorted
            TestCase.assertTrue(pj[i].printerId <= pj[i + 1].printerId)
            //same printer group; sorted according to date
            if (pj[i].printerId == pj[i + 1].printerId) {
                TestCase.assertTrue(pj[i].date!!.after(pj[i + 1].date))
            }
        }
    }

    @Test
    fun testGetPrintersWithJobs_EmptyJobs() {
        val db = _manager!!.writableDatabase
        db.delete(TABLE, null, null)

        //empty jobs yet but w/ existing printers -> printers list is empty
        val printers = _printJobManager!!.printersWithJobs
        TestCase.assertNotNull(printers)
        TestCase.assertEquals(0, printers.size)
    }

    @Test
    fun testGetPrintersWithJobs_WithJobs() {
        val newPrinterId = _printerId2 + 1
        var db = _manager!!.writableDatabase
        db.delete(TABLE, null, null)
        val pvalues = ContentValues()
        pvalues.put(C_PRN_ID, _printerId)
        pvalues.put(C_PJB_NAME, "Print Job Name")
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal)
        pvalues.put(C_PJB_DATE, "2014-03-17 13:12:11")
        //add a job to an existing printer -> will be added to the printers list
        db.insert(TABLE, null, pvalues)

        //add another job to the same printer -> same printers list
        pvalues.put(C_PRN_ID, _printerId)
        pvalues.put(C_PJB_NAME, "Another Print Job Name")
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal)
        pvalues.put(C_PJB_DATE, "2014-03-17 13:14:11")
        db = _manager!!.writableDatabase
        db.insert(TABLE, null, pvalues)
        db.close()
        var printers = _printJobManager!!.printersWithJobs
        TestCase.assertNotNull(printers)
        TestCase.assertEquals(1, printers.size)
        TestCase.assertEquals(_printerId, printers[0].id)
        TestCase.assertEquals(_printerName1, printers[0].name)

        //add another printer -> same printers list
        pvalues.clear()
        pvalues.put(C_PRN_ID, newPrinterId)
        pvalues.put(C_PRN_NAME, "another printer")
        db = _manager!!.writableDatabase
        db.insert("Printer", null, pvalues)
        db.close()
        printers = _printJobManager!!.printersWithJobs
        TestCase.assertNotNull(printers)
        TestCase.assertEquals(1, printers.size)
        TestCase.assertEquals(_printerId, printers[0].id)
        TestCase.assertEquals(_printerName1, printers[0].name)

        // add another job on a different printer -> added in printers list
        pvalues.clear()
        pvalues.put(C_PRN_ID, newPrinterId)
        pvalues.put(C_PJB_NAME, "A Print Job in another printer")
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal)
        pvalues.put(C_PJB_DATE, "2014-03-17 13:22:11")
        db = _manager!!.writableDatabase
        db.insert(TABLE, null, pvalues)
        db.close()
        printers = _printJobManager!!.printersWithJobs
        TestCase.assertNotNull(printers)
        TestCase.assertEquals(2, printers.size)
        TestCase.assertEquals(_printerId, printers[0].id)
        TestCase.assertEquals(_printerName1, printers[0].name)
        TestCase.assertEquals(newPrinterId, printers[1].id)
        TestCase.assertEquals("another printer", printers[1].name)
    }

    @Test
    fun testGetPrintersWithJobs_NewPrinterAdded() {
        var db = _manager!!.writableDatabase
        db.delete(TABLE, null, null)
        val pvalues = ContentValues()
        pvalues.put(C_PRN_ID, _printerId)
        pvalues.put(C_PJB_NAME, "Print Job Name")
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal)
        pvalues.put(C_PJB_DATE, "2014-03-17 13:12:11")
        db.insert(TABLE, null, pvalues)
        pvalues.put(C_PRN_ID, _printerId)
        pvalues.put(C_PJB_NAME, "Another Print Job Name")
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal)
        pvalues.put(C_PJB_DATE, "2014-03-17 13:14:11")
        db = _manager!!.writableDatabase
        db.insert(TABLE, null, pvalues)
        db.close()
        var printers = _printJobManager!!.printersWithJobs
        TestCase.assertNotNull(printers)
        TestCase.assertEquals(1, printers.size)
        TestCase.assertEquals(_printerId, printers[0].id)
        TestCase.assertEquals(_printerName1, printers[0].name)

        //add another printer -> same printers list
        pvalues.clear()
        pvalues.put(C_PRN_NAME, "a new printer")
        db = _manager!!.writableDatabase
        db.insert("Printer", null, pvalues)
        db.close()
        printers = _printJobManager!!.printersWithJobs
        TestCase.assertNotNull(printers)
        TestCase.assertEquals(1, printers.size)
        TestCase.assertEquals(_printerId, printers[0].id)
        TestCase.assertEquals(_printerName1, printers[0].name)
    }

    @Test
    fun testGetPrintersWithJobs_WithDeletedPrinter() {
        val newPrinterId = _printerId2 + 1
        var db = _manager!!.writableDatabase
        db.delete(TABLE, null, null)
        val pvalues = ContentValues()
        pvalues.put(C_PRN_ID, _printerId)
        pvalues.put(C_PJB_NAME, "Print Job Name")
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal)
        pvalues.put(C_PJB_DATE, "2014-03-17 13:12:11")
        //add a job to an existing printer -> will be added to the printers list
        db.insert(TABLE, null, pvalues)

        //add another job to the same printer -> same printers list
        pvalues.put(C_PRN_ID, _printerId)
        pvalues.put(C_PJB_NAME, "Another Print Job Name")
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal)
        pvalues.put(C_PJB_DATE, "2014-03-17 13:14:11")
        db = _manager!!.writableDatabase
        db.insert(TABLE, null, pvalues)
        pvalues.clear()
        pvalues.put(C_PRN_ID, newPrinterId)
        pvalues.put(C_PRN_NAME, "another printer")
        db = _manager!!.writableDatabase
        db.insert("Printer", null, pvalues)

        // add another job on a different printer -> added in printers list
        pvalues.clear()
        pvalues.put(C_PRN_ID, newPrinterId)
        pvalues.put(C_PJB_NAME, "A Print Job in another printer")
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal)
        pvalues.put(C_PJB_DATE, "2014-03-17 13:22:11")
        db = _manager!!.writableDatabase
        db.insert(TABLE, null, pvalues)
        db.close()
        var printers = _printJobManager!!.printersWithJobs
        TestCase.assertNotNull(printers)
        TestCase.assertEquals(2, printers.size)
        TestCase.assertEquals(_printerId, printers[0].id)
        TestCase.assertEquals(_printerName1, printers[0].name)
        TestCase.assertEquals(newPrinterId, printers[1].id)
        TestCase.assertEquals("another printer", printers[1].name)
        db = _manager!!.writableDatabase
        db.delete("Printer", "prn_id=?", arrayOf(newPrinterId.toString()))
        db.close()
        printers = _printJobManager!!.printersWithJobs
        TestCase.assertNotNull(printers)
        TestCase.assertEquals(1, printers.size)
        TestCase.assertEquals(_printerId, printers[0].id)
        TestCase.assertEquals(_printerName1, printers[0].name)
    }

    @Test
    fun testGetPrintersWithJobs_WithDeletedEmptyPrinter() {
        var db = _manager!!.writableDatabase
        db.delete(TABLE, null, null)
        val pvalues = ContentValues()
        pvalues.put(C_PRN_ID, _printerId)
        pvalues.put(C_PJB_NAME, "Print Job Name")
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal)
        pvalues.put(C_PJB_DATE, "2014-03-17 13:12:11")
        db.insert(TABLE, null, pvalues)
        pvalues.put(C_PRN_ID, _printerId)
        pvalues.put(C_PJB_NAME, "Another Print Job Name")
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal)
        pvalues.put(C_PJB_DATE, "2014-03-17 13:14:11")
        db = _manager!!.writableDatabase
        db.insert(TABLE, null, pvalues)
        db.close()
        var printers = _printJobManager!!.printersWithJobs
        TestCase.assertNotNull(printers)
        TestCase.assertEquals(1, printers.size)
        TestCase.assertEquals(_printerId, printers[0].id)
        TestCase.assertEquals(_printerName1, printers[0].name)

        //delete existing printer w/o job -> same printers list
        db = _manager!!.writableDatabase
        db.delete("Printer", "prn_id=?", arrayOf(_printerId2.toString()))
        db.close()
        printers = _printJobManager!!.printersWithJobs
        TestCase.assertNotNull(printers)
        TestCase.assertEquals(1, printers.size)
        TestCase.assertEquals(_printerId, printers[0].id)
        TestCase.assertEquals(_printerName1, printers[0].name)
    }

    @Test
    fun testCreatePrintJob() {
        val result: Boolean
        var date: Date? = null
        var db = _manager!!.writableDatabase
        db.delete(TABLE, null, null)
        db.close()
        try {
            date = _sdf!!.parse("2014-03-17 13:12:11")
        } catch (e: ParseException) {
            Log.e("PrintJobManagerTest", "convertSQLToDate parsing error.")
        }
        result = _printJobManager!!.createPrintJob(
            _printerId, "printjob.pdf", date,
            JobResult.ERROR
        )
        TestCase.assertTrue(result)
        TestCase.assertTrue(_printJobManager!!.isRefreshFlag)
        db = _manager!!.readableDatabase
        val cur = db.query(TABLE, null, null, null, null, null, null)
        TestCase.assertNotNull(cur)
        TestCase.assertEquals(1, cur.count)
        cur.moveToFirst()
        TestCase.assertEquals(
            "printjob.pdf",
            cur.getString(cur.getColumnIndex(C_PJB_NAME))
        )
        TestCase.assertEquals(
            "2014-03-17 13:12:11",
            cur.getString(cur.getColumnIndex(C_PJB_DATE))
        )
        TestCase.assertEquals(
            JobResult.ERROR.ordinal,
            cur.getInt(cur.getColumnIndex(C_PJB_RESULT))
        )
        cur.close()
        db.close()
    }

    @Test
    fun testCreatePrintJob_Max() {
        var db = _manager!!.writableDatabase
        db.delete(TABLE, null, null)
        for (j in 1..100) {
            val sql = String.format(
                Locale.getDefault(), "INSERT INTO PrintJob" +
                        "(prn_id, pjb_name, pjb_date, pjb_result) VALUES ('%d', '%s', '%s', '%d')",
                1, "Print Job $j", convertDateToString(Date()), j % 2
            )
            db.execSQL(sql)
        }
        db.close()
        val result: Boolean = _printJobManager!!.createPrintJob(
            1, "printjob.pdf", Date(),
            JobResult.ERROR
        )
        TestCase.assertTrue(result)
        TestCase.assertTrue(_printJobManager!!.isRefreshFlag)
        db = _manager!!.readableDatabase
        val cur = db.query(TABLE, null, null, null, null, null, "pjb_id ASC")
        TestCase.assertNotNull(cur)
        TestCase.assertEquals(100, cur.count)

        // Print Job 1 will be deleted and Print Job 2 will be retrieved as first entry 
        cur.moveToFirst()
        TestCase.assertEquals(
            cur.getString(cur.getColumnIndex("pjb_id")), "Print Job 2",
            cur.getString(cur.getColumnIndex(C_PJB_NAME))
        )
        TestCase.assertEquals(
            JobResult.SUCCESSFUL.ordinal,
            cur.getInt(cur.getColumnIndex(C_PJB_RESULT))
        )
        cur.moveToLast()
        TestCase.assertEquals(
            "printjob.pdf",
            cur.getString(cur.getColumnIndex(C_PJB_NAME))
        )
        TestCase.assertEquals(
            JobResult.ERROR.ordinal,
            cur.getInt(cur.getColumnIndex(C_PJB_RESULT))
        )
        cur.close()
        db.close()
    }

    @Test
    fun testCreatePrintJob_Invalid() {
        var result: Boolean
        var date: Date? = null
        var db = _manager!!.writableDatabase
        db.delete(TABLE, null, null)
        db.close()
        try {
            date = _sdf!!.parse("2014-03-17 13:12:11")
        } catch (e: ParseException) {
            Log.e("PrintJobManagerTest", "convertSQLToDate parsing error.")
        }
        result = _printJobManager!!.createPrintJob(
            _printerId, "printjob.pdf", date,
            JobResult.ERROR
        )
        TestCase.assertTrue(result)
        TestCase.assertTrue(_printJobManager!!.isRefreshFlag)
        db = _manager!!.writableDatabase
        val cur = db.query(TABLE, null, null, null, null, null, null)
        TestCase.assertNotNull(cur)
        TestCase.assertEquals(1, cur.count)
        cur.moveToFirst()
        TestCase.assertEquals(
            "printjob.pdf",
            cur.getString(cur.getColumnIndex(C_PJB_NAME))
        )
        TestCase.assertEquals(
            "2014-03-17 13:12:11",
            cur.getString(cur.getColumnIndex(C_PJB_DATE))
        )
        TestCase.assertEquals(
            JobResult.ERROR.ordinal,
            cur.getInt(cur.getColumnIndex(C_PJB_RESULT))
        )
        cur.close()
        db.close()

        //not existing printer
        result = _printJobManager!!.createPrintJob(
            -1, "printjob.pdf", date,
            JobResult.ERROR
        )
        TestCase.assertFalse(result)

        //not existing printer
        result = _printJobManager!!.createPrintJob(
            -1, null, null,
            JobResult.ERROR
        )
        TestCase.assertFalse(result)
    }

    @Test
    fun testCreatePrintJob_NullValues() {
        var db = _manager!!.writableDatabase
        db.delete(TABLE, null, null)
        db.close()
        val result: Boolean = _printJobManager!!.createPrintJob(
            _printerId, null, null,
            JobResult.ERROR
        )
        TestCase.assertTrue(result)
        TestCase.assertTrue(_printJobManager!!.isRefreshFlag)
        db = _manager!!.writableDatabase
        val cur = db.query(TABLE, null, null, null, null, null, null)
        TestCase.assertNotNull(cur)
        TestCase.assertEquals(1, cur.count)
        cur.moveToFirst()
        TestCase.assertNull(cur.getString(cur.getColumnIndex(C_PJB_NAME)))
        //invalid date uses value of Date(0) = January 1, 1970 00:00:00
        TestCase.assertEquals(
            "1970-01-01 00:00:00",
            cur.getString(cur.getColumnIndex(C_PJB_DATE))
        )
        TestCase.assertEquals(
            JobResult.ERROR.ordinal,
            cur.getInt(cur.getColumnIndex(C_PJB_RESULT))
        )
        cur.close()
        db.close()
    }

    @Test
    fun testDeleteWithPrinterId_Invalid() {
        val result = _printJobManager!!.deleteWithPrinterId(-1)
        TestCase.assertFalse(result)
    }

    @Test
    fun testDeleteWithJobId_Invalid() {
        val result = _printJobManager!!.deleteWithJobId(-1)
        TestCase.assertFalse(result)
    }

    @Test
    fun testDeleteWithPrinterId_Empty() {
        val db = _manager!!.writableDatabase
        db.delete(TABLE, null, null)
        db.close()
        val result = _printJobManager!!.deleteWithPrinterId(_printerId)
        TestCase.assertFalse(result)
    }

    @Test
    fun testDeleteWithJobId_Empty() {
        val db = _manager!!.writableDatabase
        db.delete(TABLE, null, null)
        db.close()
        val result = _printJobManager!!.deleteWithJobId(1)
        TestCase.assertFalse(result)
    }

    @Test
    fun testDeleteWithPrinterId() {
        var db = _manager!!.writableDatabase
        db.delete(TABLE, null, null)
        val pvalues = ContentValues()
        pvalues.put(C_PRN_ID, _printerId)
        pvalues.put(C_PJB_NAME, "Print Job Name")
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal)
        pvalues.put(C_PJB_DATE, "2014-03-17 13:12:11")
        db.insert(TABLE, null, pvalues)
        db.close()
        val result = _printJobManager!!.deleteWithPrinterId(_printerId)
        TestCase.assertTrue(result)
        db = _manager!!.readableDatabase
        val cur = db.query(TABLE, null, null, null, null, null, null)
        TestCase.assertNotNull(cur)
        TestCase.assertEquals(0, cur.count)
        cur.close()
        db.close()
    }

    @Test
    fun testDeleteWithJobId() {
        val jobId = 1
        var db = _manager!!.writableDatabase
        db.delete(TABLE, null, null)
        val pvalues = ContentValues()
        pvalues.put("pjb_id", jobId)
        pvalues.put(C_PRN_ID, _printerId)
        pvalues.put(C_PJB_NAME, "Print Job Name")
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal)
        pvalues.put(C_PJB_DATE, "2014-03-17 13:12:11")
        db.insert(TABLE, null, pvalues)
        db.close()
        val result = _printJobManager!!.deleteWithJobId(jobId)
        TestCase.assertTrue(result)
        db = _manager!!.readableDatabase
        val cur = db.query(TABLE, null, null, null, null, null, null)
        TestCase.assertEquals(0, cur.count)
        cur.close()
        db.close()
    }

    @Test
    fun testGetFlag() {
        TestCase.assertEquals(_initialFlag, _printJobManager!!.isRefreshFlag)
    }

    @Test
    fun testSetFlag() {
        val flag = true
        _printJobManager!!.isRefreshFlag = flag
        TestCase.assertEquals(flag, _printJobManager!!.isRefreshFlag)
    }

    companion object {
        private const val TABLE = "PrintJob"
        private const val C_PRN_ID = "prn_id"
        private const val C_PJB_NAME = "pjb_name"
        private const val C_PJB_DATE = "pjb_date"
        private const val C_PJB_RESULT = "pjb_result"
        private const val TABLE_PRINTER = "Printer"
        private const val C_PRN_NAME = "prn_name"
        private const val C_SQL_DATEFORMAT = "yyyy-MM-dd HH:mm:ss"
        private const val C_TIMEZONE = "UTC"
    }
}