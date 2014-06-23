/*
 * Copyright (c) 2014 RISO, Inc. All rights reserved.
 *
 * PrintJob.java
 * SmartDeviceApp
 * Created by: a-LINK Group
 */

package jp.co.riso.smartdeviceapp.model;

import java.util.Date;

/**
 * @class PrintJob
 * 
 * @brief Class representing the Print Job data in Print Job History.
 */
public class PrintJob {
    
    private int mId;
    private int mPrinterId;
    private String mName;
    private Date mDate;
    private JobResult mResult;
    
    /**
     * @brief Creates a PrintJob instance
     * 
     * @param mId Print job ID
     * @param mPrinterId Print job's printer ID
     * @param mName Print job name
     * @param mDate Print job's date of execution
     * @param mResult Print job result
     */
    public PrintJob(int mId, int mPrinterId, String mName, Date mDate, JobResult mResult) {
        this.mId = mId;
        this.mPrinterId = mPrinterId;
        this.mName = mName;
        this.mDate = mDate;
        this.mResult = mResult;
    }
    
    /**
     * @brief Creates a PrintJob instance
     * 
     * @param mPrinterId Print job's printer ID
     * @param mName Print job name
     * @param mDate Print job's date of execution
     * @param mResult Print job result
     */
    public PrintJob(int mPrinterId, String mName, Date mDate, JobResult mResult) {
        this.mPrinterId = mPrinterId;
        this.mName = mName;
        this.mDate = mDate;
        this.mResult = mResult;
    }
    
    /** 
     * @brief Retrieves the ID of the Print Job
     * 
     * @return print job ID
     */
    public int getId() {
        return mId;
    }
    
    /**
     * @brief Sets print job ID
     * 
     * @param mId Print job ID
     */
    public void setId(int mId) {
        this.mId = mId;
    }
    
    /** 
     * @brief Retrieves the Printer ID of the Print Job
     * 
     * @return Printer ID of the Print Job
     */
    public int getPrinterId() {
        return mPrinterId;
    }
    
    /**
     * @brief Sets printer ID
     * 
     * @param mPrinterId Printer ID of the Print Job
     */
    public void setPrinterId(int mPrinterId) {
        this.mPrinterId = mPrinterId;
    }
    
    /** 
     * @brief Retrieves the name of the Print Job
     * 
     * @return print job name
     */
    public String getName() {
        return mName;
    }
    
    /**
     * @brief Sets print job name
     * 
     * @param mName Print job name
     */
    public void setName(String mName) {
        this.mName = mName;
    }
    
    /** 
     * @brief Retrieves the Print Job date of execution
     * 
     * @return print job date
     */
    public Date getDate() {
        return mDate;
    }
    
    /**
     * @brief Set print job date of execution
     * 
     * @param mDate Print job date
     */
    public void setDate(Date mDate) {
        this.mDate = mDate;
    }
    
    /** 
     * @brief Retrieves the Print Job status (JobResult)
     * 
     * @retval SUCCESSFUL Printing is successful
     * @retval ERROR Printing has failed
     */
    public JobResult getResult() {
        return mResult;
    }
    
    /**
     * @brief Sets Print Job status (JobResult)
     * 
     * @param mResult print job result
     */
    public void setResult(JobResult mResult) {
        this.mResult = mResult;
    }
    
    /**
     * @brief Printing result status
     */
    public enum JobResult{
        /// Printing is successful
        SUCCESSFUL,
       /// Printing has failed
        ERROR
    }
    
}
