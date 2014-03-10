package jp.co.riso.smartdeviceapp.model;

import java.util.Date;

public class PrintJob {
    
    private int mId;
    private int mPrinterId;
    private String mName;
    private Date mDate;
    private int mResult;
    
    public PrintJob(int mId, int mPrinterId, String mName, Date mDate, int mResult) {
        super();
        this.mId = mId;
        this.mPrinterId = mPrinterId;
        this.mName = mName;
        this.mDate = mDate;
        this.mResult = mResult;
    }
    
    public int getId() {
        return mId;
    }
    
    public void setId(int mId) {
        this.mId = mId;
    }
    
    public int getPrinterId() {
        return mPrinterId;
    }
    
    public void setPrinterId(int mPrinterId) {
        this.mPrinterId = mPrinterId;
    }
    
    public String getName() {
        return mName;
    }
    
    public void setName(String mName) {
        this.mName = mName;
    }
    
    public Date getDate() {
        return mDate;
    }
    
    public void setDate(Date mDate) {
        this.mDate = mDate;
    }
    
    public int getResult() {
        return mResult;
    }
    
    public void setResult(int mResult) {
        this.mResult = mResult;
    }
    
}
