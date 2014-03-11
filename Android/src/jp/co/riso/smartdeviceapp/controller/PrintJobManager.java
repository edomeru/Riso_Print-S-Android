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
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class PrintJobManager {
    private static DatabaseManager manager;
    private static PrintJobManager instance;
    private final static String TABLE = "PrintJob";
    private final static String C_PJB_ID = "pjb_id";
    private final static String C_PRN_ID = "prn_id";
    private final static String C_PJB_NAME = "pjb_name";
    private final static String C_PJB_DATE = "pjb_date";
    private final static String C_PJB_RESULT = "pjb_result";
    private final static String WHERE_PJB_ID = C_PJB_ID + "=?";
    private final static String WHERE_PRN_ID = C_PRN_ID + "=?";
    
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
        manager.getReadableDatabase();
        
        Cursor c = manager.query(TABLE, null, null, null, null, null, null);
        
        while (c.moveToNext()) {
            int pjb_id = c.getInt(c.getColumnIndex(C_PJB_ID));
            int prn_id = c.getInt(c.getColumnIndex(C_PRN_ID));
            String pjb_name = c.getString(c.getColumnIndex(C_PJB_NAME));
            Date pjb_date = convertStringToDate(c.getString(c.getColumnIndex(C_PJB_DATE)));
            //String pjb_date = c.getString(c.getColumnIndex(C_PJB_DATE));
            
            JobResult pjb_result = JobResult.values()[c.getInt(c.getColumnIndex(C_PJB_RESULT))];
            //int pjb_result = c.getInt(c.getColumnIndex(C_PJB_RESULT));

            printJobs.add(new PrintJob(pjb_id, prn_id, pjb_name, pjb_date, pjb_result));
            
            Log.d("CESTEST", "dates" + c.getLong(c.getColumnIndex(C_PJB_DATE)) + "+++" + pjb_date.toString());
        }
        
        return printJobs;
    }
    
    public static boolean deleteWithPrinterId(int prn_id) {
        manager.getWritableDatabase();
        
        manager.delete(TABLE, WHERE_PRN_ID, new String[] { String.valueOf(prn_id) });
        manager.close();
        return true;
    }
    
    public static boolean deleteWithJobId(int pjb_id) {
        manager.getWritableDatabase();
        manager.delete(TABLE, WHERE_PJB_ID, new String[] { String.valueOf(pjb_id) });
        manager.close();
        return true;
    }
    
    public static int getJobsCount() {
        return 0;
    }
    
    public static boolean createPrintJob(int prn_id, String PDFfilename, Date pjb_date, JobResult pjb_result) {  
    //public static boolean createPrintJob(int prn_id, String PDFfilename, String pjb_date, int pjb_result) {
        PrintJob pj = new PrintJob(prn_id, PDFfilename, pjb_date, pjb_result);
        return insertPrintJob(pj);
    }
    
    public static boolean insertPrintJob(PrintJob printJob) {

        
        ContentValues pjvalues = new ContentValues();
        pjvalues.put(C_PRN_ID, printJob.getPrinterId());
        pjvalues.put(C_PJB_NAME, printJob.getName());
        pjvalues.put(C_PJB_RESULT, printJob.getResult().ordinal());
        pjvalues.put(C_PJB_DATE, convertToDateTime(printJob.getDate()));
        //pjvalues.put(C_PJB_DATE, printJob.getDate());//convertToDateTime(printJob.getDate()));
        
        // manager.getWritableDatabase();
        
        manager.insert(TABLE, null, pjvalues);
        
        // manager.close();
        
        return true;
    }
    
    public static String convertToDateTime(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Log.d("CESTEST", "convertdate" + dateFormat.format(date));
        return dateFormat.format(date);
    }
    
    private static Date convertStringToDate(String strDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        Date date = null;
        String dateformat = "";
        
        try {
            Log.d("CESTEST", "1convert" + strDate+ "---" + date);
            date = sdf.parse(strDate);
            // sdf.applyPattern("dd-MMM-yyyy");
            Log.d("CESTEST", "2convert" + strDate+ "---" + date);
            dateformat = sdf.format(date);
            Log.d("CESTEST", "convert" + strDate+ "---" + date);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return date;        
    }
}
