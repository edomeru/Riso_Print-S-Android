package jp.co.riso.smartdeviceapp.controller;

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
    private static final String WHERE_PJB_ID = C_PJB_ID + "=?";
    private static final String WHERE_PRN_ID = C_PRN_ID + "=?";
    private static DatabaseManager manager;
    private static PrintJobManager instance;
    
    private PrintJobManager(Context context) {
        manager = new DatabaseManager(context);
    }
    
    public static PrintJobManager getInstance() {
        return instance;
    }
    
    public static void initializeInstance(Context context) {
        if (instance == null) {
            instance = new PrintJobManager(context);
        }
        
    }
    
    public static List<PrintJob> getPrintJobs() {  
        List<PrintJob> printJobs = new ArrayList<PrintJob>();      
        Cursor c = manager.query(TABLE, null, null, null, null, null, C_PRN_ID);
        
        while (c.moveToNext()) {
            int pjb_id = c.getInt(c.getColumnIndex(C_PJB_ID));
            int prn_id = c.getInt(c.getColumnIndex(C_PRN_ID));
            String pjb_name = c.getString(c.getColumnIndex(C_PJB_NAME));
            Date pjb_date = convertSQLToDate(c.getString(c.getColumnIndex(C_PJB_DATE)));
            
            JobResult pjb_result = JobResult.values()[c.getInt(c.getColumnIndex(C_PJB_RESULT))];
            
            printJobs.add(new PrintJob(pjb_id, prn_id, pjb_name, pjb_date, pjb_result));
        }
        c.close();
        return printJobs;
    }
    
    public static boolean deleteWithPrinterId(int prn_id) {
        return manager.delete(TABLE, WHERE_PRN_ID, new String[] { String.valueOf(prn_id) });
    }
    
    public static boolean deleteWithJobId(int pjb_id) {
        return manager.delete(TABLE, WHERE_PJB_ID, new String[] { String.valueOf(pjb_id) });
        
    }
    
    public static boolean createPrintJob(int prn_id, String PDFfilename, Date pjb_date, JobResult pjb_result) {
        PrintJob pj = new PrintJob(prn_id, PDFfilename, pjb_date, pjb_result);
        return insertPrintJob(pj);
    }
    
    public static boolean insertPrintJob(PrintJob printJob) {
        ContentValues pjvalues = new ContentValues();
        pjvalues.put(C_PRN_ID, printJob.getPrinterId());
        pjvalues.put(C_PJB_NAME, printJob.getName());
        pjvalues.put(C_PJB_RESULT, printJob.getResult().ordinal());
        pjvalues.put(C_PJB_DATE, formatSQLDateTime(printJob.getDate()));
        
        return manager.insert(TABLE, null, pjvalues);
        
    }
    
    public static String formatSQLDateTime(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(date);
    }
    
    private static Date convertSQLToDate(String strDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = null;
        
        try {
            date = sdf.parse(strDate);
            
        } catch (ParseException e) {
            Log.e(TAG, e.toString());
        }
        return date;
    }
    
    //for testing only - may use PrinterManager's method instead
    public static List<Printer> getPrinters() {  
        List<Printer> printers = new ArrayList<Printer>();      
        Cursor c = manager.query("Printer", null, null, null, null, null, null);
        Log.d("CESTEST", "getprinters" + c.getCount());

        while (c.moveToNext()) {
            int prn_id = c.getInt(c.getColumnIndex(C_PRN_ID));
            String prn_name = c.getString(c.getColumnIndex("prn_name"));
            
            printers.add(new Printer(prn_id, prn_name));
        }
        c.close();
        return printers;
    }
}
