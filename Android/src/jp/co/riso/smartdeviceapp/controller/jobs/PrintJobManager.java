/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintJobManager.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.controller.jobs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import jp.co.riso.android.util.Logger;
import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager;
import jp.co.riso.smartdeviceapp.controller.db.KeyConstants;
import jp.co.riso.smartdeviceapp.model.PrintJob;
import jp.co.riso.smartdeviceapp.model.PrintJob.JobResult;
import jp.co.riso.smartdeviceapp.model.Printer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

/**
 * @class PrintJobManager
 * 
 * @brief Helper class for managing the database transactions of Print Job History.
 */
public class PrintJobManager {
    private static final String C_WHERE_PJB_ID = KeyConstants.KEY_SQL_PRINTJOB_ID + "=?";
    private static final String C_WHERE_PRN_ID = KeyConstants.KEY_SQL_PRINTER_ID + "=?";
    private static final String C_ORDERBY_DATE = KeyConstants.KEY_SQL_PRINTER_ID + " ASC ,"
            + KeyConstants.KEY_SQL_PRINTJOB_DATE + " DESC," + KeyConstants.KEY_SQL_PRINTJOB_ID + " DESC";
    private static final String C_SEL_PRN_ID = KeyConstants.KEY_SQL_PRINTER_TABLE + "."
            + KeyConstants.KEY_SQL_PRINTER_ID + " IN (SELECT DISTINCT "
            + KeyConstants.KEY_SQL_PRINTER_ID + " FROM " + KeyConstants.KEY_SQL_PRINTJOB_TABLE + ")";
    private static final String C_SQL_DATEFORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String C_TIMEZONE = "UTC";
    
    private static PrintJobManager sInstance;
    
    private DatabaseManager mManager;
    private boolean mRefreshFlag;
    
    /**
     * @brief Creates a PrintJobManager instance.
     * 
     * @param context Context object to use to manage the database.
     */
    private PrintJobManager(Context context) {
        mManager = new DatabaseManager(context);
    }
    
