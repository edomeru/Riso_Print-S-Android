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

public class PrintJobManager {
    private static final String C_WHERE_PJB_ID = KeyConstants.KEY_SQL_PRINTJOB_ID + "=?";
    private static final String C_WHERE_PRN_ID = KeyConstants.KEY_SQL_PRINTER_ID + "=?";
    private static final String C_ORDERBY_DATE = KeyConstants.KEY_SQL_PRINTER_ID + " ASC ,"
            + KeyConstants.KEY_SQL_PRINTJOB_DATE + " DESC";
    private static final String C_SEL_PRN_ID = KeyConstants.KEY_SQL_PRINTER_TABLE + "."
            + KeyConstants.KEY_SQL_PRINTER_ID + " IN (SELECT DISTINCT "
            + KeyConstants.KEY_SQL_PRINTER_ID + " FROM " + KeyConstants.KEY_SQL_PRINTJOB_TABLE + ")";
    private static final String C_SQL_DATEFORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String C_TIMEZONE = "UTC";
    
    private static PrintJobManager sInstance;
    
    private DatabaseManager mManager;
    private boolean mRefreshFlag;
    
    /**
     * Constructor
     * 
     * @param context
     */
    private PrintJobManager(Context context) {
        mManager = new DatabaseManager(context);
    }
    
