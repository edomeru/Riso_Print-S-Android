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
    
    /**
     * Constructor
     * 
     * @param mId
     *            print job ID
     * @param mPrinterId
     *            printer ID
     * @param mName
     *            print job name
     * @param mDate
     *            date of execution
     * @param mResult
     *            print job result
     */
    public PrintJob(int mId, int mPrinterId, String mName, Date mDate, JobResult mResult) {
        this.mId = mId;
        this.mPrinterId = mPrinterId;
        this.mName = mName;
        this.mDate = mDate;
        this.mResult = mResult;
    }
    
    /**
     * Constructor
     * 
     * @param mPrinterId
     *            printer ID
     * @param mName
     *            print job name
     * @param mDate
     *            date of execution
     * @param mResult
     *            print job result
     */
    public PrintJob(int mPrinterId, String mName, Date mDate, JobResult mResult) {
        this.mPrinterId = mPrinterId;
        this.mName = mName;
        this.mDate = mDate;
        this.mResult = mResult;
    }
    
    /** 
     * @return print job ID
     */
    public int getId() {
        return mId;
    }
    
    /**
     * Set print job ID
     * 
     * @param mName
     *            print job ID
     */
    public void setId(int mId) {
        this.mId = mId;
    }
    
    /** 
     * @return printer ID
     */
    public int getPrinterId() {
        return mPrinterId;
    }
    
    /**
     * Set printer ID
     * 
     * @param mPrinterId
     *            printer ID
     */
    public void setPrinterId(int mPrinterId) {
        this.mPrinterId = mPrinterId;
    }
    
    /** 
     * @return print job name
     */
    public String getName() {
        return mName;
    }
    
    /**
     * Set print job name
     * 
     * @param mName
     *            print job name
     */
    public void setName(String mName) {
        this.mName = mName;
    }
    
    /** 
     * @return print job date
     */
    public Date getDate() {
        return mDate;
    }
    
    /**
     * Set print job date
     * 
     * @param mDate
     *            print job date
     */
    public void setDate(Date mDate) {
        this.mDate = mDate;
    }
    
    /** 
     * @return print job result
     */
    public JobResult getResult() {
        return mResult;
    }
    
    /**
     * Set print job result
     * 
     * @param mResult
     *            print job result
     */
    public void setResult(JobResult mResult) {
        this.mResult = mResult;
    }
    
    public enum JobResult{
        SUCCESSFUL,
        ERROR
    }
    
}