    /**
     * @brief Gets instance of the PrintJobManager
     * 
     * @param context Context object to use to manage the database.
     * 
     * @return instance of PrintJobManager
     */
    public static PrintJobManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PrintJobManager(context);
        }
        return sInstance;
    }
    
    /**
     * @brief Sets the refresh flag to determine that Print Job History contains new data.
     * 
     * @param refreshFlag true if Print Job History contains new data
     */
    public void setRefreshFlag(boolean refreshFlag) {
        this.mRefreshFlag = refreshFlag;
    }
    
    /**
     * @brief Returns true if Print Job History contains new data. 
     * 
     * @retval true There is a new job.
     * @retval false No new job. 
     */
    public boolean isRefreshFlag() {
        return mRefreshFlag;
    }
    
    /**
     * @brief Retrieves a list of PrintJob objects. This method retrieves the PrintJob objects 
     * from the database sorted according to printer ID (in ascending order) 
     * and print job date (from latest to oldest).
     * 
     * @return List of PrintJob objects
     */
    public List<PrintJob> getPrintJobs() {
        List<PrintJob> printJobs = new ArrayList<PrintJob>();
        Cursor c = mManager.query(KeyConstants.KEY_SQL_PRINTJOB_TABLE, null, null, null, null, null, C_ORDERBY_DATE);
        
        if (c != null) {
            while (c.moveToNext()) {
                int pjb_id = DatabaseManager.getIntFromCursor(c, KeyConstants.KEY_SQL_PRINTJOB_ID);
                int prn_id = DatabaseManager.getIntFromCursor(c, KeyConstants.KEY_SQL_PRINTER_ID);
                String pjb_name = DatabaseManager.getStringFromCursor(c, KeyConstants.KEY_SQL_PRINTJOB_NAME);
                Date pjb_date = convertStringToDate(DatabaseManager.getStringFromCursor(c, KeyConstants.KEY_SQL_PRINTJOB_DATE));
                
                JobResult pjb_result = JobResult.values()[DatabaseManager.getIntFromCursor(c, KeyConstants.KEY_SQL_PRINTJOB_RESULT)];
                
                printJobs.add(new PrintJob(pjb_id, prn_id, pjb_name, pjb_date, pjb_result));
            }
            c.close();
            mManager.close();
        }
        return printJobs;
    }
    
    /**
     * @brief Retrieves a list of Printer objects with Print Jobs. This method retrieves the 
     * Printer objects from the database if it has corresponding print jobs sorted according
     * to printer ID.
     * 
     * @return List of Printer objects
     */
    public List<Printer> getPrintersWithJobs() {
        List<Printer> printers = new ArrayList<Printer>();
        
        Cursor c = mManager.query(KeyConstants.KEY_SQL_PRINTER_TABLE, null, C_SEL_PRN_ID, null, null, null, KeyConstants.KEY_SQL_PRINTER_ID);
        
        if (c != null) {
            while (c.moveToNext()) {
                int prn_id = DatabaseManager.getIntFromCursor(c, KeyConstants.KEY_SQL_PRINTER_ID);
                String prn_name = DatabaseManager.getStringFromCursor(c, KeyConstants.KEY_SQL_PRINTER_NAME);
                String prn_ip = DatabaseManager.getStringFromCursor(c, KeyConstants.KEY_SQL_PRINTER_IP);
                Printer printer = new Printer(prn_name, prn_ip);
                
                printer.setId(prn_id);
                printers.add(printer);
            }
            c.close();
            mManager.close();
        }
        return printers;
    }
    
    /**
     * @brief Deletes all print jobs with the given printer id in the database.
     * 
     * @param prn_id Printer ID of the print jobs to be deleted
     * 
     * @retval true Delete is successful.
     * @retval false Delete has failed.
     */
    public boolean deleteWithPrinterId(int prn_id) {
        return mManager.delete(KeyConstants.KEY_SQL_PRINTJOB_TABLE, C_WHERE_PRN_ID, String.valueOf(prn_id));
    }
    
    /**
     * @brief Deletes the print job with the given print job id in the database.
     * 
     * @param pjb_id ID of the print job to be deleted
     * 
     * @retval true Delete is successful.
     * @retval false Delete has failed.
     */
    public boolean deleteWithJobId(int pjb_id) {
        return mManager.delete(KeyConstants.KEY_SQL_PRINTJOB_TABLE, C_WHERE_PJB_ID, String.valueOf(pjb_id));
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
    public boolean createPrintJob(int prn_id, String PDFfilename, Date pjb_date, JobResult pjb_result) {
        PrintJob pj = new PrintJob(prn_id, PDFfilename, pjb_date, pjb_result);
        boolean result = insertPrintJob(pj);
        
        if (result) {
            setRefreshFlag(true);
        }
        
        return result;
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
    public int getOldest(int printerId) {
        int jobId = -1;
        String[] columns = new String[] {KeyConstants.KEY_SQL_PRINTJOB_ID, "MIN(" +KeyConstants.KEY_SQL_PRINTJOB_DATE+")"};
        String selection = KeyConstants.KEY_SQL_PRINTER_ID + "=?";
        String[] selArgs = new String[] {Integer.toString(printerId)};
        String groupBy = KeyConstants.KEY_SQL_PRINTER_ID;
        String having = "COUNT("+ KeyConstants.KEY_SQL_PRINTER_ID+") >= 100";
        String orderBy = KeyConstants.KEY_SQL_PRINTJOB_ID + " ASC";
        
        Cursor c = mManager.query(KeyConstants.KEY_SQL_PRINTJOB_TABLE, columns, selection, selArgs, groupBy, having, orderBy);
        if (c.moveToFirst()) {
             jobId = c.getInt(c.getColumnIndex(KeyConstants.KEY_SQL_PRINTJOB_ID));
        }
        c.close();
        mManager.close();
        return jobId;
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
    private boolean insertPrintJob(PrintJob printJob) {
        ContentValues pjvalues = new ContentValues();
        int jobId = getOldest(printJob.getPrinterId());

        // if print jobs of a printer is >= 100, delete oldest
        if (jobId != -1) {
            deleteWithJobId(jobId);
        }
        
        pjvalues.put(KeyConstants.KEY_SQL_PRINTER_ID, printJob.getPrinterId());
        pjvalues.put(KeyConstants.KEY_SQL_PRINTJOB_NAME, printJob.getName());
        pjvalues.put(KeyConstants.KEY_SQL_PRINTJOB_RESULT, printJob.getResult().ordinal());
        pjvalues.put(KeyConstants.KEY_SQL_PRINTJOB_DATE, convertDateToString(printJob.getDate()));
        return mManager.insert(KeyConstants.KEY_SQL_PRINTJOB_TABLE, null, pjvalues);
    }
    
    /**
     * @brief Converts the date into String using the UTC/GMT timezone and format C_SQL_DATEFORMAT.
     * 
     * @param date The date to be converted to String
     * 
     * @return Converted string format
     * @retval "1970-01-01 00:00:00" date is null
     */
    public static String convertDateToString(Date date) {
        if (date == null) {
            date = new Date(0);
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat(C_SQL_DATEFORMAT, Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone(C_TIMEZONE));
        return sdf.format(date);
    }
    
    /**
     * @brief Converts the String into Date using the UTC/GMT timezone and format C_SQL_DATEFORMAT.
     * 
     * @param strDate the string to be converted to Date
     * @return Converted date if strDate is in valid format
     * @retval "Date(0) equivalent of Jan.1,1970 UTC" strDate is in invalid format
     */
    private static Date convertStringToDate(String strDate) {
        SimpleDateFormat sdf = new SimpleDateFormat(C_SQL_DATEFORMAT, Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone(C_TIMEZONE));
        Date date = null;
        
        try {
            date = sdf.parse(strDate);
        } catch (ParseException e) {
            Logger.logWarn(PrintJobManager.class, String.format("convertStringToDate cannot parse %s to string.", strDate));
            date = new Date(0);
        }
        return date;
    }
}