    /**
     * Get instance of the PrintJob Manager
     * 
     * @param context
     * @return instance of PrintJobManager
     */
    public static PrintJobManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PrintJobManager(context);
        }
        return sInstance;
    }
    
    /**
     * set Refresh flag
     * 
     * @param refreshFlag
     *            refresh flag
     */
    public void setRefreshFlag(boolean refreshFlag) {
        this.mRefreshFlag = refreshFlag;
    }
    
    /**
     * @return mRefreshFlag
     */
    public boolean isRefreshFlag() {
        return mRefreshFlag;
    }
    
    /**
     * Returns a list of PrintJob objects
     * <p>
     * This method retrieves the PrintJob objects from the database sorted according to printer ID (in ascending order)
     * and print job date (from latest to oldest).
     * 
     * @return list of PrintJob objects
     */
    public List<PrintJob> getPrintJobs() {
        List<PrintJob> printJobs = new ArrayList<PrintJob>();
        Cursor c = mManager.query(KeyConstants.KEY_SQL_PRINTJOB_TABLE, null, null, null, null, null, C_ORDERBY_DATE);
        
        while (c.moveToNext()) {
            int pjb_id = DatabaseManager.getIntFromCursor(c,KeyConstants.KEY_SQL_PRINTJOB_ID);
            int prn_id = DatabaseManager.getIntFromCursor(c,KeyConstants.KEY_SQL_PRINTER_ID);
            String pjb_name = DatabaseManager.getStringFromCursor(c,KeyConstants.KEY_SQL_PRINTJOB_NAME);
            Date pjb_date = convertStringToDate(DatabaseManager.getStringFromCursor(c,KeyConstants.KEY_SQL_PRINTJOB_DATE));
            
            JobResult pjb_result = JobResult.values()[DatabaseManager.getIntFromCursor(c,KeyConstants.KEY_SQL_PRINTJOB_RESULT)];
            
            printJobs.add(new PrintJob(pjb_id, prn_id, pjb_name, pjb_date, pjb_result));
        }
        
        c.close();
        mManager.close();
        return printJobs;
    }
    
    /**
     * Returns a list of Printer objects with Print Jobs
     * <p>
     * This method retrieves the Printer objects from the database if it has corresponding print jobs sorted according
     * to printer ID.
     * 
     * @return list of Printer objects
     */
    public List<Printer> getPrintersWithJobs() {
        List<Printer> printers = new ArrayList<Printer>();
        
        Cursor c = mManager.query(KeyConstants.KEY_SQL_PRINTER_TABLE, null, C_SEL_PRN_ID, null, null, null, KeyConstants.KEY_SQL_PRINTER_ID);
        
        while (c.moveToNext()) {
            int prn_id = DatabaseManager.getIntFromCursor(c,KeyConstants.KEY_SQL_PRINTER_ID);
            String prn_name = DatabaseManager.getStringFromCursor(c,KeyConstants.KEY_SQL_PRINTER_NAME);
            String prn_ip = DatabaseManager.getStringFromCursor(c,KeyConstants.KEY_SQL_PRINTER_IP);
            Printer printer = new Printer(prn_name, prn_ip);
            
            printer.setId(prn_id);
            printers.add(printer);
        }
        c.close();
        mManager.close();
        return printers;
    }
    
    /**
     * Delete all print jobs with Printer ID
     * <p>
     * This method deletes all print jobs with printer id in the database.
     * 
     * @param prn_id
     *            printer ID of the print jobs to be deleted
     * @return boolean result of delete in the database
     */
    public boolean deleteWithPrinterId(int prn_id) {
        return mManager.delete(KeyConstants.KEY_SQL_PRINTJOB_TABLE, C_WHERE_PRN_ID, String.valueOf(prn_id));
    }
    
    /**
     * Delete print job with the given print job id
     * <p>
     * This method deletes the print job with print job id in the database.
     * 
     * @param pjb_id
     *            ID of the print job to be deleted
     * @return boolean result of deletee in the database
     */
    public boolean deleteWithJobId(int pjb_id) {
        return mManager.delete(KeyConstants.KEY_SQL_PRINTJOB_TABLE, C_WHERE_PJB_ID, String.valueOf(pjb_id));
    }
    
    /**
     * This method creates a print job and inserts the value to the database.
     * 
     * @param prn_id
     *            printer ID
     * @param PDFfilename
     *            PDF filename to be used as job name
     * @param pjb_date
     *            date when job is created
     * @param pjb_result
     *            the status of print job (SUCCESSFUL, ERROR)
     * @return boolean result of insert to the database
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
     * This method inserts the value of the print job to the database.
     * 
     * @param printJob
     *            the PrintJob object containing the values to be inserted
     * @return boolean result of insert to the database
     */
    private boolean insertPrintJob(PrintJob printJob) {
        ContentValues pjvalues = new ContentValues();
        pjvalues.put(KeyConstants.KEY_SQL_PRINTER_ID, printJob.getPrinterId());
        pjvalues.put(KeyConstants.KEY_SQL_PRINTJOB_NAME, printJob.getName());
        pjvalues.put(KeyConstants.KEY_SQL_PRINTJOB_RESULT, printJob.getResult().ordinal());
        pjvalues.put(KeyConstants.KEY_SQL_PRINTJOB_DATE, convertDateToString(printJob.getDate()));
        return mManager.insert(KeyConstants.KEY_SQL_PRINTJOB_TABLE, null, pjvalues);
    }
    
    /**
     * This method converts the date into String using the UTC/GMT timezone
     * 
     * @param date
     *            the date to be converted to String
     * @return converted string
     */
    private String convertDateToString(Date date) {
        if (date == null) {
            date = new Date(0);
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat(C_SQL_DATEFORMAT, Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone(C_TIMEZONE));
        return sdf.format(date);
    }
    
    /**
     * This method converts the String into Date using the UTC/GMT timezone
     * 
     * @param strDate
     *            the string to be converted to Date
     * @return converted date
     */
    private Date convertStringToDate(String strDate) {
        SimpleDateFormat sdf = new SimpleDateFormat(C_SQL_DATEFORMAT, Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone(C_TIMEZONE));
        Date date = null;
        
        try {
            date = sdf.parse(strDate);
        } catch (ParseException e) {
            Logger.logError(PrintJobManager.class, String.format("convertStringToDate cannot parse %s to string.", strDate));
            date = new Date(0);
        }
        return date;
    }
}
