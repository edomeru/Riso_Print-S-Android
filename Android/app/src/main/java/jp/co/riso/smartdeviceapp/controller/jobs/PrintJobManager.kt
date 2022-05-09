/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintJobManager.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */
package jp.co.riso.smartdeviceapp.controller.jobs

import android.content.ContentValues
import android.content.Context
import jp.co.riso.android.util.Logger.logError
import jp.co.riso.android.util.Logger.logWarn
import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager
import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager.Companion.getIntFromCursor
import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager.Companion.getStringFromCursor
import jp.co.riso.smartdeviceapp.controller.db.KeyConstants
import jp.co.riso.smartdeviceapp.model.PrintJob
import jp.co.riso.smartdeviceapp.model.PrintJob.JobResult
import jp.co.riso.smartdeviceapp.model.Printer
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * @class PrintJobManager
 *
 * @brief Helper class for managing the database transactions of Print Job History.
 */
class PrintJobManager private constructor(context: Context) {
    private val _manager: DatabaseManager = DatabaseManager(context)
    /**
     * @brief Returns true if Print Job History contains new data.
     *
     * @retval true There is a new job.
     * @retval false No new job.
     */
    /**
     * @brief Sets the refresh flag to determine that Print Job History contains new data.
     *
     * @param refreshFlag true if Print Job History contains new data
     */
    var isRefreshFlag = false

    /**
     * @brief Retrieves a list of PrintJob objects. This method retrieves the PrintJob objects
     * from the database sorted according to printer ID (in ascending order)
     * and print job date (from latest to oldest).
     *
     * @return List of PrintJob objects
     */
    val printJobs: List<PrintJob>
        get() {
            val printJobs: MutableList<PrintJob> = ArrayList()
            val c = _manager.query(
                KeyConstants.KEY_SQL_PRINTJOB_TABLE,
                null,
                null,
                null,
                null,
                null,
                C_ORDERBY_DATE
            )
            if (c != null) {
                while (c.moveToNext()) {
                    val pjb_id = getIntFromCursor(c, KeyConstants.KEY_SQL_PRINTJOB_ID)
                    val prn_id = getIntFromCursor(c, KeyConstants.KEY_SQL_PRINTER_ID)
                    val pjb_name = getStringFromCursor(c, KeyConstants.KEY_SQL_PRINTJOB_NAME)
                    val pjb_date = convertStringToDate(
                        getStringFromCursor(
                            c,
                            KeyConstants.KEY_SQL_PRINTJOB_DATE
                        )
                    )
                    val pjb_result = JobResult.values()[getIntFromCursor(
                        c,
                        KeyConstants.KEY_SQL_PRINTJOB_RESULT
                    )]
                    printJobs.add(PrintJob(pjb_id, prn_id, pjb_name.toString(), pjb_date, pjb_result))
                }
                c.close()
                _manager.close()
            }
            return printJobs
        }

    /**
     * @brief Retrieves a list of Printer objects with Print Jobs. This method retrieves the
     * Printer objects from the database if it has corresponding print jobs sorted according
     * to printer ID.
     *
     * @return List of Printer objects
     */
    val printersWithJobs: List<Printer>
        get() {
            val printers: MutableList<Printer> = ArrayList()
            val c = _manager.query(
                KeyConstants.KEY_SQL_PRINTER_TABLE,
                null,
                C_SEL_PRN_ID,
                null,
                null,
                null,
                KeyConstants.KEY_SQL_PRINTER_ID
            )
            if (c != null) {
                while (c.moveToNext()) {
                    val prn_id = getIntFromCursor(c, KeyConstants.KEY_SQL_PRINTER_ID)
                    val prn_name = getStringFromCursor(c, KeyConstants.KEY_SQL_PRINTER_NAME)
                    val prn_ip = getStringFromCursor(c, KeyConstants.KEY_SQL_PRINTER_IP)
                    val printer = Printer(prn_name, prn_ip)
                    printer.id = prn_id
                    printers.add(printer)
                }
                c.close()
                _manager.close()
            }
            return printers
        }

    /**
     * @brief Deletes all print jobs with the given printer id in the database.
     *
     * @param prn_id Printer ID of the print jobs to be deleted
     *
     * @retval true Delete is successful.
     * @retval false Delete has failed.
     */
    fun deleteWithPrinterId(prn_id: Int): Boolean {
        return _manager.delete(
            KeyConstants.KEY_SQL_PRINTJOB_TABLE,
            C_WHERE_PRN_ID,
            prn_id.toString()
        )
    }

    /**
     * @brief Deletes the print job with the given print job id in the database.
     *
     * @param pjb_id ID of the print job to be deleted
     *
     * @retval true Delete is successful.
     * @retval false Delete has failed.
     */
    fun deleteWithJobId(pjb_id: Int): Boolean {
        return _manager.delete(
            KeyConstants.KEY_SQL_PRINTJOB_TABLE,
            C_WHERE_PJB_ID,
            pjb_id.toString()
        )
    }

    /**
     * @brief Creates a print job and inserts the value to the database.
     *
     * @param prn_id Printer ID
     * @param PDFfilename PDF filename to be used as job name
     * @param pjb_date Date when job is created
     * @param pjb_result The of print job status (SUCCESSFUL, ERROR)
     *
     * @retval true Insert to database is successful.
     * @retval false Insert to database has failed.
     */
    fun createPrintJob(
        prn_id: Int,
        PDFfilename: String?,
        pjb_date: Date?,
        pjb_result: JobResult?
    ): Boolean {
        val pj = PrintJob(prn_id, PDFfilename, pjb_date, pjb_result)
        val result = insertPrintJob(pj)
        if (result) {
            isRefreshFlag = true
        }
        return result
    }

