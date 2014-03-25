/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintJob.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.model;

import java.util.Date;

public class PrintJob {
    
    private int mId;
    private int mPrinterId;
    private String mName;
    private Date mDate;
    private JobResult mResult;
    
    public PrintJob(int mId, int mPrinterId, String mName, Date mDate, JobResult mResult) {
        super();
        this.mId = mId;
        this.mPrinterId = mPrinterId;
        this.mName = mName;
        this.mDate = mDate;
        this.mResult = mResult;
    }
    
    public PrintJob(int mPrinterId, String mName, Date mDate, JobResult mResult) {
        super();
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
    
    public JobResult getResult() {
        return mResult;
    }
    
    public void setResult(JobResult mResult) {
        this.mResult = mResult;
    }
    
    public enum JobResult{
        SUCCESSFUL,
        ERROR
    }
    
}
