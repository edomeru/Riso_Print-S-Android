package jp.co.riso.smartdeviceapp.controller;

import jp.co.riso.smartdeviceapp.controller.db.DatabaseManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class PrintJobManager {
    private static DatabaseManager manager;
    private final static String TABLE = "PrintJob";
    private final static String C_PJB_ID = "pjb_id";
    private final static String C_PRN_ID = "prn_id";
    private final static String C_PJB_NAME = "pjb_name";
    private final static String C_PJB_DATE = "pjb_date";
    private final static String C_PJB_RESULT = "pjb_result";
    private final static String WHERE_PJB_ID = C_PJB_ID + "=?";
    private final static String WHERE_PRN_ID = C_PRN_ID + "=?";
    
    public PrintJobManager(Context context){
        manager = new DatabaseManager(context);
    }
    
    public static Cursor getPrintJobs(){
        manager.getReadableDatabase();
        
        Cursor c = manager.query(TABLE, null, null, null, null, null, null);

        return c;   
    }
    
    public static boolean deleteWithPrinterId(int prn_id){
        manager.getWritableDatabase();
        
        manager.delete(TABLE, WHERE_PRN_ID, new String[]{ String.valueOf(prn_id)});
        manager.close();
       return true;   
    }
    
    public static boolean deleteWithJobId(int pjb_id){
        manager.getWritableDatabase();
        manager.delete(TABLE, WHERE_PJB_ID, new String[]{ String.valueOf(pjb_id)});
        manager.close();
        return true;   
     }
    
    public static int getJobsCount(){
        return 0;   
     }
    
    public static boolean createPrintJob(String PDFfilename, long pjb_date, int pjb_result, int prn_id){

        ContentValues pjvalues = new ContentValues();
        pjvalues.put(C_PRN_ID, prn_id);
        pjvalues.put(C_PJB_NAME, PDFfilename);
        pjvalues.put(C_PJB_RESULT, pjb_result);
        pjvalues.put(C_PJB_DATE, pjb_date);
        manager.getWritableDatabase();

        manager.insert(TABLE, null, pjvalues);
        manager.close();

        return true;   
    }
    

}