    /**
     * @brief Retrieves the id of the oldest print job of a printer in the database
     * if the printer contains 100 or more print jobs, else returns -1.
     *
     * @param printerId Printer ID of the print jobs
     *
     * @return Print job id of the oldest print job if the printer contains 100 or more print jobs.
     * @retval -1 Printer contains less than 100 print jobs.
     */
    private fun getOldest(printerId: Int): Int {
        var jobId = -1
        val columns = arrayOf<String?>(
            KeyConstants.KEY_SQL_PRINTJOB_ID,
            "MIN(" + KeyConstants.KEY_SQL_PRINTJOB_DATE + ")"
        )
        val selection = KeyConstants.KEY_SQL_PRINTER_ID + "=?"
        val selArgs = arrayOf<String?>(printerId.toString())
        val groupBy = KeyConstants.KEY_SQL_PRINTER_ID
        val having = "COUNT(" + KeyConstants.KEY_SQL_PRINTER_ID + ") >= 100"
        val orderBy = KeyConstants.KEY_SQL_PRINTJOB_ID + " ASC"
        val c = _manager.query(
            KeyConstants.KEY_SQL_PRINTJOB_TABLE,
            columns,
            selection,
            selArgs,
            groupBy,
            having,
            orderBy
        )
        if (c!!.moveToFirst()) {
            val columnIndex = c.getColumnIndex(KeyConstants.KEY_SQL_PRINTJOB_ID)
            if (columnIndex >= 0) {
                jobId = c.getInt(columnIndex)
            } else {
                logError(
                    PrintJobManager::class.java,
                    "columnName:" + KeyConstants.KEY_SQL_PRINTJOB_ID + " not found"
                )
            }
        }
        c.close()
        _manager.close()
        return jobId
    }

    /**
     * @brief Inserts the value of the print job to the database and deletes the
     * oldest print job of a printer if print jobs >= 100.
     *
     * @param printJob The PrintJob object containing the values to be inserted
     *
     * @retval true Insert to database is successful.
     * @retval false Insert to database has failed.
     */
    private fun insertPrintJob(printJob: PrintJob): Boolean {
        val pjvalues = ContentValues()
        val jobId = getOldest(printJob.printerId)

        // if print jobs of a printer is >= 100, delete oldest
        if (jobId != -1) {
            deleteWithJobId(jobId)
        }
        pjvalues.put(KeyConstants.KEY_SQL_PRINTER_ID, printJob.printerId)
        pjvalues.put(KeyConstants.KEY_SQL_PRINTJOB_NAME, printJob.name)
        pjvalues.put(KeyConstants.KEY_SQL_PRINTJOB_RESULT, printJob.result!!.ordinal)
        pjvalues.put(KeyConstants.KEY_SQL_PRINTJOB_DATE, convertDateToString(printJob.date))
        return _manager.insert(KeyConstants.KEY_SQL_PRINTJOB_TABLE, null, pjvalues)
    }

    companion object {
        private const val C_WHERE_PJB_ID = KeyConstants.KEY_SQL_PRINTJOB_ID + "=?"
        private const val C_WHERE_PRN_ID = KeyConstants.KEY_SQL_PRINTER_ID + "=?"
        private const val C_ORDERBY_DATE = (KeyConstants.KEY_SQL_PRINTER_ID + " ASC ,"
                + KeyConstants.KEY_SQL_PRINTJOB_DATE + " DESC," + KeyConstants.KEY_SQL_PRINTJOB_ID + " DESC")
        private const val C_SEL_PRN_ID = (KeyConstants.KEY_SQL_PRINTER_TABLE + "."
                + KeyConstants.KEY_SQL_PRINTER_ID + " IN (SELECT DISTINCT "
                + KeyConstants.KEY_SQL_PRINTER_ID + " FROM " + KeyConstants.KEY_SQL_PRINTJOB_TABLE + ")")
        private const val C_SQL_DATEFORMAT = "yyyy-MM-dd HH:mm:ss"
        private const val C_TIMEZONE = "UTC"
        private var sInstance: PrintJobManager? = null

        /**
         * @brief Gets instance of the PrintJobManager
         *
         * @param context Context object to use to manage the database.
         *
         * @return instance of PrintJobManager
         */
        @JvmStatic
        fun getInstance(context: Context): PrintJobManager? {
            if (sInstance == null) {
                sInstance = PrintJobManager(context)
            }
            return sInstance
        }

        /**
         * @brief Converts the date into String using the UTC/GMT timezone and format C_SQL_DATEFORMAT.
         *
         * @param date The date to be converted to String
         *
         * @return Converted string format
         * @retval "1970-01-01 00:00:00" date is null
         */
        @JvmStatic
        fun convertDateToString(date: Date?): String {
            var convertDate = date
            if (convertDate == null) {
                convertDate = Date(0)
            }
            val sdf = SimpleDateFormat(C_SQL_DATEFORMAT, Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone(C_TIMEZONE)
            return sdf.format(convertDate)
        }

        /**
         * @brief Converts the String into Date using the UTC/GMT timezone and format C_SQL_DATEFORMAT.
         *
         * @param strDate the string to be converted to Date
         * @return Converted date if strDate is in valid format
         * @retval "Date(0) equivalent of Jan.1,1970 UTC" strDate is in invalid format
         */
        private fun convertStringToDate(strDate: String?): Date {
            val sdf = SimpleDateFormat(C_SQL_DATEFORMAT, Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone(C_TIMEZONE)
            val date: Date = try {
                sdf.parse(strDate) as Date
            } catch (e: ParseException) {
                logWarn(
                    PrintJobManager::class.java,
                    String.format("convertStringToDate cannot parse %s to string.", strDate)
                )
                Date(0)
            }
            return date
        }
    }

}