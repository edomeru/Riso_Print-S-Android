package jp.co.riso.smartdeviceapp.controller.jobs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager;
import jp.co.riso.smartdeviceapp.model.PrintJob;
import jp.co.riso.smartdeviceapp.model.PrintJob.JobResult;
import jp.co.riso.smartdeviceapp.model.Printer;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class PrintJobManager {
    
    private static final String TAG = "PrintJobManager";
    private static final String TABLE = "PrintJob";
    private static final String C_PJB_ID = "pjb_id";
    private static final String C_PRN_ID = "prn_id";
    private static final String C_PJB_NAME = "pjb_name";
    private static final String C_PJB_DATE = "pjb_date";
    private static final String C_PJB_RESULT = "pjb_result";
    private static final String C_WHERE_PJB_ID = C_PJB_ID + "=?";
    private static final String C_WHERE_PRN_ID = C_PRN_ID + "=?";
    private static final String TABLE_PRINTER = "Printer";
    private static final String C_PRN_NAME = "prn_name";
    private static final String C_SEL_PRN_ID = TABLE_PRINTER + "." + C_PRN_ID + " IN (SELECT DISTINCT " + C_PRN_ID + " FROM " + TABLE + ")";
    private static final String C_SQL_DATEFORMAT = "yyyy-MM-dd HH:mm:ss";
    private static DatabaseManager mManager;
    private static PrintJobManager mInstance;
    
    private PrintJobManager(Context context) {
        mManager = new DatabaseManager(context);
    }
    
    public static PrintJobManager getInstance() {
        return mInstance;
    }
    
    public static void initializeInstance(Context context) {
        if (mInstance == null) {
            mInstance = new PrintJobManager(context);
        }
        
    }
    
    public static List<PrintJob> getPrintJobs() {
        List<PrintJob> printJobs = new ArrayList<PrintJob>();
        Cursor c = mManager.query(TABLE, null, null, null, null, null, C_PRN_ID);
        
        while (c.moveToNext()) {
            int pjb_id = c.getInt(c.getColumnIndex(C_PJB_ID));
            int prn_id = c.getInt(c.getColumnIndex(C_PRN_ID));
            String pjb_name = c.getString(c.getColumnIndex(C_PJB_NAME));
            Date pjb_date = convertSQLToDate(c.getString(c.getColumnIndex(C_PJB_DATE)));
            
            JobResult pjb_result = JobResult.values()[c.getInt(c.getColumnIndex(C_PJB_RESULT))];
            
            printJobs.add(new PrintJob(pjb_id, prn_id, pjb_name, pjb_date, pjb_result));
        }
        c.close();
        mManager.close();
        return printJobs;
    }
    
    public static boolean deleteWithPrinterId(int prn_id) {
        return mManager.delete(TABLE, C_WHERE_PRN_ID, new String[] { String.valueOf(prn_id) });
    }
    
    public static boolean deleteWithJobId(int pjb_id) {
        return mManager.delete(TABLE, C_WHERE_PJB_ID, new String[] { String.valueOf(pjb_id) });
        
    }
    
    public static boolean createPrintJob(int prn_id, String PDFfilename, Date pjb_date, JobResult pjb_result) {
        PrintJob pj = new PrintJob(prn_id, PDFfilename, pjb_date, pjb_result);
        return insertPrintJob(pj);
    }
    
    private static boolean insertPrintJob(PrintJob printJob) {
        ContentValues pjvalues = new ContentValues();
        pjvalues.put(C_PRN_ID, printJob.getPrinterId());
        pjvalues.put(C_PJB_NAME, printJob.getName());
        pjvalues.put(C_PJB_RESULT, printJob.getResult().ordinal());
        pjvalues.put(C_PJB_DATE, formatSQLDateTime(printJob.getDate()));
        
        return mManager.insert(TABLE, null, pjvalues);
        
    }
    
    private static String formatSQLDateTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(C_SQL_DATEFORMAT, Locale.getDefault());
        return sdf.format(date);
    }
    
    private static Date convertSQLToDate(String strDate) {
        SimpleDateFormat sdf = new SimpleDateFormat(C_SQL_DATEFORMAT, Locale.getDefault());
        Date date = null;
        
        try {
            date = sdf.parse(strDate);
            
        } catch (ParseException e) {
            Log.e(TAG, "convertSQLToDate parsing error.");
        }
        return date;
    }
    
    public static List<Printer> getPrintersWithJobs() {
        List<Printer> printers = new ArrayList<Printer>();
        
        Cursor c = mManager.query(TABLE_PRINTER, null, C_SEL_PRN_ID, null, null, null, C_PRN_ID);
        
        while (c.moveToNext()) {
            
            int prn_id = c.getInt(c.getColumnIndex(C_PRN_ID));
            String prn_name = c.getString(c.getColumnIndex(C_PRN_NAME));
            printers.add(new Printer(prn_id, prn_name));
        }
        c.close();
        mManager.close();
        return printers;
    }
}
