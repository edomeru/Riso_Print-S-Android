package jp.co.riso.smartdeviceapp.controller.jobs

import android.content.ContentValues
import android.test.AndroidTestCase
import android.test.RenamingDelegatingContext
import android.util.Log
import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager
import jp.co.riso.smartdeviceapp.controller.jobs.PrintJobManager.Companion.convertDateToString
import jp.co.riso.smartdeviceapp.controller.jobs.PrintJobManager.Companion.getInstance
import jp.co.riso.smartdeviceapp.model.PrintJob.JobResult
import junit.framework.TestCase
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class PrintJobManagerTest : AndroidTestCase() {
    private var mSdf: SimpleDateFormat? = null
    private val mContext: RenamingDelegatingContext? = null
    private var mPrintJobManager: PrintJobManager? = null
    private var mManager: DatabaseManager? = null
    private var mPrinterId = -1
    private var mPrinterId2 = -1
    private val mInitialFlag = false
    private val mPrinterName1 = "printer with job"
    private val mPrinterName2 = "printer without job"
    @Throws(Exception::class)
    override fun setUp() {
        super.setUp()
        mContext = RenamingDelegatingContext(context, "test_")
        mSdf = SimpleDateFormat(C_SQL_DATEFORMAT, Locale.getDefault())
        mSdf!!.timeZone = TimeZone.getTimeZone(C_TIMEZONE)
        mPrintJobManager = getInstance(mContext)
        mPrintJobManager!!.isRefreshFlag = mInitialFlag
        mManager = DatabaseManager(mContext)
        val db = mManager!!.writableDatabase
        // set printers
        db.delete(TABLE_PRINTER, null, null)
        val pvalues = ContentValues()
        pvalues.put(C_PRN_ID, 1)
        pvalues.put(C_PRN_NAME, mPrinterName1)
        db.insert(TABLE_PRINTER, "true", pvalues)
        pvalues.put(C_PRN_ID, 2)
        pvalues.put(C_PRN_NAME, mPrinterName2)
        db.insert(TABLE_PRINTER, "true", pvalues)
        val c = db.query(TABLE_PRINTER, null, null, null, null, null, null)
        c.moveToFirst()
        mPrinterId = c.getInt(c.getColumnIndex(C_PRN_ID))
        c.moveToNext()
        mPrinterId2 = c.getInt(c.getColumnIndex(C_PRN_ID))
        c.close()
        db.close()
    }

    @Throws(Exception::class)
    override fun tearDown() {
        super.tearDown()
        // clear data
        val db = mManager!!.writableDatabase
        db.delete(TABLE_PRINTER, null, null)
        db.delete(TABLE, null, null)
        db.close()
    }

    fun testPreConditions() {
        TestCase.assertNotNull(mPrintJobManager)
        val db = mManager!!.readableDatabase
        val c = db.query(TABLE_PRINTER, null, null, null, null, null, null)
        TestCase.assertEquals(2, c.count)
        c.moveToFirst()
        TestCase.assertEquals(c.getInt(c.getColumnIndex(C_PRN_ID)), mPrinterId)
        c.moveToNext()
        TestCase.assertEquals(c.getInt(c.getColumnIndex(C_PRN_ID)), mPrinterId2)
        TestCase.assertFalse(mPrintJobManager!!.isRefreshFlag)
        c.close()
        db.close()
    }

    fun testGetInstance() {
        TestCase.assertEquals(mPrintJobManager, getInstance(mContext))
    }

    fun testGetPrintJobs_WithData() {
        var db = mManager!!.writableDatabase
        db.delete(TABLE, null, null)
        var pvalues = ContentValues()
        pvalues.put(C_PRN_ID, mPrinterId)
        pvalues.put(C_PJB_NAME, "Print Job Name")
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal)
        pvalues.put(C_PJB_DATE, "2014-03-17 13:12:11")
        db.insert(TABLE, null, pvalues)
        db.close()
        var pj = mPrintJobManager!!.printJobs
        TestCase.assertNotNull(pj)
        TestCase.assertEquals(1, pj.size)
        TestCase.assertEquals(mPrinterId, pj[0].printerId)
        TestCase.assertEquals("Print Job Name", pj[0].name)
        TestCase.assertEquals(JobResult.SUCCESSFUL, pj[0].result)
        TestCase.assertTrue(
            mSdf!!.format(pj[0].date) ==
                    "2014-03-17 13:12:11"
        )
        db = mManager!!.writableDatabase
        //insert invalid date
        pvalues = ContentValues()
        pvalues.put(C_PRN_ID, mPrinterId)
        pvalues.put(C_PJB_NAME, "Print Job Name")
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal)
        pvalues.put(C_PJB_DATE, "invalid date")
        db.insert(TABLE, null, pvalues)
        db.close()
        pj = mPrintJobManager!!.printJobs
        TestCase.assertNotNull(pj)
        TestCase.assertEquals(2, pj.size)
        TestCase.assertEquals(mPrinterId, pj[1].printerId)
        TestCase.assertEquals("Print Job Name", pj[1].name)
        TestCase.assertEquals(JobResult.SUCCESSFUL, pj[1].result)
        //invalid date uses value of Date(0) = January 1, 1970 00:00:00
        TestCase.assertEquals(
            "1970-01-01 00:00:00",
            mSdf!!.format(pj[0].date)
        )
    }

    fun testGetPrintJobs_WithoutData() {
        val db = mManager!!.writableDatabase
        //test no data
        db.delete(TABLE, null, null)
        db.close()
        val pj = mPrintJobManager!!.printJobs
        TestCase.assertNotNull(pj)
        TestCase.assertEquals(0, pj.size)
    }

    fun testGetPrintJobs_Order() {
        mManager!!.writableDatabase
        val db = mManager!!.writableDatabase
        db.delete(TABLE, null, null)
        val pvalues = ContentValues()
        pvalues.put(C_PRN_ID, mPrinterId)
        pvalues.put(C_PJB_NAME, "Print Job Name")
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal)
        pvalues.put(C_PJB_DATE, "2014-03-17 13:12:11")
        db.insert(TABLE, null, pvalues)
        pvalues.clear()
        pvalues.put(C_PRN_ID, mPrinterId)
        pvalues.put(C_PJB_NAME, "Print Job Name1")
        pvalues.put(C_PJB_RESULT, JobResult.ERROR.ordinal)
        pvalues.put(C_PJB_DATE, "2014-03-18 08:12:11")
        db.insert(TABLE, null, pvalues)
        pvalues.clear()
        pvalues.put(C_PRN_ID, mPrinterId)
        pvalues.put(C_PJB_NAME, "Print Job Name2")
        pvalues.put(C_PJB_RESULT, JobResult.ERROR.ordinal)
        pvalues.put(C_PJB_DATE, "2012-03-18 08:12:11")
        db.insert(TABLE, null, pvalues)
        pvalues.clear()
        pvalues.put(C_PRN_ID, mPrinterId2)
        pvalues.put(C_PJB_NAME, "Print Job Name3")
        pvalues.put(C_PJB_RESULT, JobResult.ERROR.ordinal)
        pvalues.put(C_PJB_DATE, "2014-01-18 08:12:11")
        db.insert(TABLE, null, pvalues)
        pvalues.clear()
        pvalues.put(C_PRN_ID, mPrinterId)
        pvalues.put(C_PJB_NAME, "Print Job Name4")
        pvalues.put(C_PJB_RESULT, JobResult.ERROR.ordinal)
        pvalues.put(C_PJB_DATE, "2014-01-18 08:12:11")
        db.insert(TABLE, null, pvalues)
        db.close()
        val pj = mPrintJobManager!!.printJobs
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

    fun testGetPrintersWithJobs_EmptyJobs() {
        val db = mManager!!.writableDatabase
        db.delete(TABLE, null, null)

        //empty jobs yet but w/ existing printers -> printers list is empty
        val printers = mPrintJobManager!!.printersWithJobs
        TestCase.assertNotNull(printers)
        TestCase.assertEquals(0, printers.size)
    }

    fun testGetPrintersWithJobs_WithJobs() {
        val newPrinterId = mPrinterId2 + 1
        var db = mManager!!.writableDatabase
        db.delete(TABLE, null, null)
        val pvalues = ContentValues()
        pvalues.put(C_PRN_ID, mPrinterId)
        pvalues.put(C_PJB_NAME, "Print Job Name")
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal)
        pvalues.put(C_PJB_DATE, "2014-03-17 13:12:11")
        //add a job to an existing printer -> will be added to the printers list
        db.insert(TABLE, null, pvalues)

        //add another job to the same printer -> same printers list
        pvalues.put(C_PRN_ID, mPrinterId)
        pvalues.put(C_PJB_NAME, "Another Print Job Name")
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal)
        pvalues.put(C_PJB_DATE, "2014-03-17 13:14:11")
        db = mManager!!.writableDatabase
        db.insert(TABLE, null, pvalues)
        db.close()
        var printers = mPrintJobManager!!.printersWithJobs
        TestCase.assertNotNull(printers)
        TestCase.assertEquals(1, printers.size)
        TestCase.assertEquals(mPrinterId, printers[0].id)
        TestCase.assertEquals(mPrinterName1, printers[0].name)

        //add another printer -> same printers list
        pvalues.clear()
        pvalues.put(C_PRN_ID, newPrinterId)
        pvalues.put(C_PRN_NAME, "another printer")
        db = mManager!!.writableDatabase
        db.insert("Printer", null, pvalues)
        db.close()
        printers = mPrintJobManager!!.printersWithJobs
        TestCase.assertNotNull(printers)
        TestCase.assertEquals(1, printers.size)
        TestCase.assertEquals(mPrinterId, printers[0].id)
        TestCase.assertEquals(mPrinterName1, printers[0].name)

        // add another job on a different printer -> added in printers list
        pvalues.clear()
        pvalues.put(C_PRN_ID, newPrinterId)
        pvalues.put(C_PJB_NAME, "A Print Job in another printer")
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal)
        pvalues.put(C_PJB_DATE, "2014-03-17 13:22:11")
        db = mManager!!.writableDatabase
        db.insert(TABLE, null, pvalues)
        db.close()
        printers = mPrintJobManager!!.printersWithJobs
        TestCase.assertNotNull(printers)
        TestCase.assertEquals(2, printers.size)
        TestCase.assertEquals(mPrinterId, printers[0].id)
        TestCase.assertEquals(mPrinterName1, printers[0].name)
        TestCase.assertEquals(newPrinterId, printers[1].id)
        TestCase.assertEquals("another printer", printers[1].name)
    }

    fun testGetPrintersWithJobs_NewPrinterAdded() {
        var db = mManager!!.writableDatabase
        db.delete(TABLE, null, null)
        val pvalues = ContentValues()
        pvalues.put(C_PRN_ID, mPrinterId)
        pvalues.put(C_PJB_NAME, "Print Job Name")
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal)
        pvalues.put(C_PJB_DATE, "2014-03-17 13:12:11")
        db.insert(TABLE, null, pvalues)
        pvalues.put(C_PRN_ID, mPrinterId)
        pvalues.put(C_PJB_NAME, "Another Print Job Name")
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal)
        pvalues.put(C_PJB_DATE, "2014-03-17 13:14:11")
        db = mManager!!.writableDatabase
        db.insert(TABLE, null, pvalues)
        db.close()
        var printers = mPrintJobManager!!.printersWithJobs
        TestCase.assertNotNull(printers)
        TestCase.assertEquals(1, printers.size)
        TestCase.assertEquals(mPrinterId, printers[0].id)
        TestCase.assertEquals(mPrinterName1, printers[0].name)

        //add another printer -> same printers list
        pvalues.clear()
        pvalues.put(C_PRN_NAME, "a new printer")
        db = mManager!!.writableDatabase
        db.insert("Printer", null, pvalues)
        db.close()
        printers = mPrintJobManager!!.printersWithJobs
        TestCase.assertNotNull(printers)
        TestCase.assertEquals(1, printers.size)
        TestCase.assertEquals(mPrinterId, printers[0].id)
        TestCase.assertEquals(mPrinterName1, printers[0].name)
    }

    fun testGetPrintersWithJobs_WithDeletedPrinter() {
        val newPrinterId = mPrinterId2 + 1
        var db = mManager!!.writableDatabase
        db.delete(TABLE, null, null)
        val pvalues = ContentValues()
        pvalues.put(C_PRN_ID, mPrinterId)
        pvalues.put(C_PJB_NAME, "Print Job Name")
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal)
        pvalues.put(C_PJB_DATE, "2014-03-17 13:12:11")
        //add a job to an existing printer -> will be added to the printers list
        db.insert(TABLE, null, pvalues)

        //add another job to the same printer -> same printers list
        pvalues.put(C_PRN_ID, mPrinterId)
        pvalues.put(C_PJB_NAME, "Another Print Job Name")
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal)
        pvalues.put(C_PJB_DATE, "2014-03-17 13:14:11")
        db = mManager!!.writableDatabase
        db.insert(TABLE, null, pvalues)
        pvalues.clear()
        pvalues.put(C_PRN_ID, newPrinterId)
        pvalues.put(C_PRN_NAME, "another printer")
        db = mManager!!.writableDatabase
        db.insert("Printer", null, pvalues)

        // add another job on a different printer -> added in printers list
        pvalues.clear()
        pvalues.put(C_PRN_ID, newPrinterId)
        pvalues.put(C_PJB_NAME, "A Print Job in another printer")
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal)
        pvalues.put(C_PJB_DATE, "2014-03-17 13:22:11")
        db = mManager!!.writableDatabase
        db.insert(TABLE, null, pvalues)
        db.close()
        var printers = mPrintJobManager!!.printersWithJobs
        TestCase.assertNotNull(printers)
        TestCase.assertEquals(2, printers.size)
        TestCase.assertEquals(mPrinterId, printers[0].id)
        TestCase.assertEquals(mPrinterName1, printers[0].name)
        TestCase.assertEquals(newPrinterId, printers[1].id)
        TestCase.assertEquals("another printer", printers[1].name)
        db = mManager!!.writableDatabase
        db.delete("Printer", "prn_id=?", arrayOf(newPrinterId.toString()))
        db.close()
        printers = mPrintJobManager!!.printersWithJobs
        TestCase.assertNotNull(printers)
        TestCase.assertEquals(1, printers.size)
        TestCase.assertEquals(mPrinterId, printers[0].id)
        TestCase.assertEquals(mPrinterName1, printers[0].name)
    }

    fun testGetPrintersWithJobs_WithDeletedEmptyPrinter() {
        var db = mManager!!.writableDatabase
        db.delete(TABLE, null, null)
        val pvalues = ContentValues()
        pvalues.put(C_PRN_ID, mPrinterId)
        pvalues.put(C_PJB_NAME, "Print Job Name")
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal)
        pvalues.put(C_PJB_DATE, "2014-03-17 13:12:11")
        db.insert(TABLE, null, pvalues)
        pvalues.put(C_PRN_ID, mPrinterId)
        pvalues.put(C_PJB_NAME, "Another Print Job Name")
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal)
        pvalues.put(C_PJB_DATE, "2014-03-17 13:14:11")
        db = mManager!!.writableDatabase
        db.insert(TABLE, null, pvalues)
        db.close()
        var printers = mPrintJobManager!!.printersWithJobs
        TestCase.assertNotNull(printers)
        TestCase.assertEquals(1, printers.size)
        TestCase.assertEquals(mPrinterId, printers[0].id)
        TestCase.assertEquals(mPrinterName1, printers[0].name)

        //delete existing printer w/o job -> same printers list
        db = mManager!!.writableDatabase
        db.delete("Printer", "prn_id=?", arrayOf(mPrinterId2.toString()))
        db.close()
        printers = mPrintJobManager!!.printersWithJobs
        TestCase.assertNotNull(printers)
        TestCase.assertEquals(1, printers.size)
        TestCase.assertEquals(mPrinterId, printers[0].id)
        TestCase.assertEquals(mPrinterName1, printers[0].name)
    }

    fun testCreatePrintJob() {
        val result: Boolean
        var date: Date? = null
        var db = mManager!!.writableDatabase
        db.delete(TABLE, null, null)
        db.close()
        try {
            date = mSdf!!.parse("2014-03-17 13:12:11")
        } catch (e: ParseException) {
            Log.e("PrintJobManagerTest", "convertSQLToDate parsing error.")
        }
        result = mPrintJobManager!!.createPrintJob(
            mPrinterId, "printjob.pdf", date,
            JobResult.ERROR
        )
        TestCase.assertTrue(result)
        TestCase.assertTrue(mPrintJobManager!!.isRefreshFlag)
        db = mManager!!.readableDatabase
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

    fun testCreatePrintJob_Max() {
        val result: Boolean
        var db = mManager!!.writableDatabase
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
        result = mPrintJobManager!!.createPrintJob(
            1, "printjob.pdf", Date(),
            JobResult.ERROR
        )
        TestCase.assertTrue(result)
        TestCase.assertTrue(mPrintJobManager!!.isRefreshFlag)
        db = mManager!!.readableDatabase
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

    fun testCreatePrintJob_Invalid() {
        var result: Boolean
        var date: Date? = null
        var db = mManager!!.writableDatabase
        db.delete(TABLE, null, null)
        db.close()
        try {
            date = mSdf!!.parse("2014-03-17 13:12:11")
        } catch (e: ParseException) {
            Log.e("PrintJobManagerTest", "convertSQLToDate parsing error.")
        }
        result = mPrintJobManager!!.createPrintJob(
            mPrinterId, "printjob.pdf", date,
            JobResult.ERROR
        )
        TestCase.assertTrue(result)
        TestCase.assertTrue(mPrintJobManager!!.isRefreshFlag)
        db = mManager!!.writableDatabase
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
        result = mPrintJobManager!!.createPrintJob(
            -1, "printjob.pdf", date,
            JobResult.ERROR
        )
        TestCase.assertFalse(result)

        //not existing printer
        result = mPrintJobManager!!.createPrintJob(
            -1, null, null,
            JobResult.ERROR
        )
        TestCase.assertFalse(result)
    }

    fun testCreatePrintJob_NullValues() {
        val result: Boolean
        var db = mManager!!.writableDatabase
        db.delete(TABLE, null, null)
        db.close()
        result = mPrintJobManager!!.createPrintJob(
            mPrinterId, null, null,
            JobResult.ERROR
        )
        TestCase.assertTrue(result)
        TestCase.assertTrue(mPrintJobManager!!.isRefreshFlag)
        db = mManager!!.writableDatabase
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

    fun testDeleteWithPrinterId_Invalid() {
        val result = mPrintJobManager!!.deleteWithPrinterId(-1)
        TestCase.assertFalse(result)
    }

    fun testDeleteWithJobId_Invalid() {
        val result = mPrintJobManager!!.deleteWithJobId(-1)
        TestCase.assertFalse(result)
    }

    fun testDeleteWithPrinterId_Empty() {
        val db = mManager!!.writableDatabase
        db.delete(TABLE, null, null)
        db.close()
        val result = mPrintJobManager!!.deleteWithPrinterId(mPrinterId)
        TestCase.assertFalse(result)
    }

    fun testDeleteWithJobId_Empty() {
        val db = mManager!!.writableDatabase
        db.delete(TABLE, null, null)
        db.close()
        val result = mPrintJobManager!!.deleteWithJobId(1)
        TestCase.assertFalse(result)
    }

    fun testDeleteWithPrinterId() {
        var db = mManager!!.writableDatabase
        db.delete(TABLE, null, null)
        val pvalues = ContentValues()
        pvalues.put(C_PRN_ID, mPrinterId)
        pvalues.put(C_PJB_NAME, "Print Job Name")
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal)
        pvalues.put(C_PJB_DATE, "2014-03-17 13:12:11")
        db.insert(TABLE, null, pvalues)
        db.close()
        val result = mPrintJobManager!!.deleteWithPrinterId(mPrinterId)
        TestCase.assertTrue(result)
        db = mManager!!.readableDatabase
        val cur = db.query(TABLE, null, null, null, null, null, null)
        TestCase.assertNotNull(cur)
        TestCase.assertEquals(0, cur.count)
        cur.close()
        db.close()
    }

    fun testDeleteWithJobId() {
        val jobId = 1
        var db = mManager!!.writableDatabase
        db.delete(TABLE, null, null)
        val pvalues = ContentValues()
        pvalues.put("pjb_id", jobId)
        pvalues.put(C_PRN_ID, mPrinterId)
        pvalues.put(C_PJB_NAME, "Print Job Name")
        pvalues.put(C_PJB_RESULT, JobResult.SUCCESSFUL.ordinal)
        pvalues.put(C_PJB_DATE, "2014-03-17 13:12:11")
        db.insert(TABLE, null, pvalues)
        db.close()
        val result = mPrintJobManager!!.deleteWithJobId(jobId)
        TestCase.assertTrue(result)
        db = mManager!!.readableDatabase
        val cur = db.query(TABLE, null, null, null, null, null, null)
        TestCase.assertEquals(0, cur.count)
        cur.close()
        db.close()
    }

    fun testGetFlag() {
        TestCase.assertEquals(mInitialFlag, mPrintJobManager!!.isRefreshFlag)
    }

    fun testSetFlag() {
        val flag = true
        mPrintJobManager!!.isRefreshFlag = flag
        TestCase.assertEquals(flag, mPrintJobManager!!.isRefreshFlag)
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